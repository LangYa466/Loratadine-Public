package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.utils.item.SpoofItemUtil;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class ItemInHandLayerTransformer extends ASMTransformer implements Wrapper {
    public ItemInHandLayerTransformer() {
        super(ItemInHandLayer.class);
    }

    @Inject(method = "renderArmWithItem", desc = "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void renderArmWithItem(MethodNode methodNode) {
        AbstractInsnNode[] instructions = methodNode.instructions.toArray();
        for (AbstractInsnNode insn : instructions) {
            if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.name.equals(Mapping.get(ArmedModel.class, "translateToHand", "(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
                        && methodInsn.owner.equals("net/minecraft/client/model/ArmedModel")
                        && methodInsn.desc.equals("(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V")) {
                    InsnList insertCode = new InsnList();

                    insertCode.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    insertCode.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    insertCode.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    insertCode.add(new VarInsnNode(Opcodes.ALOAD, 4));
                    insertCode.add(new VarInsnNode(Opcodes.ALOAD, 5));
                    insertCode.add(new VarInsnNode(Opcodes.ALOAD, 6));
                    insertCode.add(new VarInsnNode(Opcodes.ILOAD, 7));
                    insertCode.add(
                            new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    Type.getInternalName(ItemInHandLayerTransformer.class),
                                    "renderArmWithSpoofItem",
                                    "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                                    false
                            )
                    );
                    insertCode.add(new InsnNode(Opcodes.RETURN));

                    methodNode.instructions.insert(insn.getNext(), insertCode);
                    break;
                }
            }
        }
    }

    public static void renderArmWithSpoofItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource bufferSource, int i) {
        boolean humanoidArmIsLeft = arm == HumanoidArm.LEFT;

        if (SpoofItemUtil.getStack() != null && displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            itemStack = SpoofItemUtil.getStack();
        }

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-90.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(180.0F)));
        poseStack.translate((float) (humanoidArmIsLeft ? -1 : 1) / 16.0F, 0.125, -0.625);
        mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(entity, itemStack, displayContext, humanoidArmIsLeft, poseStack, bufferSource, i);
        poseStack.popPose();
    }
}