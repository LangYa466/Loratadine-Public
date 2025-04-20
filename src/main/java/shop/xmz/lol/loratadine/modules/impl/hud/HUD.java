package shop.xmz.lol.loratadine.modules.impl.hud;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.impl.player.Scaffold;
import shop.xmz.lol.loratadine.modules.impl.setting.Theme;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.ConnectionUtil;
import shop.xmz.lol.loratadine.utils.misc.EntityUtils;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.GlowUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;

import static net.minecraft.stats.StatFormatter.DECIMAL_FORMAT;
import static shop.xmz.lol.loratadine.utils.render.RenderUtils.drawImage;

public class HUD extends Module {
    public static HUD INSTANCE;
    private final BooleanSetting shadow = new BooleanSetting("Shadow", this, true);

    public final ModeSetting waterMark_Value = new ModeSetting("WaterMark", this, new String[]{
            "Logo",
            "Modern",
            "Modern Bar",
            "Simple",
            "Simple Extra",
            "Simple Borel",
            "Simple PlymouthRock",
            "Classic",
            "LoraSense",
            "None"
    }, "Logo");

    public final ModeSetting hotBar_Value = new ModeSetting("Hot Bar Mode", this, new String[]{
            "Loratadine",
            "Minecraft",
            "Modern Vertical",
            "Simple"
    }, "Loratadine");

    public final ModeSetting count_Value = new ModeSetting("CountMode",this,new String[]{
            "Loratadine",
            "Modern",
            "Simple",
            "Basic",
            "Off"
    },"Simple");

    private final BooleanSetting xyzValue = new BooleanSetting("XYZ", this, false);
    private final BooleanSetting bpsValue = new BooleanSetting("BPS", this, false);
    private final BooleanSetting rightCustomImageValue = new BooleanSetting("RightCustomImage", this, false);

    public final ModeSetting fontValue = new ModeSetting("Font Mode", this, new String[]{"Minecraft", "Normal"}, "Minecraft");
    public final ModeSetting languageValue = new ModeSetting("Language", this, new String[]{"Chinese", "English"}, "English");

    private Vec3 lastPosition = null; // 改为null，表示尚未初始化
    private long lastUpdateTime = 0; // 添加时间记录
    private double bps = 0.0; // 存储当前的 BPS 值

    public HUD() {
        super("HUD", "平视显示器", Category.RENDER);
        INSTANCE = this;
        setEnabled(true);
    }

    public Color getColor(int tick) {
        return switch (Theme.INSTANCE.color_Value.getValue()) {
            case "Fade" -> ColorUtils.fade(5, tick * 20, Theme.INSTANCE.firstColor, 1);
            case "Static" -> Theme.INSTANCE.firstColor;
            case "Rainbow" ->
                    new Color(Color.HSBtoRGB((float) ((mc.player == null ? 0 : mc.player.tickCount / 50.0) + Math.sin((double) tick / 50.0 * 1.6)) % 1.0f, 0.5f, 1.0f));
            case "Double" -> {
                tick *= 100;
                yield new Color(ColorUtils.colorSwitch(Theme.INSTANCE.firstColor, Theme.INSTANCE.secondColor, 2000, -tick / 40, 75, 2, 255));
            }
            default -> new Color(255, 255, 255, 255);
        };
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final PoseStack poseStack = event.poseStack();
        final FontManager fontManager = Loratadine.INSTANCE.getFontManager();
        if (poseStack == null || mc == null || mc.level == null || mc.player == null) return;

        // 客户端主题色
        Theme.INSTANCE.firstColor = new Color(Theme.INSTANCE.red.getValue().intValue(), Theme.INSTANCE.green.getValue().intValue(), Theme.INSTANCE.blue.getValue().intValue());
        Theme.INSTANCE.secondColor = new Color(Theme.INSTANCE.red2.getValue().intValue(), Theme.INSTANCE.green2.getValue().intValue(), Theme.INSTANCE.blue2.getValue().intValue());

        // 绘制图案
        if (rightCustomImageValue.getValue()) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int imageWidth = 100;
            int imageHeight = 100;
            int x_ = screenWidth - imageWidth;
            int y_ = mc.player.getActiveEffectsMap().isEmpty() ? 1 : 25;

            drawImage(event.poseStack(), Loratadine.INSTANCE.CLIENT_CUSTOM_IMAGE_PNG, x_, y_, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight);
        }

