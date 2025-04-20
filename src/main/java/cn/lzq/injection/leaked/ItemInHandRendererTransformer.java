package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.impl.render.Animations;
import shop.xmz.lol.loratadine.utils.item.SpoofItemUtil;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;


public class ItemInHandRendererTransformer extends ASMTransformer implements Wrapper {

    public ItemInHandRendererTransformer() {
        super(ItemInHandRenderer.class);
    }

    public static boolean isAnimationEnabled() {
        return Loratadine.INSTANCE != null
                && Loratadine.INSTANCE.getModuleManager() != null
                && Loratadine.INSTANCE.getModuleManager().getModule(Animations.class) != null
                && Loratadine.INSTANCE.getModuleManager().getModule(Animations.class).isEnabled();
    }

    public static boolean doReplace() {
        return mc.player != null && !mc.player.getMainHandItem().is(Items.FILLED_MAP) && !mc.player.getOffhandItem().is(Items.FILLED_MAP);
    }

    @Inject(method = "renderArmWithItem", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void renderArmWithItem(MethodNode methodNode) {
        Animations(methodNode);
    }

    private static void Animations(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        LabelNode continueLabel = new LabelNode();

        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(ItemInHandRendererTransformer.class),
                "doReplace",
                "()Z",
                false
        ));

        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 5));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 6));
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 7));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 8));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 9));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 10));

        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        Type.getInternalName(ItemInHandRendererTransformer.class),
                        "animation",
                        "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                        false
                )
        );

        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);

        methodNode.instructions.insert(instructions);
    }

    public static void animation(AbstractClientPlayer player, float partialTicks, float ignore, InteractionHand hand, float swingProgress, ItemStack itemStack, float sp, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight) {
        if (!player.isScoping() && mc.player != null) {
            boolean isMainHand = hand == InteractionHand.MAIN_HAND;
            HumanoidArm humanoidarm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            poseStack.pushPose();

            if (SpoofItemUtil.getStack() != null && isMainHand) {
                itemStack = SpoofItemUtil.getStack();
            }

            if (isAnimationEnabled()) {
                if (isMainHand) {
                    poseStack.translate(Animations.INSTANCE.rightX.getValue().doubleValue(), Animations.INSTANCE.rightY.getValue().doubleValue(), Animations.INSTANCE.rightZ.getValue().doubleValue());
                } else {
                    poseStack.translate(Animations.INSTANCE.leftX.getValue().doubleValue(), Animations.INSTANCE.leftY.getValue().doubleValue(), Animations.INSTANCE.leftZ.getValue().doubleValue());
                }
                poseStack.scale(Animations.INSTANCE.itemScale.getValue().floatValue(), Animations.INSTANCE.itemScale.getValue().floatValue(), Animations.INSTANCE.itemScale.getValue().floatValue());
            }

            if (itemStack.isEmpty()) {
                if (isMainHand && !player.isInvisible()) {
                    renderPlayerArm(poseStack, bufferSource, combinedLight, sp, swingProgress, humanoidarm);
                }
            } else {
                boolean flag3;
                float f12;
                float f11;
                float f14;
                float f17;
                float f7;

                if (itemStack.is(Items.CROSSBOW)) {
                    flag3 = CrossbowItem.isCharged(itemStack);
                    boolean flag2 = humanoidarm == HumanoidArm.RIGHT;
                    int i = flag2 ? 1 : -1;
                    if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                        applyItemArmTransform(poseStack, humanoidarm);
                        poseStack.translate((float) i * -0.4785682F, -0.0943870022892952, 0.05731530860066414);
                        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-11.935F)));
                        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(i * 65.3F)));
                        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(i * -9.785F)));
                        f12 = (float) itemStack.getUseDuration() - ((float) mc.player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                        f7 = f12 / (float) CrossbowItem.getChargeDuration(itemStack);
                        if (f7 > 1.0F) {
                            f7 = 1.0F;
                        }

                        if (f7 > 0.1F) {
                            f11 = Mth.sin((f12 - 0.1F) * 1.3F);
                            f14 = f7 - 0.1F;
                            f17 = f11 * f14;
                            poseStack.translate(f17 * 0.0F, f17 * 0.004F, f17 * 0.0F);
                        }

                        poseStack.translate(f7 * 0.0F, f7 * 0.0F, f7 * 0.04F);
                        poseStack.scale(1.0F, 1.0F, 1.0F + f7 * 0.2F);
                        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(i * -45.0F)));
                    } else {
                        f12 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * 3.1415927F);
                        f7 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * 6.2831855F);
                        f11 = -0.2F * Mth.sin(swingProgress * 3.1415927F);
                        poseStack.translate((float) i * f12, f7, f11);
                        applyItemArmTransform(poseStack, humanoidarm);
                        applyItemArmAttackTransform(poseStack, humanoidarm, swingProgress);
                        if (flag3 && swingProgress < 0.001F && isMainHand) {
                            poseStack.translate((float) i * -0.641864F, 0.0, 0.0);
                            poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(i * 10.0F)));
                        }
                    }

                    mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, itemStack, flag2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag2, poseStack, bufferSource, combinedLight);
                } else {
                    flag3 = humanoidarm == HumanoidArm.RIGHT;
                    int k;
                    float f8;

                    if ((itemStack.getItem() instanceof SwordItem && ((!mc.player.getOffhandItem().getItem().isEdible() && mc.options.keyUse.isDown()) || KillAura.INSTANCE.blocking) || (Animations.INSTANCE.everythingBlock.getValue() && mc.options.keyUse.isDown())) && isAnimationEnabled()) {
                        HumanoidArm enumHandSide = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
                        switch (Animations.INSTANCE.swordValue.getValue()) {
                            case "1.7":
                                transformSideFirstPersonBlock_1_7(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "SideDown":
                                transformSideFirstPersonBlock(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "SigmaOld":
                                sigmaOld(enumHandSide, sp, swingProgress, poseStack);
                                break;
                            case "Zoom":
                                Zoom(sp, swingProgress, poseStack);
                                break;
                            case "WindMill":
                                WindMill(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "Push":
                                Push(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "Slide":
                                avatar(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "Boop":
                                boop(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "SpinnyBoi":
                                spinnyBoi(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "Slide2":
                                half(poseStack, enumHandSide, sp, swingProgress);
                                break;
                            case "Smooth":
                                SmoothBlock(poseStack, enumHandSide, sp, swingProgress);
                                break;
                        }
                    } else if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
                        k = flag3 ? 1 : -1;
                        switch (itemStack.getUseAnimation()) {
                            case NONE, BLOCK:
                                applyItemArmTransform(poseStack, humanoidarm);
                                break;
                            case EAT:
                            case DRINK:
                                applyEatTransform(poseStack, partialTicks, humanoidarm, itemStack);
                                applyItemArmTransform(poseStack, humanoidarm);
                                break;
                            case BOW:
                                applyItemArmTransform(poseStack, humanoidarm);
                                poseStack.translate((float) k * -0.2785682F, 0.18344387412071228, 0.15731531381607056);
                                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-13.935F)));
                                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(k * 35.3F)));
                                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(k * -9.785F)));
                                f8 = (float) itemStack.getUseDuration() - ((float) mc.player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                f12 = f8 / 20.0F;
                                f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
                                if (f12 > 1.0F) {
                                    f12 = 1.0F;
                                }

                                if (f12 > 0.1F) {
                                    f7 = Mth.sin((f8 - 0.1F) * 1.3F);
                                    f11 = f12 - 0.1F;
                                    f14 = f7 * f11;
                                    poseStack.translate(f14 * 0.0F, f14 * 0.004F, f14 * 0.0F);
                                }

                                poseStack.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
                                poseStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
                                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(k * -45.0F)));
                                break;
                            case SPEAR:
                                applyItemArmTransform(poseStack, humanoidarm);
                                poseStack.translate((float) k * -0.5F, 0.699999988079071, 0.10000000149011612);
                                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-55.0F)));
                                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(k * 35.3F)));
                                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(k * -9.785F)));
                                f7 = (float) itemStack.getUseDuration() - ((float) mc.player.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                f11 = f7 / 10.0F;
                                if (f11 > 1.0F) {
                                    f11 = 1.0F;
                                }

                                if (f11 > 0.1F) {
                                    f14 = Mth.sin((f7 - 0.1F) * 1.3F);
                                    f17 = f11 - 0.1F;
                                    float f19 = f14 * f17;
                                    poseStack.translate(f19 * 0.0F, f19 * 0.004F, f19 * 0.0F);
                                }

                                poseStack.translate(0.0, 0.0, f11 * 0.2F);
                                poseStack.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
                                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(k * -45.0F)));
                        }
                    } else if (player.isAutoSpinAttack()) {
                        applyItemArmTransform(poseStack, humanoidarm);
                        k = flag3 ? 1 : -1;
                        poseStack.translate((float) k * -0.4F, 0.800000011920929, 0.30000001192092896);
                        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(k * 65.0F)));
                        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(k * -85.0F)));
                    } else {
                        float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * 3.1415927F);
                        f8 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * 6.2831855F);
                        f12 = -0.2F * Mth.sin(swingProgress * 3.1415927F);
                        int l = flag3 ? 1 : -1;
                        poseStack.translate((float) l * f5, f8, f12);
                        applyItemArmTransform(poseStack, humanoidarm);
                        applyItemArmAttackTransform(poseStack, humanoidarm, swingProgress);
                    }

                    mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player, itemStack, flag3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag3, poseStack, bufferSource, combinedLight);
                }
            }
            poseStack.popPose();
        }
    }

    private static void renderPlayerArm(PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, float sp, float swingProgress, HumanoidArm arm) {
        boolean flag = arm != HumanoidArm.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(swingProgress);
        float f2 = -0.3F * Mth.sin(f1 * 3.1415927F);
        float f3 = 0.4F * Mth.sin(f1 * 6.2831855F);
        float f4 = -0.4F * Mth.sin(swingProgress * 3.1415927F);
        poseStack.translate(f * (f2 + 0.64000005F), f3 - 0.6F + sp * -0.6F, f4 - 0.71999997F);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * 45.0F)));
        float f5 = Mth.sin(swingProgress * swingProgress * 3.1415927F);
        float f6 = Mth.sin(f1 * 3.1415927F);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * f6 * 70.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f * f5 * -20.0F)));
        AbstractClientPlayer abstractclientplayer = mc.player;
        if (abstractclientplayer == null) return;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
        poseStack.translate(f * -1.0F, 3.5999999046325684, 3.5);
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f * 120.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(200.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * -135.0F)));
        poseStack.translate(f * 5.6F, 0.0, 0.0);
        PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(abstractclientplayer);
        if (flag) {
            playerrenderer.renderRightHand(poseStack, bufferSource, combinedLight, abstractclientplayer);
        } else {
            playerrenderer.renderLeftHand(poseStack, bufferSource, combinedLight, abstractclientplayer);
        }
    }

    private static void applyEatTransform(PoseStack poseStack, float partialTicks, HumanoidArm arm, ItemStack itemStack) {
        if (mc.player == null) return;
        float f = (float) mc.player.getUseItemRemainingTicks() - partialTicks + 1.0F;
        float f1 = f / (float) itemStack.getUseDuration();
        float f3;
        if (f1 < 0.8F) {
            f3 = Mth.abs(Mth.cos(f / 4.0F * 3.1415927F) * 0.1F);
            poseStack.translate(0.0, f3, 0.0);
        }

        f3 = 1.0F - (float) Math.pow(f1, 27.0);
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(f3 * 0.6F * (float) i, f3 * -0.5F, f3 * 0.0F);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(i * f3 * 90.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f3 * 10.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(i * f3 * 30.0F)));
    }

    private static void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate((float) i * 0.56F, -0.52F, -0.72F);
    }

    private static void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(i * (45.0F + f * -20.0F))));
        float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(i * f1 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -80.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(i * -45.0F)));
    }

    private static void WindMill(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        poseStack.translate(side * -0.1414214, 0.08, 0.1414214);
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-102.25F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * 13.365F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * 78.050003F)));
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f1 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -50.0F)));
    }

    private static void SmoothBlock(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        poseStack.translate(side * -0.1414214, 0.08, 0.1414214);
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-102.25F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * 13.365F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * 78.050003F)));
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f1 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -30.0F)));
    }

    private static void Push(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        poseStack.translate(side * -0.1414214, 0.08, 0.1414214);
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-102.25F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * 13.365F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * 78.050003F)));
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f * -10.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * -10.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f * -10.0F)));

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -10.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f1 * -10.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f1 * -10.0F)));

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -10.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f1 * -10.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f1 * -10.0F)));
    }

    private static void Zoom(float equippedProg, float swingProgress, PoseStack poseStack) {
        poseStack.translate(0.56F, -0.52F, -0.71999997F);
        poseStack.translate(0.0F, equippedProg * -0.6F, 0.0F);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(45.0F)));

        float var3 = (float) Math.sin(swingProgress * swingProgress * Math.PI);
        float var4 = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(var3 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(var4 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(var4 * -20.0F)));
    }

    private static void sigmaOld(HumanoidArm handSide, float equippedProg, float swingProgress, PoseStack poseStack) {
        int side = handSide == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(0.56F, -0.52F, -0.71999997F);
        poseStack.translate(0.0F, equippedProg * -0.6F, 0.0F);

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-102.25F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * 13.365F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * 78.050003F)));

        float var3 = (float) Math.sin(swingProgress * swingProgress * Math.PI);
        float var4 = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);

        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(var3 * -15.0F)));

        poseStack.mulPose(new Quaternionf().rotateXYZ(
                (float) Math.toRadians(0.2F),
                (float) Math.toRadians(var4 * -10.0F),
                (float) Math.toRadians(1.0F)
        ));

        poseStack.mulPose(new Quaternionf().rotateXYZ(
                (float) Math.toRadians(1.3F),
                (float) Math.toRadians(var4 * -30.0F),
                (float) Math.toRadians(0.2F)
        ));
    }

    private static void transformSideFirstPersonBlock(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56D, -0.52D + equippedProg * -0.6D, -0.72D);
        poseStack.translate(side * -0.1414214D, 0.08D, 0.1414214D);
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-102.25F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * 13.365F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * 78.05F)));
        double f = Math.sin(swingProgress * swingProgress * Math.PI);
        double f1 = Math.sin(Math.sqrt(swingProgress) * Math.PI);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f1 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -80.0F)));
    }

    private static void transformSideFirstPersonBlock_1_7(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(side * 0.56F, -0.52F + equippedProg * -0.6F, -0.71999997F);
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * (45.0F + f * -20.0F))));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * f1 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f1 * -80.0F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * -45.0F)));
        poseStack.scale(0.9F, 0.9F, 0.9F);
        poseStack.translate(-0.2F, 0.126F, 0.2F);
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-102.25F)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(side * 15.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(side * 80.0F)));
    }

    private static void avatar(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(side * 0.56F, -0.40, -0.71999997F);

        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(45.0F)));
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = Mth.sqrt(swingProgress) * (float) Math.PI;
        float f2 = Mth.sin(f1);

        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f2 * -20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f2 * -30.0F)));
    }

    private static void half(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(side * 0.62F, -0.28F, -0.71999997F);
        poseStack.translate(0.0F, equippedProg * -0.6F, 0.0F);
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(50.0F)));

        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = Mth.sqrt(swingProgress) * (float) Math.PI;
        float f2 = Mth.sin(f1);

        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(f * 0.0F)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(f2 * 0.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f2 * -40.0F)));
    }

    private static void boop(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(side * 0.56F, -0.40, -0.71999997F);

        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(45.0F)));
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = Mth.sqrt(swingProgress) * (float) Math.PI;
        float f2 = Mth.sin(f1);

        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f * 20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f2 * 20.0F)));
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(f2 * 20.0F)));
    }

    private static void spinnyBoi(PoseStack poseStack, HumanoidArm arm, float equippedProg, float swingProgress) {
        float rotationAngle = (float) (System.currentTimeMillis() / 2 % 360);

        poseStack.translate(0, 0.2f, -1.0f);
        poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-59)));
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-rotationAngle)));
        poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(30.0F)));
    }
}