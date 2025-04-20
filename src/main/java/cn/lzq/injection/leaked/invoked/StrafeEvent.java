package cn.lzq.injection.leaked.invoked;

import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.event.impl.Event;

@Setter
@AllArgsConstructor
public class StrafeEvent implements Event {
    public double motionX, motionY, motionZ;
    public float rotationYaw, slowSize;

    public Vec3 getMotion() {
        return new Vec3(motionX, motionY, motionZ);
    }
}
