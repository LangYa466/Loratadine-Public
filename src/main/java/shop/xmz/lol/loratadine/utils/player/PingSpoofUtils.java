package shop.xmz.lol.loratadine.utils.player;

import cn.lzq.injection.leaked.invoked.MotionEvent;
import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.utils.StopWatch;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PingSpoofUtils implements Wrapper {
    public static ConcurrentLinkedQueue<TimedPacket> packets = new ConcurrentLinkedQueue<>();
    static StopWatch enabledTimer = new StopWatch();
    public static boolean enabled;
    static long amount;

    static PacketType<ServerboundPongPacket> regular = new PacketType<>(new Class[]{ServerboundPongPacket.class, ServerboundKeepAlivePacket.class}, false);
    static PacketType<ClientboundSetEntityMotionPacket> velocity = new PacketType<>(new Class[]{ClientboundSetEntityMotionPacket.class, ClientboundExplodePacket.class}, false);
    static PacketType<ClientboundPlayerPositionPacket> teleports = new PacketType<>(new Class[]{ClientboundPlayerPositionPacket.class, ClientboundPlayerAbilitiesPacket.class, ClientboundSetCarriedItemPacket.class}, false);
    static PacketType<ClientboundRemoveEntitiesPacket> players = new PacketType<>(new Class[]{ClientboundRemoveEntitiesPacket.class, ClientboundMoveEntityPacket.class, ClientboundMoveEntityPacket.Rot.class, ClientboundMoveEntityPacket.Pos.class, ClientboundMoveEntityPacket.PosRot.class, ClientboundTeleportEntityPacket.class, ClientboundUpdateAttributesPacket.class, ClientboundRotateHeadPacket.class}, false);
    static PacketType<ServerboundInteractPacket> blink = new PacketType<>(new Class[]{ServerboundInteractPacket.class, ServerboundContainerClosePacket.class, ServerboundContainerClickPacket.class, ServerboundMovePlayerPacket.class, ServerboundMovePlayerPacket.Pos.class, ServerboundMovePlayerPacket.Rot.class, ServerboundMovePlayerPacket.PosRot.class, ServerboundPlayerActionPacket.class, ServerboundUseItemPacket.class, ServerboundUseItemOnPacket.class, ServerboundSetCarriedItemPacket.class, ServerboundPlayerAbilitiesPacket.class, ServerboundClientInformationPacket.class, ServerboundClientCommandPacket.class, ServerboundCustomPayloadPacket.class, ServerboundSwingPacket.class}, false);
    static PacketType<ServerboundMovePlayerPacket> movement = new PacketType<>(new Class[]{ServerboundMovePlayerPacket.class, ServerboundMovePlayerPacket.Pos.class, ServerboundMovePlayerPacket.Rot.class, ServerboundMovePlayerPacket.PosRot.class}, false);

    static PacketType<?>[] types = new PacketType[]{regular, velocity, teleports, players, blink, movement};

    @EventTarget
    public void onPacketC(PacketEvent event) {
        if (event.getSide() == Event.Side.POST)
            event.setCancelled(onPacket(event.getPacket(), event).isCancelled());
    }

    @EventTarget
    public void onPacketS(PacketEvent event) {
        if (event.getSide() == Event.Side.PRE)
            event.setCancelled(onPacket(event.getPacket(), event).isCancelled());
    }

    public PacketEvent onPacket(Packet<?> packet, PacketEvent event) {
        if (!event.isCancelled() && enabled && Arrays.stream(types).anyMatch(tuple -> tuple.enabled && Arrays.stream(tuple.packetClasses).anyMatch(regularpacket -> regularpacket == packet.getClass()))) {
            event.setCancelled(true);
            packets.add(new TimedPacket(packet));
        }
        return event;
    }

    public static void dispatch() {
        if (!packets.isEmpty()) {
            // 防止数据包被调用两次
            boolean wasEnabled = enabled;
            enabled = false;
            packets.forEach(timedPacket -> mc.getConnection().send(timedPacket.getPacket()));
            enabled = wasEnabled;
            packets.clear();
        }
    }

    public static void disable() {
        enabled = false;
        enabledTimer.setMillis(enabledTimer.millis - 999999999);
    }

    @EventTarget
    public void onWorldChange(WorldEvent event) {
        dispatch();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPost()) {
            if (!(enabled = !enabledTimer.finished(100) && !(mc.screen instanceof ReceivingLevelScreen))) {
                dispatch();
            } else {
                // 防止数据包被调用两次
                enabled = false;

                packets.forEach(packet -> {
                    if (packet.getMillis() + amount < System.currentTimeMillis()) {
                        mc.getConnection().send(packet.getPacket());
                        packets.remove(packet);
                    }
                });

                enabled = true;
            }
        }
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players) {
        spoof(amount, regular, velocity, teleports, players, false);
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players, boolean blink, boolean movement) {
        enabledTimer.reset();

        PingSpoofUtils.regular.enabled = regular;
        PingSpoofUtils.velocity.enabled = velocity;
        PingSpoofUtils.teleports.enabled = teleports;
        PingSpoofUtils.players.enabled = players;
        PingSpoofUtils.blink.enabled = blink;
        PingSpoofUtils.movement.enabled = movement;
        PingSpoofUtils.amount = amount;
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players, boolean blink) {
        spoof(amount, regular, velocity, teleports, players, blink, false);
    }

    public static void blink() {
        spoof(9999999, true, false, false, false, true);
    }

    public static class PacketType<T extends Packet<?>> {
        private final Class<?>[] packetClasses;
        private boolean enabled;

        public PacketType(Class<?>[] packetClasses, boolean enabled) {
            this.packetClasses = packetClasses;
            this.enabled = enabled;
        }
    }
}
