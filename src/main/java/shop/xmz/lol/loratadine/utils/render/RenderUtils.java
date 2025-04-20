package shop.xmz.lol.loratadine.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.minecraft.client.Camera;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;
import java.util.UUID;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR;

/**
 *  @author DSJ / Cool / Jon_awa / AquaVase
 */
public class RenderUtils implements Wrapper {

    /**
     * GlScissor状态变化
     */
    public static void startGlScissor(int x, int y, int width, int height) {
        Window window = mc.getWindow();

        double guiScale = window.getGuiScale();
        int realX = (int) (x * guiScale);
        int realY = (int) (y * guiScale);
        int realWidth = (int) (width * guiScale);
        int realHeight = (int) (height * guiScale);
        int scissorY = window.getHeight() - (realY + realHeight);

        RenderSystem.enableScissor(realX, scissorY, realWidth, realHeight);
    }

    public static void stopGlScissor() {
        RenderSystem.disableScissor();
    }

    /**
     * 绘制玩家头像
     */
    public static void drawPlayerHead(PoseStack poseStack, float x, float y, float width, float height, AbstractClientPlayer player) {
        if (mc.player == null || mc.level == null) return;

        ResourceLocation skin = mc.player.getSkinTextureLocation();

        try {
            skin = player.getSkinTextureLocation();
        } catch (Exception e) {
            // empty
        }

        int hurtTime = player.hurtTime;
        float redTint = 1.0f;

        if (hurtTime > 0) {
            float progress = (float) hurtTime / 10.0f;
            progress = Math.min(progress, 1.0f);
            redTint = lerp(progress, 0.6f, 1.0f);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, skin);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, redTint, redTint, 1.0f);

        // 使用正确的 Minecraft 1.20.1 blit 方法
        blit(poseStack, (int)x, (int)y, (int)width, (int)height, 8, 8, 8, 8, 64, 64);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    // 添加辅助方法处理 blit
    private static void blit(PoseStack poseStack, int x, int y, int width, int height, int uOffset, int vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        float u0 = (float)uOffset / (float)textureWidth;
        float u1 = (float)(uOffset + uWidth) / (float)textureWidth;
        float v0 = (float)vOffset / (float)textureHeight;
        float v1 = (float)(vOffset + vHeight) / (float)textureHeight;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    /**
     * 线性插值函数：在 start 和 end 之间根据 progress 进行插值。
     * progress = 0 时返回 end，progress = 1 时返回 start，反向渐变。
     */
    public static float lerp(float progress, float start, float end) {
        return start + (end - start) * (1.0f - progress);
    }

    /**
     * 获取颜色组件
     */
    public static float[] getColorComponents(int color) {
        float alpha = (color >> 24 & 255) / 255.0F; // 提取 alpha 通道
        float red = (color >> 16 & 255) / 255.0F;   // 提取 red 通道
        float green = (color >> 8 & 255) / 255.0F;  // 提取 green 通道
        float blue = (color & 255) / 255.0F;        // 提取 blue 通道
        return new float[]{red, green, blue, alpha}; // 返回 RGBA 数组
    }

    public static void blitTextureWithColor(PoseStack poseStack, int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight, Color c, Color c1, Color c2, Color c3) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, x, y + height, 0).uv(u * f, (v + (float) height) * f1).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv((u + (float) width) * f, (v + (float) height) * f1).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv((u + (float) width) * f, v * f1).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
        builder.vertex(matrix, x, y, 0).uv(u * f, v * f1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();

        BufferUploader.drawWithShader(builder.end());
    }

