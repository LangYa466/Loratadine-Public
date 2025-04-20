package shop.xmz.lol.loratadine.utils;

import cn.lzq.injection.leaked.invoked.*;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.impl.setting.MoveFix;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.helper.Vector3d;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

public class RotationUtils implements Wrapper {
    @Getter
    private static Rotation angle;
    private static float lastForward, lastStrafe;
    public static int keepLength, revTick;

    public static void init() {
        Loratadine.INSTANCE.getEventManager().register(new RotationUtils());
    }

    public static void setRotation(Rotation rotation) {
        angle = rotation;
        keepLength = 0;
    }

    public static void setRotation(Rotation rotation, int keepLength) {
        angle = rotation;
        RotationUtils.keepLength = keepLength;
    }

    public static void reset() {
        if (mc.player == null) return;

        keepLength = 0;
        if (revTick > 0) {
            angle = new Rotation(angle.yaw - getAngleDifference(angle.yaw, mc.player.getYRot()) / revTick
                    , angle.pitch - getAngleDifference(angle.pitch, mc.player.getXRot()) / revTick);
            angle.fixedSensitivity(mc.options.sensitivity().get());
        } else {
            lastForward = lastStrafe = 0;
            angle = null;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (angle != null) {
            keepLength--;
            if (keepLength < 0) {
                if (revTick > 0) {
                    revTick--;
                }
                reset();
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent e) {
        if (e.getSide() == Event.Side.POST) {
            if (e.getPacket() instanceof ServerboundMovePlayerPacket packet) {
                if (!e.isCancelled()) {
                    if (packet.getYRot(0) < 360 && packet.getYRot(0) > -360) {
                        WrapperUtils.setPacketYRot(packet, packet.getYRot(0) + 720F);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUseItem(UseItemEvent event) {
        if (angle != null) {
            event.setYaw(angle.yaw);
            event.setPitch(angle.pitch);
        }
    }

    @EventTarget
    public void onPitchRender(PitchRenderEvent event) {
        if (angle != null && MoveFix.INSTANCE.renderRotation.getValue()) {
            event.pitch = angle.pitch;
        }
    }

    @EventTarget
    public void onRenderer(RenderPlayerEvent event) {
        if (angle != null && MoveFix.INSTANCE.renderRotation.getValue()) {
            event.rotationYaw = angle.yaw;
            event.rotationPitch = angle.pitch;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (angle != null && !event.post) {
            event.setYaw(angle.yaw);
            event.setPitch(angle.pitch);
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (angle != null) {
            event.setRotationYaw(angle.yaw);
        }
    }

    @EventTarget
    public void onJump(JumpEvent event) {
        if (angle != null) {
            event.setRotationYaw(angle.yaw);
        }
    }

    @EventTarget
    public void onLook(LookEvent event) {
        if (angle != null) {
            event.rotationYaw = angle.yaw;
            event.rotationPitch = angle.pitch;
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player == null || angle == null || MoveFix.INSTANCE.strictValue.getValue() || event.forwardImpulse == lastForward && event.leftImpulse == lastStrafe) return;

        final float forward = event.forwardImpulse;
        final float strafe = event.leftImpulse;

        final double yaw = Mth.wrapDegrees(Math.toDegrees(MoveUtils.direction(Mth.wrapDegrees(mc.player.getYRot()), forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = Mth.wrapDegrees(Math.toDegrees(MoveUtils.direction(Mth.wrapDegrees(angle.yaw), predictedForward, predictedStrafe)));
                final double difference = Math.abs(yaw - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        lastForward = closestForward;
        lastStrafe = closestStrafe;
        event.forwardImpulse = closestForward;
        event.leftImpulse = closestStrafe;
    }

    /**
     * Returns the distance to the entity. Args: entity
     */
    public static float getDistanceToEntity(Entity target) {
        if (mc.player == null) return 0.0F;

        Vec3 eyes = mc.player.getEyePosition(1F);
        Vec3 pos = getNearestPointBB(eyes, target.getBoundingBox());
        double xDist = Math.abs(pos.x - eyes.x);
        double yDist = Math.abs(pos.y - eyes.y);
        double zDist = Math.abs(pos.z - eyes.z);
        return (float) Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2) + Math.pow(zDist, 2));
    }

    public static double getYaw(Entity entity) {
        if (mc.player == null) return 0.0D;

        return mc.player.getYRot() + Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX())) - 90f - mc.player.getYRot());
    }

    public static Rotation getRotationForEntity(Entity entity) {
        if (mc.player == null) return null;

        Vec3 playerEyePos = mc.player.getEyePosition(1.0F);
        Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        Vec3 direction = targetPos.subtract(playerEyePos).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        return new Rotation(yaw, pitch);
    }

    public static Rotation calculate(final Vector3d position, final Direction direction) {
        double x = position.x + 0.5D;
        double y = position.y + 0.5D;
        double z = position.z + 0.5D;

        // 计算方向向量
        x += direction.getStepX() * 0.5D;
        y += direction.getStepY() * 0.5D;
        z += direction.getStepZ() * 0.5D;

        return calculate(new Vector3d(x, y, z));
    }


    public static Rotation calculate(Vector3d target) {
        if (mc.player == null) return null;

        // 计算视角所需的偏移量
        Vec3 eyePosition = mc.player.getEyePosition(1.0F);
        double deltaX = target.x - eyePosition.x;
        double deltaY = target.y - eyePosition.y;
        double deltaZ = target.z - eyePosition.z;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        return new Rotation(yaw, pitch);
    }

    public static Rotation getAngles(Entity entity) {
        if (entity == null) return null;

        // 获取当前玩家实例
        final LocalPlayer thePlayer = mc.player;

        if (thePlayer == null) return null;

        // 计算位置差异
        final double diffX = entity.getX() - thePlayer.getX(),
                diffY = entity.getY() + entity.getEyeHeight() * 0.9 - (thePlayer.getY() + thePlayer.getEyeHeight()),
                diffZ = entity.getZ() - thePlayer.getZ();

        // 计算水平距离
        final double dist = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));

        // 计算旋转角度
        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        final float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        // 返回旋转结果
        return new Rotation(
                thePlayer.getYRot() + Mth.wrapDegrees(yaw - thePlayer.getYRot()),
                thePlayer.getXRot() + Mth.wrapDegrees(pitch - thePlayer.getXRot())
        );
    }

    public static boolean isInViewRange(float fov, LivingEntity entity) {
        if (mc.player == null) return false;

        Vec3 playerPos = mc.player.getEyePosition(1.0F);
        Vec3 targetPos = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 toTargetVec = targetPos.subtract(playerPos).normalize();

        double dotProduct = lookVec.dot(toTargetVec);
        double angle = Math.toDegrees(Math.acos(dotProduct));

        return fov == 360F || angle <= fov;
    }

    public static float[] getRotations(BlockPos pos, float partialTicks) {
        if (mc.player == null) return new float[]{};

        Vec3 playerVector = new Vec3(mc.player.getX() + mc.player.getDeltaMovement().x * partialTicks, mc.player.getY() + mc.player.getEyeHeight() + mc.player.getDeltaMovement().y() * partialTicks, mc.player.getZ() + mc.player.getDeltaMovement().z() * partialTicks);
        double x = pos.getX() - playerVector.x + 0.5;

        double y = pos.getY() - playerVector.y + 0.5 + 0.2;
        double z = pos.getZ() - playerVector.z + 0.5;
        return diffCalc(x, y, z);
    }

    /**
     * Calculate difference between two angle points
     *
     * @param a angle point
     * @param b angle point
     * @return difference between angle points
     */
    public static float getAngleDifference(final float a, final float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public static double getRotationDifference(final Rotation a, final Rotation b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getYaw()), a.getPitch() - b.getPitch());
    }

    public static float[] diffCalc(double diffX, double diffY, double diffZ) {
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{Mth.wrapDegrees(yaw), Mth.wrapDegrees(pitch)};
    }

    public static Vec3 getNearestPointBB(Vec3 eye, AABB box) {
        double[] origin = {eye.x, eye.y, eye.z};
        double[] destMinis = {box.minX, box.minY, box.minZ};
        double[] destMaxis = {box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i < 3; i++) {
            if (origin[i] > destMaxis[i]) {
                origin[i] = destMaxis[i];
            } else if (origin[i] < destMinis[i]) {
                origin[i] = destMinis[i];
            }
        }

        return new Vec3(origin[0], origin[1], origin[2]);
    }

    public static float[] getRotationFromEyeToPoint(Vector3d point3d) {
        if (mc.player == null) return new float[]{};

        return getRotation(new Vector3d(mc.player.getX(), mc.player.getBoundingBox().minY + mc.player.getEyeHeight(), mc.player.getZ()), point3d);
    }

    public static float[] getRotation(Vector3d from, Vector3d to) {
        final double x = to.getX() - from.getX();
        final double y = to.getY() - from.getY();
        final double z = to.getZ() - from.getZ();

        final double sqrt = Math.sqrt(x * x + z * z);

        final float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90F;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(y, sqrt)));

        return new float[]{yaw, Math.min(Math.max(pitch, -90), 90)};
    }

    private static float[] getRotationsByVec(final Vec3 origin, final Vec3 position) {
        final Vec3 difference = position.subtract(origin);
        final double distance = flat(difference).length();
        final float yaw = (float) Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(difference.y, distance)));
        return new float[]{yaw, pitch};
    }

    public static Vec3 flat(Vec3 s) {
        return new Vec3(s.x, 0.0, s.z);
    }

    public static float[] getRotationBlock(final BlockPos pos) {
        if (mc.player == null) return new float[]{0.0F, 0.0F};

        return getRotationsByVec(mc.player.position().add(0.0, mc.player.getEyeHeight(), 0.0), new Vec3(pos.getX() + 0.51, pos.getY() + 0.51, pos.getZ() + 0.51));
    }

    public static float[] getRotationBlock(final BlockPos pos, final Direction facing) {
        if (mc.player == null || mc.level == null) return new float[]{0.0F, 0.0F};

        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Vec3 dirVec = new Vec3(facing.getNormal().getX(),facing.getNormal().getY(),facing.getNormal().getZ());

        double dirX = dirVec.x * 0.5;
        double dirY = dirVec.y * 0.5;
        double dirZ = dirVec.z * 0.5;

        Vec3 hitVec = center.add(new Vec3(dirX, dirY, dirZ));

        return getRotationsByVec(mc.player.position().add(0.0, mc.player.getEyeHeight(), 0.0),hitVec);
    }

    /**
     * Calculate rotation to vector
     *
     * @param rotation your rotation
     * @return target vector
     */
    public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = (float) Math.cos(-rotation.getYaw() * 0.017453292F - 3.1415927F);
        float yawSin = (float) Math.sin(-rotation.getYaw() * 0.017453292F - 3.1415927F);
        float pitchCos = (float) -Math.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = (float) Math.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    /**
     * Get the closest point on a boundingBox from start
     *
     * @param start       Src
     * @param boundingBox boundingBox to calculate closest point from start
     * @return The closest point on boundingBox as a hit vec
     */
    public static Vec3 getClosestPoint(final Vec3 start,
                                       final AABB boundingBox) {
        final double closestX = start.x >= boundingBox.maxX ? boundingBox.maxX :
                start.x <= boundingBox.minX ? boundingBox.minX :
                        boundingBox.minX + (start.x - boundingBox.minX);

        final double closestY = start.y >= boundingBox.maxY ? boundingBox.maxY :
                start.y <= boundingBox.minY ? boundingBox.minY :
                        boundingBox.minY + (start.y - boundingBox.minY);

        final double closestZ = start.z >= boundingBox.maxZ ? boundingBox.maxZ :
                start.z <= boundingBox.minZ ? boundingBox.minZ :
                        boundingBox.minZ + (start.z - boundingBox.minZ);

        return new Vec3(closestX, closestY, closestZ);
    }

    public static float[] getHVHRotation(Entity entity) {
        if (entity == null || mc.player == null) {
            return null;
        } else {
            double diffX = entity.getX() - mc.player.getX();
            double diffZ = entity.getZ() - mc.player.getZ();
            Vec3 BestPos = getClosestPoint(mc.player.getEyePosition(1f), entity.getBoundingBox());
            Vec3 myEyePos = new Vec3(mc.player.getX(), mc.player.getY() +
                    mc.player.getEyeHeight(), mc.player.getZ());

            double diffY;

            diffY = BestPos.y - myEyePos.y;
            double dist = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
            float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
            return new float[]{yaw, pitch};
        }
    }

    public static float[] getSimpleRotations(LivingEntity target) {
        if (mc.player == null) return new float[]{};

        Vector3d targetPos; // i paste
        final double yDist = target.getY() - mc.player.getY();
        if (yDist >= 1.547) {
            targetPos = new Vector3d(target.getX(), target.getY(), target.getZ());
        } else if (yDist <= -1.547) {
            targetPos = new Vector3d(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
        } else {
            targetPos = new Vector3d(target.getX(), target.getY() + target.getEyeHeight() / 2, target.getZ());
        }
        return getRotationFromEyeToPoint(targetPos);
    }

    public static float[] getFacingRotations(final int x, final double y, final int z) {
        if (mc.level == null) return new float[]{};

        // 创建一个雪球实体
        Snowball snowball = new Snowball(mc.level, x + 0.5, y + 0.5, z + 0.5);

        // 返回需要的旋转角度
        return getRotationsNeeded(snowball);
    }

    public static float[] getRotationsNeeded(Entity entity) {
        if (mc.player == null) return new float[]{};

        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 entityPos = entity.position();
        Vec3 delta = entityPos.subtract(playerPos);

        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontalDistance));

        return new float[]{yaw, pitch};
    }

    public static void setVisualRotations(float yaw, float pitch) {
        if (mc.player == null) return;

        mc.player.yHeadRot = mc.player.yBodyRot = yaw; // 设置头部和身体的 Yaw 值
        mc.player.xRotO = pitch; // 设置 Pitch 值
    }
}
