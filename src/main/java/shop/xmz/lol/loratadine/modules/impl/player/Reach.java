package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.MouseOverEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;

public class Reach extends Module {
    public final NumberSetting range = new NumberSetting("Range",this,3,3,6,0.1);
    public Reach() {
        super("Reach","距离", Category.PLAYER);
    }

    @EventTarget
    public void onMouseOverEvent(MouseOverEvent event) {
        event.setRange(Math.pow(range.getValue().doubleValue(), 2));
    }
}