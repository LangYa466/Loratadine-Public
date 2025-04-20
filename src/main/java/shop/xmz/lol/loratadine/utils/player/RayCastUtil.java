package shop.xmz.lol.loratadine.utils.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.List;
import java.util.Optional;

import static shop.xmz.lol.loratadine.utils.RotationUtils.getVectorForRotation;
import static shop.xmz.lol.loratadine.utils.player.PlayerUtil.pickCustom;

/**
 * @author Patrick
 * @since 11/17/2021
 */
public final class RayCastUtil implements Wrapper {

    public static HitResult rayCast(final Rotation rotation, final double range) {
        return rayCast(rotation, range, 0);
    }

    public static HitResult rayCast(final Rotation rotation, final double range, final float expand) {
        return rayCast(rotation, range, expand, mc.player);
    }

    public static HitResult rayCast(final Rotation rotation, final double range, final float expand, Entity entity) {
        if (entity == null || mc.level == null) return null;

        // Cache values for performance
        final float partialTicks = mc.getFrameTime();
        final Vec3 eyePosition = entity.getEyePosition(partialTicks);
        final Vec3 lookVector = getVectorForRotation(rotation);
        final Vec3 targetVec = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);

        // Pre-calculate expanded box dimensions once
        final double expandedRange = range + expand;
        final AABB searchBox = new AABB(
                eyePosition.x - expandedRange, eyePosition.y - expandedRange, eyePosition.z - expandedRange,
                eyePosition.x + expandedRange, eyePosition.y + expandedRange, eyePosition.z + expandedRange
        );

        // More efficient entity filtering
        List<Entity> entities = mc.level.getEntitiesOfClass(
                Entity.class,
                searchBox,
                e -> e != entity && EntitySelector.NO_SPECTATORS.test(e) && e.isPickable()
        );

        // Optimize entity hit detection
        Entity pointedEntity = null;
        Vec3 hitVec = null;
        double closestDistance = range * range; // Square distance for faster comparison

        for (Entity e : entities) {
            AABB entityBox = e.getBoundingBox().inflate(expand);
            Optional<Vec3> intercept = entityBox.clip(eyePosition, targetVec);

            if (intercept.isPresent()) {
                Vec3 interceptPoint = intercept.get();
                double distSq = eyePosition.distanceToSqr(interceptPoint); // Faster than using distanceTo

                if (distSq < closestDistance) {
                    closestDistance = distSq;
                    pointedEntity = e;
                    hitVec = interceptPoint;
                }
            }
        }

        // Return the most appropriate hit result
        if (pointedEntity != null) {
            return new EntityHitResult(pointedEntity, hitVec);
        } else {
            // Only perform block raycast if no entity was hit
            return mc.level.clip(
                    new ClipContext(
                            eyePosition,
                            targetVec,
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            entity
                    )
            );
        }
    }

    public static boolean overBlock(final Rotation rotation, final Direction facing, final BlockPos pos, final boolean strict) {
        HitResult hitResult = pickCustom(4.5, rotation.yaw, rotation.pitch);

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        return blockHitResult.getBlockPos().equals(pos) && (!strict || blockHitResult.getDirection() == facing);
    }

    public static boolean overBlock(final Direction facing, final BlockPos pos, final boolean strict) {
        HitResult hitResult = mc.hitResult;

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;

        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        return blockHitResult.getBlockPos().equals(pos) && (!strict || blockHitResult.getDirection() == facing);
    }

    public static Boolean overBlock(final Rotation rotation, final BlockPos pos) {
        return overBlock(rotation, Direction.UP, pos, false);
    }

    public static boolean isOnBlock() {
        final HitResult movingObjectPosition = mc.hitResult;

        if (movingObjectPosition == null) return false;

        return movingObjectPosition.getType() == HitResult.Type.BLOCK;
    }

    public static Boolean overBlock(final Rotation rotation, final BlockPos pos, final Direction facing) {
        return overBlock(rotation, facing, pos, true);
    }

    public static boolean isOver(Rotation rotation,BlockPos data,Direction direction,boolean strict) {
        if (mc.player == null || mc.level == null) return false;

        Vec3 eyesPosition = mc.player.getEyePosition(1f);
        Vec3 rotationVec = getVectorForRotation(rotation);
        Vec3 vector = eyesPosition.add(rotationVec.x * 5, rotationVec.y * 5, rotationVec.z * 5);
        BlockHitResult rayTrace = mc.level.clip(new ClipContext(eyesPosition, vector, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));

        return !(rayTrace.getType() == BlockHitResult.Type.MISS) && (data.equals(rayTrace.getBlockPos()) && (!strict || direction.equals(rayTrace.getDirection())));
    }
}
