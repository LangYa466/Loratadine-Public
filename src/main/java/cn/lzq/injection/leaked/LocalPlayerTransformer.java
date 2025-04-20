package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import cn.lzq.injection.leaked.invoked.MotionEvent;
import cn.lzq.injection.leaked.invoked.SlowEvent;
import cn.lzq.injection.leaked.invoked.UpdateEvent;
import cn.lzq.injection.leaked.mapping.Mapping;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import shop.xmz.lol.loratadine.Loratadine;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.modules.impl.combat.SuperKnockBack;
import shop.xmz.lol.loratadine.modules.impl.misc.Disabler;

import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class LocalPlayerTransformer extends ASMTransformer implements Wrapper {
    public LocalPlayerTransformer() {
        super(LocalPlayer.class);
    }
    public static int offGroundTicks = 0, onGroundTicks = 0;

    public static SlowEvent onSlow(boolean isUsingItem) {
        return (SlowEvent) Loratadine.INSTANCE.getEventManager().call(new SlowEvent(isUsingItem));
    }

    public static void postRelease() {
        Disabler.INSTANCE.release();
    }

    public static MotionEvent onMotion(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean post) {
        return (MotionEvent) Loratadine.INSTANCE.getEventManager().call(new MotionEvent(x, y, z, yaw, pitch, onGround, post));
    }

    public static boolean isSprint() {
        return SuperKnockBack.sprint;
    }

    public static void onUpdateEvent() {
        Loratadine.INSTANCE.getEventManager().call(new UpdateEvent());
    }

    public static void ground() {
        if (mc.player == null) return;

        if (mc.player.onGround()) {
            offGroundTicks = 0;
            onGroundTicks++;
        } else {
            onGroundTicks = 0;
            offGroundTicks++;
        }
    }

    public static void onLivingUpdateEvent() {
        Loratadine.INSTANCE.getEventManager().call(new LivingUpdateEvent());
    }

    @Inject(method = "aiStep", desc = "()V")
    public void aiStep(MethodNode methodNode) {
        // Find the isUsingItem call in the if statement
        AbstractInsnNode targetInsn = null;

        for (AbstractInsnNode insn : methodNode.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.name.equals(Mapping.get(LocalPlayer.class, "isUsingItem", "()Z")) &&
                        methodInsn.desc.equals("()Z") &&
                        methodInsn.getNext() instanceof JumpInsnNode) {
                    targetInsn = insn;
                    break;
                }
            }
        }

        // Add a new local variable for SlowEvent
        int slowEventVarIndex = methodNode.maxLocals;
        methodNode.maxLocals += 1;

        // 1. Insert onLivingUpdateEvent() at the very beginning
        InsnList beginInsns = new InsnList();
        beginInsns.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(LocalPlayerTransformer.class),
                "onLivingUpdateEvent",
                "()V",
                false
        ));

        // 2. Create and store SlowEvent
        beginInsns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        beginInsns.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "net/minecraft/client/player/LocalPlayer",
                Mapping.get(LocalPlayer.class, "isUsingItem", "()Z"),
                "()Z",
                false
        ));
        beginInsns.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(LocalPlayerTransformer.class),
                "onSlow",
                "(Z)L" + SlowEvent.class.getName().replace(".", "/") + ";",
                false
        ));
        beginInsns.add(new VarInsnNode(Opcodes.ASTORE, slowEventVarIndex));

        // Insert at beginning of method
        methodNode.instructions.insert(beginInsns);

        // 3. Replace the isUsingItem call with slowEvent.state
        InsnList replaceInsns = new InsnList();
        replaceInsns.add(new VarInsnNode(Opcodes.ALOAD, slowEventVarIndex));
        replaceInsns.add(new FieldInsnNode(
                Opcodes.GETFIELD,
                SlowEvent.class.getName().replace(".", "/"),
                "state",
                "Z"
        ));

        // Insert before and remove old instruction
        methodNode.instructions.insertBefore(targetInsn, replaceInsns);
        methodNode.instructions.remove(targetInsn);
    }

    @Inject(method = "hasEnoughImpulseToStartSprinting", desc = "()Z")
    public void hasEnoughImpulseToStartSprinting(MethodNode methodNode) {
        InsnList insnNodes = new InsnList();

        LabelNode falseLabel = new LabelNode();
        LabelNode returnLabel = new LabelNode();

        // 加载this.isUnderWater()
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "net/minecraft/client/player/LocalPlayer",
                Mapping.get(LocalPlayer.class, "isUnderWater", "()Z"),
                "()Z",
                false));

        // 条件判断（三元运算符）
        LabelNode notUnderwaterLabel = new LabelNode();
        insnNodes.add(new JumpInsnNode(Opcodes.IFEQ, notUnderwaterLabel));

        // 水下分支：this.input.hasForwardImpulse()
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnNodes.add(new FieldInsnNode(Opcodes.GETFIELD,
                "net/minecraft/client/player/LocalPlayer",
                Mapping.get(LocalPlayer.class, "input", null),
                "Lnet/minecraft/client/player/Input;"));
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "net/minecraft/client/player/Input",
                Mapping.get(Input.class, "hasForwardImpulse", "()Z"),
                "()Z",
                false));
        insnNodes.add(new JumpInsnNode(Opcodes.GOTO, returnLabel));

        // 非水下分支
        insnNodes.add(notUnderwaterLabel);
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnNodes.add(new FieldInsnNode(Opcodes.GETFIELD,
                "net/minecraft/client/player/LocalPlayer",
                Mapping.get(LocalPlayer.class, "input", null),
                "Lnet/minecraft/client/player/Input;"));
        insnNodes.add(new FieldInsnNode(Opcodes.GETFIELD,
                "net/minecraft/client/player/Input",
                Mapping.get(Input.class, "forwardImpulse", null),
                "F"));
        insnNodes.add(new InsnNode(Opcodes.F2D));
        insnNodes.add(new LdcInsnNode(0.8D));
        insnNodes.add(new InsnNode(Opcodes.DCMPL));
        insnNodes.add(new JumpInsnNode(Opcodes.IFLT, falseLabel));

        // 新增的isSprint检查
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(LocalPlayerTransformer.class),
                "isSprint",
                "()Z",
                false));
        insnNodes.add(new JumpInsnNode(Opcodes.IFEQ, falseLabel));

        // 返回true
        insnNodes.add(new InsnNode(Opcodes.ICONST_1));
        insnNodes.add(new JumpInsnNode(Opcodes.GOTO, returnLabel));

        // 返回false
        insnNodes.add(falseLabel);
        insnNodes.add(new InsnNode(Opcodes.ICONST_0));

        // 设置返回标签
        insnNodes.add(returnLabel);
        insnNodes.add(new InsnNode(Opcodes.IRETURN));

        // 替换原方法体
        methodNode.instructions.clear();
        methodNode.instructions.insert(insnNodes);
        methodNode.maxStack = 3; // 调整栈深度
    }

    @Inject(method = "tick", desc = "()V")
    public void tick(MethodNode methodNode) {
        InsnList list = new InsnList();

        // 插入 LocalPlayerTransformer.onUpdateEvent() 的调用
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(LocalPlayerTransformer.class),
                "onUpdateEvent",
                "()V",
                false));

        // 遍历指令以找到 if 判断的位置
        AbstractInsnNode targetNode = null;
        for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
            // 查找符合条件的 if 判断的指令位置
            if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                    insn instanceof MethodInsnNode &&
                    ((MethodInsnNode) insn).name.equals(Mapping.get(LevelReader.class, "hasChunkAt", "(II)Z"))) {

                // 找到 if 判断后的一条指令
                targetNode = insn.getNext();
                break;
            }
        }

        // 确认找到目标位置后插入指令
        if (targetNode != null) {
            methodNode.instructions.insert(targetNode, list);
        }
    }

    @Inject(method = "sendPosition", desc = "()V") // 混淆后的 "sendPosition" 方法名
    public void sendPosition(MethodNode methodNode) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(LocalPlayerTransformer.class), "ground", "()V"));
        methodNode.instructions.insert(list);
        int j = 1;

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getX", "()D"), "()D")); // 混淆后的 "getX" 方法
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getY", "()D"), "()D")); // 混淆后的 "getY" 方法
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getZ", "()D"), "()D")); // 混淆后的 "getZ" 方法
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getYRot", "()F"), "()F")); // 混淆后的 "getYRot" 方法
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getXRot", "()F"), "()F")); // 混淆后的 "getXRot" 方法
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "onGround", "()Z"), "()Z")); // 混淆后的 "onGround" 变量名称
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(LocalPlayerTransformer.class), "onMotion", "(DDDFFZZ)L" + MotionEvent.class.getName().replace(".", "/") + ";"));
        list.add(new VarInsnNode(Opcodes.ASTORE, 1));

        // 在方法末尾插入调用
        InsnList postCall = new InsnList();
        postCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
        postCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getX", "()D"), "()D")); // 混淆后的 "getX" 方法
        postCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
        postCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getY", "()D"), "()D")); // 混淆后的 "getY" 方法
        postCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
        postCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getZ", "()D"), "()D")); // 混淆后的 "getZ" 方法
        postCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
        postCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getYRot", "()F"), "()F")); // 混淆后的 "getYRot" 方法
        postCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
        postCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getXRot", "()F"), "()F")); // 混淆后的 "getXRot" 方法
        postCall.add(new VarInsnNode(Opcodes.ALOAD, 0));
        postCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "onGround", "()Z"), "()Z")); // 混淆后的 "onGround" 变量名称
        postCall.add(new InsnNode(Opcodes.ICONST_1));
        postCall.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(LocalPlayerTransformer.class), "onMotion", "(DDDFFZZ)L" + MotionEvent.class.getName().replace(".", "/") + ";"));
        postCall.add(new InsnNode(Opcodes.POP));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var >= j) {
                ((VarInsnNode) node).var += j;
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "onGround", "()Z"))) {
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, MotionEvent.class.getName().replace(".", "/"), "onGround", "Z"));
                    methodNode.instructions.remove(node);
                }
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getYRot", "()F"))) { // 混淆后的 "getYRot" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, MotionEvent.class.getName().replace(".", "/"), "yaw", "F"));
                    methodNode.instructions.remove(node);
                }
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getXRot", "()F"))) { // 混淆后的 "getXRot" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, MotionEvent.class.getName().replace(".", "/"), "pitch", "F"));
                    methodNode.instructions.remove(node);
                }
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getX", "()D"))) { // 混淆后的 "getX" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, MotionEvent.class.getName().replace(".", "/"), "x", "D"));
                    methodNode.instructions.remove(node);
                }
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getY", "()D"))) { // 混淆后的 "getY" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, MotionEvent.class.getName().replace(".", "/"), "y", "D"));
                    methodNode.instructions.remove(node);
                }
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getZ", "()D"))) { // 混淆后的 "getZ" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, MotionEvent.class.getName().replace(".", "/"), "z", "D"));
                    methodNode.instructions.remove(node);
                }
            }
        }
        // 遍历指令寻找目标位置
        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
            // 匹配 new ServerboundMovePlayerPacket.StatusOnly(...) 的实例化
            if (node.getOpcode() == Opcodes.NEW) {
                TypeInsnNode typeNode = (TypeInsnNode) node;
                if (typeNode.desc.equals("net/minecraft/network/protocol/game/ServerboundMovePlayerPacket$StatusOnly")) {
                    // 找到后续的 INVOKESPECIAL（构造函数调用）
                    AbstractInsnNode next = node.getNext();
                    while (next != null && next.getOpcode() != Opcodes.INVOKESPECIAL) {
                        next = next.getNext();
                    }
                    if (next != null) {
                        // 找到 send 方法调用（发送数据包）
                        AbstractInsnNode sendPacketNode = next.getNext();
                        while (sendPacketNode != null && !(sendPacketNode instanceof MethodInsnNode)) {
                            sendPacketNode = sendPacketNode.getNext();
                        }
                        if (sendPacketNode != null) {
                            MethodInsnNode methodCall = (MethodInsnNode) sendPacketNode;
                            if (methodCall.name.equals(Mapping.get(ClientPacketListener.class, "send", "(Lnet/minecraft/network/protocol/Packet;)V"))
                                    && methodCall.desc.equals("(Lnet/minecraft/network/protocol/Packet;)V")) {
                                // 在发送数据包后插入 postRelease()
                                InsnList insertList = new InsnList();
                                insertList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        Type.getInternalName(LocalPlayerTransformer.class),
                                        "postRelease",
                                        "()V"
                                ));
                                methodNode.instructions.insert(methodCall.getNext(), insertList);
                                break;
                            }
                        }
                    }
                }
            }
        }
        methodNode.instructions.insert(list);
        methodNode.instructions.insertBefore(
                methodNode.instructions.getLast().getPrevious(),
                postCall
        );
    }
}