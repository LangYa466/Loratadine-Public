package shop.xmz.lol.loratadine.modules.impl.player;

import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

public class FastBreak extends Module {
    public FastBreak() {
        super("FastBreak", "快速挖掘" ,Category.PLAYER);
    }
    private final NumberSetting breakDamage = new NumberSetting("BreakDamage",this ,0.8, 0.1, 1, 0.1);

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        WrapperUtils.setDestroyDelay(0);

        if (WrapperUtils.getDestroyProgress() > breakDamage.getValue().floatValue())
            WrapperUtils.setDestroyProgress(1F);
    }
}
