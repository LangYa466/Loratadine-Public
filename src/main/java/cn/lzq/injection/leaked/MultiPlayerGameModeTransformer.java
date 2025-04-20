package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.AttackEvent;
import cn.lzq.injection.leaked.invoked.BlockDamageEvent;
import cn.lzq.injection.leaked.invoked.UseItemEvent;
import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.CancellableEvent;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class MultiPlayerGameModeTransformer extends ASMTransformer implements Wrapper {
    public MultiPlayerGameModeTransformer() {
        super(MultiPlayerGameMode.class);
    }

    public static AttackEvent onAttack(Entity entity) {
        return (AttackEvent) Loratadine.INSTANCE.getEventManager().call(new AttackEvent(entity));
    }

    public static UseItemEvent onUseItem(float yaw, float pitch) {
        return (UseItemEvent) Loratadine.INSTANCE.getEventManager().call(new UseItemEvent(yaw, pitch));
    }

    //死妈东西不知道怎么修
/*    @Inject(method = "startDestroyBlock", desc = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z")
    public void startDestroyBlock(MethodNode methodNode) {
        InsnList insnNodes = new InsnList();

        // 创建对 MultiPlayerGameModeTransformer.blockDamage 的调用
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 0)); // 加载 "this"
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 加载 p_105270_ (BlockPos)
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(MultiPlayerGameModeTransformer.class),
                "blockDamage",
                "(Lnet/minecraft/core/BlockPos;)V",
                false));

        // 遍历指令以找到 if 判断的位置
        AbstractInsnNode targetNode = null;
        for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
            // 查找符合条件的 if 判断的指令位置
            if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                    insn instanceof MethodInsnNode &&
                    ((MethodInsnNode) insn).name.equals(Mapping.get(BlockBehaviour.BlockStateBase.class, "attack", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"))) {

                // 找到 if 判断后的一条指令
                targetNode = insn.getNext();
                break;
            }
        }

        // 确认找到目标位置后插入指令
        if (targetNode != null) {
            methodNode.instructions.insert(targetNode, insnNodes);
        }
    }

    public static void blockDamage(BlockPos pos) {
        if (mc.hitResult != null && mc.hitResult.getType().equals(HitResult.Type.BLOCK) && ((BlockHitResult) mc.hitResult).getBlockPos().equals(pos)) {
            final BlockDamageEvent bdEvent = new BlockDamageEvent(mc.player, mc.level, pos);
            Loratadine.INSTANCE.getEventManager().call(bdEvent);
        }
    }*/

    @Inject(method = "useItem", desc = "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")
    public void useItem(MethodNode methodNode) {
        InsnList insnNodes = new InsnList();
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getYRot", "()F"), "()F")); // 混淆后的 "getYRot" 方法
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", Mapping.get(Entity.class, "getXRot", "()F"), "()F")); // 混淆后的 "getXRot" 方法
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MultiPlayerGameModeTransformer.class), "onUseItem", "(FF)L" + UseItemEvent.class.getName().replace(".", "/") + ";"));
        insnNodes.add(new VarInsnNode(Opcodes.ASTORE, 4));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var >= 4) {
                ((VarInsnNode) node).var += 1;
            }

            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getYRot", "()F"))) { // 混淆后的 "getYRot" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 4));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, UseItemEvent.class.getName().replace(".", "/"), "yaw", "F"));
                    methodNode.instructions.remove(node);
                }
            }
            if (node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals(Mapping.get(Entity.class, "getXRot", "()F"))) { // 混淆后的 "getXRot" 方法
                AbstractInsnNode aload_0 = methodNode.instructions.get(i - 1);
                if (aload_0 instanceof VarInsnNode) {
                    methodNode.instructions.insert(aload_0, new VarInsnNode(Opcodes.ALOAD, 4));
                    methodNode.instructions.remove(aload_0);
                    methodNode.instructions.insert(node, new FieldInsnNode(Opcodes.GETFIELD, UseItemEvent.class.getName().replace(".", "/"), "pitch", "F"));
                    methodNode.instructions.remove(node);
                }
            }
        }

        methodNode.instructions.insert(insnNodes);
    }

    @Inject(method = "attack", desc = "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V")
    public void attack(MethodNode methodNode) {
        // Find the highest local variable index used in the method
        int maxVarIndex = 2; // Start with the known parameters (0-2)
        for (AbstractInsnNode insn : methodNode.instructions) {
            if (insn instanceof VarInsnNode) {
                VarInsnNode varInsn = (VarInsnNode) insn;
                maxVarIndex = Math.max(maxVarIndex, varInsn.var);
            }
        }

        // Use the next available index for our event variable
        int eventVarIndex = maxVarIndex + 1;

        InsnList insnNodes = new InsnList();

        // Load the Entity parameter for the onAttack call
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 2)); // Entity parameter

        // Call the static onAttack method
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(MultiPlayerGameModeTransformer.class),
                "onAttack",
                "(Lnet/minecraft/world/entity/Entity;)L" + AttackEvent.class.getName().replace(".", "/") + ";",
                false));

        // Store the event in a safe local variable
        insnNodes.add(new VarInsnNode(Opcodes.ASTORE, eventVarIndex));

        // Load the event to check if it's cancelled
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, eventVarIndex));

        // Call isCancelled method
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(CancellableEvent.class),
                "isCancelled",
                "()Z",
                false));

        // If not cancelled, continue to the original method; otherwise return early
        LabelNode continueLabel = new LabelNode();
        insnNodes.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        insnNodes.add(new InsnNode(Opcodes.RETURN));
        insnNodes.add(continueLabel);

        // Insert our instructions at the beginning of the method
        methodNode.instructions.insert(insnNodes);
    }
}