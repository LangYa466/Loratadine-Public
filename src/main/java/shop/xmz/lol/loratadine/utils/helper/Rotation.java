package shop.xmz.lol.loratadine.utils.helper;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

/**
 * Rotations
 */
@Getter
@Setter
public class Rotation implements Wrapper {
    public float yaw;
    public float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Set rotations to player
     */
    public void toPlayer(Player player) {
        if (Float.isNaN(yaw) || Float.isNaN(pitch))
            return;

        fixedSensitivity(mc.options.sensitivity().get());

        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    public void fixedSensitivity(double sensitivity) {
        double f = sensitivity * 0.6F + 0.2F;
        double gcd = f * f * f * 1.2F;

        // get previous rotation
        Rotation rotation = new Rotation(yaw, pitch);

        // fix yaw
        float deltaYaw = yaw - rotation.getYaw();
        deltaYaw -= (float) (deltaYaw % gcd);
        yaw = rotation.getYaw() + deltaYaw;

        // fix pitch
        float deltaPitch = pitch - rotation.getPitch();
        deltaPitch -= (float) (deltaPitch % gcd);
        pitch = rotation.getPitch() + deltaPitch;
    }
}
