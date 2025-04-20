package shop.xmz.lol.loratadine.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;

@AllArgsConstructor
@Getter
public class PacketEvent extends CancellableEvent {
    private final Side side; // Post是客户端发包 Pre是收服务端发过来的包
    private final Packet<?> packet;
}
