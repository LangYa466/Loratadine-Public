package shop.xmz.lol.loratadine.utils.render;

import lombok.experimental.UtilityClass;
import shop.xmz.lol.loratadine.utils.math.MathUtils;

import java.awt.*;

@UtilityClass
public class ColorUtils {
    public static int removeAlphaComponent(final int colour) {
        final int red = colour >> 16 & 0xFF;
        final int green = colour >> 8 & 0xFF;
        final int blue = colour & 0xFF;

        return ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                (blue & 0xFF);
    }

    public static int fadeTo(int startColour, int endColour, double progress) {
        double invert = 1.0 - progress;
        int r = (int) ((startColour >> 16 & 0xFF) * invert +
                (endColour >> 16 & 0xFF) * progress);
        int g = (int) ((startColour >> 8 & 0xFF) * invert +
                (endColour >> 8 & 0xFF) * progress);
        int b = (int) ((startColour & 0xFF) * invert +
                (endColour & 0xFF) * progress);
        int a = (int) ((startColour >> 24 & 0xFF) * invert +
                (endColour >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public static int blendHealthColours(final double progress) {
        return blendColours(HEALTH_COLOURS, progress);
    }

    public static int fadeBetween(int startColour, int endColour, double progress) {
        if (progress > 1) progress = 1 - progress % 1;
        return fadeTo(startColour, endColour, progress);
    }

    private static final int[] HEALTH_COLOURS = {
            0xFF00FF59, // Green
            0xFFFFFF00, // Yellow
            0xFFFF8000, // Orange
            0xFFFF0000, // Red
            0xFF800000 // Dark-red
    };

    public static int blendColours(final int[] colours, final double progress) {
        final int size = colours.length;
        if (progress == 1.f) return colours[0];
        else if (progress == 0.f) return colours[size - 1];
        final double mulProgress = Math.max(0, (1 - progress) * (size - 1));
        final int index = (int) mulProgress;
        return fadeBetween(colours[index], colours[index + 1], mulProgress - index);
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(final int red, final int green, final int blue, final int alpha) {
        int color = MathUtils.clamp_int(alpha, 0, 255) << 24;
        color |= MathUtils.clamp_int(red, 0, 255) << 16;
        color |= MathUtils.clamp_int(green, 0, 255) << 8;
        color |= MathUtils.clamp_int(blue, 0, 255);
        return color;
    }

    public static int getColor(Color color) {
        return getColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int getColor(int brightness) {
        return getColor(brightness, brightness, brightness, 255);
    }

    public static int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public static Color rainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        return new Color(Color.HSBtoRGB(hue, 0.7f, 1));
    }

    public static int getRGB(int r, int g, int b, int a) {
        return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
    }

    public static Color fade(final int speed, final int index, final Color color, final float alpha) {
        final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        angle = ((angle > 180) ? (360 - angle) : angle) + 180;
        final Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0f));
        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255.0f))));
    }

    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed, double alpha) {
        long now = (long) (speed * (double) System.currentTimeMillis() + (double) ((long) index * timePerIndex));

        float redDiff = (float) (firstColor.getRed() - secondColor.getRed()) / time;
        float greenDiff = (float) (firstColor.getGreen() - secondColor.getGreen()) / time;
        float blueDiff = (float) (firstColor.getBlue() - secondColor.getBlue()) / time;

        int red = Math.round((float) secondColor.getRed() + redDiff * (float) (now % (long) time));
        int green = Math.round((float) secondColor.getGreen() + greenDiff * (float) (now % (long) time));
        int blue = Math.round((float) secondColor.getBlue() + blueDiff * (float) (now % (long) time));

        float redInverseDiff = (float) (secondColor.getRed() - firstColor.getRed()) / time;
        float greenInverseDiff = (float) (secondColor.getGreen() - firstColor.getGreen()) / time;
        float blueInverseDiff = (float) (secondColor.getBlue() - firstColor.getBlue()) / time;

        int inverseRed = Math.round((float) firstColor.getRed() + redInverseDiff * (float) (now % (long) time));
        int inverseGreen = Math.round((float) firstColor.getGreen() + greenInverseDiff * (float) (now % (long) time));
        int inverseBlue = Math.round((float) firstColor.getBlue() + blueInverseDiff * (float) (now % (long) time));

        if (now % ((long) time * 2L) < (long) time) {
            return new Color(inverseRed, inverseGreen, inverseBlue, (int) alpha).getRGB();
        }

        return new Color(red, green, blue, (int) alpha).getRGB();
    }

    public static int reAlpha(int rgb, int alpha) {
        return getRGB(red(rgb), green(rgb), blue(rgb), alpha);
    }

    public static Color getGradientOffset(Color color1, Color color2, double offset) {
        double inverse_percent;
        int redPart;
        if(offset > 1.0D) {
            inverse_percent = offset % 1.0D;
            redPart = (int)offset;
            offset = redPart % 2 == 0?inverse_percent:1.0D - inverse_percent;
        }
        inverse_percent = 1.0D - offset;
        redPart = (int)((double)color1.getRed() * inverse_percent + (double)color2.getRed() * offset);
        int greenPart = (int)((double)color1.getGreen() * inverse_percent + (double)color2.getGreen() * offset);
        int bluePart = (int)((double)color1.getBlue() * inverse_percent + (double)color2.getBlue() * offset);
        return new Color(redPart, greenPart, bluePart);
    }

    public static Color getMultiGradientOffset(Color[] colors, double offset) {
        if (colors == null || colors.length < 2) {
            throw new IllegalArgumentException("Colors array must contain at least two colors");
        }

        // 确保 offset 在 0 和 1 之间
        offset = Math.min(Math.max(offset, 0.0), 1.0);

        // 计算每个颜色段的比例长度
        double segmentLength = 1.0 / (colors.length - 1);
        int segmentIndex = (int) (offset / segmentLength);

        // 确保 segmentIndex 不超过数组范围
        if (segmentIndex >= colors.length - 1) {
            segmentIndex = colors.length - 2;
        }

        // 当前颜色段内的相对进度
        double segmentOffset = (offset - (segmentIndex * segmentLength)) / segmentLength;

        // 获取当前颜色段的两种颜色
        Color color1 = colors[segmentIndex];
        Color color2 = colors[segmentIndex + 1];

        // 计算渐变颜色
        double inversePercent = 1.0 - segmentOffset;
        int redPart = (int) (color1.getRed() * inversePercent + color2.getRed() * segmentOffset);
        int greenPart = (int) (color1.getGreen() * inversePercent + color2.getGreen() * segmentOffset);
        int bluePart = (int) (color1.getBlue() * inversePercent + color2.getBlue() * segmentOffset);

        return new Color(redPart, greenPart, bluePart);
    }

    public static int alpha(int hex) {
        return (hex >> 24) & 0xFF;
    }

    public static int red(int hex) {
        return (hex >> 16) & 0xFF;
    }

    public static int green(int hex) {
        return (hex >> 8) & 0xFF;
    }

    public static int blue(int hex) {
        return hex & 0xFF;
    }

    public static Color getBlack(float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(0, 0, 0, opacity);
    }

    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static int color(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }
    public static int applyAlpha(int color, float alpha) {
        int a = (int) ((color >> 24 & 0xFF) * alpha); // 计算新的透明度
        return (color & 0xFFFFFF) | (a << 24);       // 将透明度与原颜色合成
    }

    public static Color lerpColor(Color startColor, Color endColor, float progress) {
        // 确保 progress 在 0 到 1 之间
        progress = Math.max(0, Math.min(1, progress));

        // 分别对 R、G、B、A 进行插值
        int red = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
        int green = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
        int blue = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);
        int alpha = (int) (startColor.getAlpha() + (endColor.getAlpha() - startColor.getAlpha()) * progress);

        // 返回插值后的颜色
        return new Color(red, green, blue, alpha);
    }

    public static Color brighter(Color color, float FACTOR) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255),
                alpha);
    }

    public static int interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return interpolateColorC(color1, color2, amount).getRGB();
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue){
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }
    public static Double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static Color darker(Color color, float FACTOR) {
        return new Color(Math.max((int) (color.getRed() * FACTOR), 0),
                Math.max((int) (color.getGreen() * FACTOR), 0),
                Math.max((int) (color.getBlue() * FACTOR), 0),
                color.getAlpha());
    }
}