    public static void fill(@NotNull PoseStack matrices, double x1, double y1, double x2, double y2, int color) {
        Matrix4f matrix = matrices.last().pose();
        double i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float f = (float) (color >> 24 & 0xFF) / 255.0f;
        float g = (float) (color >> 16 & 0xFF) / 255.0f;
        float h = (float) (color >> 8 & 0xFF) / 255.0f;
        float j = (float) (color & 0xFF) / 255.0f;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0f).color(g, h, j, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0f).color(g, h, j, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0f).color(g, h, j, f).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0f).color(g, h, j, f).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * 绘制 OutLine 边框
     */
    public static void renderEntityOutline(
            PoseStack poseStack,
            LivingEntity entity,
            int color,
            float lineWidth
    ) {
        // 获取包围盒尺寸
        AABB bb = entity.getBoundingBox()
                .move(-entity.getX(), -entity.getY(), -entity.getZ()) // 转换为相对坐标
                .inflate(0.1); // 稍微扩大避免Z-fighting

        // 配置GL状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 开始绘制
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 应用实体位置变换
        poseStack.pushPose();
        poseStack.translate(entity.getX(), entity.getY(), entity.getZ());

        // 绘制轮廓（使用包围盒的8个顶点）
        buildOutlineVertices(bb, buffer, poseStack.last().pose(), r, g, b, a);

        // 结束绘制
        BufferUploader.drawWithShader(buffer.end());
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static void buildOutlineVertices(
            AABB bb,
            BufferBuilder buffer,
            Matrix4f matrix,
            float r, float g, float b, float a
    ) {
        // 底部四边形
        addLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.minZ, r, g, b, a);
        addLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, r, g, b, a);
        addLine(buffer, matrix, bb.maxX, bb.minY, bb.maxZ, bb.minX, bb.minY, bb.maxZ, r, g, b, a);
        addLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.minY, bb.minZ, r, g, b, a);

        // 顶部四边形
        addLine(buffer, matrix, bb.minX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(buffer, matrix, bb.maxX, bb.maxY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, r, g, b, a);
        addLine(buffer, matrix, bb.maxX, bb.maxY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, r, g, b, a);
        addLine(buffer, matrix, bb.minX, bb.maxY, bb.maxZ, bb.minX, bb.maxY, bb.minZ, r, g, b, a);

        // 垂直边
        addLine(buffer, matrix, bb.minX, bb.minY, bb.minZ, bb.minX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(buffer, matrix, bb.maxX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.minZ, r, g, b, a);
        addLine(buffer, matrix, bb.maxX, bb.minY, bb.maxZ, bb.maxX, bb.maxY, bb.maxZ, r, g, b, a);
        addLine(buffer, matrix, bb.minX, bb.minY, bb.maxZ, bb.minX, bb.maxY, bb.maxZ, r, g, b, a);
    }

    private static void addLine(
            BufferBuilder buffer,
            Matrix4f matrix,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a).endVertex();
    }

    /**
     * 绘制未填充的边框
     */
    static void drawBoundingBox(PoseStack ps,
                                float minX, float maxX,
                                float minY, float maxY,
                                float minZ, float maxZ,
                                int color1, int color2) {
        Matrix4f matrix = ps.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // 配置线条渲染
        RenderSystem.lineWidth(2.5f); // 更清晰的线宽

        // 绘制立方体12条边
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // 底部四边形（深色）
        addLine(matrix, buffer, minX, minY, minZ, maxX, minY, minZ, color2);
        addLine(matrix, buffer, maxX, minY, minZ, maxX, minY, maxZ, color2);
        addLine(matrix, buffer, maxX, minY, maxZ, minX, minY, maxZ, color2);
        addLine(matrix, buffer, minX, minY, maxZ, minX, minY, minZ, color2);

        // 顶部四边形（亮色）
        addLine(matrix, buffer, minX, maxY, minZ, maxX, maxY, minZ, color1);
        addLine(matrix, buffer, maxX, maxY, minZ, maxX, maxY, maxZ, color1);
        addLine(matrix, buffer, maxX, maxY, maxZ, minX, maxY, maxZ, color1);
        addLine(matrix, buffer, minX, maxY, maxZ, minX, maxY, minZ, color1);

        // 垂直连接线（渐变）
        addLine(matrix, buffer, minX, minY, minZ, minX, maxY, minZ, color1);
        addLine(matrix, buffer, maxX, minY, minZ, maxX, maxY, minZ, color1);
        addLine(matrix, buffer, maxX, minY, maxZ, maxX, maxY, maxZ, color1);
        addLine(matrix, buffer, minX, minY, maxZ, minX, maxY, maxZ, color1);

        // 提交绘制
        BufferUploader.drawWithShader(buffer.end());
    }

    /**
     * 绘制填充的边框
     */
    static void drawFilledBoundingBox(PoseStack ps,
                                      float minX, float maxX,
                                      float minY, float maxY,
                                      float minZ, float maxZ,
                                      int color1, int color2,
                                      float alpha) {
        Matrix4f matrix = ps.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // 禁用面剔除，操你妈弱智Minecraft，默认打开你老妈子面剔除，要给你老妈子从逼里面刮出来是不是？
        RenderSystem.disableCull();

        // 配置填充渲染
        RenderSystem.lineWidth(1.0f);

        // 开始绘制立方体6个面
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // 前面 (Front face)
        addQuad(matrix, buffer,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                minX, maxY, minZ, applyAlpha(color1, alpha));

        // 后面 (Back face)
        addQuad(matrix, buffer,
                minX, minY, maxZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, minY, maxZ, applyAlpha(color2, alpha));

        // 左面 (Left face)
        addQuad(matrix, buffer,
                minX, minY, minZ,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                minX, minY, maxZ, applyAlpha(color1, alpha));

        // 右面 (Right face)
        addQuad(matrix, buffer,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ, applyAlpha(color2, alpha));

        // 底面 (Bottom face)
        addQuad(matrix, buffer,
                minX, minY, minZ,
                minX, minY, maxZ,
                maxX, minY, maxZ,
                maxX, minY, minZ, applyAlpha(color1, alpha));

        // 顶面 (Top face)
        addQuad(matrix, buffer,
                minX, maxY, minZ,
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ, applyAlpha(color2, alpha));

        // 提交绘制
        BufferUploader.drawWithShader(buffer.end());

        // 重新启用面剔除，恢复默认设置
        RenderSystem.enableCull();
    }


    private static void addLine(Matrix4f matrix, BufferBuilder buffer,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                int color) {
        buffer.vertex(matrix, x1, y1, z1)
                .color(color)
                .endVertex();
        buffer.vertex(matrix, x2, y2, z2)
                .color(color)
                .endVertex();
    }

    private static void addQuad(Matrix4f matrix, BufferBuilder buffer,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float x4, float y4, float z4,
                                int color) {
        buffer.vertex(matrix, x1, y1, z1)
                .color(color)
                .endVertex();
        buffer.vertex(matrix, x2, y2, z2)
                .color(color)
                .endVertex();
        buffer.vertex(matrix, x3, y3, z3)
                .color(color)
                .endVertex();
        buffer.vertex(matrix, x4, y4, z4)
                .color(color)
                .endVertex();
    }

    private static int applyAlpha(int color, float alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     *  绘制 3D BlockESP
     */
    public static void render3DBlockBoundingBox(PoseStack poseStack, BlockEntity blockEntity, int color, boolean fillMode, boolean wireframeMode, float fillAlpha) {
        if (blockEntity == null) return;

        // 获取方块状态和位置
        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = blockEntity.getBlockState();
        VoxelShape shape = state.getShape(mc.level, pos);

        // 如果当前方块没有碰撞箱则跳过（如空气）
        if (shape.isEmpty()) return;

        // 获取相机位置
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        // 转换方块位置为相机相对坐标
        double x = pos.getX() - camPos.x;
        double y = pos.getY() - camPos.y;
        double z = pos.getZ() - camPos.z;

        // 矩阵变换
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // 获取方块颜色
        int blockColor = color;

        // 处理多部分碰撞箱（如上下半的箱子）
        for (AABB aabb : shape.toAabbs()) {
            // 计算相对坐标
            float minX = (float) aabb.minX;
            float maxX = (float) aabb.maxX;
            float minY = (float) aabb.minY;
            float maxY = (float) aabb.maxY;
            float minZ = (float) aabb.minZ;
            float maxZ = (float) aabb.maxZ;

            // 启用3D渲染状态
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            // 填充模式
            if (fillMode) {
                drawFilledBoundingBox(poseStack, minX, maxX, minY, maxY, minZ, maxZ, blockColor, blockColor, fillAlpha);
            }

            // 线框模式
            if (wireframeMode) {
                RenderSystem.disableDepthTest();
                RenderSystem.defaultBlendFunc();
                drawBoundingBox(poseStack, minX, maxX, minY, maxY, minZ, maxZ, blockColor, darkenColor(blockColor));
            }
        }

        // 恢复状态
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    // 颜色加深工具方法
    private static int darkenColor(int color) {
        return (color & 0xFF000000) |
                ((Math.max((color >> 16) & 0xFF - 40, 0) << 16)) |
                ((Math.max((color >> 8) & 0xFF - 40, 0) << 8)) |
                (Math.max(color & 0xFF - 40, 0));
    }

    /**
     * 绘制方块人模型
     */
    public static void drawModel(PoseStack poseStack, float yaw, float pitch, LivingEntity target) {
        Lighting.setupFor3DItems();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        float originalYaw = target.getYRot();
        float originalPitch = target.getXRot();
        float originalBodyYaw = target.yBodyRot;

        target.yBodyRot = yaw - 0.4f;
        target.setYRot(yaw - 0.2f);
        target.setXRot(pitch);

        poseStack.pushPose();
        // 使用正确的旋转方式
        poseStack.mulPose(new org.joml.Quaternionf().rotationZ((float)Math.toRadians(180F)));
        poseStack.scale(-50, 50, 50);

        mc.getEntityRenderDispatcher().render(
                target,
                0.0D, 0.0D, 0.0D,
                0.0F,
                1.0F,
                poseStack,
                buffer,
                15728880
        );

        buffer.endBatch();
        poseStack.popPose();

        target.setYRot(originalYaw);
        target.setXRot(originalPitch);
        target.yBodyRot = originalBodyYaw;

        Lighting.setupForFlatItems();
    }

    /**
     * 绘制 GUI 物品图标
     */
    public static void renderItemIcon(PoseStack poseStack, double x, double y, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            renderGuiItem(poseStack, itemStack, (int) x, (int) y);
        }
    }

    public static void renderGuiItem(PoseStack poseStack, ItemStack itemStack, int x, int y, BakedModel model) {
        mc.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pushPose();

        // 使用固定的 Z 偏移值 (100.0F) 替代 blitOffset
        poseStack.translate(x, y, 100.0F);
        poseStack.translate(8.0, 8.0, 0.0);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        // 在1.20.1中使用 ItemDisplayContext.GUI
        mc.getItemRenderer().render(itemStack, ItemDisplayContext.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderGuiItem(PoseStack poseStack, ItemStack itemStack, int x, int y) {
        renderGuiItem(poseStack, itemStack, x, y, mc.getItemRenderer().getModel(itemStack, null, null, 0));
    }

    public static void drawSmoothCircle(GuiGraphics guiGraphics, double x, double y, float radius, int color) {
        // 颜色分量提取
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        // 渲染状态设置
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        // 尺寸计算
        int diameter = (int) Math.ceil(radius * 2);
        int left = (int) (x - radius);
        int top = (int) (y - radius);

        // 创建抗锯齿纹理
        NativeImage circleImage = createSmoothCircleImage(diameter, color);
        DynamicTexture circleTexture = new DynamicTexture(circleImage);
        ResourceLocation textureId = new ResourceLocation("your_modid", "dynamic/circle_" + UUID.randomUUID());

        // 纹理注册和绑定
        mc.getTextureManager().register(textureId, circleTexture);
        RenderSystem.setShaderTexture(0, textureId);

        // 关键设置：启用线性过滤
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // 使用新版 GuiGraphics 绘制
        guiGraphics.blit(
                textureId,
                left, top,           // 屏幕坐标
                0, 0,                // 纹理起始UV
                diameter, diameter,  // 绘制尺寸
                diameter, diameter   // 纹理尺寸
        );

        // 清理资源
        mc.getTextureManager().release(textureId);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static NativeImage createSmoothCircleImage(int diameter, int color) {
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, diameter, diameter, false);
        final float center = diameter / 2f - 0.5f; // 精确到像素中心
        final float radius = diameter / 2f;
        final float feather = Math.max(1.5f, radius * 0.1f); // 动态过渡区域

        final int baseAlpha = (color >> 24) & 0xFF;
        final int rgb = color & 0x00FFFFFF;

        for (int y = 0; y < diameter; y++) {
            for (int x = 0; x < diameter; x++) {
                // 亚像素精度距离计算
                float dx = x - center;
                float dy = y - center;
                float distance = Mth.sqrt(dx*dx + dy*dy);

                // 抗锯齿边缘计算
                float alpha = Mth.clamp((radius - distance) / feather + 0.5f, 0, 1);
                int finalAlpha = (int)(baseAlpha * alpha);

                image.setPixelRGBA(x, y, (finalAlpha << 24) | rgb);
            }
        }
        return image;
    }


    /**
     * 绘制现代圆角矩形
     */
    public static void drawRoundedRect(PoseStack poseStack, double x, double y, double width, double height, double radius, Color color) {
        renderRoundedQuad(poseStack, color, x, y, x + width, y + height, radius, 128); // 进一步增加采样数
    }

    public static void renderRoundedQuad(PoseStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrices.last().pose(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        if (toX - fromX < radius) {
            toX = fromX + radius;
        }

        if (toY - fromY < radius) {
            toY = fromY + radius;
        }

        double[][] map = new double[][]{
                new double[]{toX - radius, toY - radius, radius},
                new double[]{toX - radius, fromY + radius, radius},
                new double[]{fromX + radius, fromY + radius, radius},
                new double[]{fromX + radius, toY - radius, radius}
        };

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];

            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);

                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
            }

            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);

            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * 绘制 NameTag
     */
    public static void renderNameTagRoundedQuad(@NotNull PoseStack matrices, double fromX, double fromY, double toX, double toY, double rad, double samples, @NotNull Color c) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.last().pose();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad, samples);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * 绘制现代渐变圆角矩形
     */
    public static void drawGradientRoundedRect(PoseStack poseStack, double x, double y, double width, double height, double radius, Color color, Color color2) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        drawGradientRoundedQuad(poseStack.last().pose(), color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f, color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f, x, y, x + width, y + height, radius, 32);
        endRender();
    }

    /**
     * 绘制原版渐变圆角矩形
     */
    public static void drawGradientRoundedQuad(Matrix4f matrix, float cr, float cg, float cb, float ca, float cr2, float cg2, float cb2, float ca2, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        if (toX - fromX < radius) {
            toX = fromX + radius;
        }

        if (toY - fromY < radius) {
            toY = fromY + radius;
        }

        double[][] map = new double[][]{
                new double[]{toX - radius, toY - radius, radius},
                new double[]{toX - radius, fromY + radius, radius},
                new double[]{fromX + radius, fromY + radius, radius},
                new double[]{fromX + radius, toY - radius, radius}
        };

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];

            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);

                if ((float) current[0] + sin < toX - (toX - fromX) / 2) {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
                } else {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2).endVertex();
                }
            }

            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);

            if ((float) current[0] + sin < toX - (toX - fromX) / 2) {
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
            } else {
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2).endVertex();
            }
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    /**
     * 绘制原版矩形
     */
    public static void drawRect(PoseStack poseStack, int left, int top, int right, int bottom, int color) {
        int j;
        if (left < right) {
            j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        bufferBuilder.vertex(matrix, (float) right, (float) top, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        bufferBuilder.vertex(matrix, (float) left, (float) top, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    /**
     * 绘制图片
     */
    public static void drawImage(PoseStack poseStack, ResourceLocation texture, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight, float alpha) {
        // 绑定纹理
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        // 保存当前状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 设置透明度
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        // 设置纹理参数以避免模糊
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // 使用我们自己的 blit 方法
        blitWithUV(poseStack, x, y, width, height, u, v, uWidth, vHeight);

        // 恢复以前的状态
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void blitWithUV(PoseStack poseStack, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        float u1 = u / uWidth;
        float v1 = v / vHeight;
        float u2 = (u + width) / uWidth;
        float v2 = (v + height) / vHeight;

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(u1, v2).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(u2, v2).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(u2, v1).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(u1, v1).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    // 保留原来的方法，但内部调用新方法，默认不透明
    public static void drawImage(PoseStack poseStack, ResourceLocation texture, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight) {
        drawImage(poseStack, texture, x, y, width, height, u, v, uWidth, vHeight, 1.0F);
    }


    /**
     * 绘制顶部线条，包含主矩形和顶部的两条矩形
     */
    public static void drawTopLine(PoseStack poseStack, float x, float y, float width, float height, float radius, int color, int topColor) {
        // 绘制主矩形
        drawRoundedRectangle(poseStack, x, y, width, height, radius, color, -1);
        // 绘制顶部矩形
        drawRoundedRectangle(poseStack, x, y, width, 2, radius, topColor, -1);
        // 绘制顶部两条边缘矩形
        drawRoundedRectangle(poseStack, x, y + radius, width, 2 - radius, radius, topColor, -1);
    }

    /**
     * 绘制矩形
     */
    public static void drawRectangle(PoseStack poseStack, float x, float y, float width, float height, int color) {
        float endX = x + width;
        float endY = y + height;

        drawQuads(poseStack, x, endY, endX, endY, endX, y, x, y, color);
    }

    /**
     * 绘制直角梯形
     */
    public static void drawRightTrapezoid(PoseStack poseStack, float x, float y, float width, float height, float offset, boolean down, int color) {
        float endX = x + width;
        float endY = y + height;

        drawQuads(poseStack, x, endY, endX + (down ? offset : 0), endY, endX + (down ? 0 : offset), y, x, y, color);
    }

    /**
     * 绘制平行四边形
     */
    public static void drawParallelogram(PoseStack poseStack, float x, float y, float width, float height, float offset, int color) {
        float endX = x + width;
        float endY = y + height;

        drawQuads(poseStack, x + offset, endY, endX + offset, endY, endX, y, x, y, color);
    }

    /**
     * 绘制渐变平行四边形
     */
    public static void drawGradientParallelogram(PoseStack poseStack, float x, float y, float width, float height, float offset, Color color, Color color2) {
        float endY = y + height;

        for (int j = 0; j < width + 1; ++j) {
            int r = (int) (color.getRed() - ((color.getRed() - color2.getRed()) / width) * j);
            int g = (int) (color.getGreen() - ((color.getGreen() - color2.getGreen()) / width) * j);
            int b = (int) (color.getBlue() - ((color.getBlue() - color2.getBlue()) / width) * j);

            drawQuads(poseStack, x + j + offset, endY, x + j + offset + 1, endY, x + j + 1, y, x + j, y, new Color(r, g, b, 255).getRGB());
        }
    }

    /**
     * 绘制等腰梯形
     */
    public static void drawIsoscelesTrapezoid(PoseStack poseStack, float x, float y, float width, float height, float offset, int color) {
        float endX = x + width;
        float endY = y + height;

        drawQuads(poseStack, x + offset, endY, endX - offset, endY, endX, y, x, y, color);
    }

    /**
     * 绘制四边形
     */
    private static void drawQuads(PoseStack poseStack, float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, int color) {
        // 获取颜色的 RGBA 分量
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;

        // 启用混合，设置颜色和着色器
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        // 获取 BufferBuilder 用于绘制
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // 开始绘制矩形
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x3, y3, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x4, y4, 0.0F).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        // 禁用混合
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    /**
     * 绘制圆形
     */
    public static void drawCircle(PoseStack poseStack, float x, float y, float r, int startAngle, int endAngle, int color) {
        // 获取颜色的 RGBA 分量
        float[] rgba = getColorComponents(color);
        float red = rgba[0], green = rgba[1], blue = rgba[2], alpha = rgba[3];

        // 启用混合，设置颜色和着色器
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        // 获取 BufferBuilder 用于绘制
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // 开始绘制圆形
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(red, green, blue, alpha).endVertex(); // 圆心

        // 绘制圆弧部分
        for (int i = startAngle; i <= endAngle; i++) {
            float angle = (float) (i * Math.PI / 180.0); // 角度转弧度
            float dx = (float) (Math.cos(angle) * r); // 计算 x 偏移量
            float dy = (float) (Math.sin(angle) * r); // 计算 y 偏移量
            bufferBuilder.vertex(matrix, x + dx, y + dy, 0.0F).color(red, green, blue, alpha).endVertex(); // 绘制圆弧上的每个点
        }
        BufferUploader.drawWithShader(bufferBuilder.end());

        // 禁用混合
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    /**
     * 旧的圆角矩形绘制
     */
    public static void drawRoundedRectangle(PoseStack poseStack, float x, float y, float w, float h, float radius, int color, int index) {
        // 根据 index 值选择绘制的样式
        switch (index) {
            case -1:
                drawRectangle(poseStack, x, y, w, h, color); // 普通矩形
                break;
            case 0:
                // 绘制包含四个圆角的矩形
                drawRectangle(poseStack, x + radius, y + radius, w - radius * 2, h - radius * 2, color);
                drawRectangle(poseStack, x + radius, y, w - radius * 2, radius, color);
                drawRectangle(poseStack, x + w - radius, y + radius, radius, h - radius * 2, color);
                drawRectangle(poseStack, x + radius, y + h - radius, w - radius * 2, radius, color);
                drawRectangle(poseStack, x, y + radius, radius, h - radius * 2, color);
                drawCircle(poseStack, x + radius, y + radius, radius, 180, 270, color);
                drawCircle(poseStack, x + w - radius, y + radius, radius, 270, 360, color);
                drawCircle(poseStack, x + radius, y + h - radius, radius, 90, 180, color);
                drawCircle(poseStack, x + w - radius, y + h - radius, radius, 0, 90, color);
                break;
            case 1:
                // 绘制顶部和圆角矩形
                drawRectangle(poseStack, x + radius, y, w - radius * 2, radius, color);
                drawRectangle(poseStack, x, y + radius, w, h - radius, color);
                drawCircle(poseStack, x + radius, y + radius, radius, 180, 270, color);
                drawCircle(poseStack, x + w - radius, y + radius, radius, 270, 360, color);
                break;
            case 2:
                // 绘制底部圆角矩形
                drawRectangle(poseStack, x, y, w, h - radius, color);
                drawRectangle(poseStack, x + radius, y + h - radius, w - radius * 2, radius, color);
                drawCircle(poseStack, x + radius, y + h - radius, radius, 90, 180, color);
                drawCircle(poseStack, x + w - radius, y + h - radius, radius, 0, 90, color);
                break;
            case 3:
                // 绘制底部圆角矩形（无顶部圆角）
                drawRectangle(poseStack, x, y, w, h - radius, color);
                drawRectangle(poseStack, x + radius, y + h - radius, w - radius * 2, radius, color);
                break;
            default:
                // 默认情况下不做任何处理，可以选择记录日志或进行异常处理
                break;
        }
    }

    /**
     * 绘制渐变矩形（从左到右）
     */
    public static void drawGradientRectL2R(PoseStack poseStack, float x, float y, float width, float height, int startColor, int endColor) {
        drawGradientRect2(poseStack, x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawGradientRect2(PoseStack poseStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 提取颜色的 RGBA 分量
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;

        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        // 绘制渐变矩形（从左到右）
        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);

        // 左上角（使用 startColor）
        bufferBuilder.vertex(matrix, left, top, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();

        // 左下角（使用 startColor）
        bufferBuilder.vertex(matrix, left, bottom, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();

        // 右下角（使用 endColor）
        bufferBuilder.vertex(matrix, right, bottom, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();

        // 右上角（使用 endColor）
        bufferBuilder.vertex(matrix, right, top, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }


    /**
     * 绘制渐变矩形（从上到下）
     */
    public static void drawGradientRectU2D(PoseStack poseStack, float x, float y, float width, float height, int startColor, int endColor) {
        drawGradientRect(poseStack, x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawGradientRect(PoseStack poseStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;

        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);
        bufferBuilder.vertex(matrix, right, top, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        bufferBuilder.vertex(matrix, left, top, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        bufferBuilder.vertex(matrix, left, bottom, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        bufferBuilder.vertex(matrix, right, bottom, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    /**
     * 绘制频谱条
     */
    public static void renderSpectrumBar(PoseStack poseStack, float x, float y, float width, float height, float[] spectrumData, Color baseColor) {
        if (spectrumData == null || spectrumData.length == 0) return;

        int barCount = spectrumData.length;
        float barWidth = width / barCount;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < barCount; i++) {
            float barHeight =
                    spectrumData[i] * height;
            // Math.max(spectrumData[i] * height, 1); // 确保最小高度为 1
            float barX = x + i * barWidth;
            float barY = y + height - barHeight;

            Color barColor = new Color(
                    baseColor.getRed(),
                    baseColor.getGreen(),
                    baseColor.getBlue(),
                    Math.min(255, (int) (baseColor.getAlpha() * spectrumData[i] + 50))
            );

            addQuad(bufferBuilder, poseStack, barX, barY, barWidth, barHeight, barColor);
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    private static void addQuad(BufferBuilder bufferBuilder, PoseStack poseStack, float x, float y, float width, float height, Color color) {
        Matrix4f matrix = poseStack.last().pose();
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;

        bufferBuilder.vertex(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
    }

    /**
     * 绘制字符串
     */
    public static void drawMcFontOutlinedString(PoseStack poseStack, String text, float x, float y, int color, boolean shadow) {
        int shadowColor = (color & 16579836) >> 2 | color & -16777216;

        // 获取关键对象引用
        net.minecraft.client.gui.Font font = mc.font;
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // 绘制轮廓 (四个方向)
        font.drawInBatch(text, x + 1, y, shadowColor, false, matrix, bufferSource,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(text, x - 1, y, shadowColor, false, matrix, bufferSource,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(text, x, y + 1, shadowColor, false, matrix, bufferSource,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(text, x, y - 1, shadowColor, false, matrix, bufferSource,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);

        // 绘制主文本
        font.drawInBatch(text, x, y, color, shadow, matrix, bufferSource,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);

        // 将所有文本刷新到屏幕
        bufferSource.endBatch();
    }

    // 在你的RenderUtils类中添加这些方法

    /**
     * 绘制圆形
     */
    public static void drawCircle(PoseStack poseStack, float x, float y, float radius, int color) {
        // 通过近似多边形绘制圆形
        int segments = Math.max(10, (int)(radius * 2));
        float theta = (float) (2 * Math.PI / segments);
        float tangetial_factor = (float) Math.tan(theta);
        float radial_factor = (float) Math.cos(theta);

        float xx = radius;
        float yy = 0;

        // 绘制多边形
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // 中心点
        int a = (color >> 24 & 0xFF);
        int r = (color >> 16 & 0xFF);
        int g = (color >> 8 & 0xFF);
        int b = (color & 0xFF);

        bufferBuilder.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();

        // 添加所有外围点
        for (int i = 0; i <= segments; i++) {
            bufferBuilder.vertex(matrix, x + xx, y + yy, 0).color(r, g, b, a).endVertex();

            // 应用逐步旋转
            float tx = -yy;
            float ty = xx;

            xx += tx * tangetial_factor;
            yy += ty * tangetial_factor;

            xx *= radial_factor;
            yy *= radial_factor;
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}