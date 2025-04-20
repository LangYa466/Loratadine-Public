package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.PitchRenderEvent;
import shop.xmz.lol.loratadine.Loratadine;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class HumanoidModelTransformer extends ASMTransformer implements Wrapper {
    public HumanoidModelTransformer() {
        super(HumanoidModel.class);
    }

    public static PitchRenderEvent onPitchRender(LivingEntity entity, float pitch) {
        if (mc == null || mc.player == null || mc.level == null) return null;

        return entity == mc.player ? (PitchRenderEvent) Loratadine.INSTANCE.getEventManager().call(new PitchRenderEvent(pitch)) : new PitchRenderEvent(pitch);
    }

    @Inject(method = "setupAnim", desc = "(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V")
    public void setupAnim(MethodNode methodNode) {
        InsnList list = new InsnList();
        int j = 7;

        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.FLOAD, 6));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(HumanoidModelTransformer.class), "onPitchRender", "(Lnet/minecraft/world/entity/LivingEntity;F)L" + PitchRenderEvent.class.getName().replace(".", "/") + ";",false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 7));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);
            if (node instanceof VarInsnNode && ((VarInsnNode) node).var >= j) {
                ((VarInsnNode) node).var += j;
            }

            if (node instanceof VarInsnNode && ((VarInsnNode) node).var == 6) {
                InsnList insnNode = new InsnList();
                insnNode.add(new VarInsnNode(Opcodes.ALOAD, 7));
                insnNode.add(new FieldInsnNode(Opcodes.GETFIELD, PitchRenderEvent.class.getName().replace(".", "/"), "pitch", "F"));

                methodNode.instructions.insert(node, insnNode);
                methodNode.instructions.remove(node);
            }
        }

        methodNode.instructions.insert(list);
    }
}
