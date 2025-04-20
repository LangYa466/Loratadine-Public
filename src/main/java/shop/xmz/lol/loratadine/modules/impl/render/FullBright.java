package shop.xmz.lol.loratadine.modules.impl.render;

import cn.lzq.injection.leaked.invoked.TickEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;

public class FullBright extends Module {
    private double originalGamma;

    public FullBright() {
        super("FullBright", "夜视", Category.RENDER);
        setEnabled(true);
    }

    /*@Override
    public void onEnable() {
        if (mc.player == null || mc.level == null) return;

        originalGamma = mc.options.gamma().get();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.level == null) return;

        mc.options.gamma().set(originalGamma);
    }

    @EventTarget
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.level == null) return;

        if (mc.options.gamma().get() != 15.0D) {
            mc.options.gamma().set(15.0);
        }
    }*/
}