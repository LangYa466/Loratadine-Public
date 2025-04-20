package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.MotionEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.PacketUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

public class Sneak extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"GrimAC Legacy", "Heypixel"}, "GrimAC Legacy");

    public Sneak() {
        super("Sneak", "下蹲", Category.MOVEMENT);
    }

    private boolean wasSneaking = false;

    @Override
    protected void onEnable() {
        wasSneaking = WrapperUtils.getWasSneaking();
    }

    @Override
    protected void onDisable() {
        if (wasSneaking) PacketUtils.sendPacketNoEvent(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        wasSneaking = false;
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null) return;

        if (mode.is("GrimAC Legacy")) {
            if (event.post) {
                if (wasSneaking) PacketUtils.sendPacketNoEvent(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                wasSneaking = false;
            } else {
                if (!wasSneaking) PacketUtils.sendPacketNoEvent(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                wasSneaking = true;
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getSide() == Event.Side.POST) {
            final Packet<?> packet = event.getPacket();

            if (packet instanceof ServerboundPlayerCommandPacket wrapper) {
                if (wrapper.getAction() == ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY || wrapper.getAction() == ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY)
                    event.setCancelled(true);
            }

            if (mode.is("Heypixel")) {
                if (packet instanceof ServerboundInteractPacket wrapper) {
                    if (!wrapper.isUsingSecondaryAction()) WrapperUtils.setUsingSecondaryAction(wrapper, true);
                }
            }
        }
    }
}
