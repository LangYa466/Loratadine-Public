package cn.lzq.injection.leaked.invoked;

import lombok.AllArgsConstructor;
import shop.xmz.lol.loratadine.event.impl.Event;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

@AllArgsConstructor
public class RenderPlayerEvent implements Event {
    public PlayerRenderer renderer;
    public float rotationYaw, rotationPitch;
}
