package shop.xmz.lol.loratadine.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;

import java.awt.*;
import java.lang.Math;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class ESPUtils {
    /**
     * Renders a clean line outline around an entity.
     * This method creates a simple line-based outline that highlights the entity's boundaries.
     *
     * @param poseStack The pose stack for transformations
     * @param entity The entity to render the outline around
     * @param color The color of the outline
     * @param lineWidth The width of the outline lines (recommended: 1.0f to 3.0f)
     * @param throughWalls Whether the outline should be visible through walls
     */
    /**
     * 修复标准实体轮廓渲染方法，移除了对不存在的RenderSystem.getDepthTest()的调用
     */
    public static void renderEntityOutlineESP(
            PoseStack poseStack,
            Entity entity,
            int color,
            float lineWidth,
            boolean throughWalls
    ) {
        if (entity == null) return;

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks - camera.getPosition().x();
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks - camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks - camera.getPosition().z();

        // 获取实体边界框
        AABB bb = entity.getBoundingBox()
                .move(-entity.getX(), -entity.getY(), -entity.getZ()); // 转换为相对坐标

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            // 在Minecraft 1.20.1中，我们无法直接检查深度测试状态
            // 所以我们假设它已启用，并在必要时禁用它
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 绘制轮廓
        drawOutlineBox(poseStack, bb, r, g, b, a);

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * Draws the outline of a bounding box using lines.
     */
    private static void drawOutlineBox(
            PoseStack poseStack,
            AABB bb,
            float r, float g, float b, float a
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // Draw the 12 edges of the box using DEBUG_LINES
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Bottom face edges
        buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a).endVertex();

        // Top face edges
        buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a).endVertex();

        // Vertical edges
        buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buffer.end());
    }

    /**
     * 修复的虚线轮廓渲染方法
     */
    public static void renderEntityStyledOutline(
            PoseStack poseStack,
            Entity entity,
            int color,
            float lineWidth,
            float dashLength,
            float gapLength,
            boolean throughWalls
    ) {
        if (entity == null) return;

        // 如果不使用虚线，使用常规轮廓方法
        if (dashLength <= 0 || gapLength <= 0) {
            renderEntityOutlineESP(poseStack, entity, color, lineWidth, throughWalls);
            return;
        }

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks - camera.getPosition().x();
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks - camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks - camera.getPosition().z();

        // 获取实体边界框
        AABB bb = entity.getBoundingBox()
                .move(-entity.getX(), -entity.getY(), -entity.getZ()); // 转换为相对坐标

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 绘制虚线轮廓
        drawDashedOutlineBox(poseStack, bb, r, g, b, a, dashLength, gapLength);

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * Draws the outline of a bounding box using dashed lines.
     */
    private static void drawDashedOutlineBox(
            PoseStack poseStack,
            AABB bb,
            float r, float g, float b, float a,
            float dashLength,
            float gapLength
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // Define the 12 lines of the box
        Vector3f[][] lines = new Vector3f[][] {
                // Bottom face
                {new Vector3f((float)bb.minX, (float)bb.minY, (float)bb.minZ), new Vector3f((float)bb.maxX, (float)bb.minY, (float)bb.minZ)},
                {new Vector3f((float)bb.maxX, (float)bb.minY, (float)bb.minZ), new Vector3f((float)bb.maxX, (float)bb.minY, (float)bb.maxZ)},
                {new Vector3f((float)bb.maxX, (float)bb.minY, (float)bb.maxZ), new Vector3f((float)bb.minX, (float)bb.minY, (float)bb.maxZ)},
                {new Vector3f((float)bb.minX, (float)bb.minY, (float)bb.maxZ), new Vector3f((float)bb.minX, (float)bb.minY, (float)bb.minZ)},

                // Top face
                {new Vector3f((float)bb.minX, (float)bb.maxY, (float)bb.minZ), new Vector3f((float)bb.maxX, (float)bb.maxY, (float)bb.minZ)},
                {new Vector3f((float)bb.maxX, (float)bb.maxY, (float)bb.minZ), new Vector3f((float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)},
                {new Vector3f((float)bb.maxX, (float)bb.maxY, (float)bb.maxZ), new Vector3f((float)bb.minX, (float)bb.maxY, (float)bb.maxZ)},
                {new Vector3f((float)bb.minX, (float)bb.maxY, (float)bb.maxZ), new Vector3f((float)bb.minX, (float)bb.maxY, (float)bb.minZ)},

                // Vertical edges
                {new Vector3f((float)bb.minX, (float)bb.minY, (float)bb.minZ), new Vector3f((float)bb.minX, (float)bb.maxY, (float)bb.minZ)},
                {new Vector3f((float)bb.maxX, (float)bb.minY, (float)bb.minZ), new Vector3f((float)bb.maxX, (float)bb.maxY, (float)bb.minZ)},
                {new Vector3f((float)bb.maxX, (float)bb.minY, (float)bb.maxZ), new Vector3f((float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)},
                {new Vector3f((float)bb.minX, (float)bb.minY, (float)bb.maxZ), new Vector3f((float)bb.minX, (float)bb.maxY, (float)bb.maxZ)}
        };

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Draw each line with dashes
        for (Vector3f[] line : lines) {
            Vector3f start = line[0];
            Vector3f end = line[1];

            // Calculate line direction and length
            Vector3f dir = new Vector3f(end).sub(start);
            float length = dir.length();
            dir.normalize();

            // Draw the dashed line
            float dashStart = 0;
            while (dashStart < length) {
                float dashEnd = Math.min(dashStart + dashLength, length);

                // Calculate the actual dash vertices
                Vector3f dashStartPos = new Vector3f(dir).mul(dashStart).add(start);
                Vector3f dashEndPos = new Vector3f(dir).mul(dashEnd).add(start);

                // Add the dash to the buffer
                buffer.vertex(matrix, dashStartPos.x(), dashStartPos.y(), dashStartPos.z()).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, dashEndPos.x(), dashEndPos.y(), dashEndPos.z()).color(r, g, b, a).endVertex();

                // Move to the next dash
                dashStart = dashEnd + gapLength;
            }
        }

        BufferUploader.drawWithShader(buffer.end());
    }


    /**
     * 修复的玩家详细轮廓渲染方法
     */
    public static void renderPlayerDetailedOutline(
            PoseStack poseStack,
            Player player,
            int color,
            float lineWidth,
            boolean throughWalls
    ) {
        if (player == null) return;

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = player.xOld + (player.getX() - player.xOld) * partialTicks - camera.getPosition().x();
        double y = player.yOld + (player.getY() - player.yOld) * partialTicks - camera.getPosition().y();
        double z = player.zOld + (player.getZ() - player.zOld) * partialTicks - camera.getPosition().z();

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 检查玩家受伤状态以调整颜色
        if (player.hurtTime > 0) {
            float hurtProgress = (float) player.hurtTime / 10.0f;
            g *= (1.0f - hurtProgress * 0.8f);
            b *= (1.0f - hurtProgress * 0.8f);
            r = Math.min(1.0f, r + (1.0f - r) * hurtProgress * 0.8f);
        }

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // 应用玩家旋转 - 转换为弧度并绕Y轴旋转
        float yaw = (float) Math.toRadians(-player.getYRot());
        poseStack.mulPose(new Quaternionf().rotationY(yaw));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 绘制玩家模型轮廓
        drawPlayerModelOutline(poseStack, r, g, b, a, player.isCrouching());

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
    /**
     * Draws a detailed player model outline.
     */
    private static void drawPlayerModelOutline(
            PoseStack poseStack,
            float r, float g, float b, float a,
            boolean isCrouching
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Define player body points
        Vector3f head = new Vector3f(0, isCrouching ? 1.5f : 1.62f, 0);
        Vector3f neck = new Vector3f(0, isCrouching ? 1.35f : 1.42f, 0);
        Vector3f body = new Vector3f(0, isCrouching ? 0.95f : 0.85f, 0);
        Vector3f leftShoulder = new Vector3f(0.18f, isCrouching ? 1.35f : 1.42f, 0);
        Vector3f rightShoulder = new Vector3f(-0.18f, isCrouching ? 1.35f : 1.42f, 0);
        Vector3f leftElbow = new Vector3f(0.28f, isCrouching ? 1.15f : 1.2f, 0.05f);
        Vector3f rightElbow = new Vector3f(-0.28f, isCrouching ? 1.15f : 1.2f, 0.05f);
        Vector3f leftHand = new Vector3f(0.35f, isCrouching ? 0.95f : 0.95f, 0.1f);
        Vector3f rightHand = new Vector3f(-0.35f, isCrouching ? 0.95f : 0.95f, 0.1f);
        Vector3f pelvis = new Vector3f(0, isCrouching ? 0.75f : 0.75f, 0);
        Vector3f leftHip = new Vector3f(0.1f, isCrouching ? 0.75f : 0.75f, 0);
        Vector3f rightHip = new Vector3f(-0.1f, isCrouching ? 0.75f : 0.75f, 0);
        Vector3f leftKnee = new Vector3f(0.13f, isCrouching ? 0.5f : 0.4f, 0.05f);
        Vector3f rightKnee = new Vector3f(-0.13f, isCrouching ? 0.5f : 0.4f, 0.05f);
        Vector3f leftFoot = new Vector3f(0.13f, isCrouching ? 0.15f : 0.05f, 0.1f);
        Vector3f rightFoot = new Vector3f(-0.13f, isCrouching ? 0.15f : 0.05f, 0.1f);

        // Head outline
        drawCircleFilled(buffer, matrix, head, 0.18f, 0.18f, 0.18f, r, g, b, a, 0.8f);

        // Connect head to body
        buffer.vertex(matrix, head.x(), head.y() - 0.18f, head.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();

        // Draw spine
        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, body.x(), body.y(), body.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, body.x(), body.y(), body.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, pelvis.x(), pelvis.y(), pelvis.z()).color(r, g, b, a).endVertex();

        // Draw shoulders
        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftShoulder.x(), leftShoulder.y(), leftShoulder.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightShoulder.x(), rightShoulder.y(), rightShoulder.z()).color(r, g, b, a).endVertex();

        // Draw arms
        buffer.vertex(matrix, leftShoulder.x(), leftShoulder.y(), leftShoulder.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftElbow.x(), leftElbow.y(), leftElbow.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, leftElbow.x(), leftElbow.y(), leftElbow.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftHand.x(), leftHand.y(), leftHand.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightShoulder.x(), rightShoulder.y(), rightShoulder.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightElbow.x(), rightElbow.y(), rightElbow.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightElbow.x(), rightElbow.y(), rightElbow.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightHand.x(), rightHand.y(), rightHand.z()).color(r, g, b, a).endVertex();

        // Draw hips
        buffer.vertex(matrix, pelvis.x(), pelvis.y(), pelvis.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftHip.x(), leftHip.y(), leftHip.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, pelvis.x(), pelvis.y(), pelvis.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightHip.x(), rightHip.y(), rightHip.z()).color(r, g, b, a).endVertex();

        // Draw legs
        buffer.vertex(matrix, leftHip.x(), leftHip.y(), leftHip.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftKnee.x(), leftKnee.y(), leftKnee.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, leftKnee.x(), leftKnee.y(), leftKnee.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftFoot.x(), leftFoot.y(), leftFoot.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightHip.x(), rightHip.y(), rightHip.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightKnee.x(), rightKnee.y(), rightKnee.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightKnee.x(), rightKnee.y(), rightKnee.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightFoot.x(), rightFoot.y(), rightFoot.z()).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buffer.end());
    }

    /**
     * Helper method to draw a filled circle for the head.
     */
    private static void drawCircleFilled(
            BufferBuilder buffer,
            Matrix4f matrix,
            Vector3f center,
            float radiusX,
            float radiusY,
            float radiusZ,
            float r, float g, float b, float a,
            float opacity
    ) {
        // Draw circle outline
        int segments = 16;
        float angleDelta = (float) ((2 * Math.PI) / segments);

        Vector3f prev = null;
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleDelta;
            float x = (float) (center.x() + Math.cos(angle) * radiusX);
            float z = (float) (center.z() + Math.sin(angle) * radiusZ);
            Vector3f current = new Vector3f(x, center.y(), z);

            if (prev != null) {
                buffer.vertex(matrix, prev.x(), prev.y(), prev.z()).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, current.x(), current.y(), current.z()).color(r, g, b, a).endVertex();
            }

            prev = current;
        }

        // Draw vertical circle
        prev = null;
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleDelta;
            float x = (float) (center.x() + Math.cos(angle) * radiusX);
            float y = (float) (center.y() + Math.sin(angle) * radiusY);
            Vector3f current = new Vector3f(x, y, center.z());

            if (prev != null) {
                buffer.vertex(matrix, prev.x(), prev.y(), prev.z()).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, current.x(), current.y(), current.z()).color(r, g, b, a).endVertex();
            }

            prev = current;
        }
    }

    /**
     * 围绕目标旋转描边效果
     * 创建围绕实体旋转的轨道效果
     */
    public static void renderEntityOrbitalOutline(
            PoseStack poseStack,
            Entity entity,
            int color,
            float lineWidth,
            int orbitCount,
            float orbitRadius,
            boolean throughWalls
    ) {
        if (entity == null) return;

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks - camera.getPosition().x();
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks - camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks - camera.getPosition().z();

        // 获取实体边界框大小
        AABB bb = entity.getBoundingBox();
        float entityWidth = (float)(bb.maxX - bb.minX) / 2.0f;
        float entityHeight = (float)(bb.maxY - bb.minY);

        // 计算轨道半径
        float radius = entityWidth * orbitRadius;

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y + entityHeight / 2, z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 获取当前时间以产生动画效果
        float time = (System.currentTimeMillis() % 10000) / 1000.0f;

        // 绘制多条轨道
        for (int i = 0; i < orbitCount; i++) {
            float orbitPhase = (float) i / orbitCount * (float) Math.PI * 2.0f;
            float colorOffset = (float) i / orbitCount;

            // 每条轨道的颜色略有变化
            float orbitR = r;
            float orbitG = g;
            float orbitB = b;

            // 根据实体状态调整轨道颜色
            if (entity instanceof LivingEntity living) {
                if (living.hurtTime > 0) {
                    float hurtProgress = (float) living.hurtTime / 10.0f;
                    orbitG *= (1.0f - hurtProgress * 0.8f);
                    orbitB *= (1.0f - hurtProgress * 0.8f);
                    orbitR = Math.min(1.0f, orbitR + (1.0f - orbitR) * hurtProgress * 0.8f);
                }
            }

            // 绘制轨道
            drawOrbit(poseStack, radius, orbitPhase, time, orbitR, orbitG, orbitB, a);
        }

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * 绘制单个轨道
     */
    private static void drawOrbit(
            PoseStack poseStack,
            float radius,
            float phase,
            float time,
            float r, float g, float b, float a
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // 旋转速度
        float rotationSpeed = 0.5f;

        // 波浪效果参数
        float waveAmplitude = radius * 0.15f;
        float waveFrequency = 3.0f;

        // 旋转轨道
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationY(time * rotationSpeed + phase));

        // 绘制轨道
        int segments = 48; // 增加线段数使轨道更平滑
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / segments * (float) Math.PI * 2.0f;

            // 计算波浪效果
            float yOffset = waveAmplitude * (float) Math.sin(angle * waveFrequency + time * 2.0f + phase);

            // 轨道位置
            float x = (float) (radius * Math.cos(angle));
            float y = yOffset;
            float z = (float) (radius * Math.sin(angle));

            // 颜色可以根据角度变化
            float segmentOffset = (float) i / segments;
            float alpha = (float) (a * (0.7f + 0.3f * Math.sin(segmentOffset * Math.PI * 2 + time * 3)));

            buffer.vertex(matrix, x, y, z).color(r, g, b, alpha).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        poseStack.popPose();
    }

    /**
     * 带双螺旋效果的轨道描边
     */
    public static void renderEntitySpiralOutline(
            PoseStack poseStack,
            Entity entity,
            int color1,
            int color2,
            float lineWidth,
            float spiralRadius,
            boolean throughWalls
    ) {
        if (entity == null) return;

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks - camera.getPosition().x();
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks - camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks - camera.getPosition().z();

        // 获取实体边界框大小
        AABB bb = entity.getBoundingBox();
        float entityWidth = (float)(bb.maxX - bb.minX) / 2.0f;
        float entityHeight = (float)(bb.maxY - bb.minY);

        // 计算螺旋半径
        float radius = entityWidth * spiralRadius;

        // 提取颜色分量
        float a1 = (color1 >> 24 & 0xFF) / 255f;
        float r1 = (color1 >> 16 & 0xFF) / 255f;
        float g1 = (color1 >> 8 & 0xFF) / 255f;
        float b1 = (color1 & 0xFF) / 255f;

        float a2 = (color2 >> 24 & 0xFF) / 255f;
        float r2 = (color2 >> 16 & 0xFF) / 255f;
        float g2 = (color2 >> 8 & 0xFF) / 255f;
        float b2 = (color2 & 0xFF) / 255f;

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y + entityHeight * 0.5f, z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 获取当前时间以产生动画效果
        float time = (System.currentTimeMillis() % 10000) / 1000.0f;

        // 绘制第一条螺旋
        drawSpiral(poseStack, radius, 0, time, entityHeight, r1, g1, b1, a1);

        // 绘制第二条螺旋，位置偏移半个周期
        drawSpiral(poseStack, radius, (float)Math.PI, time, entityHeight, r2, g2, b2, a2);

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * 绘制螺旋效果
     */
    private static void drawSpiral(
            PoseStack poseStack,
            float radius,
            float phase,
            float time,
            float height,
            float r, float g, float b, float a
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // 旋转速度
        float rotationSpeed = 1.2f;

        // 螺旋参数
        float spiralHeight = height * 0.8f;  // 螺旋高度
        float spiralTurns = 3.0f;  // 螺旋圈数

        // 绘制螺旋
        int segments = 96;  // 增加分段数使螺旋更平滑
        for (int i = 0; i <= segments; i++) {
            float progress = (float) i / segments;
            float angle = progress * spiralTurns * (float) Math.PI * 2.0f + time * rotationSpeed + phase;

            // 螺旋位置
            float x = (float) (radius * Math.cos(angle));
            float y = (progress - 0.5f) * spiralHeight;  // 从下到上
            float z = (float) (radius * Math.sin(angle));

            // 根据高度变化颜色透明度
            float heightFactor = 1.0f - 2.0f * Math.abs(progress - 0.5f);
            float alpha = a * (0.3f + 0.7f * heightFactor);

            buffer.vertex(matrix, x, y, z).color(r, g, b, alpha).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
    }

    /**
     * 绘制 3D ESP - Updated with smooth damage color transition
     */
    public static void render3DEntityBoundingBox(PoseStack poseStack, Entity entity, int color, boolean damage, boolean fillMode, boolean wireframeMode, float fillAlpha) {
        if (entity == null || !(entity instanceof LivingEntity)) return;

        // 获取必要组件
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 获取精确插值位置
        Vec3 entityPos = entity.getPosition((float) partialTicks)
                .subtract(camera.getPosition()); // 转换为相机相对坐标

        // 获取动态碰撞箱（包含所有状态）
        AABB bb = entity.getBoundingBoxForCulling();

        // 矩阵变换配置
        poseStack.pushPose();
        poseStack.translate(entityPos.x, entityPos.y, entityPos.z);

        // 碰撞箱尺寸计算
        float minX = (float) (bb.minX - entity.getX());
        float maxX = (float) (bb.maxX - entity.getX());
        float minY = (float) (bb.minY - entity.getY());
        float maxY = (float) (bb.maxY - entity.getY());
        float minZ = (float) (bb.minZ - entity.getZ());
        float maxZ = (float) (bb.maxZ - entity.getZ());

        // 颜色逻辑 - 增强的受伤渐变效果
        int startColor = color;
        int endColor = color;

        // 检查玩家受伤状态以调整颜色 - 平滑渐变
        if (damage && entity instanceof LivingEntity living) {
            if (living.hurtTime > 0) {
                float hurtProgress = (float) living.hurtTime / 10.0f;

                // 提取原始颜色分量
                float a = (color >> 24 & 0xFF) / 255f;
                float r = (color >> 16 & 0xFF) / 255f;
                float g = (color >> 8 & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;

                // 调整颜色 - 更多红色，减少绿色和蓝色
                g *= (1.0f - hurtProgress * 0.8f);
                b *= (1.0f - hurtProgress * 0.8f);
                r = Math.min(1.0f, r + (1.0f - r) * hurtProgress * 0.8f);

                // 转回整数颜色格式
                int newColor = ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);

                // 创建渐变效果
                startColor = newColor;

                // 更深的红色作为结束颜色
                float darkerR = Math.max(0.4f, r * 0.7f);
                int darkerColor = ((int)(a * 255) << 24) | ((int)(darkerR * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
                endColor = darkerColor;
            }
        }

        // 启用3D渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 绘制填充模式
        if (fillMode) {
            RenderUtils.drawFilledBoundingBox(poseStack, minX, maxX, minY, maxY, minZ, maxZ, startColor, endColor, fillAlpha);
        }

        // 绘制线框模式
        if (wireframeMode) {
            // 禁用深度测试，确保方框始终可见
            RenderSystem.disableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderUtils.drawBoundingBox(poseStack, minX, maxX, minY, maxY, minZ, maxZ, startColor, endColor);
        }

        // 恢复渲染状态
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * 绘制 2D ESP - Updated with smooth damage color transition
     */
    public static void renderEntityBoundingBox(PoseStack poseStack, Entity entity, int color, boolean damage) {
        if (entity instanceof LivingEntity) {
            EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();

            // 插值计算实体位置
            double x = entity.xOld + (entity.getX() - entity.xOld) * mc.getFrameTime() - renderManager.camera.getPosition().x();
            double y = entity.yOld + (entity.getY() - entity.yOld) * mc.getFrameTime() - renderManager.camera.getPosition().y();
            double z = entity.zOld + (entity.getZ() - entity.zOld) * mc.getFrameTime() - renderManager.camera.getPosition().z();
            float scale = 0.03F;

            // 是否受伤 - 增强为平滑渐变
            int borderColor = color;

            // 受伤时使用平滑的红色渐变
            if (damage && entity instanceof LivingEntity living) {
                if (living.hurtTime > 0) {
                    float hurtProgress = (float) living.hurtTime / 10.0f;

                    // 提取原始颜色分量
                    float a = (color >> 24 & 0xFF) / 255f;
                    float r = (color >> 16 & 0xFF) / 255f;
                    float g = (color >> 8 & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;

                    // 调整颜色 - 更多红色，减少绿色和蓝色
                    g *= (1.0f - hurtProgress * 0.8f);
                    b *= (1.0f - hurtProgress * 0.8f);
                    r = Math.min(1.0f, r + (1.0f - r) * hurtProgress * 0.8f);

                    // 转回整数颜色格式
                    borderColor = ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
                }
            }

            // 禁用深度测试，确保边框可见
            RenderSystem.disableDepthTest();

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            // 使用正确的旋转方式
            float angle = -renderManager.camera.getYRot();
            poseStack.mulPose(new Quaternionf().rotationY((float)Math.toRadians(angle)));
            poseStack.scale(scale, scale, scale);

            // 边框参数
            int height = 73;
            int width = 21;
            int thickness = 2;

            // 绘制边框
            RenderUtils.drawRect(poseStack, -width, 0, -width - thickness, height, borderColor); // 左边框
            RenderUtils.drawRect(poseStack, width, 0, width + thickness, height, borderColor);   // 右边框
            RenderUtils.drawRect(poseStack, -width, 0, width, thickness, borderColor);           // 上边框
            RenderUtils.drawRect(poseStack, -width, height - thickness, width, height, borderColor); // 下边框

            poseStack.popPose();
            RenderSystem.enableDepthTest(); // 恢复深度测试
        }
    }

    /**
     * 绘制 TargetESP
     */
    public static void drawTextureOnEntity(PoseStack poseStack, int xPos, int yPos, int width, int height, float textureWidth, float textureHeight, Entity entity, ResourceLocation texture, boolean rotate, Color c, Color c1, Color c2, Color c3) {
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        org.joml.Quaternionf cameraRotation = renderManager.camera.rotation();

        double x = entity.xOld + (entity.getX() - entity.xOld) * mc.getFrameTime() - renderManager.camera.getPosition().x();
        double y = (entity.yOld + 1) + ((entity.getY() + 1) - (entity.yOld + 1)) * mc.getFrameTime() - renderManager.camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * mc.getFrameTime() - renderManager.camera.getPosition().z();

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(cameraRotation);
        if (rotate) {
            // 正确的 1.20.1 旋转方式
            float angle = (float) (Math.sin(System.currentTimeMillis() / 800.0) * 360);
            // 创建四元数实例并应用旋转
            poseStack.mulPose(new Quaternionf().rotationAxis((float)Math.toRadians(angle), 0, 0, 1));
        }
        poseStack.scale(0.03F, 0.03F, 0.03F);

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        // 绘制主体
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderUtils.blitTextureWithColor(poseStack, xPos, yPos, 0, 0, width, height, textureWidth, textureHeight, c, c1, c2, c3);

        // 重置渲染状态
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    /**
     * 绘制 TargetESP
     */
    public static void drawJello(PoseStack matrix, Entity target, float delta, float step) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3 cameraPos = camera.getPosition();
        double prevSinAnim = Math.abs(1 + Math.sin((double) step - 0.45f)) / 2;
        double sinAnim = Math.abs(1 + Math.sin(step)) / 2;
        double x = target.xo + (target.getX() - target.xo) * delta - cameraPos.x();
        double y = target.yo + (target.getY() - target.yo) * delta - cameraPos.y() + prevSinAnim * target.getBbHeight();
        double z = target.zo + (target.getZ() - target.zo) * delta - cameraPos.z();
        double nextY = target.yo + (target.getY() - target.yo) * delta - cameraPos.y() + sinAnim * target.getBbHeight();

        matrix.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        float cos;
        float sin;
        int count = 1;
        Color color;
        for (int i = 0; i <= 360; i += 8) {
            if (count % 2 == 0) {
                color = HUD.INSTANCE.getColor(1);
            } else {
                color = HUD.INSTANCE.getColor(4);
            }
            cos = (float) (x + Math.cos(i * 6.28 / 360) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            sin = (float) (z + Math.sin(i * 6.28 / 360) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            bufferBuilder.vertex(matrix.last().pose(), cos, (float) nextY, sin).color(color.getRGB()).endVertex();
            bufferBuilder.vertex(matrix.last().pose(), cos, (float) y, sin).color(ColorUtils.applyOpacity(color, 0).getRGB()).endVertex();
            ++count;
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        matrix.popPose();
    }

}
