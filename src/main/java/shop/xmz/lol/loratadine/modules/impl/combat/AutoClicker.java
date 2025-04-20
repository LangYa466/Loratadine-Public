package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.AttackEvent;
import cn.lzq.injection.leaked.invoked.MotionEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.player.BlockUtils;
import shop.xmz.lol.loratadine.utils.player.PlayerUtil;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoClicker extends Module {
    private final NumberSetting maxCps = new NumberSetting("Max CPS", this, 14, 1, 20, 1);
    private final NumberSetting minCps = new NumberSetting("Min CPS", this, 10, 1, 20, 1);
    private final BooleanSetting rightClick = new BooleanSetting("Right Click", this, false);
    private final BooleanSetting leftClick = new BooleanSetting("Left Click", this, true);
    private final BooleanSetting hitSelect = new BooleanSetting("Hit Select", this, false);

    private final TimerUtils clickStopWatch = new TimerUtils();
    private int ticksDown, attackTicks;
    private long nextSwing;

    public AutoClicker() {
        super("AutoClicker", "自动点击", Category.COMBAT);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null || mc.level == null || event.post) return;

        WrapperUtils.setMissTime(0);

        this.attackTicks++;

        if (clickStopWatch.delay(this.nextSwing) && (!hitSelect.getValue() || attackTicks >= 10 || mc.player.hurtTime > 0 && clickStopWatch.delay(this.nextSwing)) && mc.screen == null) {
            final long clicks = (long) (Math.round(MathUtils.getRandomNumber(this.minCps.getValue().intValue(), this.maxCps.getValue().intValue())) * 1.5);

            if (mc.options.keyAttack.isDown()) {
                ticksDown++;
            } else {
                ticksDown = 0;
            }

            this.nextSwing = 1000 / clicks;

            if (rightClick.getValue() && mc.options.keyUse.isDown() && !mc.options.keyAttack.isDown()) {
                PlayerUtil.sendClick(1, true);

                if (Math.random() > 0.9) {
                    PlayerUtil.sendClick(1, true);
                }
            }

            if (leftClick.getValue() && ticksDown > 1 && (Math.sin(nextSwing) + 1 > Math.random() || Math.random() > 0.25 || clickStopWatch.delay(4 * 50)) && !mc.options.keyUse.isDown() && (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK || BlockUtils.isAirBlock(((BlockHitResult) mc.hitResult).getBlockPos()) || mc.hitResult.getType() == HitResult.Type.MISS)) {
                PlayerUtil.sendClick(0, true);
            }

            this.clickStopWatch.reset();
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        this.attackTicks = 0;
    }
}
