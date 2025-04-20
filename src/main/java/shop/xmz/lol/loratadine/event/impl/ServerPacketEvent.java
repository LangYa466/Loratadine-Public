package shop.xmz.lol.loratadine.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

@AllArgsConstructor
@Getter
public class ServerPacketEvent implements Event {
    private Packet<ClientGamePacketListener> packet;
}