        // 绘制坐标
        if (xyzValue.getValue()) {
            if (fontValue.is("Minecraft")) {
               WrapperUtils.drawShadow(poseStack, "X: " + DECIMAL_FORMAT.format(mc.player.getX()) + " Y: " + DECIMAL_FORMAT.format(mc.player.getY()) + " Z: " + DECIMAL_FORMAT.format(mc.player.getZ()), 4, mc.getWindow().getGuiScaledHeight() - 10, Theme.INSTANCE.firstColor.getRGB(), shadow.getValue());
            } else {
                fontManager.tenacity20.drawString(poseStack, "X: " + DECIMAL_FORMAT.format(mc.player.getX()) + " Y: " + DECIMAL_FORMAT.format(mc.player.getY()) + " Z: " + DECIMAL_FORMAT.format(mc.player.getZ()), 4, mc.getWindow().getGuiScaledHeight() - 10, Theme.INSTANCE.firstColor.getRGB(), shadow.getValue());
            }
        }

        if (bpsValue.getValue()) {
            String bpsText = String.format("%.2f", bps);

            if (fontValue.is("Minecraft")) {
                WrapperUtils.drawShadow(poseStack, "BPS: " + bpsText, 4, mc.getWindow().getGuiScaledHeight() - 20, Theme.INSTANCE.firstColor.getRGB(), shadow.getValue());
            } else {
                fontManager.tenacity20.drawString(poseStack, "BPS: " + bpsText, 4, mc.getWindow().getGuiScaledHeight() - 20, Theme.INSTANCE.firstColor.getRGB(), shadow.getValue());
            }
        }

