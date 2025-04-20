package shop.xmz.lol.loratadine.utils.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import lombok.Getter;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.GLUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author AquaVase, Hoshimi Miyabi
 * @since 3/15/2024 - 9:22 PM
 */
@Getter
public class TrueTypeFont implements AutoCloseable {
    // fix linear filter black border
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 1);

    private static final Color SHADOW_COLOR = new Color(26, 26, 26, 160);

    private static final HashMap<Integer, Font> ALTERNATIVE_FONT_MAP = new HashMap<>();
    private static final HashMap<Integer, Font> ALTERNATIVE_BOLD_FONT_MAP = new HashMap<>();

    // no cutting chars
    private static final int GAP = 5;

    // for obfuscate style
    private static final Random RANDOM = new Random();
    private static final String RANDOM_STRING = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

    // for different styles
    private static final int[] COLOR_TABLE = new int[32];
    private static final String FORMAT_CODE_IDENTIFIER = "0123456789abcdefklmnor";

    private static final String EMOJI_REGEX = "/(\\uD83C[\\uDF00-\\uDFFF])|(\\uD83D[\\uDC00-\\uDE4F\\uDE80-\\uDEFF])|[\\u2600-\\u2B55]/g";

    private final Font font;
    private final Font boldFont;

    // rendering options
    private final boolean antiAliased, usingFractionMetrics;

    // awt stuffs
    private final FontRenderContext context;
    private final AffineTransform transform = new AffineTransform();
    private final FontMetrics fontMetrics;

    // store cached chars
    private final HashMap<Character, GlyphData> glyphMap = new HashMap<>(512);
    private final HashMap<Character, GlyphData> boldGlyphMap = new HashMap<>(128);

    // setup color table
    static {
        for (int i = 0; i < COLOR_TABLE.length; i++) {
            final int offset = (i >> 3 & 1) * 85;

            int red = (i >> 2 & 1) * 170 + offset;
            int green = (i >> 1 & 1) * 170 + offset;
            int blue = (i & 1) * 170 + offset;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            COLOR_TABLE[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    public TrueTypeFont(Font font, boolean antiAliased, boolean usingFractionMetrics) {
        this.font = font;
        this.boldFont = font.deriveFont(Font.BOLD);
        this.antiAliased = antiAliased;
        this.usingFractionMetrics = usingFractionMetrics;
        this.context = new FontRenderContext(transform, antiAliased, usingFractionMetrics);
        this.fontMetrics = new Canvas().getFontMetrics(font);
    }

    public float getMiddleOfBox(float boxHeight) {
        return boxHeight / 2f - getHeight() / 2f;
    }

    public float getStringHeight(String text) {
        return getHeight() * 2;
    }

    // actually return font size.
    public int getHeight() {
        return fontMetrics.getAscent() / 2;
    }


    public float drawCenteredString(PoseStack matrices, String str, float x, float y, int color) {
        return drawString(matrices, str, x - getStringWidth(str) / 2F, y, color, false);
    }

    public float drawCenteredString(PoseStack matrices, String str, float x, float y, int color, boolean shadow) {
        return drawString(matrices, str, x - getStringWidth(str) / 2F, y, color, shadow);
    }
    public float drawCenteredStringWithShadow(PoseStack matrices, String str, float x, float y, int color) {
        return drawString(matrices, str, x - getStringWidth(str) / 2F, y, color, true);
    }

    public float drawString(PoseStack matrices, String str, float x, float y, int color) {
        return drawString(matrices, str, x, y, color, false);
    }

    public float drawStringWithShadow(PoseStack matrices, String str, float x, float y, int color) {
        return drawString(matrices, str, x, y, color, true);
    }

    public float drawString(PoseStack matrices, String str, float x, float y, int color, boolean dropShadow) {

        if (dropShadow) {
            float shadow = renderString(matrices, str, x + 0.5f, y + 0.5f, ColorUtils.applyOpacity(SHADOW_COLOR.getRGB(), (int) (160 * (ColorUtils.alpha(color) / 255f))), true);
            float normal = renderString(matrices, str, x, y, color, false);
            return Math.max(normal, shadow);
        }

        return renderString(matrices, str, x, y, color, false);
    }

    private float renderString(PoseStack matrices, String str, float x, float y, int color, boolean shadow) {
        if (str == null || str.isEmpty()) return x;

        float originalX = x;

        matrices.pushPose();
        matrices.scale(0.5F, 0.5F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // 不需要显式禁用纹理，只是设置默认的纹理ID
        GLUtils.color(color);

        x *= 2;
        y = (y - 2) * 2;

        boolean obfuscated = false;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underlined = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '\n' && i < str.length() - 1) {
                y += fontMetrics.getAscent();
                x = originalX * 2;
                i++;
            }

            if ((c == '§') && i < str.length() - 1) {
                i++;

                int colorIndex = FORMAT_CODE_IDENTIFIER.indexOf(str.charAt(i));

                switch (colorIndex) {
                    case 16:
                        obfuscated = true;
                        break;
                    case 17:
                        bold = true;
                        break;
                    case 18:
                        strikethrough = true;
                        break;
                    case 19:
                        underlined = true;
                        break;
                    case 20:
                        italic = true;
                        break;
                    case 21:
                        obfuscated = false;
                        bold = false;
                        italic = false;
                        underlined = false;
                        strikethrough = false;
                        GLUtils.color(color);
                        break;
                    default:
                        if (!shadow) {
                            if (colorIndex == -1) {
                                colorIndex = 15;
                            }

                            final int finalColor = COLOR_TABLE[colorIndex];

                            GLUtils.color(ColorUtils.red(finalColor), ColorUtils.green(finalColor), ColorUtils.blue(finalColor), ColorUtils.alpha(color));
                        }

                        break;
                }
            } else {
                char targetChar = c;

                if (obfuscated && RANDOM_STRING.indexOf(c) != -1) {
                    final float charWidth = getCharWidth(c, bold);
                    int index;

                    do {
                        index = RANDOM.nextInt(RANDOM_STRING.length());
                        targetChar = RANDOM_STRING.charAt(index);
                    } while (MathUtils.approximatelyEquals(charWidth, getCharWidth(targetChar, bold)));
                }

                final GlyphData glyph = getGlyphData(targetChar, bold);

                if (shadow) {
                    drawGlyph(matrices, glyph, x, y, false, false, italic);
                } else {
                    drawGlyph(matrices, glyph, x, y, strikethrough, underlined, italic);
                }

                x += glyph.charWidth;
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        matrices.popPose();

        return originalX + getStringWidth(str);
    }

    private void drawGlyph(PoseStack matrices, GlyphData glyphData, float x, float y, boolean strikethrough, boolean underlined, boolean italic) {
        float offset = italic ? 4 : 0;

        float xTexel = 1.0F / glyphData.width;
        float yTexel = 1.0F / glyphData.height;

        Matrix4f mat4 = matrices.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, glyphData.texture);

        BufferBuilder worldRenderer = Tesselator.getInstance().getBuilder();
        worldRenderer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);
        worldRenderer.vertex(mat4, x + offset, y, 0.0F).uv(glyphData.u * xTexel, glyphData.v * yTexel).endVertex();
        worldRenderer.vertex(mat4, x - offset, y + glyphData.charHeight, 0.0F).uv(glyphData.u * xTexel, (glyphData.v + glyphData.charHeight) * yTexel).endVertex();
        worldRenderer.vertex(mat4, x + glyphData.charWidth + offset, y, 0.0F).uv((glyphData.u + glyphData.charWidth) * xTexel, glyphData.v * yTexel).endVertex();
        worldRenderer.vertex(mat4, x + glyphData.charWidth - offset, y + glyphData.charHeight, 0.0F).uv((glyphData.u + glyphData.charWidth) * xTexel, (glyphData.v + glyphData.charHeight) * yTexel).endVertex();
        BufferUploader.drawWithShader(worldRenderer.end());

        if (strikethrough || underlined) {
            // 在1.20.1中，使用setShaderTexture传入0来禁用纹理
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShader(GameRenderer::getPositionShader);

            if (strikethrough) {
                worldRenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                worldRenderer.vertex(mat4, x, y + glyphData.charHeight / 2F, 0.0F).endVertex();
                worldRenderer.vertex(mat4, x + glyphData.charWidth, y + glyphData.charHeight / 2F, 0.0F).endVertex();
                worldRenderer.vertex(mat4, x + glyphData.charWidth, y + glyphData.charHeight / 2F - 1, 0.0F).endVertex();
                worldRenderer.vertex(mat4, x, y + glyphData.charHeight / 2F - 1, 0.0F).endVertex();
                BufferUploader.drawWithShader(worldRenderer.end());
            }

            if (underlined) {
                worldRenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                worldRenderer.vertex(mat4, x, y + font.getSize(), 0.0F).endVertex();
                worldRenderer.vertex(mat4, x + glyphData.charWidth, y + font.getSize(), 0.0F).endVertex();
                worldRenderer.vertex(mat4, x + glyphData.charWidth, y + font.getSize() - 1, 0.0F).endVertex();
                worldRenderer.vertex(mat4, x, y + font.getSize() - 1, 0.0F).endVertex();
                BufferUploader.drawWithShader(worldRenderer.end());
            }

            // 重新启用纹理，恢复到之前的纹理ID
            RenderSystem.setShaderTexture(0, glyphData.texture);
        }
    }

    private GlyphData getGlyphData(char character, boolean bold) {
        Map<Character, GlyphData> map = bold ? boldGlyphMap : glyphMap;

        if (!map.containsKey(character)) {
            GlyphData glyphData = createGlyphData(character, bold);
            map.put(character, glyphData);
            return glyphData;
        }

        return map.get(character);
    }

    private GlyphData createGlyphData(char character, boolean bold) {
        String charStr = String.valueOf(character);
        boolean available = this.font.canDisplay(character);

        Font font = available ? (bold ? this.boldFont : this.font) : getAlternativeFont(this.font.getSize(), bold);

        Rectangle charBounds = font.getStringBounds(charStr, context).getBounds();
        int charWidth = charBounds.width;
        int charHeight = charBounds.height;

        int imageSize = Math.max(charWidth, charHeight) * 2 + GAP * 2;

        int u = imageSize / 2 - charWidth / 2;
        int v = imageSize / 2 - charHeight / 2;

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, imageSize, imageSize);

        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, usingFractionMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliased ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        graphics.setColor(Color.WHITE);
        graphics.drawString(charStr, u, v + fontMetrics.getAscent());

        graphics.dispose();

        int texture = GLUtils.uploadTexture(image);
        RenderSystem.bindTexture(texture);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        return new GlyphData(character, charWidth, charHeight, imageSize, imageSize, u, v, texture);
    }

    public float getStringWidth(String s) {
        if (s == null || s.isEmpty()) return 0;

        float ret = 0;

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            if ((c == '§') && i < s.length() - 1) {
                i++;
            } else {
                ret += getCharWidth(c, false);
            }
        }

        return ret / 2F;
    }

    public float getCharWidth(char c, boolean bold) {
        return getGlyphData(c, bold).charWidth;
    }

    public float getCharHeight(char c, boolean bold) {
        return getGlyphData(c, bold).charHeight;
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float) width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidth(c0, false);

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;

                if (flag1) {
                    ++f;
                }
            }

            if (f > (float) width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, (char) c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }


    @Override
    public void close() throws Exception {
        glyphMap.values().forEach(glyphData -> RenderSystem.deleteTexture(glyphData.texture));
        glyphMap.clear();

        boldGlyphMap.values().forEach(glyphData -> RenderSystem.deleteTexture(glyphData.texture));
        boldGlyphMap.clear();
    }

    public static void clearAlternativeFonts() {
        ALTERNATIVE_FONT_MAP.clear();
        ALTERNATIVE_BOLD_FONT_MAP.clear();
    }

    private static Font getAlternativeFont(int size, boolean bold) {
        HashMap<Integer, Font> fontMap = bold ? ALTERNATIVE_BOLD_FONT_MAP : ALTERNATIVE_FONT_MAP;
        Font font = fontMap.get(size);

        if (font == null) {
            font = FontManager.driveFont(FontManager.defRegular, bold ? Font.BOLD : Font.PLAIN, size);
            fontMap.put(size, font);
        }

        return font;
    }

    // You'll need to implement or update this class to match your 1.20.1 code
    public static class GlyphData {
        public final char character;
        public final float charWidth;
        public final float charHeight;
        public final int width;
        public final int height;
        public final float u;
        public final float v;
        public final int texture;

        public GlyphData(char character, float charWidth, float charHeight, int width, int height, float u, float v, int texture) {
            this.character = character;
            this.charWidth = charWidth;
            this.charHeight = charHeight;
            this.width = width;
            this.height = height;
            this.u = u;
            this.v = v;
            this.texture = texture;
        }
    }
}