package shop.xmz.lol.loratadine.ui.progressbar;

import com.mojang.blaze3d.vertex.PoseStack;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.impl.DecelerateAnimation;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;

import static shop.xmz.lol.loratadine.utils.render.RenderUtils.drawRoundedRect;
import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

/**
 * @author DSJ_ / Jon_awa
 * @since 2025/2/20
 * @description 我觉的我写的应该很高雅
 */
public class ProgressbarManager {

    /**
     * 绘制 Modern 的 Progressbar
     * @author DSJ_
     */
    public static void drawModernCountInfo(PoseStack poseStack, Animation animation, float progress, int y) {
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 68;

        float target = (120F * progress);
        float percentage = progress * 100F;
        int roundedPercentage = Math.round(percentage);

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + 70) * (1 - animation.getOutput()), (y + 25) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        drawRoundedRect(poseStack, x + 9, (float) (y + 6.5), 122, 7, 0, new Color(0, 0, 0, 150));

        // 绘制进度条
        drawRoundedRect(poseStack, x + 10, (float) (y + 7.5), Math.min(target, 120), 5, 0, HUD.INSTANCE.getColor(0));

        // 绘制文字
        Loratadine.INSTANCE.getFontManager().zw20.drawCenteredString(poseStack, roundedPercentage + "%", x + 71, y + 5F, new Color(225, 225, 225).getRGB());

        poseStack.popPose();
    }

    /**
     * 绘制 Simple 的 Progressbar
     * @author DSJ_
     */
    public static void drawSimpleCountInfo(PoseStack poseStack, String text, float progress, float ticks, int y) {
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 45;
        float target = 80 * progress;

        // 绘制背景
        RenderUtils.drawGradientRectL2R(poseStack, x, y, 90, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());
        RenderUtils.drawRectangle(poseStack, x, y, 90, 25, new Color(0, 0, 0, 160).getRGB());
        RenderUtils.drawRectangle(poseStack, x + 5, y + 15, 80, 7, new Color(0, 0, 0, 160).getRGB());

        // 绘制进度条
        RenderUtils.drawGradientRectL2R(poseStack, x + 5, y + 15, Math.min(target, 80), 7, HUD.INSTANCE.getColor(0).getRGB(), HUD.INSTANCE.getColor(4).getRGB());

        // 绘制文字
        Loratadine.INSTANCE.getFontManager().zw18.drawCenteredString(poseStack, text + ticks + "Tick", x + 45, y + 5, new Color(225, 225, 225, 160).getRGB());
    }

    /**
     * 绘制 Loratadine(Normal) 的 Progressbar
     * @author Jon_awa
     */
    public static void drawLoratadineCountInfo(PoseStack poseStack, String text, float progress, int y) {
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 50;
        float target = 100.0f * progress;

        // 绘制背景
        drawRoundedRect(poseStack, x, y, 100, 3, 1.5, new Color(20, 20, 20, 100));
        RenderUtils.drawGradientRoundedRect(poseStack, x, y, target, 3, 1.5, HUD.INSTANCE.getColor(1), HUD.INSTANCE.getColor(4));

        // 绘制文字
        Loratadine.INSTANCE.getFontManager().ax14.drawString(poseStack, text + "(" + Math.round(target) + "%)", x, y + 5, new Color(225, 225, 225, 160).getRGB());
    }

    /**
     * 绘制 Basic 的 Progressbar
     * @author DSJ_
     */
    public static void drawBasicCountInfo(PoseStack poseStack, float progress, int y) {
        Animation animation = new DecelerateAnimation(250, 1);
        float target = 100.0f * progress;
        String text = Math.round(target) + "%";
        int x = (int) (mc.getWindow().getGuiScaledWidth() / 2F - mc.font.width(text) / 2F);
        int color = Math.round(target) > 66 ? new Color(63, 157, 4, 150).getRGB() : (Math.round(target) < 33 ? new Color(168, 1, 1, 150).getRGB() : new Color(255, 144, 2, 150).getRGB());
        float output = (float) animation.getOutput();

        // 绘制文字
        RenderUtils.drawMcFontOutlinedString(poseStack, Math.round(target) + "%", x, y + 5, ColorUtils.applyOpacity(color, output),false);
    }
}