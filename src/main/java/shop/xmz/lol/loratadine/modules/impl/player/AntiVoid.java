package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

/**
 * Anti-Void module to prevent falling into the void
 * @author Jon_awa / DSJ_
 * @since 2025/2/28
 */
public class AntiVoid extends Module {
    private final BooleanSetting doStuck = new BooleanSetting("Stuck when can't scaffold", this, true);
    private final NumberSetting distance = new NumberSetting("Do stuck fall distance", this, 8, 1, 15, 1);
    private boolean scaffoldEnabledByAntiVoid = false;
    private boolean stuckEnabledByAntiVoid = false;
    private int baseY = Integer.MAX_VALUE;

    public AntiVoid() {
        super("AntiVoid", "反虚空", Category.MOVEMENT);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        Module scaffold = Loratadine.INSTANCE.getModuleManager().getModule(Scaffold.class);

        if (mc.player == null) return;

        // Update base Y coordinate
        if (baseY != (int) Math.floor(mc.player.getY()) - 1) {
            baseY = (int) Math.floor(mc.player.getY()) - 1;
        }

        // Scenario 1: Activate protection
        if ((mc.player.fallDistance > 0 || (mc.player.hurtTime > 0 && mc.player.hurtTime < 10))
                && !mc.player.onGround()
                && isInVoid()
                && isBlockedAround()
                && !scaffold.isEnabled()) {

            // Record module enabled status
            stuckEnabledByAntiVoid = true;
            scaffoldEnabledByAntiVoid = true;

            scaffold.setEnabled(true);
            WrapperUtils.setSkipTicks(5);
            Scaffold.INSTANCE.bigVelocityTick = 10;
            Scaffold.INSTANCE.canTellyPlace = true;
        }
        // Scenario 2: Activate Stuck module
        else if (!isBlockedAround()
                && mc.player.fallDistance > distance.getValue().floatValue()
                && isInVoid()
                && doStuck.getValue()) {

            stuckEnabledByAntiVoid = true;
        }

        // Auto-disable logic
        if (isOnSolidBlock()) {
            if (scaffold.isEnabled() && scaffoldEnabledByAntiVoid) {
                scaffold.setEnabled(false);
                scaffoldEnabledByAntiVoid = false;
            }
        }
    }

    /**
     * Check if player is standing on a solid block
     * @return true if on a solid block, false otherwise
     */
    private boolean isOnSolidBlock() {
        if (mc.player == null || mc.level == null) return false;
        BlockPos blockBelow = new BlockPos((int) mc.player.getX(), (int) mc.player.getY() - 1, (int) mc.player.getZ());
        return mc.level.getBlockState(blockBelow).isSolid();
    }

    @Override
    public void onEnable() {
        baseY = Integer.MAX_VALUE;
        scaffoldEnabledByAntiVoid = false;
        stuckEnabledByAntiVoid = false;
    }

    /**
     * Check if there are blocks around the player
     * @return true if blocks are detected around the player, false otherwise
     */
    private boolean isBlockedAround() {
        if (mc.player == null || mc.level == null) return false;

        int[][] offsets = {
                {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
                {2, 1, 0}, {-2, 1, 0}, {0, 1, 2}, {0, 1, -2},
                {3, 1, 0}, {-3, 1, 0}, {0, 1, 3}, {0, 1, -3},
                {4, 1, 0}, {-4, 1, 0}, {0, 1, 4}, {0, 1, -4}
        };

        // Check all offset positions
        for (int[] offset : offsets) {
            BlockPos currentPos = new BlockPos((int) mc.player.getX(), baseY, (int) mc.player.getZ()).offset(offset[0], offset[1], offset[2]);
            BlockPosWithFacing result = checkNearBlocks(currentPos);
            if (result != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check nearby blocks for solid materials
     * @param blockPos block position to check around
     * @return BlockPosWithFacing if a solid block is found, null otherwise
     */
    private static BlockPosWithFacing checkNearBlocks(BlockPos blockPos) {
        if (mc.level != null) {
            if (mc.level.getBlockState(blockPos.offset(0, -1, 0)).isSolid()) {
                return new BlockPosWithFacing(blockPos.offset(0, -1, 0), Direction.UP);
            } else if (mc.level.getBlockState(blockPos.offset(-1, 0, 0)).isSolid()) {
                return new BlockPosWithFacing(blockPos.offset(-1, 0, 0), Direction.EAST);
            } else if (mc.level.getBlockState(blockPos.offset(1, 0, 0)).isSolid()) {
                return new BlockPosWithFacing(blockPos.offset(1, 0, 0), Direction.WEST);
            } else if (mc.level.getBlockState(blockPos.offset(0, 0, 1)).isSolid()) {
                return new BlockPosWithFacing(blockPos.offset(0, 0, 1), Direction.NORTH);
            } else if (mc.level.getBlockState(blockPos.offset(0, 0, -1)).isSolid()) {
                return new BlockPosWithFacing(blockPos.offset(0, 0, -1), Direction.SOUTH);
            }
        }
        return null;
    }

    /**
     * Check if the player is in the void
     * @return true if player is in the void, false otherwise
     */
    public static boolean isInVoid() {
        for (int i = 0; i <= 384; ++i) {
            if (isOnGround(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if player is on ground at a specific height
     * @param height vertical distance to check
     * @return true if player is on ground, false otherwise
     */
    private static boolean isOnGround(double height) {
        if (mc.player == null || mc.level == null) return false;

        AABB shiftedBox = mc.player.getBoundingBox().move(0.0, -height, 0.0);
        Iterable<VoxelShape> collisions = mc.level.getCollisions(mc.player, shiftedBox);

        return collisions.iterator().hasNext();
    }

    /**
     * Record for storing a block position with its facing direction
     */
    private record BlockPosWithFacing(BlockPos position, Direction facing) {
    }
}