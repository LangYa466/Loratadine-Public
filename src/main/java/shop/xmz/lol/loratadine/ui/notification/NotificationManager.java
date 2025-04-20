package shop.xmz.lol.loratadine.ui.notification;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.StopWatch;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.hud.NotificationHUD;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.DecelerateAnimation;
import shop.xmz.lol.loratadine.utils.animations.impl.EaseBackIn;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.GlowUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Jon_awa / DSJ_
 * @since 2025/2/16
 */
public class NotificationManager implements Wrapper {
    private final NotificationType notificationType;
    private final String title;
    private final String description;
    private final float time;
    private final StopWatch timer_;
    private final TimerUtils timer;
    private final Animation animation;
    private final Animation animation2;
    private static float toggleTime = 2.0f;
    private static final CopyOnWriteArrayList<NotificationManager> notifications = new CopyOnWriteArrayList<>();
    private static final FontManager fontManager = Loratadine.INSTANCE.getFontManager();

    public NotificationManager(NotificationType notificationType, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000.0f);
        this.timer = new TimerUtils();
        this.timer_ = new StopWatch();
        this.notificationType = notificationType;
        this.animation = new DecelerateAnimation(300, 1.0);
        this.animation2 = new EaseBackIn(500, 1.0, 1.3f);
    }

    public static void drawTopNotifications(PoseStack poseStack) {
        final String defaultText = Loratadine.CLIENT_NAME + " | " + mc.player.getName().getString() + " | " + (Objects.requireNonNull(mc.getCurrentServer()).ip);
        final TrueTypeFont textFont = fontManager.zw18;
        final TrueTypeFont titleFont = fontManager.zw20;
        float defaultWidth = textFont.getStringWidth(defaultText) + 32;
        float defaultHeight = textFont.getHeight() + 20;
        float height = 0, width = 0;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        setToggleTime(3.0f);

        for (NotificationManager notification : notifications) {
            Animation animation = notification.animation;
            animation.setDirection(notification.timer_.hasTimePassed((long) notification.time) ? Direction.BACKWARDS : Direction.FORWARDS);
            float output = (float) animation.getOutput();

            if (animation.finished(Direction.BACKWARDS)) {
                notifications.remove(notification);
                continue;
            }

            animation.setDuration(400);

            float deltaWidth = Math.max(textFont.getStringWidth(notification.description), titleFont.getStringWidth(notification.title)) + 50.0f;
            if (deltaWidth > width) {
                width += (deltaWidth - width) * output;
            }

            if (height > 7) {
                height += 28.0f * output;
            } else {
                height += 35.0f * output;
            }
        }

        if (notifications.isEmpty()) {
            height = defaultHeight;
            width = width - defaultWidth;
        }

        if (width < defaultWidth) {
            width = defaultWidth;
        }

        if (height < defaultHeight) {
            height = defaultHeight;
        }

        float x = (screenWidth - width) / 2.0f;
        float finalWidth = width;
        float finalHeight = height;

        RenderUtils.drawRoundedRect(poseStack,x, 10.0f, finalWidth, finalHeight, 14, new Color(0, 0, 0, 110));

        int color = Color.WHITE.getRGB();
        float iconY = height - 10f;
        float titleY = height - 14f;
        float textY = height - 3f;
        if (!notifications.isEmpty()) {
            for (NotificationManager notification : notifications) {
                Animation animation = notification.animation;
                if ((iconY > 10 && titleY > 10 && textY > 10) || animation.isState()) {
                    if (animation.isState()) {
                        float finalIconY = iconY;
                        float finalTitleY = titleY;
                        float finalTextY = textY;

                        // 初始绘制
                        poseStack.pushPose();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();

                        // GL裁剪王开始工作了
                        RenderUtils.startGlScissor(0, 10, screenWidth, screenHeight);

                        // 绘制图标
                        Color iconcolor = ColorUtils.applyOpacity(notification.notificationType.getColor(), 70.0f);
                        fontManager.icon30.drawString(poseStack, notification.notificationType.getIcon(), x + 12, finalIconY, iconcolor.getRGB());

                        // 绘制标题
                        titleFont.drawString(poseStack, notification.title, x + 35.0f, finalTitleY, color);
                        textFont.drawString(poseStack, notification.description, x + 35.0f, finalTextY, color);

                        // GL裁剪王结束工作了
                        RenderUtils.stopGlScissor();
                        poseStack.popPose();


                        iconY -= 28.0f;
                        titleY -= 28.0f;
                        textY -= 28.0f;
                    } else if (notifications.size() < 2) {
                        drawDefault(poseStack, x);
                    }
                } else if (notifications.size() < 2) {
                    drawDefault(poseStack,  x);
                }
            }
        } else {
            drawDefault(poseStack, x);
        }
    }

    private static void drawDefault(PoseStack poseStack,  float x) {
        final TrueTypeFont clientNameFont = fontManager.tenacityBold22;
        final TrueTypeFont font__ = fontManager.tenacityBold15;
        final TrueTypeFont font_ = fontManager.tenacityBold14;
        final TrueTypeFont I = fontManager.zw30;
        final int color = HUD.INSTANCE.getColor(1).getRGB();


        RenderUtils.drawImage(poseStack,Loratadine.INSTANCE.CLIENT_ICON_PNG, (int) (x + 6.0f), (int) 17f, 17, 17, 0,0,17,17);

        clientNameFont.drawString(poseStack,Loratadine.CLIENT_NAME, x +
                        26.0f
                , 19.5f, color);

        I.drawString(poseStack," §7|§r ", x +
                        23.0f +
                        clientNameFont.getStringWidth(Loratadine.CLIENT_NAME)
                , 15.0f, Color.WHITE.getRGB());

        font__.drawString(poseStack, mc.player.getName().getString(), x +
                        20.0f +
                        clientNameFont.getStringWidth(Loratadine.CLIENT_NAME) +
                        I.getStringWidth(" §7|§r ")
                , 17.0f, Color.WHITE.getRGB());

        font_.drawString(poseStack,"§7v" + Loratadine.CLIENT_VERSION, x +
                        20.0f +
                        clientNameFont.getStringWidth(Loratadine.CLIENT_NAME)+
                        I.getStringWidth(" §7|§r ")
                , 26, Color.WHITE.getRGB());

        I.drawString(poseStack," §7|§r ", x +
                        17 +
                        I.getStringWidth(" §7|§r ") +
                        clientNameFont.getStringWidth(Loratadine.CLIENT_NAME) +
                        font__.getStringWidth(mc.player.getName().getString())
                , 15.0f, Color.WHITE.getRGB());

        font__.drawString(poseStack,(Objects.requireNonNull(mc.getCurrentServer()).ip), x +
                        14 +
                        font__.getStringWidth(mc.player.getName().getString()) +
                        I.getStringWidth(" §7|§r ") +
                        I.getStringWidth(" §7|§r ") +
                        clientNameFont.getStringWidth(Loratadine.CLIENT_NAME)
                , 17.0f, Color.WHITE.getRGB());

        font_.drawString(poseStack,"§7" +  WrapperUtils.getFPS() + "fps", x +
                        14 +
                        font__.getStringWidth(mc.player.getName().getString()) +
                        I.getStringWidth(" §7|§r ") +
                        I.getStringWidth(" §7|§r ") +
                        clientNameFont.getStringWidth(Loratadine.CLIENT_NAME)
                , 26, Color.WHITE.getRGB());

    }

    public static void drawNotifications(PoseStack poseStack) {
        float yOffset = 0;
        float notificationHeight = 0;
        float notificationWidth = 0;
        int actualOffset;
        final Window window = mc.getWindow();

        setToggleTime(2f);

        for (NotificationManager noti : notifications) {
            boolean isEnglish = HUD.INSTANCE.languageValue.is("English");
            Animation animation = noti.animation;
            Animation animation2 = noti.animation2;
            animation.setDirection(noti.timer.hasTimeElapsed((long) noti.time) ? Direction.BACKWARDS : Direction.FORWARDS);
            animation2.setDirection(noti.timer.hasTimeElapsed((long) noti.time) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS) || animation2.finished(Direction.BACKWARDS)) {
                notifications.remove(noti);
                continue;
            }

            float x, y;
            animation.setDuration(200);
            animation2.setDuration(200);
            actualOffset = Objects.equals(NotificationHUD.notiStyle, "White") ? 0 : 3;
            if (isEnglish) {
                notificationWidth = switch (NotificationHUD.notiStyle) {
                    case "Loratadine" -> {
                        notificationHeight = 31;
                        yield Math.max(100, Math.max(fontManager.tenacityBold20.getStringWidth(noti.description), fontManager.ax18.getStringWidth(noti.title)) + 38);
                    }

                    case "Exhibition" -> {
                        notificationHeight = 27;
                        yield Math.max(100, Math.max(fontManager.ax18.getStringWidth(noti.title) + 4, fontManager.ax14.getStringWidth(noti.description) + 26));
                    }

                    case "LSD" -> {
                        notificationHeight = 27.0F;
                        yield Math.max(100, Math.max(fontManager.ax18.getStringWidth(noti.title) + 4, fontManager.ax14.getStringWidth(noti.description) + 26));
                    }

                    case "Xylitol" -> {
                        notificationHeight = 23;
                        yield fontManager.ax18.getStringWidth(noti.description) + 25;
                    }

                    default -> notificationWidth;
                };
            } else {
                notificationWidth = switch (NotificationHUD.notiStyle) {
                    case "Loratadine" -> {
                        notificationHeight = 31;
                        yield Math.max(100, Math.max(fontManager.zw20.getStringWidth(noti.description), fontManager.zw18.getStringWidth(noti.title)) + 38);
                    }

                    case "Exhibition" -> {
                        notificationHeight = 27;
                        yield Math.max(100, Math.max(fontManager.zw18.getStringWidth(noti.title) + 4, fontManager.zw14.getStringWidth(noti.description) + 26));
                    }

                    case "LSD" -> {
                        notificationHeight = 27.0F;
                        yield Math.max(100, Math.max(fontManager.zw18.getStringWidth(noti.title) + 4, fontManager.zw14.getStringWidth(noti.description) + 26));
                    }

                    case "Xylitol" -> {
                        notificationHeight = 23;
                        yield fontManager.zw18.getStringWidth(noti.description) + 25;
                    }

                    default -> notificationWidth;
                };
            }

            boolean zoom = Objects.equals(NotificationHUD.notiAnimation, "Zoom");

            if (Objects.equals(NotificationHUD.notiMode, "Center")) {
                x = (float) ((window.getGuiScaledWidth() / 2 + notificationWidth / 2 - notificationWidth) / (Objects.equals(NotificationHUD.notiAnimation, "Side") ? animation.getOutput() : 1));
                y = (float) window.getGuiScaledHeight() / 2 + yOffset + 15.0f;
            } else {
                x = (float) (window.getGuiScaledWidth() - notificationWidth * (Objects.equals(NotificationHUD.notiAnimation, "Side") ? animation.getOutput() : 1));
                y = window.getGuiScaledHeight() - yOffset - notificationHeight - 33.0f;
            }

            poseStack.pushPose();

            if (zoom) {
                poseStack.translate((x + (double) notificationWidth / 2) * (1 - animation2.getOutput()), (y + (double) notificationHeight / 2) * (1 - animation2.getOutput()), 0.0);
                poseStack.scale((float) animation2.getOutput(), (float) animation2.getOutput(), 0f);
            }

            if (NotificationHUD.notiStyle.equals("LSD") || NotificationHUD.notiStyle.equals("Xylitol")) noti.render(poseStack, x - 5, y, notificationWidth, notificationHeight);
            else noti.render(poseStack, x, y, notificationWidth, notificationHeight);
            poseStack.popPose();

            yOffset += (float) ((notificationHeight + actualOffset) * animation.getOutput());
        }
    }

    private void render(PoseStack poseStack, float x, float y, float width, float height) {
        float percentage = Math.min((float) this.timer.getTime() / this.time, 1.0f);
        boolean isEnglish = HUD.INSTANCE.languageValue.is("English");
        switch (NotificationHUD.notiStyle) {
            case "Loratadine" -> {
                RenderUtils.renderRoundedQuad(poseStack, new Color(20, 20, 20, 80), x, y, x + width, y + height, 8, 36);
                RenderUtils.renderRoundedQuad(poseStack, new Color(20, 20, 20, 40), x + 4, y + 4, x + height - 4, y + height - 4, 4, 36);

                fontManager.icon30.drawString(poseStack, this.notificationType.getIcon(), x + 9.5f, y + 11, new Color(200, 200, 200, 255).getRGB());

                if (isEnglish) {
                    fontManager.tenacityBold20.drawString(poseStack, this.title, x + 30, y + 4, new Color(200, 200, 200, 255).getRGB());
                    fontManager.ax18.drawString(poseStack, this.description, x + 30, y + 17, new Color(200, 200, 200, 255).getRGB());
                } else {
                    fontManager.zw20.drawString(poseStack, this.title, x + 30, y + 4, new Color(200, 200, 200, 255).getRGB());
                    fontManager.zw18.drawString(poseStack, this.description, x + 30, y + 17, new Color(200, 200, 200, 255).getRGB());
                }
            }

            case "Exhibition" -> {
                float finalX = this.notificationType == NotificationType.INFO ? x + 3.0f : x;

                Color bgColor = Objects.equals(NotificationHUD.notiColor, "White") ? Color.WHITE : Objects.equals(NotificationHUD.notiColor, "Alpha") ? new Color(0, 0, 0, 160) : Objects.equals(NotificationHUD.notiColor, "Classic") ? new Color(23, 23, 23) : Color.BLACK;
                Color fontColor = Objects.equals(NotificationHUD.notiColor, "White") ? Color.BLACK : Color.WHITE;
                Color iconcolor = ColorUtils.applyOpacity(this.notificationType.getColor(), 70.0f);

                RenderUtils.drawRoundedRect(poseStack, x, y, width, height,0, bgColor);
                RenderUtils.drawRoundedRect(poseStack, x, y + height - 1, width * percentage, 1, 0,this.notificationType.getColor());

                fontManager.icon30.drawString(poseStack, this.notificationType.getIcon(), finalX + 5.0f, y + 7.0f, iconcolor.getRGB());

                if (isEnglish) {
                    fontManager.ax18.drawString(poseStack, this.title, x + 22f, y + 3.5f, fontColor.getRGB());
                    fontManager.ax14.drawString(poseStack, this.description, x + 22f, y + 15, fontColor.getRGB());
                } else {
                    fontManager.zw18.drawString(poseStack, this.title, x + 22f, y + 3.5f, fontColor.getRGB());
                    fontManager.zw14.drawString(poseStack, this.description, x + 22f, y + 15, fontColor.getRGB());
                }
            }

            case "LSD" -> {
                float finalX = this.notificationType == NotificationType.INFO ? x + 3.0f : x;

                Color bgColor = new Color(29, 29, 31);
                Color titleColor =  Color.WHITE;
                Color descriptionColor =  new Color(104, 104, 104, 255);
                Color iconcolor = this.notificationType.getColor();


                RenderUtils.drawRoundedRect(poseStack, x, y, width, height, 2,bgColor);

                GlowUtils.drawGlow(poseStack, x + width - 10, y + height / 2 - 2, 4, 4, 4, iconcolor, () -> RenderUtils.drawRoundedRect(poseStack, x + width - 10, y + height / 2 - 2, 4, 4, 4,iconcolor));

                fontManager.icon30.drawString(poseStack, this.notificationType.getIcon(), finalX + 7.0f, y + 8.0f, Color.WHITE.getRGB());

                if (isEnglish) {
                    fontManager.tenacityBold20.drawString(poseStack, this.title, x + 25f, y + 5f, titleColor.getRGB());
                    fontManager.ax12.drawString(poseStack, this.description, x + 25f, y + 16, descriptionColor.getRGB());
                } else {
                    fontManager.tenacityBold20.drawString(poseStack, this.title, x + 25f, y + 5, titleColor.getRGB());
                    fontManager.zw12.drawString(poseStack, this.description, x + 25f, y + 16, descriptionColor.getRGB());
                }
            }

            case "Xylitol" -> {
                Color color = ColorUtils.applyOpacity(ColorUtils.interpolateColorC(Color.BLACK, this.notificationType.getColor(), 0.65f), 70.0f);
                Color iconColor = ColorUtils.applyOpacity(this.notificationType.getColor(), 70);
                Color textColor = ColorUtils.applyOpacity(Color.WHITE, 80.0f);

                RenderUtils.drawRectangle(poseStack, x, y, width, height, new Color(0, 0, 0, 70).getRGB());
                RenderUtils.drawRectangle(poseStack, x, y, width * percentage, height, color.getRGB());

                fontManager.icon30.drawString(poseStack, this.notificationType.getIcon(), x + 5.0f, y + 7, iconColor.getRGB());

                if (isEnglish) {
                    fontManager.ax18.drawString(poseStack, this.description, x + 21.0f, y + 7.0f, textColor.getRGB());
                } else {
                    fontManager.zw18.drawString(poseStack, this.description, x + 21.0f, y + 7.0f, textColor.getRGB());
                }
            }
        }
    }

    public static void add(NotificationType NotificationType, String title, String description) {
        notifications.add(new NotificationManager(NotificationType, title, description, toggleTime));
    }

    public static void add(NotificationType NotificationType, String title, String description, float time) {
        notifications.add(new NotificationManager(NotificationType, title, description, time));
    }

    public static void setToggleTime(float toggleTime) {
        NotificationManager.toggleTime = toggleTime;
    }
}
