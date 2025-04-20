package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.Event;
import shop.xmz.lol.loratadine.event.impl.ServerPacketEvent;
import shop.xmz.lol.loratadine.modules.impl.misc.Disabler;
import shop.xmz.lol.loratadine.utils.FuckerUtils;

public class PacketUtilsTransformer extends ASMTransformer {
    public PacketUtilsTransformer() {
        super(PacketUtils.class);
    }

    @Inject(method = "lambda$ensureRunningOnSameThread$0",
            desc = "(Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/protocol/Packet;)V")
    public void ensureRunningOnSameThread(MethodNode node) {
        AbstractInsnNode targetNode = null;
        for (AbstractInsnNode insn : node.instructions) {
            if (insn instanceof MethodInsnNode methodInsn
                    && methodInsn.name.equals(Mapping.get(Packet.class, "handle", "(Lnet/minecraft/network/PacketListener;)V"))) {
                targetNode = insn;
                break;
            }
        }

        InsnList toInsert = new InsnList();

        toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
        toInsert.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(PacketUtilsTransformer.class),
                "postDisablerInGameTick",
                "(Lnet/minecraft/network/protocol/Packet;)V",
                false
        ));

        toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
        toInsert.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(PacketUtilsTransformer.class),
                "postDisabler",
                "(Lnet/minecraft/network/protocol/Packet;)Z",
                false
        ));

        LabelNode postDisableFalse = new LabelNode();
        toInsert.add(new JumpInsnNode(Opcodes.IFEQ, postDisableFalse));
        toInsert.add(new InsnNode(Opcodes.RETURN));
        toInsert.add(postDisableFalse);

        toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
        toInsert.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(PacketUtilsTransformer.class),
                "packetReceived",
                "(Lnet/minecraft/network/protocol/Packet;)Z",
                false
        ));

        LabelNode packetReceivedFalse = new LabelNode();
        toInsert.add(new JumpInsnNode(Opcodes.IFEQ, packetReceivedFalse));
        toInsert.add(new InsnNode(Opcodes.RETURN));
        toInsert.add(packetReceivedFalse);

        node.instructions.insertBefore(targetNode, toInsert);
    }

    public static boolean postDisabler(Packet<?> packet) {
        if (Disabler.INSTANCE != null
                && Disabler.INSTANCE.isEnabled()
                && Disabler.INSTANCE.usePost()
                && Disabler.INSTANCE.delayPacket(packet)) {
            final Packet<ClientGamePacketListener> wrapped = (Packet<ClientGamePacketListener>) packet;
            Loratadine.INSTANCE.getEventManager().call(new ServerPacketEvent(wrapped));

            Disabler.INSTANCE.packets.add(wrapped);

            return true;
        }

        return false;
    }

    public static boolean packetReceived(Packet<?> packet) {
        return Loratadine.INSTANCE != null && FuckerUtils.onPacket(packet, Event.Side.PRE);
    }

    public static void postDisablerInGameTick(Packet<?> packet) {
        if (Disabler.INSTANCE != null && Disabler.INSTANCE.isEnabled() && Disabler.INSTANCE.post.getValue()) {
            if (packet instanceof ClientboundPingPacket) if (Disabler.INSTANCE.inGameTick < 60) Disabler.INSTANCE.inGameTick++;
        }
    }
}
