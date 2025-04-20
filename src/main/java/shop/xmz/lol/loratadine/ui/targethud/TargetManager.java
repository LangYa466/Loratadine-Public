package shop.xmz.lol.loratadine.ui.targethud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.hud.TargetHUD;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.CharUtils;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * @author Jon_awa / DSJ_
 * @since 13/2/2025
 */

public class TargetManager implements Wrapper {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static LivingEntity lastTarget;
    private static float easingHealth = 0f;
    private static float currentWidth = 0;
    private static int alpha = 0;

    /**
     * 绘制 Loratadine(Normal) 的 TargetInfo
     * @author Jon_awa
     */
    public static void drawLoratadineTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int xAddition, int yAddition) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 坐标
        float x = mc.getWindow().getGuiScaledWidth() / 2.0f + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2.0f + yAddition;

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        final Color color = healthPresent > 0.5 ? new Color(63, 157, 4, 150) : (healthPresent > 0.25 ? new Color(255, 144, 2, 150) : new Color(168, 1, 1, 150));

        // 受伤间隔
        int offset = target != null ? -target.hurtTime * 23 : 0;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        int width = target != null ? (int) Math.max(140, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : 140;
        float barWidth = width - 55;
        float presentWidth = Math.min(healthPresent, 1) * barWidth;

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        RenderUtils.drawRoundedRect(poseStack, x, y, width, 40, 12, new Color(0, 0, 0, 160));

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 7, y + 7, 26, 26, player);
            } else {
                RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 28, 28, 6, Color.BLACK);
            }
        } catch (Exception e) {
            RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 28, 28, 6, Color.BLACK);
        }

        // 绘制文字
        fontManager.zw22.drawString(poseStack, name, x + 40, y + 5, new Color(200, 200, 200, 255).getRGB());
        fontManager.ax18.drawString(poseStack, String.valueOf(health), x + 40, y + 18, new Color(200, 200, 200, 255).getRGB());
        fontManager.icon30.drawString(poseStack, "P", x + fontManager.ax20.getStringWidth(String.valueOf(health)) + 42, y + 17, ColorUtils.getColor(255, 255 + offset, 255 + offset));

        // 绘制血条
        RenderUtils.drawRoundedRect(poseStack, x + 40, y + 30, barWidth, 4, 2, new Color(0, 0, 0, 30));
        RenderUtils.drawRoundedRect(poseStack, x + 40, y + 30, presentWidth, 4, 2, color);

        // 结束绘制
        poseStack.popPose();
    }

    /**
     * 绘制 Modern 的 TargetInfo
     * @author Jon_awa
     */
    public static void drawModernTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int xAddition, int yAddition) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 坐标
        float x = mc.getWindow().getGuiScaledWidth() / 2.0f + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2.0f + yAddition;

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        float width = fontManager.zw22.getStringWidth(name) + 75;
        float presentWidth = Math.min(healthPresent, 1) * width;

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        RenderUtils.drawRectangle(poseStack, x, y, width, 40, new Color(0, 0, 0, 100).getRGB());
        RenderUtils.drawRectangle(poseStack, x, y, presentWidth, 40, new Color(230, 230, 230, 100).getRGB());
        RenderUtils.drawRectangle(poseStack, x, y + 12.5f, 3, 15,healthPresent > 0.5 ? new Color(63, 157, 4, 150).getRGB() : (healthPresent > 0.25 ? new Color(255, 144, 2, 150).getRGB() : new Color(168, 1, 1, 150).getRGB()));

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 7, y + 7, 26, 26, player);
            } else {
                RenderUtils.drawRectangle(poseStack, x + 6, y + 6, 28, 28, Color.BLACK.getRGB());
            }
        } catch (Exception e) {
            RenderUtils.drawRectangle(poseStack, x + 6, y + 6, 28, 28, Color.BLACK.getRGB());
        }

        // 绘制文字
        fontManager.zw22.drawString(poseStack, name, x + 40, y + 7, new Color(200, 200, 200, 255).getRGB());
        fontManager.ax18.drawString(poseStack, health + " HP", x + 40, y + 22, new Color(200, 200, 200, 255).getRGB());

        // 绘制血条
        if (target != null && !target.getMainHandItem().isEmpty()) {
            RenderUtils.renderItemIcon(poseStack, x + fontManager.zw22.getStringWidth(name) + 50, y + 12, target.getMainHandItem());
        } else {
            fontManager.zw30.drawString(poseStack, "?", x + fontManager.zw22.getStringWidth(name) + 55, y + 11, new Color(200, 200, 200, 255).getRGB());
        }

        // 结束绘制
        poseStack.popPose();
    }

    /**
     * 绘制 LSD 的 TargetInfo
     * @author DSJ
     */
    public static void drawLSDTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int xAddition, int yAddition) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 坐标
        float x = mc.getWindow().getGuiScaledWidth() / 2.0f + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2.0f + yAddition;

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        // 盔甲
        int armor = target != null ? (int) target.getArmorValue() : 0;
        float armorPresent = target != null ? (float) armor / 20 : 0;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        int width = target != null ? (int) Math.max(140, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : 140;
        float barWidth = width - 60;
        float presentWidth_health = Math.min(healthPresent, 1) * barWidth;
        float presentWidth_armor = Math.min(armorPresent, 1) * barWidth;

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制背景
        RenderUtils.drawRoundedRect(poseStack, x, y, width, 40, 2,new Color(31, 30, 29));

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 7, y + 7, 26, 26, player);
            } else {
                RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 28, 28, 6, Color.BLACK);
            }
        } catch (Exception e) {
            RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 28, 28, 6, Color.BLACK);
        }

        // 绘制Target Name
        fontManager.zw22.drawString(poseStack, name, x + 41, y + 4, new Color(200, 200, 200, 255).getRGB());

        // 绘制血量图标
        fontManager.icon18.drawString(poseStack, "P", x + 41, y + 17,HUD.INSTANCE.getColor(1).getRGB());

        // 绘制盔甲图标
        fontManager.icon18.drawString(poseStack, "E", x + 41, y + 28.5F, Color.WHITE.getRGB());

        // 绘制血条
        RenderUtils.drawRectangle(poseStack, x + 50, y + 18.5F, barWidth, 3,  new Color(70,70,70).getRGB());
        RenderUtils.drawGradientRectL2R(poseStack, x + 50, y + 18.5F, presentWidth_health, 3, HUD.INSTANCE.getColor(1).getRGB(),HUD.INSTANCE.getColor(4).getRGB());

        // 绘制盔甲
        RenderUtils.drawRectangle(poseStack, x + 50, y + 30, barWidth, 3,  new Color(70,70,70).getRGB());
        RenderUtils.drawRectangle(poseStack, x + 50, y + 30, presentWidth_armor, 3, Color.WHITE.getRGB());

        // 结束绘制
        poseStack.popPose();
    }

    /**
     * 绘制 Exhibition 的 TargetInfo
     * @author DSJ_
     */
    public static void drawExhibitionTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int xAddition, int yAddition) {
        float x = mc.getWindow().getGuiScaledWidth() / 2F + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2F + yAddition;

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 各种颜色
        Color darkest = new Color(0, 0, 0);
        Color lineColor = new Color(104, 104, 104);
        Color dark = new Color(70, 70, 70);

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + 70) * (1 - animation.getOutput()), (y + 25) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制边框
        RenderUtils.drawRectangle(poseStack, x, y, 140, 50, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 0.5F, y + 0.5F, 139, 49, lineColor.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 1.5F, y + 1.5F, 137, 47, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 2, y + 2, 136, 46, dark.getRGB());

        // 绘制名字
        String targetName = target != null ? target.getName().getString() : "Player";
        WrapperUtils.draw(poseStack, targetName, x + 40, y + 6, Color.WHITE.getRGB());

        // 获取计分板血量
        int healthScore = target != null ? (int) target.getHealth() : 0;

        // 绘制详细文本
        String name = "HP: " + healthScore + " | Dist: " + (target != null ? Math.round(RotationUtils.getDistanceToEntity(target)) : 0);
        poseStack.pushPose();
        poseStack.scale(0.7F, 0.7F, 0.7F);
        WrapperUtils.draw(poseStack, name, (x + 40F) * (1F / 0.7F), (y + 17F) * (1F / 0.7F), Color.WHITE.getRGB());
        poseStack.popPose();

        // 绘制血条
        double health = Math.min(healthScore, target != null ? target.getMaxHealth() : 20);
        int healthColor = target != null ? getColor(target).getRGB() : new Color(120, 0, 0).getRGB();

        // 绘制整个血条
        float x2 = x + 40F;
        RenderUtils.drawRectangle(poseStack, x2, y + 25, (float) ((100 - 9) * (health / (target != null ? target.getMaxHealth() : 20))), 6, healthColor);
        RenderUtils.drawRectangle(poseStack, x2, y + 25, 91, 1, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x2, y + 30, 91, 1, darkest.getRGB());

        // 绘制血条中间的线
        for (int i = 0; i < 10; i++) {
            RenderUtils.drawRectangle(poseStack, x2 + 10 * i, y + 25, 1, 6, darkest.getRGB());
        }

        // 绘制手持物品
        if (target != null) {
            RenderUtils.renderItemIcon(poseStack, x2, y + 31, target.getMainHandItem());
            RenderUtils.renderItemIcon(poseStack, x2 + 15, y + 31, target.getItemBySlot(EquipmentSlot.HEAD));
            RenderUtils.renderItemIcon(poseStack, x2 + 30, y + 31, target.getItemBySlot(EquipmentSlot.CHEST));
            RenderUtils.renderItemIcon(poseStack, x2 + 45, y + 31, target.getItemBySlot(EquipmentSlot.LEGS));
            RenderUtils.renderItemIcon(poseStack, x2 + 60, y + 31, target.getItemBySlot(EquipmentSlot.FEET));
        }

        // 绘制模型
        if (target != null) {
            poseStack.pushPose();
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.translate((x + 20) * (1 / 0.4), (y + 44) * (1 / 0.4), 40f * (1 / 0.4));
            RenderUtils.drawModel(poseStack, target.getYRot(), target.getXRot(), target);
            poseStack.popPose();
        }

        // 结束绘制
        poseStack.popPose();
    }

    //最好的血条颜色
    private static Color getColor(LivingEntity target) {
        Color healthColor = new Color(0, 165, 0);
        if (target.getHealth() < target.getMaxHealth() / 1.5)
            healthColor = new Color(200, 200, 0);
        if (target.getHealth() < target.getMaxHealth() / 2.5)
            healthColor = new Color(200, 155, 0);
        if (target.getHealth() < target.getMaxHealth() / 4)
            healthColor = new Color(120, 0, 0);
        return healthColor;
    }

    /**
     *  绘制 ModernRemix 的 TargetInfo
     *   @author DSJ_
     */
    public static void drawModernRemixTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int xAddition, int yAddition) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        float x = mc.getWindow().getGuiScaledWidth() / 2F + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2F + yAddition;

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;
        float healthPresent = target != null ? health / target.getMaxHealth() : 0;

        // 盔甲
        int armor = target != null ? target.getArmorValue() : 0;
        float armorPresent = target != null ? (float) armor / 20 : 0;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 宽度
        int healthWidth = target != null ? (int) Math.max(130, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : 130;
        int armorWidth = target != null ? (int) Math.max(95.5F, fontManager.zw22.getStringWidth(target.getName().getString()) + 50) : (int) 95.5F;
        float presentWidth_health = Math.min(healthPresent, 1) * healthWidth;
        float presentWidth_armor = Math.min(armorPresent, 1) * armorWidth;

        //各种颜色
        Color bgColor = new Color(30, 30, 30);

        Color healthBgColor = new Color(80, 0, 0);
        Color healthColor = new Color(0, 165, 0);

        Color armorBgColor = new Color(0, 0, 80);
        Color armorColor = new Color(0, 0, 165);


        Color rectBgColor = new Color(70, 70, 70);
        Color rectOutlineColor = new Color(0, 0, 0);

        Color textColor = new Color(200, 200, 200);

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + 70) * (1 - animation.getOutput()), (y + 25) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 绘制边框
        RenderUtils.drawRoundedRect(poseStack, x, y, 140, 50,0, bgColor);

        // 绘制头像
        try {
            if (target != null) {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 5, y + 5, 32, 32, player);
            } else {
                RenderUtils.drawRoundedRect(poseStack, x + 5, y + 5, 32, 32, 0, rectBgColor);
            }
        } catch (Exception e) {
            RenderUtils.drawRoundedRect(poseStack, x + 5, y + 5, 32, 32, 0, rectBgColor);
        }

        // 绘制血条
        RenderUtils.drawRoundedRect(poseStack, x + 5, y + 40, 130, 5,0, healthBgColor);
        RenderUtils.drawRoundedRect(poseStack, x + 5, y + 40, presentWidth_health, 5,0, healthColor);

        // 绘制盔甲条
        RenderUtils.drawRoundedRect(poseStack, x + 39, y + 35.5F, 95.5F, 1,0, armorBgColor);
        RenderUtils.drawRoundedRect(poseStack, x + 39, y + 35.5F, presentWidth_armor, 1,0, armorColor);

        // 绘制盔甲框
        RenderUtils.drawRoundedRect(poseStack, x + 39, y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, x + 40, y + 17, 16, 16,0, rectBgColor);

        RenderUtils.drawRoundedRect(poseStack, x + 39 + 20, y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, x + 40 + 20, y + 17, 16, 16,0, rectBgColor);

        RenderUtils.drawRoundedRect(poseStack, x + 39 + 40, y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, x + 40 + 40, y + 17, 16, 16,0, rectBgColor);

        RenderUtils.drawRoundedRect(poseStack, x + 39 + 60, y + 16, 18, 18,0, rectOutlineColor);
        RenderUtils.drawRoundedRect(poseStack, x + 40 + 60, y + 17, 16, 16,0, rectBgColor);

        if (target != null) {
            RenderUtils.renderItemIcon(poseStack, x + 40, y + 18, target.getItemBySlot(EquipmentSlot.HEAD));
            RenderUtils.renderItemIcon(poseStack, x + 60, y + 18, target.getItemBySlot(EquipmentSlot.CHEST));
            RenderUtils.renderItemIcon(poseStack, x + 80, y + 18, target.getItemBySlot(EquipmentSlot.LEGS));
            RenderUtils.renderItemIcon(poseStack, x + 100, y + 18, target.getItemBySlot(EquipmentSlot.FEET));
        }

        // 绘制名字
        fontManager.zw22.drawString(poseStack, name, x + 40, y + 4, textColor.getRGB(),true);

        // 绘制Ping数值
        if (target instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) target;
            PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
            if (info != null) {
                int ping = info.getLatency();

                // 绘制Ping数值（带颜色）
                String pingText = ping + "ms";
                int color = textColor.getRGB();
                fontManager.zw12.drawString(poseStack, pingText, x + 135 - fontManager.zw12.getStringWidth(pingText), y + 28, color,true);
                fontManager.icon18.drawString(poseStack, "y", x + 128, y + 22, getPingColor(ping),true);
            }
        }
    }

    private static int getPingColor(int ping) {
        if (ping < 100) return new Color(0, 165, 0).getRGB();  // 亮绿
        if (ping < 200) return new Color(165, 165, 0).getRGB();  // 黄色
        if (ping < 300) return new Color(165, 80, 0).getRGB();  // 橙色
        if (ping < 400) return new Color(165, 0, 0).getRGB();  // 红色
        return new Color(80, 0, 0).getRGB();                  // 深红
    }

    /**
     *   绘制 Simple 的 TargetInfo
     *   @author DSJ_
     */
    public static void drawSimpleTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, int xAddition, int yAddition) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

        float x = mc.getWindow().getGuiScaledWidth() / 2F + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2F + yAddition;

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 各种颜色
        Color bgColor = new Color(0, 0, 0, 160);

        // 血量
        int health = target != null ? (int) target.getHealth() : 0;

        // 盔甲
        int armor = target != null ? target.getArmorValue() : 0;

        // 受伤间隔
        int offset = target != null ? -target.hurtTime * 23 : 0;

        // 目标宽度
        int targetWidth = (target != null && KillAura.target != null)
                ? (int) (mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + mc.font.width(String.valueOf(health)) + fontManager.icon18.getStringWidth("E") + mc.font.width(String.valueOf(armor)) + 10)
                : mc.font.width("TargetInfo") + 4;

        int height = mc.font.lineHeight + 4;

        // 计算时间差
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // 平滑过渡宽度
        float widthSpeed = 0.5f; // 宽度变化速度
        if (currentWidth < targetWidth) {
            currentWidth = Math.min(currentWidth + widthSpeed * deltaTime, targetWidth);
        } else if (currentWidth > targetWidth) {
            currentWidth = Math.max(currentWidth - widthSpeed * deltaTime, targetWidth);
        }

        // 平滑过渡透明度
        int alphaSpeed = 2; // 透明度变化速度
        if (target != null && KillAura.target != null) {
            alpha = Math.min(alpha + alphaSpeed, 255); // 渐显
        } else {
            alpha = Math.max(alpha - alphaSpeed, 0); // 渐隐
        }

        // 绘制背景
        RenderUtils.drawRoundedRect(poseStack, x, y, (int) currentWidth, height, 0, bgColor);
        RenderUtils.drawGradientRectU2D(poseStack, x, y, 1, height, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());

        // 绘制文字
        Color textColor = new Color(200, 200, 200, alpha);
        Color narmalTextColor = new Color(200, 200, 200, 255);
        //mc.font.drawShadow(poseStack, "TargetInfo", x + 2, y + 2.5F, narmalTextColor.getRGB());

        // 绘制头像
        if (target != null && KillAura.target != null || mc.screen instanceof ChatScreen) {
            AbstractClientPlayer player = (AbstractClientPlayer) target;
            RenderUtils.drawPlayerHead(poseStack, x - 20, y - 2, 18, 18, player);

            // 绘制名字
            String name = target.getName().getString();
           // mc.font.drawShadow(poseStack, name, x + 2, y + 15, new Color(200, 0, 0, alpha).getRGB());

            // 绘制血量
            fontManager.icon18.drawString(poseStack, "P",
                    x + mc.font.width("TargetInfo") + 4,
                    y + 4,
                    ColorUtils.getColor(255, 255 + offset, 255 + offset, alpha));

           // mc.font.drawShadow(poseStack, String.valueOf(health),
                   // x + mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + 6,
                  //  y + 2.5F,
                 //   textColor.getRGB());

            // 绘制盔甲
            fontManager.icon18.drawString(poseStack, "E",
                    x + mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + mc.font.width(String.valueOf(health)) + 7,
                    y + 4,
                    textColor.getRGB());

           // mc.font.drawShadow(poseStack, String.valueOf(armor),
                  //  x + mc.font.width("TargetInfo") + fontManager.icon18.getStringWidth("P") + mc.font.width(String.valueOf(health)) + fontManager.icon18.getStringWidth("E") + 8,
                  //  y + 2.5F,
                   // textColor.getRGB());
        }
    }

    /**
     * 绘制 Chill 的 TargetInfo
     * @author DSJ_
     */
    public static void drawChillTargetInfo(PoseStack poseStack, @Nullable LivingEntity target, Animation animation, int xAddition, int yAddition) {
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();
        final CharUtils numberRenderer = new CharUtils();

        if (target != null) {
            lastTarget = target;
        }

        if (!TargetHUD.targetTimer.hasTimeElapsed(50000) && lastTarget != null && target == null) {
            target = lastTarget;
        }

        // 坐标
        float x = mc.getWindow().getGuiScaledWidth() / 2.0f + xAddition;
        float y = mc.getWindow().getGuiScaledHeight() / 2.0f + yAddition;

        // 名字
        final String name = target != null ? target.getName().getString() : "Player";

        // 血量
        float health = target != null ? target.getHealth() : 0;
        float maxHealth = target != null ? target.getMaxHealth() : 20;

        // 更新缓动血量动画
        updateEasingHealth(health);

        // 计算宽度
        float tWidth = Math.max(45F + Math.max(fontManager.zw22.getStringWidth(name),
                fontManager.zw22.getStringWidth(String.format("%.2f", health))), 120F);

        // 动画
        poseStack.pushPose();
        poseStack.translate((x + (double) tWidth / 2) * (1 - animation.getOutput()), (y + 20) * (1 - animation.getOutput()), 0.0);
        poseStack.scale((float) animation.getOutput(), (float) animation.getOutput(), 0);

        // 背景
        RenderUtils.drawGradientRectL2R(poseStack, x, y - 1, tWidth, 1, HUD.INSTANCE.getColor(1).getRGB(), HUD.INSTANCE.getColor(4).getRGB());
        RenderUtils.drawRoundedRect(poseStack, x, y, tWidth, 48F, 0, new Color(23, 23, 23));

        // 头像
        if (target != null) {
            try {
                AbstractClientPlayer player = (AbstractClientPlayer) target;
                RenderUtils.drawPlayerHead(poseStack, x + 4, y + 4, 30, 30, player);
            } catch (Exception e) {
                RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 30, 30, 0, Color.BLACK);
            }
        } else {
            RenderUtils.drawRoundedRect(poseStack, x + 6, y + 6, 30, 30, 0, Color.BLACK);
        }

        // 名字和血量
        fontManager.zw22.drawString(poseStack, name, x + 38F, y + 5F, new Color(200, 200, 200, 255).getRGB());

        // 使用CharRenderer渲染血量数字
        float calcTranslateX = 0f; // 这里需要替换为实际的计算值
        float calcTranslateY = 5f; // 这里需要替换为实际的计算值
        float calcScaleX = 1f;     // 默认缩放为1
        float calcScaleY = 1f;     // 默认缩放为1

        numberRenderer.renderChar(
                poseStack,
                easingHealth, // 使用缓动的血量值
                calcTranslateX,
                calcTranslateY,
                x + 38F,
                y + 17F,
                calcScaleX,
                calcScaleY,
                false,
                0.5F, // 字体速度
                new Color(200, 200, 200, 255).getRGB()
        );

        // 绘制血条背景
        RenderUtils.drawRoundedRect(poseStack, x + 4, y + 38, tWidth - 8, 6, 0, new Color(0, 0, 0, 100));

        Color barColor;
        float healthPercent = easingHealth / maxHealth; // 使用缓动的血量值计算百分比

        if (healthPercent > 0.66f) {
            // 血量高 - 绿色
            barColor = new Color(30, 220, 30);
        } else if (healthPercent > 0.33f) {
            // 血量中等 - 黄色
            barColor = new Color(220, 220, 30);
        } else {
            // 血量低 - 红色
            barColor = new Color(220, 30, 30);
        }

        // 获取HUD颜色或使用上面计算的颜色
        Color finalBarColor = barColor;

        // 绘制血条
        RenderUtils.drawRoundedRect(
                poseStack,
                x + 4,
                y + 38,
                (easingHealth / maxHealth) * (tWidth - 8), // 使用缓动的血量值
                6,
                0,
                finalBarColor
        );

        // 结束绘制
        poseStack.popPose();
    }

    /**
     * 更新缓动动画血量值
     */
    private static void updateEasingHealth(float targetHealth) {
        // 计算时间差
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // 根据血量变化的幅度动态调整速度
        float changeAmount = Math.abs(easingHealth - targetHealth);
        float baseSpeed = 0.02f; // 基础速度
        float speed = baseSpeed * deltaTime; // 根据时间差缩放速度

        // 如果变化较大，加快动画速度
        if (changeAmount > 5) {
            speed *= 2.0f;
        } else if (changeAmount > 2) {
            speed *= 1.5f;
        }

        // 接近目标值时使用更精确的插值
        if (Math.abs(easingHealth - targetHealth) < 0.1) {
            easingHealth = targetHealth;
        } else if (easingHealth > targetHealth) {
            // 血量减少 - 更快速度
            easingHealth -= Math.min(speed * 1.2f, easingHealth - targetHealth);
        } else {
            // 血量增加
            easingHealth += Math.min(speed, targetHealth - easingHealth);
        }
    }
}
