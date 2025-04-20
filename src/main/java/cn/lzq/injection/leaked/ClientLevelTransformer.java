package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class ClientLevelTransformer extends ASMTransformer implements Wrapper  {
    public ClientLevelTransformer() {
        super(ClientLevel.class);
    }

    public static int skipTicks = 0;

    public static boolean skipTicks(Entity entity) {
        if (skipTicks > 0 && entity == mc.player) {
            skipTicks--;
            return true;
        }
        return false;
    }

    @Inject(method = "tickNonPassenger", desc = "(Lnet/minecraft/world/entity/Entity;)V")
    public void tickNonPassenger(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        LabelNode continueLabel = new LabelNode();

        for (AbstractInsnNode node : methodNode.instructions.toArray()) {
            // 定位到 GETFIELD tickCount 的指令
            if (node.getOpcode() == Opcodes.GETFIELD) {
                FieldInsnNode fieldNode = (FieldInsnNode) node;
                if (fieldNode.name.equals(Mapping.get(Entity.class, "tickCount", null)) && fieldNode.desc.equals("I")) {
                    // 在 tickCount 字段访问后插入逻辑
                    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); // 加载 Entity 对象
                    instructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            Type.getInternalName(ClientLevelTransformer.class),
                            "skipTicks",
                            "(Lnet/minecraft/world/entity/Entity;)Z",
                            false
                    ));
                    instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
                    instructions.add(new InsnNode(Opcodes.RETURN));
                    instructions.add(continueLabel);
                    methodNode.instructions.insert(node, instructions);
                    break;
                }
            }
        }
    }
}
