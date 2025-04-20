package shop.xmz.lol.loratadine.modules.impl.misc;

import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.utils.FuckerUtils;

import java.util.concurrent.LinkedBlockingQueue;

public class Disabler extends Module {
    public static Disabler INSTANCE;
    public final BooleanSetting post = new BooleanSetting("Post", this, false);
    public LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new LinkedBlockingQueue<>();
    private boolean lastResult = false;
    public int inGameTick;

    public Disabler() {
        super("Disabler", "禁用器", Category.MISC);
        setEnabled(true);
        INSTANCE = this;
    }

    public boolean usePost() {
        boolean result = isEnabled()
                && post.getValue()
                && mc.player != null
                && mc.level != null
                && mc.player.isAlive()
                && inGameTick >= 60
                && !(mc.screen instanceof ProgressScreen);
        if (lastResult && !result) {
            lastResult = false;

            release();
            reset();
        }
        lastResult = result;
        return result;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundLoginPacket) {
            release();
            reset();
        }
    }

    @Override
    protected void onEnable() {
        setSuffix("Grim");

        release();
        reset();
    }

    @Override
    protected void onDisable() {
        release();
        reset();
    }

    @EventTarget
    public void onWorldChange(WorldEvent event) {
        release();
        reset();
    }

    public void reset() {
        inGameTick = 0;
        packets.clear();
    }

    public void release() {
        ClientGamePacketListener connection = mc.getConnection();

        if (connection == null) return;

        if (!packets.isEmpty()) {
            for (Packet<ClientGamePacketListener> packet : packets) {
                if (FuckerUtils.onPacket(packet, Event.Side.PRE)) continue;

                try {
                    packet.handle(connection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            packets.clear();
        }
    }

    public boolean delayPacket(Packet<?> packet) {
        return packet instanceof ClientboundExplodePacket
                || packet instanceof ClientboundSetEntityMotionPacket
                || packet instanceof ClientboundSetEntityDataPacket
                || packet instanceof ClientboundInitializeBorderPacket
                || packet instanceof ClientboundContainerSetSlotPacket
                || packet instanceof ClientboundUpdateAttributesPacket
                || packet instanceof ClientboundUpdateEnabledFeaturesPacket
                || packet instanceof ClientboundAddPlayerPacket
                || packet instanceof ClientboundPingPacket
                || packet instanceof ClientboundPongResponsePacket
                || packet instanceof ClientboundPlayerPositionPacket
                || packet instanceof ClientboundTeleportEntityPacket
                || packet instanceof ClientboundEntityEventPacket
                || packet instanceof ClientboundSetEquipmentPacket
                || packet instanceof ClientboundPlayerInfoUpdatePacket
                || packet instanceof ClientboundPlayerInfoRemovePacket
                || packet instanceof ClientboundRemoveEntitiesPacket
                || packet instanceof ClientboundKeepAlivePacket
                || packet instanceof ClientboundSetHealthPacket
                || packet instanceof ClientboundMoveEntityPacket
                || packet instanceof ClientboundOpenScreenPacket
                || packet instanceof ClientboundContainerSetContentPacket
                || packet instanceof ClientboundCustomPayloadPacket
                || packet instanceof ClientboundLevelEventPacket
                || packet instanceof ClientboundRemoveMobEffectPacket
                || packet instanceof ClientboundAddEntityPacket
                || packet instanceof ClientboundTakeItemEntityPacket
                || packet instanceof ClientboundAnimatePacket
                || packet instanceof ClientboundPlayerAbilitiesPacket;
    }
}
