package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.MouseOverEvent;
import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.event.impl.Render3DEvent;
import shop.xmz.lol.loratadine.modules.impl.render.BetterCamera;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class GameRendererTransformer extends ASMTransformer implements Wrapper {
    private static double prevRenderX = 0;
    private static double prevRenderY = 0;
    private static double prevRenderZ = 0;
    /*private static double range;*/

    public GameRendererTransformer() {
        super(GameRenderer.class);
    }

    public static boolean cancelRendering() {
        return BetterCamera.INSTANCE != null && BetterCamera.INSTANCE.isEnabled() && BetterCamera.INSTANCE.noHurtShake.getValue();
    }

    public static boolean doMotionCamera() {
        return BetterCamera.INSTANCE != null && BetterCamera.INSTANCE.isEnabled() && BetterCamera.INSTANCE.motionCamera.getValue() && !mc.options.getCameraType().isFirstPerson() && mc.player != null && mc.level != null;
    }

    public static void onRender2D(float partialTicks) {
        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        if (Loratadine.INSTANCE != null && Loratadine.INSTANCE.getEventManager() != null)
            Loratadine.INSTANCE.getEventManager().call(new Render2DEvent(partialTicks, guiGraphics, guiGraphics.pose()));
    }

    public static void onRender3D(float partialTicks, PoseStack poseStack) {
        if (Loratadine.INSTANCE != null && Loratadine.INSTANCE.getEventManager() != null)
            Loratadine.INSTANCE.getEventManager().call(new Render3DEvent(partialTicks, poseStack));
    }

    @Inject(method = "render", desc = "(FJZ)V")
    private void render(MethodNode methodNode) {
        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
            // Find the line where minecraft.gui.render is called
            if (node instanceof MethodInsnNode methodInsnNode &&
                    methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                    methodInsnNode.desc.contains("GuiGraphics") && methodInsnNode.desc.endsWith("F)V")) {

                // Check if this is followed by RenderSystem.clear - which comes right after
                // the minecraft.gui.render call in the original code
                AbstractInsnNode nextNode = node.getNext();
                boolean foundRenderSystemClear = false;

                // Look ahead for RenderSystem.clear within a few instructions
                for (int i = 0; i < 10 && nextNode != null; i++) {
                    if (nextNode instanceof MethodInsnNode nextMethod &&
                            nextMethod.name.contains("clear") &&
                            nextMethod.owner.contains("RenderSystem")) {
                        foundRenderSystemClear = true;
                        break;
                    }
                    nextNode = nextNode.getNext();
                }

                // If we found the pattern we're looking for
                if (foundRenderSystemClear) {
                    // Create our instruction list
                    InsnList instructions = new InsnList();

                    // Load the partialTicks parameter (p_109094_) which is the first float parameter
                    instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));

                    // Call the static onRender2D method
                    instructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            Type.getInternalName(GameRendererTransformer.class),
                            "onRender2D",
                            "(F)V",
                            false
                    ));

                    // Insert our call right after the gui.render call
                    methodNode.instructions.insert(node, instructions);
                    break;
                }
            }
        }
    }

    @Inject(method = "renderLevel", desc = "(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void renderLevel(MethodNode methodNode) {
        // 第一部分 - 现有的 motion camera 注入代码保持不变
        InsnList motionCameraInstructions = new InsnList();
        LabelNode continueLabel = new LabelNode();

        motionCameraInstructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GameRendererTransformer.class),
                "doMotionCamera",
                "()Z",
                false
        ));

        motionCameraInstructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));

        boolean motionCameraInjected = false;
        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode methodInsnNode) {
                if (methodInsnNode.name.equals(Mapping.get(PoseStack.Pose.class, "normal", "()Lorg/joml/Matrix3f;"))) {
                    motionCameraInstructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
                    motionCameraInstructions.add(new VarInsnNode(Opcodes.LLOAD, 2));
                    motionCameraInstructions.add(new VarInsnNode(Opcodes.ALOAD, 4));

                    motionCameraInstructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            Type.getInternalName(GameRendererTransformer.class),
                            "motionCamera",
                            "(FJLcom/mojang/blaze3d/vertex/PoseStack;)V",
                            false
                    ));

                    motionCameraInstructions.add(continueLabel);
                    methodNode.instructions.insert(node.getNext(), motionCameraInstructions);
                    motionCameraInjected = true;
                    break;
                }
            }
        }

        // 第二部分 - onRender3D 方法的注入，直接使用字节码查找和注入
        boolean onRender3DInjected = false;

        // 遍历以查找 renderHand 字段访问
        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
            if (node.getOpcode() == Opcodes.GETFIELD && node instanceof FieldInsnNode fieldNode) {
                String mappedFieldName = Mapping.get(GameRenderer.class, "renderHand", null);

                if (fieldNode.name.equals(mappedFieldName)) {
                    // 找到字段访问后，查找条件跳转指令 (IFEQ 或 IFNE)
                    AbstractInsnNode currentNode = node;
                    AbstractInsnNode jumpNode = null;

                    // 通常条件判断后会有一个跳转指令
                    while (currentNode != null) {
                        currentNode = currentNode.getNext();
                        if (currentNode != null &&
                                (currentNode.getOpcode() == Opcodes.IFEQ || currentNode.getOpcode() == Opcodes.IFNE)) {
                            jumpNode = currentNode;
                            break;
                        }
                    }

                    if (jumpNode != null) {
                        // 获取 if 块的第一条指令
                        AbstractInsnNode blockStartNode = jumpNode.getNext();

                        // 创建要插入的指令列表
                        InsnList injectInstructions = new InsnList();

                        // 加载参数
                        injectInstructions.add(new VarInsnNode(Opcodes.FLOAD, 1)); // p_109090_ (float)
                        injectInstructions.add(new VarInsnNode(Opcodes.ALOAD, 4)); // p_109092_ (PoseStack)

                        // 调用 onRender3D 方法
                        injectInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(GameRendererTransformer.class),
                                "onRender3D",
                                "(FLcom/mojang/blaze3d/vertex/PoseStack;)V",
                                false
                        ));

                        // 在 if 块开始处插入指令
                        methodNode.instructions.insert(blockStartNode, injectInstructions);
                        onRender3DInjected = true;
                        break;
                    }
                }
            }
        }

        // 检查注入是否成功
        if (!motionCameraInjected) {
            throw new RuntimeException("Failed to inject motionCamera bytecode");
        }

        if (!onRender3DInjected) {
            throw new RuntimeException("Failed to inject onRender3D bytecode");
        }
    }

    @Inject(method = "bobHurt", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public void bobHurt(MethodNode methodNode) {
        InsnList instructions = new InsnList();

        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GameRendererTransformer.class),
                "cancelRendering",
                "()Z",
                false
        ));

        LabelNode continueLabel = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);

        methodNode.instructions.insert(instructions);
    }

    /*@Inject(method = "pick", desc = "(F)V")
    public void pick(MethodNode methodNode) {
        InsnList list = new InsnList();
        LabelNode continueLabel = new LabelNode();

        // 调用静态方法 mouseOver()
        list.add(new  MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(GameRendererTransformer.class),
                "mouseOver",
                "()Z",  // 修正描述符为返回boolean
                false));

        // 添加条件跳转指令
        list.add(new  JumpInsnNode(Opcodes.IFEQ, continueLabel)); // 如果false则跳转
        list.add(new  InsnNode(Opcodes.RETURN));  // 立即返回
        list.add(continueLabel);   // 继续执行后续代码

        // 查找目标插入位置
        AbstractInsnNode targetNode = null;
        for (AbstractInsnNode insn : methodNode.instructions)  {
            if (insn instanceof MethodInsnNode methodInsn) {
                if (methodInsn.name.equals(Mapping.get(Entity.class, "getEyePosition", "(F)Lnet/minecraft/world/phys/Vec3;")))  { // getEyePosition
                    // 在方法调用之后的下一条指令前插入
                    targetNode = insn.getNext();
                    break;
                }
            }
        }

        // 插入条件返回逻辑
        if (targetNode != null) {
            methodNode.instructions.insert(targetNode,  list);
        }

        // 处理原有9.0数值的替换逻辑
        for (AbstractInsnNode insn : methodNode.instructions.toArray())  {
            if (insn.getOpcode()  == Opcodes.DCMPL) {
                AbstractInsnNode prev = insn.getPrevious();
                if (prev instanceof LdcInsnNode ldc && ldc.cst.equals(9.0))  {
                    // 替换为方法调用
                    InsnList replaceList = new InsnList();
                    replaceList.add(new  MethodInsnNode(Opcodes.INVOKESTATIC,
                            Type.getInternalName(GameRendererTransformer.class),
                            "mouseOver_Reach",
                            "()D",
                            false));

                    // 插入新指令并移除旧指令
                    methodNode.instructions.insertBefore(prev,  replaceList);
                    methodNode.instructions.remove(prev);
                }
            }
        }
    }*/

    public static void motionCamera(float partialTicks, long ignore2, PoseStack poseStack) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Entity entity = camera.getEntity();

        if (entity != null) { // 几把的不要管idea的傻逼警告，如果没有这个检查游戏会崩溃
            float eyeHeight = entity.getEyeHeight();
            float interpolation = BetterCamera.INSTANCE.interpolation.getValue().floatValue();
            double renderX = entity.xo + (entity.getX() - entity.xo) * (double) partialTicks;
            double renderY = entity.yo + (entity.getY() - entity.yo) * (double) partialTicks + (double) eyeHeight;
            double renderZ = entity.zo + (entity.getZ() - entity.zo) * (double) partialTicks;

            prevRenderX = prevRenderX + (renderX - prevRenderX) * interpolation;
            prevRenderY = prevRenderY + (renderY - prevRenderY) * interpolation;
            prevRenderZ = prevRenderZ + (renderZ - prevRenderZ) * interpolation;

            if (mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
                poseStack.translate(renderX - prevRenderX, renderY - prevRenderY, renderZ - prevRenderZ);
            } else {
                poseStack.translate(prevRenderX - renderX, renderY - prevRenderY, prevRenderZ - renderZ);
            }
        }
    }

/*    public static boolean mouseOver() {
        MouseOverEvent mouseOverEvent = new MouseOverEvent(9.0);
        Loratadine.INSTANCE.getEventManager().call(mouseOverEvent);

        range = mouseOverEvent.getRange();

        if (mouseOverEvent.getMovingObjectPosition() != null) {
            mc.hitResult = mouseOverEvent.getMovingObjectPosition();
            return true;
        }

        return false;
    }

    public static double mouseOver_Reach() {
        return range;
    }*/
}
