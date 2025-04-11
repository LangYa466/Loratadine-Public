package shop.xmz.lol.loratadine.movement;

import net.minecraft.client.KeyMapping;
import shop.xmz.lol.loratadine.player.Stuck;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "疾跑" ,Category.MOVEMENT);

        setEnabled(true);
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        Stuck stuck = (Stuck) Loratadine.INSTANCE.getModuleManager().getModule(Stuck.class);

        if (stuck.isEnabled()) return;

        KeyMapping.set(mc.options.keySprint.getKey(), true);
    }

    @Override
    public void onDisable() {
        mc.options.keySprint.setDown(mc.options.keySprint.isDown());
    }
}