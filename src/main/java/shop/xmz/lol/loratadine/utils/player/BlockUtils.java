package shop.xmz.lol.loratadine.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

@UtilityClass
public class BlockUtils implements Wrapper {

    public static boolean isAirBlock(final BlockPos blockPos) {
        if (mc.level == null || mc.player == null) return false;

        final Block block = mc.level.getBlockState(blockPos).getBlock();
        return block instanceof AirBlock;
    }

    public static boolean isValidBlock(final BlockPos blockPos) {
        if (mc.level == null || mc.player == null) return false;

        return isValidBlock(mc.level.getBlockState(blockPos).getBlock());
    }

    public static boolean isValidBlock(Block block) {
        if (mc.level == null || mc.player == null) return false;

        return !(block instanceof LiquidBlock) && !(block instanceof AirBlock) && !(block instanceof ChestBlock) &&
                !(block instanceof FurnaceBlock) && !(block instanceof LadderBlock) && !(block instanceof TntBlock);
    }

    public static Block getBlock(final BlockPos blockPos) {
        if (mc.level == null || mc.player == null) return null;

        return mc.level.getBlockState(blockPos).getBlock();
    }

    public static BlockState getState(BlockPos blockPos) {
        if (mc.level == null || mc.player == null) return null;

        return mc.level.getBlockState(blockPos);
    }

    public static boolean canBeClicked(BlockPos blockPos) {
        // 检查当前世界和玩家是否为 null
        if (mc.level == null || mc.player == null) return false;

        BlockState blockState = getState(blockPos);
        Block block = blockState.getBlock();

        // 使用 getShape 需要传递足够的参数：BlockState、BlockGetter (Level)、BlockPos 和 CollisionContext
        VoxelShape shape = block.getShape(blockState, mc.level, blockPos, CollisionContext.empty());

        // 判断是否可以碰撞检测，直接使用 BlockState 的 collision check
        boolean canCollideCheck = !shape.isEmpty() && blockState.isCollisionShapeFullBlock(mc.level, blockPos);

        // 检查方块是否在世界边界内
        ClientLevel world = mc.level;
        WorldBorder worldBorder = world.getWorldBorder();

        return canCollideCheck && worldBorder.isWithinBounds(blockPos);
    }
}
