package shop.xmz.lol.loratadine.modules.impl.misc;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.impl.player.ChestStealer;
import shop.xmz.lol.loratadine.modules.impl.player.InvCleaner;
import shop.xmz.lol.loratadine.modules.impl.player.Scaffold;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;

public class ModuleHelper extends Module {
    public ModuleHelper() {
        super("ModuleHelper", "模块助手", Category.MISC);
    }

    private final BooleanSetting lagBackCheckValue = new BooleanSetting("LagBack Check", this, false);

    @EventTarget
    public void onPacketReceive(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundLoginPacket || (packet instanceof ClientboundPlayerPositionPacket && lagBackCheckValue.getValue())) {
            disableModule();
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        disableModule();
    }

    public void disableModule() {
        if (Loratadine.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled())
            Loratadine.INSTANCE.getModuleManager().getModule(KillAura.class).setEnabled(false);
        if (Loratadine.INSTANCE.getModuleManager().getModule(InvCleaner.class).isEnabled())
            Loratadine.INSTANCE.getModuleManager().getModule(InvCleaner.class).setEnabled(false);
        if (Loratadine.INSTANCE.getModuleManager().getModule(ChestStealer.class).isEnabled())
            Loratadine.INSTANCE.getModuleManager().getModule(ChestStealer.class).setEnabled(false);
        if (Loratadine.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled())
            Loratadine.INSTANCE.getModuleManager().getModule(Scaffold.class).setEnabled(false);
    }

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        if (mc.player == null) return;

        if (mc.player.isSpectator() || !mc.player.isAlive() || mc.player.isDeadOrDying()) {
            if (Loratadine.INSTANCE.getModuleManager().getModule(InvCleaner.class).isEnabled())
                Loratadine.INSTANCE.getModuleManager().getModule(InvCleaner.class).setEnabled(false);
            if (Loratadine.INSTANCE.getModuleManager().getModule(ChestStealer.class).isEnabled())
                Loratadine.INSTANCE.getModuleManager().getModule(ChestStealer.class).setEnabled(false);
        }
    }
}
