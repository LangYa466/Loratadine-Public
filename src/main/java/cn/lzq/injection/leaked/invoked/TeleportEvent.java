package cn.lzq.injection.leaked.invoked;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.xmz.lol.loratadine.event.impl.Event;

@Getter
@Setter
@AllArgsConstructor
public class TeleportEvent implements Event {
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
}
