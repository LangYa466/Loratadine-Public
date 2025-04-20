package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.StrafeEvent;
import net.minecraft.client.KeyMapping;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "疾跑" ,Category.MOVEMENT);

        setEnabled(true);
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        KeyMapping.set(mc.options.keySprint.getKey(), true);
    }

    @Override
    public void onDisable() {
        mc.options.keySprint.setDown(mc.options.keySprint.isDown());
    }
}