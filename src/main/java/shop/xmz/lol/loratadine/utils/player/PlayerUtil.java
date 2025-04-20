package shop.xmz.lol.loratadine.utils.player;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.helper.Vector3d;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import net.minecraft.client.KeyMapping;

public class PlayerUtil implements Wrapper {
    @Setter
    @Getter
    private static int offGroundTicks = 0;

    /**
     * 计算最大下落距离
     */
    public static double getMaxFallDistance() {
        BlockPos playerPos = mc.player.blockPosition();
        for (int y = playerPos.getY(); y > mc.level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(playerPos.getX(), y, playerPos.getZ());
            if (!mc.level.getBlockState(pos).isAir()) {
                return playerPos.getY() - y; // 返回玩家到地面的距离
            }
        }
        return 0; // 如果没有找到地面，返回 0
    }

    public static void sendClick(final int button, final boolean state) {
        final InputConstants.Key keyBind = button == 0 ? mc.options.keyAttack.getKey() : mc.options.keyUse.getKey();

        KeyMapping.set(keyBind, state);

        if (state) {
            KeyMapping.click(keyBind);
        }
    }

    public static int getTotalArmorValue(LivingEntity entity) {
        int totalArmor = 0;

        for (ItemStack armorItem : entity.getArmorSlots()) {
            if (armorItem.getItem() instanceof ArmorItem armor) {
                totalArmor += armor.getDefense();
            }
        }

        return totalArmor;
    }

    public static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        if (mc.level == null || mc.player == null) return null;

        BlockPos playerPos = mc.player.blockPosition();
        BlockPos offsetPos = playerPos.offset((int)offsetX, (int)offsetY, (int)offsetZ);

        return mc.level.getBlockState(offsetPos).getBlock();
    }

    public static Block getBlock(BlockPos pos) {
        if (mc.level == null || mc.player == null) return null;

        return mc.level.getBlockState(pos).getBlock();
    }

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public static Block block(final double x, final double y, final double z) {
        if (mc.level == null || mc.player == null) return null;

        return mc.level.getBlockState(BlockPos.containing(x, y, z)).getBlock();
    }

    public static Block block(final Vector3d pos) {
        return block(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     *
     * @param world   The world in which the ray tracing is performed
     * @param pos     The position of the block
     * @param start   The start vector
     * @param end     The end vector
     * @return BlockHitResult
     */
    public static BlockHitResult collisionRayTrace(final Level world, final BlockPos pos, Vec3 start, Vec3 end) {
        // Create the RayTraceContext
        ClipContext context = new ClipContext(
                start,  // Start vector
                end,    // End vector
                ClipContext.Block.OUTLINE, // Collision mode
                ClipContext.Fluid.NONE,    // Fluid mode
                null);  // No entity for ray tracing

        // Perform the ray trace
        BlockHitResult result = world.clip(context);

        // Check if the result hit this block spot
        if (result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
            return result; // Return the hit result if it hit the block at the given position
        }

        return null; // No hit or didn't hit the specific block
    }

    /**
     * Compares a BlockPos with a Vector3d for equality on integer coordinates.
     *
     * @param blockPos The BlockPos to compare.
     * @param vec      The Vector3d to compare.
     * @return true if the BlockPos and Vector3d are equal, false otherwise.
     */
    public static boolean equalsVector(BlockPos blockPos, Vector3d vec) {
        return (Math.floor(vec.getX()) == blockPos.getX() &&
                Math.floor(vec.getY()) == blockPos.getY() &&
                Math.floor(vec.getZ()) == blockPos.getZ());
    }

    public static Block getBlockUnderPlayer(final Player player) {
        return getBlock(BlockPos.containing(player.getX(), player.getY() - 1.0, player.getZ()));
    }

    /**
     * Gets the distance to the position. Args: x, y, z
     */
    public static double getDistance(double x, double y, double z) {
        if (mc.player == null) return 0.0d;

        double d0 = mc.player.getX() - x;
        double d1 = mc.player.getY() - y;
        double d2 = mc.player.getZ() - z;
        return Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
    }

    public static HitResult pickCustom(double blockReachDistance, float yaw, float pitch) {
        if (mc.player == null || mc.level == null) return null;

        Vec3 vec3 = mc.player.getEyePosition(1.0F);
        Vec3 vec31 = RotationUtils.getVectorForRotation(new Rotation(yaw, pitch));
        Vec3 vec32 = vec3.add(vec31.x * blockReachDistance, vec31.y * blockReachDistance, vec31.z * blockReachDistance);
        return mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
    }

    public static float getBlockHardness(final Level worldIn, final BlockPos pos) {
        BlockState blockState = worldIn.getBlockState(pos);
        return blockState.getDestroySpeed(worldIn, pos);
    }

    /**
     * 每tick更新玩家的离地状态计数
     */
    @EventTarget
    public static void onUpdate(UpdateEvent event) {
        if (mc.player != null) {
            if (mc.player.onGround()) {
                offGroundTicks = 0;
            } else {
                offGroundTicks++;
            }
        }
    }

    /**
     * 檢測玩家是否在水裏面
     */
    public static boolean inLiquid() {
        return mc.player.isInWaterOrBubble() || mc.player.isInLava();
    }
}