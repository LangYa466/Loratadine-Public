package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.MotionEvent;
import cn.lzq.injection.leaked.invoked.StrafeEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.level.block.Blocks;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;
import shop.xmz.lol.loratadine.utils.player.PlayerUtil;

public class Speed extends Module {
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"WatchDog", "Legit"}, "Legit");


    public Speed() {
        super("Speed", "速度", Category.PLAYER);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        if (mc.player == null || event.post) return;

        if (mode_Value.is("Legit")) {
            if (MoveUtils.isMoving() && mc.player.onGround()) {
                KeyMapping.set(mc.options.keyJump.getKey(), true);
            } else {
                mc.options.keyJump.setDown(mc.options.keyJump.isDown());
            }
        }
        this.setSuffix(mode_Value.getValue());
    }

    @EventTarget
    public void onStrafeEvent(StrafeEvent event) {
        if (mc.player == null) return;

        if (mode_Value.is("WatchDog")) {
            if (mc.player.onGround() && MoveUtils.isMoving()) {
                MoveUtils.strafe(0.4);
                mc.player.jumpFromGround();
            }

            if (PlayerUtil.getOffGroundTicks() == 1
                    || (PlayerUtil.blockRelativeToPlayer(0, mc.player.getDeltaMovement().y, 0)
                    != Blocks.AIR && PlayerUtil.getOffGroundTicks() > 2)) {
                MoveUtils.strafe();
            }
        }
    }
}
