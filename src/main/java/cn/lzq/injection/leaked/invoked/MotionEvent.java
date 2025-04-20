package cn.lzq.injection.leaked.invoked;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class MotionEvent extends CancellableEvent {
    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;
    public boolean post;
}