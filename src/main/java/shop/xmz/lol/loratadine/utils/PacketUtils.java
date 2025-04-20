package shop.xmz.lol.loratadine.utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.ArrayList;

public class PacketUtils implements Wrapper {
    private static final ArrayList<Packet<ServerGamePacketListener>> packets = new ArrayList<>();

    public static boolean handleSendPacket(Packet<ServerGamePacketListener> packet) {
        if (packets.contains(packet)) {
            packets.remove(packet);
            return true;
        }
        return false;
    }

    public static void sendPacket(Packet<ServerGamePacketListener> packet) {
        if(mc.player == null) return;

        mc.player.connection.send(packet);
    }

    public static void sendPacketNoEvent(Packet<ServerGamePacketListener> packet) {
        if(mc.player == null) return;

        packets.add(packet);
        mc.player.connection.send(packet);
    }
}
