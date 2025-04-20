package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;

public class Velocity extends Module {
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Vanilla", "Jump Reset"}, "Jump Reset");

    private final BooleanSetting onlyMove = new BooleanSetting("OnlyMove",this,false);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround",this,false);

    private final BooleanSetting flagCheckValue = new BooleanSetting("FlagCheck",this,false);
    public final NumberSetting flagTickValue = new NumberSetting("FlagTicks",this,6, 0, 30, 1);
    public final BooleanSetting debugMessageValue = new BooleanSetting("FlagDebugMessage",this,false);
    int flags;

    public Velocity() {
        super("Velocity", "反击退", Category.COMBAT);
        this.setEnabled(true);
    }

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (flagCheckValue.getValue()) {
            if (flags > 0) flags--;
        }

        if (mc.player.isDeadOrDying() || mc.player.getHealth() <= 0) return;

        if (mc.player.onGround() && mc.player.hurtTime > 0) {
            mc.player.setSprinting(false);
            mc.player.input.jumping = true;
        }

        setSuffix(mode_Value.getValue());
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.level == null || mc.player == null) return;

        Packet<?> packet = event.getPacket();

        if ((onlyGround.getValue() && !mc.player.onGround()) || (onlyMove.getValue() && !MoveUtils.isMoving()) || flags != 0) {
            return;
        }

        if (packet instanceof ClientboundPlayerPositionPacket && flagCheckValue.getValue()) {
            flags = flagTickValue.getValue().intValue();
            if (debugMessageValue.getValue()) ClientUtils.log("[Velocity]Debug Flags.");
        }
    }
}