package shop.xmz.lol.loratadine.player;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;

public class AutoTool extends Module {
    private final BooleanSetting spoof = new BooleanSetting("Spoof", this, true);
    private boolean mining = false;
    private int prevItem = 0;

    public AutoTool() {
        super("AutoTool", "自动工具" ,Category.PLAYER);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (!mc.options.keyUse.isDown() && mc.options.keyAttack.isDown() && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            if (!mining) {
                prevItem = mc.player.getInventory().selected;
                if (spoof.getValue() && !SpoofItemUtil.spoofing) {
                    SpoofItemUtil.startSpoof(prevItem);
                }
            }
            switchSlot();
            mining = true;
        } else {
            if (mining) {
                restore();
                mining = false;
            } else {
                prevItem = mc.player.getInventory().selected;
            }
        }
    }

    public void switchSlot() {
        if (mc.level == null || mc.player == null) return;

        float bestSpeed = 1F;
        int bestSlot = -1;

        if (mc.hitResult == null || mc.level.isEmptyBlock(((BlockHitResult) mc.hitResult).getBlockPos())) {
            return;
        }

        BlockState blockState = mc.level.getBlockState(((BlockHitResult) mc.hitResult).getBlockPos());
        for (int i = 0; i <= 8; i++) {
            ItemStack item = mc.player.getInventory().getItem(i);
            if (item.isEmpty()) {
                continue;
            }
            float speed = item.getDestroySpeed(blockState);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        if (bestSlot != -1) {
            mc.player.getInventory().selected = bestSlot;
        }
    }

    public void restore() {
        if (mc.level == null || mc.player == null || mc.gameMode == null) return;

        if (spoof.getValue() && SpoofItemUtil.spoofing) {
            SpoofItemUtil.stopSpoof();
        } else {
            for (int i = 0; i <= 8; i++) {
                if (i == prevItem) {
                    mc.player.getInventory().selected = i;
                    mc.gameMode.tick();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        prevItem = 0;
    }
}
