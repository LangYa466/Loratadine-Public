package cn.lzq.injection.leaked.invoked;

import lombok.AllArgsConstructor;
import shop.xmz.lol.loratadine.event.impl.Event;

@AllArgsConstructor
public class LookEvent implements Event {
    public float rotationYaw, rotationPitch;
}
