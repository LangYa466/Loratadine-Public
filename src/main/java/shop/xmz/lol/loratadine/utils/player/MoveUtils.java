package shop.xmz.lol.loratadine.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.player.Scaffold;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import static java.lang.Math.toRadians;

@UtilityClass
public class MoveUtils implements Wrapper {
    public static final double HEAD_HITTER_MOTION = -0.0784000015258789;

    private static double x = 0.0;
    private static double y = 0.0;
    private static double z = 0.0;

    public static void stopMotion() {
        if (mc.player != null) {
            x = mc.player.getDeltaMovement().x;
            y = mc.player.getDeltaMovement().y;
            z = mc.player.getDeltaMovement().z;
        }
    }

    public static void stopMove() {
        if (mc.player != null) {
            mc.player.setDeltaMovement(0.0, 0.0, 0.0);
        }
    }

    public static void resMotion() {
        if (mc.player != null) {
            mc.player.setDeltaMovement(x, y, z);
            x = 0.0;
            y = 0.0;
            z = 0.0;
        }
    }
    
    public static boolean isMoving() {
        return mc.player != null && mc.level != null && (mc.player.input.forwardImpulse != 0.0 || mc.player.input.leftImpulse != 0.0);
    }

    public void strafe() {
        strafe(speed(), mc.player);
    }

    public double speed() {
        return Math.hypot(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z);
    }

    public void strafe(final double speed, Entity entity) {
        if (!isMoving()) {
            return;
        }

        final double yaw = direction();

        double motionY = entity.getDeltaMovement().y;

        entity.setDeltaMovement(
                -net.minecraft.util.Mth.sin((float) yaw) * speed,
                motionY,
                net.minecraft.util.Mth.cos((float) yaw) * speed
        );
    }

    /**
     * Movement utility methods converted from Minecraft 1.8.9 to 1.20.1
     */
    public static void strafe(final double speed) {
        if (!isMoving())
            return;

        final double yaw = getDirection();

        mc.player.setDeltaMovement(-Math.sin(yaw) * speed, mc.player.getDeltaMovement().y, Math.cos(yaw) * speed);
    }

    /**
     * Gets the player's speed effect level
     */
    public static int getSpeedEffect(Player player) {
        return player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED)
                ? player.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED).getAmplifier() + 1
                : 0;
    }

    /**
     * Calculates the player's movement direction in radians based on input
     */
    public static double getDirection() {
        float rotationYaw;

        rotationYaw = mc.player.getYRot();

        if (mc.player.input.forwardImpulse < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (mc.player.input.forwardImpulse < 0F)
            forward = -0.5F;
        else if (mc.player.input.forwardImpulse > 0F)
            forward = 0.5F;

        if (mc.player.input.leftImpulse > 0F)
            rotationYaw -= 90F * forward;
        if (mc.player.input.leftImpulse < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    /**
     * Gets the players' movement yaw
     */
    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    /**
     * Gets the players' movement yaw
     */
    public double direction() {
        float rotationYaw = mc.player.getYRot();

        if (mc.player.input.forwardImpulse < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.player.input.forwardImpulse < 0) {
            forward = -0.5F;
        } else if (mc.player.input.forwardImpulse > 0) {
            forward = 0.5F;
        }

        if (mc.player.input.leftImpulse > 0) {
            rotationYaw -= 90 * forward;
        }

        if (mc.player.input.leftImpulse < 0) {
            rotationYaw += 90 * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double clamp(double value, double minValue, double maxValue) {
        return Math.max(value, Math.min(minValue, maxValue));
    }

    public static float handleX(float var0) {
        return (float)(-Math.sin(Math.toRadians(var0)));
    }

    public static float handleZ(float var0) {
        return (float) Math.cos(Math.toRadians(var0));
    }
}
