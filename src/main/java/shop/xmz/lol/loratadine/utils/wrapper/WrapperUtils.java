package shop.xmz.lol.loratadine.utils.wrapper;

import cn.lzq.injection.leaked.ClientLevelTransformer;
import cn.lzq.injection.leaked.LocalPlayerTransformer;
import cn.lzq.injection.leaked.mapping.Mapping;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import shop.xmz.lol.loratadine.utils.helper.ReflectionHelper;
import shop.xmz.lol.loratadine.utils.unsafe.UnsafeUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WrapperUtils implements Wrapper {
    public static boolean isAttackAction(ServerboundInteractPacket packet) {
        try {
            long actionOffset = UnsafeUtils.getFieldOffset(ServerboundInteractPacket.class.getDeclaredField(Mapping.get(ServerboundInteractPacket.class, "action", null)));

            Object action = UnsafeUtils.getObject(packet, actionOffset);

            Field attackActionField = ServerboundInteractPacket.class.getDeclaredField(Mapping.get(ServerboundInteractPacket.class, "ATTACK_ACTION", null));
            attackActionField.setAccessible(true);
            Object attackAction = attackActionField.get(null);

            return action == attackAction;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isInteractionAction(ServerboundInteractPacket packet) {
        try {
            long actionOffset = UnsafeUtils.getFieldOffset(ServerboundInteractPacket.class.getDeclaredField(Mapping.get(ServerboundInteractPacket.class, "ATTACK_ACTION", null)));

            Object action = UnsafeUtils.getObject(packet, actionOffset);

            String actionClassName = action.getClass().getName();

            return actionClassName.endsWith("ServerboundInteractPacket$InteractionAction");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isInteractionAtLocationAction(ServerboundInteractPacket packet) {
        try {
            long actionOffset = UnsafeUtils.getFieldOffset(ServerboundInteractPacket.class.getDeclaredField(Mapping.get(ServerboundInteractPacket.class, "ATTACK_ACTION", null)));

            Object action = UnsafeUtils.getObject(packet, actionOffset);

            String actionClassName = action.getClass().getName();

            return actionClassName.endsWith("ServerboundInteractPacket$InteractionAtLocationAction");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getFPS() {
        if (mc == null) return 0;

        Field fps_field = ReflectionHelper.findField(mc.getClass(), Mapping.get(mc.getClass(), "fps", null));
        try {
            return fps_field.getInt(mc);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static void ensureHasSentCarriedItem(MultiPlayerGameMode gameMode) {
        try {
            Method syncItemMethod = gameMode.getClass().getDeclaredMethod(Mapping.get(gameMode.getClass(), "ensureHasSentCarriedItem" ,"()V"));

            syncItemMethod.setAccessible(true);

            syncItemMethod.invoke(gameMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Timer getTimer() {
        if (mc == null) return null;

        Field timerField = ReflectionHelper.findField(mc.getClass(), Mapping.get(mc.getClass(), "timer", null));
        try {
            return (Timer) timerField.get(mc);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setMsPerTick(Timer timer, float msPerTick) {
        if (timer == null) return;

        Field msPerTickField = ReflectionHelper.findField(timer.getClass(), Mapping.get(timer.getClass(), "msPerTick", null));
        try {
            msPerTickField.setFloat(timer, msPerTick);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getLastMs(Timer timer) {
        if (timer == null) return 0L;

        Field msPerTickField = ReflectionHelper.findField(timer.getClass(), Mapping.get(timer.getClass(), "lastMs", null));
        try {
            return msPerTickField.getLong(timer);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static void setLastMs(Timer timer, long lastMs) {
        if (timer == null) return;

        Field msPerTickField = ReflectionHelper.findField(timer.getClass(), Mapping.get(timer.getClass(), "lastMs", null));
        try {
            msPerTickField.setLong(timer, lastMs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRightClickDelay(int rightClickDelay) {
        if (mc == null) return;

        Field rightClickDelay_field = ReflectionHelper.findField(mc.getClass(), Mapping.get(mc.getClass(), "rightClickDelay", null));
        try {
            rightClickDelay_field.setInt(mc, rightClickDelay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*mc 1.18.1 old draw mojang family dead.*/
    public static void draw(PoseStack poseStack, String text, float x, float y, int color) {
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        int lightLevel = 0xF000F0;

        mc.font.drawInBatch(text, x, y, color, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, lightLevel);

        bufferSource.endBatch();
    }

    /*mc 1.18.1 old drawShadow mojang family dead.*/
    public static void drawShadow(PoseStack poseStack, String text, float x, float y, int color, boolean shadow) {
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        int lightLevel = 0xF000F0;

        mc.font.drawInBatch(text, x, y, color, shadow, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, lightLevel);

        bufferSource.endBatch();
    }

    /*mc 1.18.1 old drawShadow mojang family dead.*/
    public static void drawShadow(PoseStack poseStack, String text, float x, float y, int color) {
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        int lightLevel = 0xF000F0;

        mc.font.drawInBatch(text, x, y, color, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, lightLevel);

        bufferSource.endBatch();
    }

    public static void setDestroyDelay(int destroyDelay) {
        if (mc.gameMode == null) return;

        Field destroyDelay_field = ReflectionHelper.findField(mc.gameMode.getClass(), Mapping.get(mc.gameMode.getClass(), "destroyDelay", null));
        try {
            destroyDelay_field.setInt(mc.gameMode, destroyDelay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUsingSecondaryAction(ServerboundInteractPacket packet, boolean target) {
        if (packet == null) return;

        Field usingSecondaryAction_field = ReflectionHelper.findField(ServerboundInteractPacket.class, Mapping.get(packet.getClass(), "usingSecondaryAction", null));
        try {
            usingSecondaryAction_field.setBoolean(packet, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float getDestroyProgress() {
        if (mc.gameMode == null) return 0.0F;

        Field destroyProgress_field = ReflectionHelper.findField(mc.gameMode.getClass(), Mapping.get(mc.gameMode.getClass(), "destroyProgress", null));
        try {
            return destroyProgress_field.getFloat(mc.gameMode);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0F;
        }
    }

    public static boolean getWasSneaking() {
        if (mc.player == null) return false;

        Field wasSneaking_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "crouching", null));
        try {
            return wasSneaking_field.getBoolean(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static float getYRotLast() {
        if (mc.player == null) return 0.0F;

        Field yRotLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "yRotLast", null));
        try {
            return yRotLast_field.getFloat(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0F;
        }
    }

    public static double getXLast() {
        if (mc.player == null) return 0.0D;

        Field xLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "xLast", null));
        try {
            return xLast_field.getDouble(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0D;
        }
    }

    public static double getYLast() {
        if (mc.player == null) return 0.0D;

        Field yLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "yLast1", null));
        try {
            return yLast_field.getDouble(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0D;
        }
    }

    public static double getZLast() {
        if (mc.player == null) return 0.0D;

        Field zLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "zLast", null));
        try {
            return zLast_field.getDouble(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0D;
        }
    }

    public static float getXRotLast() {
        if (mc.player == null) return 0.0F;

        Field xRotLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "xRotLast", null));
        try {
            return xRotLast_field.getFloat(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0F;
        }
    }

    public static void setYRotLast(float yRot) {
        if (mc.player == null) return;

        Field yRotLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "yRotLast", null));
        try {
            yRotLast_field.setFloat(mc.player, yRot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setXRotLast(float xRot) {
        if (mc.player == null) return;

        Field xRotLast_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "xRotLast", null));
        try {
            xRotLast_field.setFloat(mc.player, xRot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPositionReminder() {
        if (mc.player == null) return 0;

        Field positionReminder_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "positionReminder", null));
        try {
            return positionReminder_field.getInt(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getOnGroundTicks() {
        return LocalPlayerTransformer.onGroundTicks;
    }

    public static int getOffGroundTicks() {
        return LocalPlayerTransformer.offGroundTicks;
    }

    public static int getSkipTicks() {
        return ClientLevelTransformer.skipTicks;
    }

    public static void setSkipTicks(int skipTicks) {
        ClientLevelTransformer.skipTicks = skipTicks;
    }

    /**
     * 在实体周围生成旧版风格的粒子效果
     * @param entity 目标实体
     * @param particleType 要生成的粒子类型
     */
    public static void spawnLegacyParticles(Entity entity, ParticleOptions particleType) {
        // 在1.20.1中使用RandomSource而不是Random
        RandomSource random = entity.level().getRandom();

        for (int i = 0; i < 16; i++) {
            // 生成随机偏移（-1到1）
            double offsetX = (random.nextDouble() * 2 - 1) * entity.getBbWidth() / 4.0;
            double offsetY = (random.nextDouble() * 2 - 1) * entity.getBbHeight() / 4.0;
            double offsetZ = (random.nextDouble() * 2 - 1) * entity.getBbWidth() / 4.0;

            // 确保在单位球内
            if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= 1.0) {
                // 计算粒子生成位置（实体中心 + 偏移）
                double posX = entity.getX() + offsetX;
                double posY = entity.getY() + entity.getBbHeight() / 2 + offsetY;
                double posZ = entity.getZ() + offsetZ;

                // 设置速度（旧版Y方向+0.2）
                double speedX = offsetX;
                double speedY = offsetY + 0.2;
                double speedZ = offsetZ;

                // 在1.20.1中level()替代了level
                entity.level().addParticle(particleType, posX, posY, posZ, speedX, speedY, speedZ);
            }
        }
    }

    public static void setPositionReminder(int positionReminder) {
        if (mc.player == null) return;

        Field positionReminder_field = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "positionReminder", null));
        try {
            positionReminder_field.setInt(mc.player, positionReminder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDestroyProgress(float destroyProgress) {
        if (mc.gameMode == null) return;

        Field destroyProgress_field = ReflectionHelper.findField(mc.gameMode.getClass(), Mapping.get(mc.gameMode.getClass(), "destroyProgress", null));
        try {
            destroyProgress_field.setFloat(mc.gameMode, destroyProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPacketYRot(ServerboundMovePlayerPacket packet, float yRot) {
        if (mc.gameMode == null) return;

        Field yRotField = ReflectionHelper.findField(packet.getClass(), Mapping.get(ServerboundMovePlayerPacket.class, "yRot", null));
        try {
            yRotField.setFloat(packet, yRot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMissTime(int missTime) {
        Field missTimeField = ReflectionHelper.findField(mc.getClass(), Mapping.get(mc.getClass(), "missTime", null));
        try {
            missTimeField.setInt(mc, missTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDepthBufferId(RenderTarget renderTarget, int depthBufferId) {
        Field depthBufferIdField = ReflectionHelper.findField(renderTarget.getClass(), Mapping.get(renderTarget.getClass(), "depthBufferId", null));
        try {
            depthBufferIdField.setInt(renderTarget, depthBufferId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getMissTime() {
        if (mc == null) return 0;

        Field missTimeField = ReflectionHelper.findField(mc.getClass(), Mapping.get(mc.getClass(), "missTime", null));
        try {
            return missTimeField.getInt(mc);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean getWasSprinting() {
        if (mc.player == null) return false;

        Field sprintingField = ReflectionHelper.findField(mc.player.getClass(), Mapping.get(mc.player.getClass(), "wasSprinting", null));
        try {
            return sprintingField.getBoolean(mc.player);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addEntity(ClientLevel level, int entityId, Entity fakePlayer) {
        try {
            Method addEntityMethod = level.getClass().getDeclaredMethod(Mapping.get(level.getClass(), "addEntity", "(ILnet/minecraft/world/entity/Entity;)V"), int.class, Entity.class);

            addEntityMethod.setAccessible(true);

            addEntityMethod.invoke(level, entityId, fakePlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
