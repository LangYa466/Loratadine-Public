/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package shop.xmz.lol.loratadine.utils.player;


import cn.lzq.injection.leaked.LivingEntityTransformer;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class FallingPlayer implements Wrapper {
    private double x;
    public double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private final float yaw;
    private final float strafe;
    private final float forward;
    private float jumpMovementFactor;

    public FallingPlayer(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float strafe, float forward, float jumpMovementFactor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.strafe = strafe;
        this.forward = forward;
        this.jumpMovementFactor = jumpMovementFactor;
    }

    public FallingPlayer(Player player) {
        this(player.getX(), player.getY(), player.getZ(), player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z, player.getYRot(), player.xxa, player.zza, LivingEntityTransformer.jumpPower);
    }

    private void calculateForTick(float strafe, float forward) {

        float v = strafe * strafe + forward * forward;

        if (v >= 0.0001f) {
            v = (float) Math.sqrt(v);

            if (v < 1.0F) {
                v = 1.0F;
            }

            v = LivingEntityTransformer.jumpPower / v;
            strafe = strafe * v;
            forward = forward * v;
            float f1 = (float) Math.sin(yaw * (float) Math.PI / 180.0F);
            float f2 = (float) Math.cos(yaw * (float) Math.PI / 180.0F);
            this.motionX += strafe * f2 - forward * f1;
            this.motionZ += forward * f2 + strafe * f1;
        }


        motionY -= 0.08;

        motionX *= 0.91F;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.91F;

        x += motionX;
        y += motionY;
        z += motionZ;
    }
    private void calculateForTick() {
        float sr = strafe * 0.9800000190734863f;
        float fw = forward * 0.9800000190734863f;
        float v = sr * sr + fw * fw;
        if (v >= 0.0001f) {
            v = Mth.sqrt(v);
            if (v < 1.0f) {
                v = 1.0f;
            }
            float fixedJumpFactor = jumpMovementFactor;
            if (mc.player.isSprinting()) {
                fixedJumpFactor = fixedJumpFactor * 1.3f;
            }
            v = fixedJumpFactor / v;
            sr *= v;
            fw *= v;
            float f1 = Mth.sin(yaw * (float) Math.PI / 180.0f);
            float f2 = Mth.cos(yaw * (float) Math.PI / 180.0f);
            motionX += sr * f2 - fw * f1;
            motionZ += fw * f2 + sr * f1;
        }
        motionY -= 0.08;
        motionY *= 0.9800000190734863;
        x += motionX;
        y += motionY;
        z += motionZ;
        motionX *= 0.91;
        motionZ *= 0.91;
    }

    public void calculate(int ticks) {
        for (int i = 0; i < ticks; i++) {
            calculateForTick();
        }
    }
}
