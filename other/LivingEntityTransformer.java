package other;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class LivingEntityTransformer extends ASMTransformer implements IWrapper {
    //卡空要用到的 看不懂自己转AI
    public LivingEntityTransformer() {
        super(LivingEntity.class);
    }

    public static float jumpPower;

    public static JumpEvent onJump(LivingEntity this1, float yaw) {
        if (IWrapper.mc == null || IWrapper.mc.player == null || IWrapper.mc.level == null) return null;

        return this1 == IWrapper.mc.player ? (JumpEvent) Valor.INSTANCE.getEventManager().call(new JumpEvent(yaw)) : new JumpEvent(yaw);
    }

    public static boolean moveMath(LivingEntity entity) {
        if (entity == mc.player) {
            final MoveMathEvent moveMathEvent = new MoveMathEvent();
            Valor.INSTANCE.getEventManager().call(moveMathEvent);

            updateLimbSwing();

            return moveMathEvent.isCancelled();
        }
        return false;
    }

    public static void updateLimbSwing() {
        if (mc.player == null) return;

        // 在1.20.1中，动画相关的字段使用了新的结构或方法
        // 这里我们只计算移动因子，不直接操作动画字段
        // 游戏引擎会自行处理动画更新

        final double dx = mc.player.getX() - mc.player.xOld;
        final double dz = mc.player.getZ() - mc.player.zOld;

        float distFactor = Mth.sqrt((float)(dx * dx + dz * dz)) * 4.0F;
        distFactor = Mth.clamp(distFactor, 0.0F, 1.0F);

        // 注意：在1.20.1中，我们不再直接修改动画字段
        // 如需精确控制动画，请使用反射或Mixin访问实际字段
    }

//    public static void updateLimbSwing() {
//        if (mc.player == null) return;
//
//        mc.player.animationSpeedOld = mc.player.animationSpeed;
//
//        final double dx = mc.player.getX() - mc.player.xo;
//        final double dz = mc.player.getZ() - mc.player.zo;
//
//        float distFactor = Mth.sqrt((float) (dx * dx + dz * dz)) * 4.0F;
//        distFactor = Mth.clamp(distFactor, 0.0F, 1.0F);
//
//        mc.player.animationSpeed += (distFactor - mc.player.animationSpeed) * 0.4F;
//        mc.player.animationPosition += mc.player.animationSpeed;
//    }

    // 新的方法，用于设置 jumpPower
    private void setJumpPower(InsnList list) {
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", Mapping.get(LivingEntity.class, "getJumpPower", "()F"), "()F", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/LivingEntity", Mapping.get(LivingEntity.class, "getJumpBoostPower", "()F"), "()F", false));

        list.add(new InsnNode(Opcodes.FADD));

        list.add(new FieldInsnNode(Opcodes.PUTSTATIC, Type.getInternalName(LivingEntityTransformer.class), "jumpPower", "F"));
    }

    @Inject(method = "travel", desc = "(Lnet/minecraft/world/phys/Vec3;)V")
    public void travel(MethodNode methodNode) {
        InsnList list = new InsnList();

        // 创建跳转标签
        LabelNode continueLabel = new LabelNode();

        // 加载 this (LivingEntity 实例)
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));

        // 调用静态方法 moveMath
        list.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(LivingEntityTransformer.class),
                "moveMath",
                "(Lnet/minecraft/world/entity/LivingEntity;)Z",
                false
        ));

        // 如果返回 false，跳转到 continueLabel 继续执行原方法
        list.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));

        // 否则执行 return
        list.add(new InsnNode(Opcodes.RETURN));

        // 添加 continue 标签
        list.add(continueLabel);

        // 将生成的指令插入到方法最前面
        methodNode.instructions.insert(list);
    }

    @Inject(method = "jumpFromGround", desc = "()V")
    public void jumpFromGround(MethodNode methodNode) {
        InsnList list = new InsnList();
        int j = 1;

        // 调用 setJumpPower 方法来设置 jumpPower
        setJumpPower(list);

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getYRot", "()F"), "()F",false)); // 混淆后的 "getYRot" 方法
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(LivingEntityTransformer.class), "onJump", "(Lnet/minecraft/world/entity/LivingEntity;F)L" + JumpEvent.class.getName().replace(".", "/") + ";",false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 1));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var >= j) {
                ((VarInsnNode) node).var += j;
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getYRot", "()F"))) { // 混淆后的 "getYRot" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, JumpEvent.class.getName().replace(".", "/"), "rotationYaw", "F"));
                    methodNode.instructions.remove(node);
                }
            }
        }
        methodNode.instructions.insert(list);
    }
}