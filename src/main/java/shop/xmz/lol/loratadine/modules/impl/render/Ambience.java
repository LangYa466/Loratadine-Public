package shop.xmz.lol.loratadine.modules.impl.render;

import cn.lzq.injection.leaked.invoked.TickEvent;
import cn.lzq.injection.leaked.invoked.UpdateEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;

public class Ambience extends Module {
    private final ModeSetting mode = new ModeSetting("Time Mode", this, new String[]{"Static", "Cycle"}, "Static");
    private final ModeSetting weatherMode = new ModeSetting("Weather Mode", this, new String[]{"Clear", "Rain"}, "Clear");
    private final NumberSetting cycleSpeed = new NumberSetting("Cycle Speed", this, 24.0, 1.0, 24.0, 1.0);
    private final BooleanSetting reverseCycle = new BooleanSetting("Reverse Cycle", this, false);
    private final NumberSetting time = new NumberSetting("Static Time", this, 24000.0, 0.0, 24000.0, 100.0);
    private final NumberSetting rainStrength = new NumberSetting("Rain Strength", this, 0.1, 0.1, 0.5, 0.05);
    private long timeCycle = 0;

    public Ambience() {
        super("Ambience", "修改时间", Category.RENDER);
    }

    @Override
    public void onEnable() {
        timeCycle = 0;
    }

    @EventTarget
    public void onTickEvent(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (mode.is("Static")) {
            mc.level.setDayTime(time.getValue().longValue());
        } else {
            mc.level.setDayTime(timeCycle);
            timeCycle += (reverseCycle.getValue() ? -cycleSpeed.getValue().longValue() : cycleSpeed.getValue().longValue()) * 10;

            if (timeCycle > 24000) {
                timeCycle = 0;
            } else if (timeCycle < 0) {
                timeCycle = 24000;
            }
        }

        if (weatherMode.is("Clear")) {
            mc.level.rainLevel = 0f;
        } else {
            mc.level.rainLevel = rainStrength.getValue().floatValue();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundSetTimePacket) {
            event.setCancelled(true);
        }
    }
}
