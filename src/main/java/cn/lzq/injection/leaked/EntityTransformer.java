package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.LookEvent;
import cn.lzq.injection.leaked.invoked.StrafeEvent;
import cn.lzq.injection.leaked.mapping.Mapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.Loratadine;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class EntityTransformer extends ASMTransformer implements Wrapper {
    public EntityTransformer() {
        super(Entity.class);
    }

    public static LookEvent onLook(Entity this1, float yaw, float pitch) {
        if (mc == null || mc.player == null || mc.level == null) return null;

        return this1 == mc.player ? (LookEvent) Loratadine.INSTANCE.getEventManager().call(new LookEvent(yaw, pitch)) : new LookEvent(yaw, pitch);
    }

    public static StrafeEvent onStrafe(Entity this1, float slowSize, float yaw, Vec3 motion) {
        if (mc == null || mc.player == null || mc.level == null) return null;

        return this1 == mc.player ? (StrafeEvent) Loratadine.INSTANCE.getEventManager().call(new StrafeEvent(motion.x, motion.y, motion.z, yaw, slowSize)) : new StrafeEvent(motion.x, motion.y, motion.z, yaw, slowSize);
    }

    @Inject(method = "getViewVector", desc = "(F)Lnet/minecraft/world/phys/Vec3;")
    public void getViewVector(MethodNode methodNode) {
        InsnList insnList = new InsnList();

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 1));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "net/minecraft/world/entity/Entity",
                Mapping.get(Entity.class, "getViewYRot" , "(F)F"),
                "(F)F"));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 1));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "net/minecraft/world/entity/Entity",
                Mapping.get(Entity.class, "getViewXRot" , "(F)F"),
                "(F)F"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(EntityTransformer.class), "onLook", "(Lnet/minecraft/world/entity/Entity;FF)L" + LookEvent.class.getName().replace(".", "/") + ";"));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 2));

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, LookEvent.class.getName().replace(".", "/"), "rotationPitch", "F"));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, LookEvent.class.getName().replace(".", "/"), "rotationYaw", "F"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "net/minecraft/world/entity/Entity",
                Mapping.get(Entity.class, "calculateViewVector", "(FF)Lnet/minecraft/world/phys/Vec3;"),
                "(FF)Lnet/minecraft/world/phys/Vec3;"));
        insnList.add(new InsnNode(Opcodes.ARETURN));

        methodNode.instructions.clear();
        methodNode.instructions.insert(insnList);
    }

    @Inject(method = "moveRelative", desc = "(FLnet/minecraft/world/phys/Vec3;)V")
    public void moveRelative(MethodNode methodNode) {
        InsnList list = new InsnList();
        int j = 3;

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.FLOAD, 1)); // 函数传入值 p_19921_ 应该是Slow的数值
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getYRot", "()F"), "()F")); // 混淆后的 "getYRot" 方法
        list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // 函数传入值 p_19922_ Vec3形式的Motion
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(EntityTransformer.class), "onStrafe", "(Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/phys/Vec3;)L" + StrafeEvent.class.getName().replace(".", "/") + ";"));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var >= j) {
                ((VarInsnNode) node).var += 1;
            }
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var == 2) { // Motion
                InsnList insnNodes = new InsnList();
                insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 3));
                insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, StrafeEvent.class.getName().replace(".", "/"), "getMotion", "()Lnet/minecraft/world/phys/Vec3;"));
                methodNode.instructions.insert(node, insnNodes);
                methodNode.instructions.remove(node);
            }
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var == 1) { // SlowSize
                InsnList insnNodes = new InsnList();
                insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 3));
                insnNodes.add(new FieldInsnNode(Opcodes.GETFIELD, StrafeEvent.class.getName().replace(".", "/"), "slowSize", "F"));
                methodNode.instructions.insert(node, insnNodes);
                methodNode.instructions.remove(node);
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getYRot", "()F"))) { // 混淆后的 "getYRot" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 3));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, StrafeEvent.class.getName().replace(".", "/"), "rotationYaw", "F"));
                    methodNode.instructions.remove(node);
                }
            }
        }
        methodNode.instructions.insert(list);
    }
}
