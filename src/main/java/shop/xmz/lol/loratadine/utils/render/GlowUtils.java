package shop.xmz.lol.loratadine.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import shop.xmz.lol.loratadine.utils.render.img.GaussianFilter;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class GlowUtils implements Wrapper {
    private static final HashMap<Integer, Integer> shadowCache = new HashMap<>();

    // 使用裁剪优化性能
    public static void drawGlow(PoseStack poseStack, float x, float y, float width, float height, int blurRadius, Color color, Runnable cutMethod) {
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        float _X = x - 0.25f;
        float _Y = y + 0.25f;

        int identifier = (int) (width * height + width + color.hashCode() * blurRadius + blurRadius);
        StencilUtil.write(false);
        cutMethod.run();
        StencilUtil.erase(false);

        // 在1.20.1中不需要显式启用纹理
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int texId;
        if (shadowCache.containsKey(identifier)) {
            texId = shadowCache.get(identifier);
            // 在1.20.1中使用setShaderTexture替代bindTexture
            RenderSystem.setShaderTexture(0, texId);
        } else {
            if (width <= 0) width = 1;
            if (height <= 0) height = 1;
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);

            Graphics g = original.getGraphics();
            g.setColor(color);
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();

            GaussianFilter op = new GaussianFilter(blurRadius);

            BufferedImage blurred = op.filter(original, null);

            texId = GLUtils.uploadTexture(blurred, true, true);

            shadowCache.put(identifier, texId);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // 设置着色器
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texId);

        // 获取BufferBuilder并开始渲染
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // 定义顶点和纹理坐标
        bufferBuilder.vertex(_X, _Y, 0).uv(0, 0).endVertex();               // 左上
        bufferBuilder.vertex(_X, _Y + height, 0).uv(0, 1).endVertex();      // 左下
        bufferBuilder.vertex(_X + width, _Y + height, 0).uv(1, 1).endVertex(); // 右下
        bufferBuilder.vertex(_X + width, _Y, 0).uv(1, 0).endVertex();       // 右上

        // 在1.20.1中使用BufferUploader.drawWithShader替代tesselator.end()
        BufferUploader.drawWithShader(bufferBuilder.end());

        // 在1.20.1中不需要显式启用纹理
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        StencilUtil.dispose();

        RenderSystem.enableCull();
        poseStack.popPose();
    }

    // 默认不使用裁剪优化性能
    public static void drawGlow(PoseStack poseStack, float x, float y, float width, float height, int blurRadius, Color color) {
        poseStack.pushPose();

        StencilUtil.initStencilToWrite();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        float _X = x - 0.25f;
        float _Y = y + 0.25f;

        int identifier = (int) (width * height + width + color.hashCode() * blurRadius + blurRadius);
        // 在1.20.1中不需要显式启用纹理
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        StencilUtil.readStencilBuffer(1);

        int texId;
        if (shadowCache.containsKey(identifier)) {
            texId = shadowCache.get(identifier);
            // 在1.20.1中使用setShaderTexture替代bindTexture
            RenderSystem.setShaderTexture(0, texId);
        } else {
            if (width <= 0) width = 1;
            if (height <= 0) height = 1;
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);

            Graphics g = original.getGraphics();
            g.setColor(color);
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();

            GaussianFilter op = new GaussianFilter(blurRadius);

            BufferedImage blurred = op.filter(original, null);

            texId = GLUtils.uploadTexture(blurred, true, true);

            shadowCache.put(identifier, texId);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // 设置着色器
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texId);

        // 获取BufferBuilder并开始渲染
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // 定义顶点和纹理坐标
        bufferBuilder.vertex(_X, _Y, 0).uv(0, 0).endVertex();               // 左上
        bufferBuilder.vertex(_X, _Y + height, 0).uv(0, 1).endVertex();      // 左下
        bufferBuilder.vertex(_X + width, _Y + height, 0).uv(1, 1).endVertex(); // 右下
        bufferBuilder.vertex(_X + width, _Y, 0).uv(1, 0).endVertex();       // 右上

        // 在1.20.1中使用BufferUploader.drawWithShader替代tesselator.end()
        BufferUploader.drawWithShader(bufferBuilder.end());

        // 在1.20.1中不需要显式启用纹理
        RenderSystem.disableBlend();
        StencilUtil.uninitStencilBuffer();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableCull();
        poseStack.popPose();
    }
}