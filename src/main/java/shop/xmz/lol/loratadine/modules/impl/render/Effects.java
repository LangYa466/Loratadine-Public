package shop.xmz.lol.loratadine.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.setting.Theme;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.EaseBackIn;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Jon_awa
 * @since 2k25/2/14
 */
public class Effects extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"Loratadine", "Modern", "Simple"}, "Loratadine");
    private static final Map<MobEffect, Integer> potionMaxDurations = new HashMap<>();
    private static final Map<MobEffect, EaseBackIn> potionAnimation = new HashMap<>();

    public Effects() {
        super("Effects", "药水显示", Category.RENDER);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        switch (mode.getValue()) {
            case "Modern", "Loratadine" -> this.drawEffects(event.guiGraphics());
            case "Simple" -> this.drawOldEffects(event.guiGraphics());
        }
    }

    private void drawEffects(GuiGraphics guiGraphics) {
        if (mc.player == null || mc.level == null || guiGraphics == null) return;

        PoseStack poseStack = guiGraphics.pose();

        List<MobEffectInstance> effects = new ArrayList<>(mc.player.getActiveEffects());
        List<MobEffect> needRemove = new ArrayList<>();

        // Cleanup expired effects
        for (Map.Entry<MobEffect, Integer> entry : potionMaxDurations.entrySet()) {
            MobEffect effect = entry.getKey();
            if (mc.player.getEffect(effect) == null || Objects.requireNonNull(mc.player.getEffect(effect)).getDuration() <= 0) {
                needRemove.add(effect);
            }
        }

        for (Map.Entry<MobEffect, EaseBackIn> entry : potionAnimation.entrySet()) {
            MobEffect effect = entry.getKey();
            if (mc.player.getEffect(effect) == null || Objects.requireNonNull(mc.player.getEffect(effect)).getDuration() <= 0) {
                needRemove.add(effect);
            }
        }

        needRemove.forEach(potionMaxDurations::remove);
        needRemove.forEach(potionAnimation::remove);

        // Update max durations and animations
        for (MobEffectInstance instance : effects) {
            MobEffect effect = instance.getEffect();
            int currentDuration = instance.getDuration();

            potionMaxDurations.compute(effect, (key, oldDuration) -> (oldDuration == null || currentDuration > oldDuration) ? currentDuration : oldDuration);
            potionAnimation.compute(effect, (key, easeBackIn) -> (easeBackIn == null) ? new EaseBackIn(300, 1.0, 1.8f) : easeBackIn);
        }

        // Render Effects
        final TrueTypeFont zw18 = Loratadine.INSTANCE.getFontManager().zw18;
        final TrueTypeFont ten22 = Loratadine.INSTANCE.getFontManager().tenacityBold22;
        switch (mode.getValue()) {
            case "Loratadine" -> {
                int totalEffectsHeight = mc.player.getActiveEffects().size() * 35;
                int x = 5;
                int y = (mc.getWindow().getGuiScaledHeight() / 2) - (totalEffectsHeight / 2);
                int yOffset = 0;

                if (!effects.isEmpty()) {
                    for (MobEffectInstance effect : effects) {
                        String potionName = this.getName(effect, true);
                        String potionTime = MobEffectUtil.formatDuration(effect, 1.0F).getString();
                        int width = (int) (zw18.getStringWidth(this.getName(effect, true)) + ten22.getStringWidth("** : **") + 48);

                        if (effect.getDuration() < 12 && effect.getDuration() >= 0) {
                            potionAnimation.get(effect.getEffect()).setDirection(Direction.BACKWARDS);
                        } else {
                            potionAnimation.get(effect.getEffect()).setDirection(Direction.FORWARDS);
                        }

                        poseStack.pushPose();
                        poseStack.translate( (width + 5) * potionAnimation.get(effect.getEffect()).getOutput() - width - 5, 0, 0);
                        RenderUtils.drawRoundedRect(poseStack, x, y + yOffset, width, 34, 10, new Color(20, 20, 20, 50));
                        RenderUtils.drawRoundedRect(poseStack, x + 5, y + yOffset + 10, 5, 15, 2.5, new Color(effect.getEffect().getColor()));

                        {
                            poseStack.pushPose();
                            RenderUtils.startGlScissor(x,y, (int) (((float) effect.getDuration() / (float) potionMaxDurations.get(effect.getEffect())) * width), y + yOffset + 34);
                            RenderUtils.drawRoundedRect(poseStack, x, y + yOffset, width, 34, 10, new Color(20, 20, 20, 50));
                            RenderUtils.stopGlScissor();
                            poseStack.popPose();
                        }

                        TextureAtlasSprite texture = mc.getMobEffectTextures().get(effect.getEffect());
                        RenderSystem.setShaderTexture(0, texture.atlasLocation());
                        guiGraphics.blit(x + 13, y + yOffset + 8, 0, 18, 18, texture);

                        zw18.drawString(poseStack, "§l" + potionName, x + 34, y + yOffset + 14, new Color(effect.getEffect().getColor()).getRGB());
                        ten22.drawString(poseStack, potionTime, x + width - ten22.getStringWidth(potionTime) - 5, y + yOffset + 12, -1);
                        poseStack.popPose();
                        yOffset += 38;
                    }
                }
            }

            case "Modern" -> {
                float width = Math.max(90, effects.isEmpty() ? 0 : zw18.getStringWidth(this.getName(effects.get(effects.size() - 1), true) + 25));
                float height = effects.size() * 25F;

                int totalEffectsHeight = mc.player.getActiveEffects().size() * 24 + 20;
                int x = 5;
                int y = (mc.getWindow().getGuiScaledHeight() / 2) - (totalEffectsHeight / 2);

                if (!effects.isEmpty()) {
                    int bgColor = new Color(0, 0, 0, 80).getRGB();
                    int color = HUD.INSTANCE.getColor(1).getRGB();

                    RenderUtils.drawRightTrapezoid(poseStack, x, y, 14F, 17F, 8F, true, color);
                    RenderUtils.drawIsoscelesTrapezoid(poseStack, x + 14, y, 85F, 17F, 8F, bgColor);
                    RenderUtils.drawParallelogram(poseStack, x + 99F, y, 1.5F, 17F, -8F, color);
                    RenderUtils.drawRectangle(poseStack, x, y + 19f, width, height, bgColor);

                    Loratadine.INSTANCE.getFontManager().icon25.drawString(poseStack, "R", x + 3, y + 6, Color.WHITE.getRGB());
                    ten22.drawString(poseStack, "Effects", x + 39, y + 3, color);

                    for (MobEffectInstance effect : effects) {
                        TextureAtlasSprite sprite = mc.getMobEffectTextures().get(effect.getEffect());
                        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
                        guiGraphics.blit(6, y + 24, 0, 18, 18, sprite);

                        String potionName = this.getName(effect, true);
                        zw18.drawString(poseStack, potionName, 25, y + 26, -1);
                        RenderUtils.renderRoundedQuad(poseStack, new Color(0, 0, 0, 40), 25, y + 38, width - 5, y + 41, 1.5f, 16);
                        RenderUtils.drawGradientRoundedRect(poseStack, 25, y + 38, ((float) effect.getDuration() / (float) potionMaxDurations.get(effect.getEffect())) * (width - 30), 3, 1.5f, Theme.INSTANCE.firstColor, Theme.INSTANCE.secondColor);
                        y += 24;
                    }
                }
            }
        }
    }

    private void drawOldEffects(GuiGraphics guiGraphics) {
        if (mc.player == null || mc.level == null || guiGraphics == null) return;

        PoseStack poseStack = guiGraphics.pose();

        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int totalEffectsHeight = mc.player.getActiveEffects().size() * 10;

        int y = (screenHeight / 2) - (totalEffectsHeight / 2);

        for (MobEffectInstance effectInstance : mc.player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            String name = effect.getDisplayName().getString();
            int level = effectInstance.getAmplifier() + 1;
            int duration = effectInstance.getDuration();

            TextureAtlasSprite sprite = mc.getMobEffectTextures().get(effect);
            RenderSystem.setShaderTexture(0, sprite.atlasLocation());
            guiGraphics.blit(6, y + 11, 0, 18, 18, sprite);

            guiGraphics.fill(5, y, 5 + 140, y + 1, new Color(255, 255, 255, 150).getRGB());
            guiGraphics.fill(5, y + 40 - 1, 5 + 140, y + 40, new Color(255, 255, 255, 150).getRGB());
            guiGraphics.fill(5, y, 5 + 1, y + 40, new Color(255, 255, 255, 150).getRGB());
            guiGraphics.fill(5 + 140 - 1, y, 5 + 140, y + 40, new Color(255, 255, 255, 150).getRGB());

            Loratadine.INSTANCE.getFontManager().zw18.drawStringWithShadow(poseStack, name + " " + this.intToRoman(level), 30, y + 6, Color.WHITE.getRGB());
            Loratadine.INSTANCE.getFontManager().zw18.drawStringWithShadow(poseStack, this.formatDuration(duration), 30, y + 18, Color.WHITE.getRGB());
            int progressBarWidth = 100;
            int progressBarHeight = 7;
            float progress = Math.max(0, Math.min((float) duration / 6000, 1.0f));
            int progressWidth = (int) (progressBarWidth * progress);
            int bgColor = new Color(50, 50, 50, 180).getRGB();
            int fgColor = this.blendColors(new Color(0, 255, 0).getRGB(), new Color(255, 0, 0).getRGB(), 1 - progress).getRGB();

            guiGraphics.fill(30, y + 30, 30 + progressBarWidth, y + 30 + progressBarHeight, bgColor);
            guiGraphics.fill(30, y + 30, 30 + progressWidth, y + 30 + progressBarHeight, fgColor);

            y += 40;
        }
    }

    private Color blendColors(int color1, int color2, float ratio) {
        int red = (int) ((color1 >> 16 & 0xFF) * (1 - ratio) + (color2 >> 16 & 0xFF) * ratio);
        int green = (int) ((color1 >> 8 & 0xFF) * (1 - ratio) + (color2 >> 8 & 0xFF) * ratio);
        int blue = (int) ((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
        return new Color(red, green, blue);
    }

    private String intToRoman(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length && num > 0; i++) {
            while (values[i] <= num) {
                num -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }

    private String formatDuration(int duration) {
        int seconds = duration / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String getName(MobEffectInstance potionEffect, boolean showStrength) {
        return potionEffect.getEffect().getDisplayName().getString() + (showStrength ? "  " + this.intToRoman(potionEffect.getAmplifier() + 1) : "");
    }
}
