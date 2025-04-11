package shop.xmz.lol.loratadine.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Eagle extends Module {
    public Eagle() {
        super("Eagle", "安全蹲搭", Category.MOVEMENT);
    }

    private final BooleanSetting autoPlaceBlocks = new BooleanSetting("Auto Place Blocks", this, false);

    public static Block getBlock(BlockPos pos) {
        if (mc.level == null) return null;
        return mc.level.getBlockState(pos).getBlock();
    }

    public static Block getBlockUnderPlayer(final Player player) {
        return getBlock(BlockPos.containing(player.getX(), player.getY() - 1, player.getZ()));
    }

    @EventTarget
    public void onUpdate(MotionEvent event) {
        if (mc.player == null || event.post) return;

        if (getBlockUnderPlayer(mc.player) instanceof AirBlock) {
            if (mc.player.onGround()) {
                mc.options.keyShift.setDown(true);
            }
        } else if (mc.player.onGround()) {
            mc.options.keyShift.setDown(false);
        }

        if (autoPlaceBlocks.getValue()) placeBlock();
    }

    /**
     * Attempt to place a block under the player.
     */
    private void placeBlock() {
        if (mc.player == null || mc.level == null) return;

        BlockPos blockBelow = BlockPos.containing(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ());
        BlockState blockState = mc.level.getBlockState(blockBelow);

        // Ensure the target location is air and the player is holding a block
        if (blockState.getBlock() instanceof AirBlock &&
                !mc.player.getMainHandItem().isEmpty() &&
                mc.player.getMainHandItem().getItem() instanceof BlockItem) {

            mc.options.keyUse.setDown(
                    RayCastUtil.isOnBlock() &&
                            !RayCastUtil.overBlock(new Rotation(mc.player.getYRot(), mc.player.getXRot()), blockBelow)
            );
        }
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        mc.player.setShiftKeyDown(false);
    }

    @Override
    public void onDisable() {
        mc.options.keyShift.setDown(false);
        if (autoPlaceBlocks.getValue()) mc.options.keyUse.setDown(false);
    }
}