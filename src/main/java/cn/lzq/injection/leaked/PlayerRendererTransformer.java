package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.RenderPlayerEvent;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.render.NameTags;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class PlayerRendererTransformer extends ASMTransformer implements Wrapper {
    public PlayerRendererTransformer() {
        super(PlayerRenderer.class);
    }

    public static RenderPlayerEvent onRenderPlayer(PlayerRenderer this1, AbstractClientPlayer player, float rotationYaw, float rotationPitch) {
        if (mc == null || mc.player == null || mc.level == null) return null;

        return player == mc.player ? (RenderPlayerEvent) Loratadine.INSTANCE.getEventManager().call(new RenderPlayerEvent(this1, rotationYaw, rotationPitch)) : new RenderPlayerEvent(this1, rotationYaw, rotationPitch);
    }

    public static boolean nameTag() {
        return Loratadine.INSTANCE != null
                && Loratadine.INSTANCE.getModuleManager() != null
                && Loratadine.INSTANCE.getModuleManager().getModule(NameTags.class) != null
                && Loratadine.INSTANCE.getModuleManager().getModule(NameTags.class).isEnabled();
    }

    @Inject(method = "renderNameTag", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void renderNameTag(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        LabelNode continueLabel = new LabelNode();

        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(PlayerRendererTransformer.class),
                        "nameTag",
                        "()Z",
                        false
                )
        );

        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);

        methodNode.instructions.insert(instructions);
    }

    @Inject(method = "setupRotations", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V")
    public void setupRotations(MethodNode methodNode) {
        InsnList insnList = new InsnList();
        int j = 6;

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 4));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 3));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(PlayerRendererTransformer.class), "onRenderPlayer", "(Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;Lnet/minecraft/client/player/AbstractClientPlayer;FF)L" + RenderPlayerEvent.class.getName().replace(".", "/") + ";"));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 6));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var >= j) {
                ((VarInsnNode) node).var += 1;
            }

            if (node instanceof VarInsnNode && ((VarInsnNode) node).var == 3) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 6));
                list.add(new FieldInsnNode(Opcodes.GETFIELD, RenderPlayerEvent.class.getName().replace(".", "/"), "rotationPitch", "F"));

                methodNode.instructions.insert(node, list);
                methodNode.instructions.remove(node);
            }

            if (node instanceof VarInsnNode && ((VarInsnNode) node).var == 4) {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 6));
                list.add(new FieldInsnNode(Opcodes.GETFIELD, RenderPlayerEvent.class.getName().replace(".", "/"), "rotationYaw", "F"));

                methodNode.instructions.insert(node, list);
                methodNode.instructions.remove(node);
            }
        }

        methodNode.instructions.insert(insnList);
    }
}