        switch (waterMark_Value.getValue()) {
            case "Modern Bar" -> {
                int x = 4, y = 5;
                Color bgColor = new Color(0, 0, 0, 80);
                int textColor = Color.WHITE.getRGB();
                int stringWidth1 = (int) (fontManager.ax20.getStringWidth("Loratadine") + fontManager.icon33.getStringWidth("Q"));
                int stringWidth2 = (int) (fontManager.ax20.getStringWidth(WrapperUtils.getFPS() + "fps") + fontManager.icon45.getStringWidth("k"));
                int stringWidth3 = (int) (fontManager.ax20.getStringWidth(mc.player.getName().getString()) + fontManager.icon45.getStringWidth("G"));

                RenderUtils.drawRoundedRect(poseStack, x, y, stringWidth1 + 15, 21, 8, bgColor);
                RenderUtils.drawRoundedRect(poseStack, x + stringWidth1 + 20, y, stringWidth2 + 15, 21, 8, bgColor);
                RenderUtils.drawRoundedRect(poseStack, x + stringWidth1 + stringWidth2 + 40, y, stringWidth3 + 15, 21, 8, bgColor);

                fontManager.icon33.drawString(poseStack, "Q", x + 4, y + 6, getColor(1).getRGB());
                fontManager.ax20.drawString(poseStack, "Loratadine", x + 22, y + 6, textColor);

                fontManager.icon45.drawString(poseStack, "k", x + stringWidth1 + 23, y + 2, getColor(2).getRGB());
                fontManager.ax20.drawString(poseStack, WrapperUtils.getFPS() + "fps", x + stringWidth1 + 42, y + 6, textColor);

                fontManager.icon45.drawString(poseStack, "G", x + stringWidth1 + stringWidth2 + 43, y + 2, getColor(1).getRGB());
                fontManager.ax20.drawString(poseStack, mc.player.getName().getString(), x + stringWidth1 + stringWidth2 + 60, y + 6, textColor);
            }

            case "Logo" -> {
            }

            case "Modern" -> {
                int x = 4, y = 5;
                final String text = mc.player.getName().getString() + " | " + WrapperUtils.getFPS() + "fps | " + ConnectionUtil.getRemoteIp();
                int stringWidth = (int) Loratadine.INSTANCE.getFontManager().ax18.getStringWidth(text);
                int bgColor = new Color(0, 0, 0, 160).getRGB();
                int color = this.getColor(1).getRGB();

                RenderUtils.drawRectangle(poseStack, x, y, 1F, 18F, color);
                RenderUtils.drawRightTrapezoid(poseStack, x + 1F, y, 14F, 18F, 8F, true, bgColor);
                RenderUtils.drawGradientParallelogram(poseStack, x + 16F, y, 52, 18, 8, getColor(1), getColor(4));
                RenderUtils.drawIsoscelesTrapezoid(poseStack, x + 70F, y, stringWidth + 40F, 18F, 8f, bgColor);
                RenderUtils.drawParallelogram(poseStack, x + stringWidth + 110F, y, 1.5f, 18F, -8f, color);

                fontManager.icon30.drawString(poseStack, "Q", x + 4F, y + 5F, color);
                fontManager.tenacityBold20.drawString(poseStack, "Loratadine", x + 23F, y + 4F, -1);
                fontManager.ax20.drawString(poseStack, text, x + 80F, y + 4F, -1);
            }

            case "Simple Borel" -> {
            }

            case "Simple PlymouthRock" -> {
            }

            case "Simple Extra" -> {
                if (fontValue.is("Minecraft")) {
                    drawImage(poseStack, Loratadine.INSTANCE.CLIENT_ICON_PNG, 3, 1, 15, 15, 0, 0, 15, 15);
                    WrapperUtils.drawShadow(poseStack, Loratadine.CLIENT_NAME.substring(0, 1), 20, 4, ColorUtils.rainbow(10, 1).getRGB(), shadow.getValue());
                    WrapperUtils.drawShadow(poseStack, Loratadine.CLIENT_NAME.substring(1) + " " + Loratadine.CLIENT_VERSION + " [" + WrapperUtils.getFPS() + "FPS]", 20 + mc.font.width(Loratadine.CLIENT_NAME.substring(0, 1)), 4, Color.WHITE.getRGB(), shadow.getValue());
                } else {
                    drawImage(poseStack, Loratadine.INSTANCE.CLIENT_ICON_PNG, 3, 1, 15, 15, 0, 0, 15, 15);
                    fontManager.tenacity20.drawString(poseStack, Loratadine.CLIENT_NAME.substring(0, 1), 20, 4, ColorUtils.rainbow(10, 1).getRGB(), shadow.getValue());
                    fontManager.tenacity20.drawString(poseStack, Loratadine.CLIENT_NAME.substring(1) + " " + Loratadine.CLIENT_VERSION + " [" + WrapperUtils.getFPS() + "FPS]", 20 + fontManager.tenacity20.getStringWidth(Loratadine.CLIENT_NAME.substring(0, 1)), 4, Color.WHITE.getRGB(), shadow.getValue());
                }
            }

            case "Simple" -> {
                if (fontValue.is("Minecraft")) {
                    WrapperUtils.drawShadow(poseStack, Loratadine.CLIENT_NAME.substring(0, 1), 5, 4, ColorUtils.rainbow(10, 1).getRGB(), shadow.getValue());
                    WrapperUtils.drawShadow(poseStack, Loratadine.CLIENT_NAME.substring(1), 5 + mc.font.width(Loratadine.CLIENT_NAME.substring(0, 1)), 4, Color.WHITE.getRGB(), shadow.getValue());
                } else {
                    fontManager.tenacity20.drawString(poseStack, Loratadine.CLIENT_NAME.substring(0, 1), 5, 4, ColorUtils.rainbow(10, 1).getRGB(), shadow.getValue());
                    fontManager.tenacity20.drawString(poseStack, Loratadine.CLIENT_NAME.substring(1), 5 + fontManager.tenacity20.getStringWidth(Loratadine.CLIENT_NAME.substring(0, 1)), 4, Color.WHITE.getRGB(), shadow.getValue());
                }
            }

            case "LoraSense" -> {
                String text = "§f" + Loratadine.CLIENT_NAME + "§asense§f" + " - " + mc.player.getName().getString() + " - " + "吉吉岛" + " - " + EntityUtils.getPing(mc.player) + "ms ";

                float x = 3.5f, y = 3.5f;
                int width = fontValue.is("Minecraft") ? mc.font.width(text) : (int) fontManager.zw19.getStringWidth(text);
                int height = 18; // 固定高度

                Color lineColor = new Color(104, 104, 104);
                Color darkest = new Color(0, 0, 0);
                Color dark = new Color(70, 70, 70);
                Color bgColor = new Color(23, 23, 23);
                int fontColor = new Color(255, 255, 255).getRGB();

                // **绘制外框**
                RenderUtils.drawRectangle(poseStack, x, y, width + 7, height, darkest.getRGB());
                RenderUtils.drawRectangle(poseStack, x + 0.5F, y + 0.5F, width + 6, height - 1, lineColor.getRGB());
                RenderUtils.drawRectangle(poseStack, x + 1.5F, y + 1.5F, width + 4, height - 3, darkest.getRGB());
                RenderUtils.drawRectangle(poseStack, x + 2F, y + 2F, width + 3, height - 4, dark.getRGB());

                // **绘制内容背景**
                RenderUtils.drawRectangle(poseStack, x + 2.5F, y + 2.5F, width + 2, height - 5, bgColor.getRGB());

                // **绘制底部渐变条**
                RenderUtils.drawGradientRectL2R(poseStack, (int) (x + 2.5), (int) (y + height - 3), width + 2, 1, getColor(1).getRGB(), getColor(4).getRGB());

                if (fontValue.is("Minecraft")) {
                    if (shadow.getValue()) {
                        WrapperUtils.drawShadow(poseStack, text, x + 4.5f, y + 5.5f, fontColor);
                    } else {
                        WrapperUtils.draw(poseStack, text, x + 4.5f, y + 5.5f, fontColor);
                    }
                } else {
                    TrueTypeFont font = fontManager.zw19;
                    font.drawString(event.poseStack(), text, x + 4.5f, y + 4.5f, fontColor);
                }
            }

            case "Classic" -> {
                String text = "      " + " | " + Loratadine.CLIENT_NAME + " " + Loratadine.CLIENT_VERSION + " | Name: " + mc.player.getDisplayName().getString() + " | FPS: " + WrapperUtils.getFPS();

                float height = fontManager.zw20.getStringHeight(text) - 2;
                float width = fontManager.zw20.getStringWidth(text) + 6;

                RenderUtils.drawRectangle(poseStack, 4, 5, width, height, new Color(23, 23, 23).getRGB());
                RenderUtils.drawGradientRectL2R(poseStack, 4, 5, width, 1,  getColor(1).getRGB(),getColor(4).getRGB());
                drawImage(poseStack, Loratadine.INSTANCE.CLIENT_ICON_PNG, 7, 7, 15, 15, 0, 0, 15, 15);
                fontManager.zw20.drawString(poseStack, text, 7, 9, new Color(255, 255, 255).getRGB(), shadow.getValue());
            }
        }
    }

    @EventTarget
    public void onUpdateEvent(UpdateEvent event) {
        if (mc.player == null) return; // 确保玩家存在

        // 获取当前时间和位置
        long currentTime = System.currentTimeMillis();
        Vec3 currentPosition = mc.player.position();

        // 检查lastPosition是否已初始化
        if (lastPosition == null) {
            lastPosition = currentPosition;
            lastUpdateTime = currentTime;
            return;
        }

        // 计算时间差（秒）
        double timeDiff = (currentTime - lastUpdateTime) / 1000.0;
        if (timeDiff <= 0) return; // 防止除零错误

        // 只计算水平距离（忽略Y轴）
        double dx = currentPosition.x - lastPosition.x;
        double dz = currentPosition.z - lastPosition.z;
        double distance = Math.sqrt(dx * dx + dz * dz);

        // 检测异常值（例如传送或维度切换）
        if (distance > 100) {
            lastPosition = currentPosition;
            lastUpdateTime = currentTime;
            return;
        }

        // 计算BPS
        bps = distance / timeDiff;

        // 更新上一帧的位置和时间
        lastPosition = currentPosition;
        lastUpdateTime = currentTime;
    }
}
