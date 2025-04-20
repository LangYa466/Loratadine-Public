package shop.xmz.lol.loratadine.utils.font;

import lombok.RequiredArgsConstructor;

/**
 * @author AquaVase
 * @since 3/15/2024 - 9:25 PM
 */
@RequiredArgsConstructor
public class GlyphData {
    public final char character;

    public final int charWidth, charHeight;

    // texture size
    public final int width, height;

    public final float u, v;

    public final int texture;
}
