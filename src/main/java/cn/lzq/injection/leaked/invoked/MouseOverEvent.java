package cn.lzq.injection.leaked.invoked;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.HitResult;
import shop.xmz.lol.loratadine.event.impl.Event;

@Getter
@Setter
public class MouseOverEvent implements Event {

    public MouseOverEvent(double range) {
        this.range = range;
    }

    private double range;
    private HitResult movingObjectPosition;
}
