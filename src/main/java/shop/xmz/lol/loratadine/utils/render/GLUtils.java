package shop.xmz.lol.loratadine.utils.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

/**
 * @author AquaVase
 * @since 6/16/2024 - 3:06 PM
 */
@UtilityClass
public class GLUtils {

    public static void color(int r, int g, int b, int a) {
        RenderSystem.setShaderColor(r / 255F, g / 255F, b / 255F, a / 255F);
    }

    public static void color(int hex) {
        color(ColorUtils.red(hex), ColorUtils.green(hex), ColorUtils.blue(hex), ColorUtils.alpha(hex));
    }

    public static int uploadTexture(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                nativeImage.setPixelRGBA(x, y, image.getRGB(x, y));
            }
        }

        DynamicTexture texture = new DynamicTexture(nativeImage);
        return texture.getId();
    }


    public static int uploadTexture(BufferedImage image, boolean allocate, boolean linear) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), !allocate);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                nativeImage.setPixelRGBA(x, y, image.getRGB(x, y));
            }
        }

        // 创建纹理对象
        DynamicTexture texture = new DynamicTexture(nativeImage);
        int textureId = texture.getId();

        // 设置纹理过滤参数
        if (linear) {
            RenderSystem.bindTexture(textureId);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        return textureId;
    }
}