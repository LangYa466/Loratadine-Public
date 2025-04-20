package shop.xmz.lol.loratadine.utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class FuckerUtils implements Wrapper {
    private static boolean loaded = false;

    public static void a() {
        if (mc.level == null || mc.player == null) return;

        loaded();
    }

    private static void loaded() {
        if (!loaded) {
            if (mc.player == null || mc.level == null) return;

            loaded = true;

            if (!Loratadine.isDllInject) Loratadine.INSTANCE.loadClientResource();
        }
    }

    public static boolean onPacket(Object packet, Event.Side side) {
        if (mc.level == null || mc.player == null || packet == null) return false;

        if (packet instanceof Packet<?> wrapper) {
            if (PacketUtils.handleSendPacket((Packet<ServerGamePacketListener>) packet)) return true;

            final PacketEvent event = new PacketEvent(side, wrapper);

            Loratadine.INSTANCE.getEventManager().call(event);

            return event.isCancelled();
        }
        return false;
    }
}
