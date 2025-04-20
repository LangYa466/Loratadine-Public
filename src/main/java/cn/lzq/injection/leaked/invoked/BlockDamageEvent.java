package cn.lzq.injection.leaked.invoked;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import shop.xmz.lol.loratadine.event.impl.CancellableEvent;

@Getter
@Setter
public final class BlockDamageEvent extends CancellableEvent {
    private LocalPlayer player;
    private ClientLevel world;
    private BlockPos blockPos;

    public BlockDamageEvent(final LocalPlayer player, final ClientLevel world, final BlockPos blockPos) {
        this.player = player;
        this.world = world;
        this.blockPos = blockPos;
    }
}