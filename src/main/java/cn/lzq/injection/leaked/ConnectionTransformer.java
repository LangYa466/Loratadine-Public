package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.utils.FuckerUtils;

public class ConnectionTransformer extends ASMTransformer {
    public ConnectionTransformer() {
        super(Connection.class);
    }

    @Inject(method = "sendPacket", desc = "(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V")
    private void sendPacket(MethodNode node) {
        InsnList instructions = new InsnList();

        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(ConnectionTransformer.class),
                "packetSend",
                "(Lnet/minecraft/network/protocol/Packet;)Z",
                false
        ));

        LabelNode continueLabel = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(continueLabel);

        node.instructions.insert(instructions);
    }

    public static boolean packetSend(Packet<?> packet) {
        return Loratadine.INSTANCE != null && FuckerUtils.onPacket(packet, Event.Side.POST);
    }
}
