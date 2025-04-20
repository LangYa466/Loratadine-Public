package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.render.ItemPhysic;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.Random;

/**
 * @author Jon_awa
 * @since 2025/2/8
 * @updated 2025/3/18 - Updated for 1.20.1
 */
public class ItemEntityRendererTransformer extends ASMTransformer implements Wrapper {
    private static final Random random = new Random();

    public ItemEntityRendererTransformer() {
        super(ItemEntityRenderer.class);
    }

    public static boolean isItemPhysicEnabled() {
        return Loratadine.INSTANCE.getModuleManager() != null
                && Loratadine.INSTANCE.getModuleManager().getModule(ItemPhysic.class) != null
                && Loratadine.INSTANCE.getModuleManager().getModule(ItemPhysic.class).isEnabled()
                && mc.player != null;
    }

    @Inject(method = "render", desc = "(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void render(MethodNode methodNode) {
        transformer(methodNode);
    }

    private static void transformer(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        LabelNode continueLabel = new LabelNode();

        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(ItemEntityRendererTransformer.class),
                        "isItemPhysicEnabled",
                        "()Z",
                        false
                )
        );

        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 5));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 6));

        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(ItemEntityRendererTransformer.class),
                        "renderItem",
                        "(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                        false
                )
        );

        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);

        methodNode.instructions.insert(instructions);
    }

    public static void renderItem(ItemEntity itemEntity, float ignore, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        ItemStack itemstack = itemEntity.getItem();
        int speed = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
        random.setSeed(speed);
        BakedModel bakedmodel = mc.getItemRenderer().getModel(itemstack, itemEntity.level(), null, itemEntity.getId());
        boolean isGui3D = bakedmodel.isGui3d();
        int amount = getRenderAmount(itemstack);
        float f;
        float f2;
        if (!isGui3D) {
            f = -0.0F * (amount - 1F) * 0.5F;
            f2 = -0.09375F * (amount - 1F) * 0.5F;
            float f3 = -0.0F * (amount - 1F) * 0.5F;
            poseStack.translate(f3, f, f2);
        }

        for (int i = 0; i < amount; ++i) {
            poseStack.pushPose();
            if (i > 0) {
                if (isGui3D) {
                    f = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    f2 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float zOffset = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    poseStack.translate(shouldSpreadItems() ? f : 0.0, shouldSpreadItems() ? f2 : 0.0, shouldSpreadItems() ? zOffset : 0.0);
                } else {
                    f = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    f2 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    poseStack.translate(shouldSpreadItems() ? f : 0.0, shouldSpreadItems() ? f2 : 0.0, 0.0);
                }
            }

            if (itemEntity.onGround()) {
                double var = ((itemEntity.getX() + itemEntity.getDeltaMovement().x * partialTicks) * 200) + ((itemEntity.getZ() + itemEntity.getDeltaMovement().z * partialTicks) * 200);
                final Item item = itemEntity.getItem().getItem();

                poseStack.translate(0, 0.06, 0);
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(var)));

                if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof TrapDoorBlock) {
                    poseStack.mulPose(new Quaternionf().rotateX(0));
                } else if (!(item instanceof BlockItem blockItem && !blockItem.getBlock().defaultBlockState().isPathfindable(itemEntity.level(), itemEntity.blockPosition(), PathComputationType.LAND))) {
                    poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(90)));
                }

            } else {
                double rotateSpeed = ItemPhysic.INSTANCE.rotateSpeed.getValue().doubleValue();
                double x = (itemEntity.getX() + itemEntity.getDeltaMovement().x * partialTicks) * rotateSpeed;
                double y = (itemEntity.getY() + itemEntity.getDeltaMovement().y * partialTicks) * rotateSpeed;
                double z = (itemEntity.getZ() + itemEntity.getDeltaMovement().z * partialTicks) * rotateSpeed;

                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-x)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-y)));
                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-z)));
            }

            mc.getItemRenderer().render(itemstack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();

            if (!isGui3D) {
                poseStack.translate(0.0, 0.0, 0.09375);
            }
        }

        poseStack.popPose();
    }

    protected static int getRenderAmount(ItemStack itemStack) {
        int i = 1;

        if (itemStack.getCount() > 48) {
            i = 5;
        } else if (itemStack.getCount() > 32) {
            i = 4;
        } else if (itemStack.getCount() > 16) {
            i = 3;
        } else if (itemStack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    private static boolean shouldSpreadItems() {
        return true;
    }
}