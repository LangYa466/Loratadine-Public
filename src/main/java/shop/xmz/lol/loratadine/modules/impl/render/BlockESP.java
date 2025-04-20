package shop.xmz.lol.loratadine.modules.impl.render;

import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render3DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class BlockESP extends Module {
    private final BooleanSetting chests = new BooleanSetting("Chests", this, true);
    private final BooleanSetting beds = new BooleanSetting("Beds", this, true);

    private final BooleanSetting fill = new BooleanSetting("Fill", this, true);
    private final BooleanSetting outline = new BooleanSetting("Outline", this, true);
    private final BooleanSetting distanceLimit = new BooleanSetting("Distance Limit", this, true);
    private final Set<BlockPos> openedChests = new HashSet<>();

    public BlockESP() {
        super("BlockESP", "方块透视", Category.RENDER);
    }

    @EventTarget
    public void onWorldEvent(WorldEvent event) {
        openedChests.clear(); // 清空已打开的箱子列表
    }

    @Override
    public void onDisable() {
        openedChests.clear(); // 清空已打开的箱子列表
        super.onDisable();
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        int chunkX = mc.player.chunkPosition().x;
        int chunkZ = mc.player.chunkPosition().z;

        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                LevelChunk chunk = mc.level.getChunk(x, z);

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (distanceLimit.getValue() && mc.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 32 * 32) continue;

                    if (isTargetBlock(blockEntity)) {
                        Color color = getBlockColor(blockEntity);
                        RenderUtils.render3DBlockBoundingBox(event.poseStack(), blockEntity, color.getRGB(), fill.getValue(), outline.getValue(), 200);
                    }
                }
            }
        }
    }

    /**
     * 判断是否为目标方块
     */
    private boolean isTargetBlock(BlockEntity blockEntity) {
        Block block = blockEntity.getBlockState().getBlock();
        return (chests.getValue() && block instanceof ChestBlock) || (beds.getValue() && block instanceof BedBlock);
    }

    /**
     * 获取方块颜色
     */
    private Color getBlockColor(BlockEntity blockEntity) {
        Block block = blockEntity.getBlockState().getBlock();

        if (block instanceof ChestBlock && blockEntity instanceof ChestBlockEntity chest) {
            BlockPos pos = chest.getBlockPos();

            float openness = chest.getOpenNess(0);

            if (openness > 0) {
                openedChests.add(pos);
            }

            return openedChests.contains(pos) ? new Color(255, 0, 80) : new Color(80, 255, 0);
        }

        else if (block instanceof BedBlock) {
            return new Color(255, 0, 80);
        }

        return new Color(255, 255, 255); // 默认颜色
    }
}