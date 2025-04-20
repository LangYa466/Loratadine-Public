package cn.lzq.injection.leaked.invoked;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import shop.xmz.lol.loratadine.event.impl.CancellableEvent;

@Getter
public class AttackEvent extends CancellableEvent {
    private final Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }
}