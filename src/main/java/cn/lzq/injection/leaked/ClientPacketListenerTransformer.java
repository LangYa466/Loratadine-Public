package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.TeleportEvent;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class ClientPacketListenerTransformer extends ASMTransformer implements Wrapper {
    public ClientPacketListenerTransformer() {
        super(ClientPacketListener.class);
    }

    @Inject(method = "handleMovePlayer", desc = "(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V")
    public void handleMovePlayer(MethodNode methodNode) {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ClientPacketListenerTransformer.class), "onTeleport", "(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V"));

        methodNode.instructions.insert(list);
    }

    public static void onTeleport(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {
        Loratadine.INSTANCE.getEventManager().call(new TeleportEvent(
                clientboundPlayerPositionPacket.getX(),
                clientboundPlayerPositionPacket.getY(),
                clientboundPlayerPositionPacket.getZ(),
                clientboundPlayerPositionPacket.getYRot(),
                clientboundPlayerPositionPacket.getXRot()));
    }
}
