package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.item.SpoofItemUtil;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.render.GlowUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;

public class GuiTransformer extends ASMTransformer implements Wrapper {
    protected static final ResourceLocation GUI_ICONS_LOCATION = ResourceLocation.tryParse("textures/gui/icons.png");
    protected static final ResourceLocation WIDGETS_LOCATION = ResourceLocation.tryParse("textures/gui/widgets.png");
    private static final TimerUtils timer = new TimerUtils();
    private static float posInv;

    public GuiTransformer() {
        super(Gui.class);
    }

    @Inject(method = "renderHotbar", desc = "(FLnet/minecraft/client/gui/GuiGraphics;)V")
    private void renderHotbar(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(GuiTransformer.class), "renderHotbar", "(FLnet/minecraft/client/gui/GuiGraphics;)V", false));
        instructions.add(new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insert(instructions);
    }

    @Inject(method = "renderExperienceBar", desc = "(Lnet/minecraft/client/gui/GuiGraphics;I)V")
    private void renderExperienceBar(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(GuiTransformer.class), "renderExperienceBar", "(Lnet/minecraft/client/gui/GuiGraphics;I)V", false));
        instructions.add(new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insert(instructions);
    }

    public static void renderHotbar(float partialTicks, GuiGraphics guiGraphics) {
        Player player = getCameraPlayer();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int i = screenWidth / 2;

        if (player != null) {
            ItemStack itemstack = player.getOffhandItem();
            HumanoidArm humanoidarm = player.getMainArm().getOpposite();

            int i1 = 1;
            int j2;
            int k2;
            int l2;
            switch (HUD.INSTANCE.hotBar_Value.getValue()) {
                case "Minecraft" -> {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);

                    posInv = MathUtils.lerp(posInv, i - 92 + SpoofItemUtil.getSlot() * 20, 0.03f * timer.getTime());
                    guiGraphics.blit(WIDGETS_LOCATION, i - 91, screenHeight - 22, 0, 0, 182, 22);
                    guiGraphics.blit(WIDGETS_LOCATION, (int) posInv, screenHeight - 22 - 1, 0, 22, 24, 22);
                    timer.reset();

                    if (!itemstack.isEmpty()) {
                        if (humanoidarm == HumanoidArm.LEFT) {
                            guiGraphics.blit(WIDGETS_LOCATION, i - 91 - 29, screenHeight - 23, 24, 22, 29, 24);
                        } else {
                            guiGraphics.blit(WIDGETS_LOCATION, i + 91, screenHeight - 23, 53, 22, 29, 24);
                        }
                    }

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    for (j2 = 0; j2 < 9; ++j2) {
                        k2 = i - 90 + j2 * 20 + 2;
                        l2 = screenHeight - 16 - 3;
                        renderSlot(guiGraphics, k2, l2, partialTicks, player, player.getInventory().items.get(j2), i1++);
                    }

                    if (!itemstack.isEmpty()) {
                        j2 = screenHeight - 16 - 3;
                        if (humanoidarm == HumanoidArm.LEFT) {
                            renderSlot(guiGraphics, i - 91 - 26, j2, partialTicks, player, itemstack, i1);
                        } else {
                            renderSlot(guiGraphics, i + 91 + 10, j2, partialTicks, player, itemstack, i1);
                        }
                    }
                }

                case "Modern Vertical" -> {
                    // 垂直布局参数
                    int startX = 20; // 左侧起始位置
                    int startY = mc.getWindow().getGuiScaledHeight() / 2 - 90; // 屏幕中间
                    int slotSize = 24; // 物品槽大小
                    int selectedSlot = SpoofItemUtil.getSlot(); // 当前选中槽位

                    // 绘制所有物品槽
                    for (int slot = 0; slot < 9; slot++) {
                        int slotX = startX;
                        int slotY = startY + slot * slotSize;
                        ItemStack item = player.getInventory().items.get(slot);
                        boolean isSelected = slot == selectedSlot;

                        // 绘制物品槽背景
                        GlowUtils.drawGlow(poseStack, slotX - 2, slotY - 2, slotSize - 4, slotSize - 4, 22,
                                new Color(40, 40, 40, 200),
                                () -> RenderUtils.drawRoundedRect(poseStack, slotX - 2, slotY - 2, slotSize - 4, slotSize - 4, 22, new Color(40, 40, 40, 200)));

                        // 选中物品放大效果
                        if (isSelected) {
                            PoseStack modelViewStack = RenderSystem.getModelViewStack();
                            modelViewStack.pushPose();
                            float scale = 1.2F; // 放大比例
                            float offset = (slotSize * (scale - 1)) / 2; // 计算偏移量以保持居中

                            // 调整位置确保物品在矩形内
                            modelViewStack.translate(slotX + slotSize / 2f, slotY + slotSize / 2f, 0); // 将原点移动到物品中心
                            modelViewStack.scale(scale, scale, 1); // 缩放
                            modelViewStack.translate(-(slotX + slotSize / 2f), -(slotY + slotSize / 2f), 0); // 将原点移回
                            RenderSystem.applyModelViewMatrix();
                        }

                        // 绘制物品
                        renderSlot(guiGraphics, slotX, slotY, partialTicks, player, item, slot + 1);

                        // 恢复变换
                        if (isSelected) {
                            RenderSystem.getModelViewStack().popPose();
                            RenderSystem.applyModelViewMatrix();
                        }

                        // 显示物品名称
                        if (isSelected && !item.isEmpty()) {
                            String name = item.getHoverName().getString();
                            int textWidth = (int) Loratadine.INSTANCE.getFontManager().zw22.getStringWidth(name);
                            int textX = startX + slotSize + 10;
                            int textY = slotY + slotSize / 2 - Loratadine.INSTANCE.getFontManager().zw22.getHeight() / 2;

                            // 绘制背景和文字
                            GlowUtils.drawGlow(poseStack, textX - 2, textY - 2, textWidth + 4, Loratadine.INSTANCE.getFontManager().zw22.getHeight() + 4, 22, new Color(0, 0, 0, 150)
                                    ,() -> RenderUtils.drawRoundedRect(poseStack, textX - 2, textY - 2, textWidth + 4, Loratadine.INSTANCE.getFontManager().zw22.getHeight() + 4, 22, new Color(0, 0, 0, 150)));
                            Loratadine.INSTANCE.getFontManager().zw22.drawString(poseStack, name, textX, textY, Color.WHITE.getRGB());
                        }
                    }

                    // 绘制副手物品
                    if (!itemstack.isEmpty()) {
                        int offhandY = startY + 9 * slotSize + 10;
                        GlowUtils.drawGlow(poseStack, startX - 2, offhandY - 2, slotSize - 4, slotSize - 4, 22, new Color(40, 40, 40, 200),
                                () -> RenderUtils.drawRoundedRect(poseStack, startX - 2, offhandY - 2, slotSize - 4, slotSize - 4, 22, new Color(40, 40, 40, 200)));
                        renderSlot(guiGraphics, startX, offhandY, partialTicks, player, itemstack, 10);
                    }
                }

                case "Loratadine" -> {
                    // 渲染快捷栏
                    posInv = MathUtils.lerp(posInv, i - 90 + SpoofItemUtil.getSlot() * 20, 0.03f * timer.getTime());
                    RenderUtils.drawRoundedRect(poseStack, i - 90, screenHeight - 26.5f, 180, 21, 6F, new Color(0, 0, 0, 60));
                    RenderUtils.drawRoundedRect(poseStack, posInv, screenHeight - 26.5f, 20, 21, 6F, new Color(0, 0, 0, 50));
                    timer.reset();

                    if (!itemstack.isEmpty()) {
                        if (humanoidarm == HumanoidArm.LEFT) {
                            RenderUtils.drawRoundedRect(poseStack, i - 118, screenHeight - 26.5f, 20, 21, 6F, new Color(0, 0, 0, 100));
                        } else {
                            RenderUtils.drawRoundedRect(poseStack, i + 90, screenHeight - 26.5f, 20, 21, 6F, new Color(0, 0, 0, 100));
                        }
                    }

                    // 渲染快捷栏物品
                    for (j2 = 0; j2 < 9; ++j2) {
                        k2 = i - 88 + j2 * 20;
                        l2 = screenHeight - 24;
                        renderSlot(guiGraphics, k2, l2, partialTicks, player, player.getInventory().items.get(j2), i1++);
                    }

                    // 渲染快捷栏副手物品
                    if (!itemstack.isEmpty()) {
                        j2 = screenHeight - 24;
                        if (humanoidarm == HumanoidArm.LEFT) {
                            renderSlot(guiGraphics, i - 116, j2, partialTicks, player, itemstack, i1);
                        } else {
                            renderSlot(guiGraphics, i + 100, j2, partialTicks, player, itemstack, i1);
                        }
                    }
                }

                case "Simple" -> {
                    // 渲染快捷栏
                    posInv = MathUtils.lerp(posInv, i - 90 + SpoofItemUtil.getSlot() * 20, 0.03f * timer.getTime());
                    RenderUtils.drawRectangle(poseStack, i - 90, screenHeight - 26.5f, 180, 21, new Color(0, 0, 0, 160).getRGB());
                    RenderUtils.drawRectangle(poseStack, posInv, screenHeight - 26.5f, 20, 21, new Color(0, 0, 0, 160).getRGB());

                    if (!itemstack.isEmpty()) {
                        if (humanoidarm == HumanoidArm.LEFT) {
                            RenderUtils.drawRectangle(poseStack, i - 118, screenHeight - 26.5f, 20, 21, new Color(0, 0, 0, 160).getRGB());
                        } else {
                            RenderUtils.drawRectangle(poseStack, i + 92, screenHeight - 26.5f, 20, 21, new Color(0, 0, 0, 160).getRGB());
                        }
                    }

                    // 渲染快捷栏物品
                    for (j2 = 0; j2 < 9; ++j2) {
                        k2 = i - 88 + j2 * 20;
                        l2 = screenHeight - 24;
                        renderSlot(guiGraphics, k2, l2, partialTicks, player, player.getInventory().items.get(j2), i1++);
                    }

                    RenderUtils.drawGradientRectL2R(poseStack, posInv, screenHeight - 26.5f, 20, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());

                    // 渲染快捷栏副手物品
                    if (!itemstack.isEmpty()) {
                        j2 = screenHeight - 24;
                        if (humanoidarm == HumanoidArm.LEFT) {
                            renderSlot(guiGraphics, i - 116, j2, partialTicks, player, itemstack, i1);
                            RenderUtils.drawGradientRectL2R(poseStack, i - 118, screenHeight - 26.5f, 20, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());
                        } else {
                            renderSlot(guiGraphics, i + 100, j2, partialTicks, player, itemstack, i1);
                            RenderUtils.drawGradientRectL2R(poseStack, i + 92, screenHeight - 26.5f, 20, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());
                        }
                    }

                    timer.reset();
                }
            }

            // 渲染攻击冷却条
            if (mc.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR && mc.player != null) {
                float f = mc.player.getAttackStrengthScale(0.0F);
                if (f < 1.0F) {
                    k2 = screenHeight - 20;
                    l2 = i + 91 + 6;
                    if (humanoidarm == HumanoidArm.RIGHT) {
                        l2 = i - 91 - 22;
                    }

                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
                    int i2 = (int) (f * 19.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(GUI_ICONS_LOCATION, l2, k2, 0, 94, 18, 18);
                    guiGraphics.blit(GUI_ICONS_LOCATION, l2, k2 + 18 - i2, 18, 112 - i2, 18, i2);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    public static void renderExperienceBar(GuiGraphics guiGraphics, int xPos) {
        mc.getProfiler().push("expBar");
        if (mc.player == null) return;
        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        int neededXp = mc.player.getXpNeededForNextLevel();
        int i;
        int yPos;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        PoseStack poseStack = guiGraphics.pose();

        switch (HUD.INSTANCE.hotBar_Value.getValue()) {
            case "Minecraft" -> {
                if (neededXp > 0) {
                    i = (int) (mc.player.experienceProgress * 183.0F);
                    yPos = screenHeight - 32 + 3;

                    guiGraphics.blit(GUI_ICONS_LOCATION, xPos, yPos, 0, 64, 182, 5);
                    if (i > 0) {
                        guiGraphics.blit(GUI_ICONS_LOCATION, xPos, yPos, 0, 69, i, 5);
                    }
                }

                mc.getProfiler().pop();
                if (mc.player.experienceLevel > 0) {
                    mc.getProfiler().push("expLevel");
                    String s = "" + mc.player.experienceLevel;
                    i = (screenWidth - mc.font.width(s)) / 2;
                    yPos = screenHeight - 31 - 4;
                    guiGraphics.drawString(mc.font, s, (i + 1), yPos, 0, false);
                    guiGraphics.drawString(mc.font, s, (i - 1), yPos, 0, false);
                    guiGraphics.drawString(mc.font, s, i, (yPos + 1), 0, false);
                    guiGraphics.drawString(mc.font, s, i, (yPos - 1), 0, false);
                    guiGraphics.drawString(mc.font, s, i, yPos, 8453920, false);
                    mc.getProfiler().pop();
                }
            }

            case "Loratadine", "Simple", "Modern Vertical" -> {
                if (mc.player.experienceLevel >= 0) {
                    mc.getProfiler().push("expLevel");
                    String level = String.valueOf(mc.player.experienceLevel);
                    i = (screenWidth - mc.font.width(level)) / 2;
                    yPos = screenHeight - 35;
                    guiGraphics.drawString(mc.font, level, i, yPos, 8453920, true);
                    mc.getProfiler().pop();
                }
            }
        }
    }

    private static Player getCameraPlayer() {
        return !(mc.getCameraEntity() instanceof Player) ? null : (Player) mc.getCameraEntity();
    }

    private static void renderSlot(GuiGraphics guiGraphics, int x, int y, float partialTicks, Player player, ItemStack itemStack, int i) {
        if (!itemStack.isEmpty()) {
            PoseStack posestack = RenderSystem.getModelViewStack();
            float f = (float) itemStack.getPopTime() - partialTicks;
            if (f > 0.0F) {
                float f1 = 1.0F + f / 5.0F;
                posestack.pushPose();
                posestack.translate(x + 8, y + 12, 0.0);
                posestack.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                posestack.translate(-(x + 8), -(y + 12), 0.0);
                RenderSystem.applyModelViewMatrix();
            }

            guiGraphics.renderItem(player, itemStack, x, y, i);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            if (f > 0.0F) {
                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            guiGraphics.renderItemDecorations(mc.font, itemStack, x, y);
        }
    }
}