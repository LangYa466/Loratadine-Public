package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.PlaceEvent;
import cn.lzq.injection.leaked.invoked.TickEvent;
import cn.lzq.injection.leaked.invoked.WorldEvent;
import cn.lzq.injection.leaked.mapping.Mapping;
import net.minecraft.client.player.LocalPlayer;
import shop.xmz.lol.loratadine.Loratadine;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.utils.FuckerUtils;

public class MinecraftTransformer extends ASMTransformer {
    public MinecraftTransformer() {
        super(Minecraft.class);
    }

    public static void onPlace() {
        Loratadine.INSTANCE.getEventManager().call(new PlaceEvent());
    }

    public static void handleTickEvent() {
        FuckerUtils.a();
        Loratadine.INSTANCE.getEventManager().call(new TickEvent());
    }

    public static void handleWorldEvent() {
        Loratadine.INSTANCE.getEventManager().call(new WorldEvent());
    }

    public static void shutdownClient() {
        Loratadine.INSTANCE.shutdown();
    }

    @Inject(method = "runTick", desc = "(Z)V")
    private void injectTickEvent(MethodNode node) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class), "handleTickEvent", "()V"));
        node.instructions.insert(list);
    }

    @Inject(method = "close", desc = "()V")
    private void injectClientClose(MethodNode node) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class), "shutdownClient", "()V"));
        node.instructions.insert(list);
    }

    @Inject(method = "setLevel", desc = "(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
    private void injectWorldEvent(MethodNode node) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class), "handleWorldEvent", "()V"));
        node.instructions.insert(list);
    }

    @Inject(method = "handleKeybinds", desc = "()V")
    public void handleKeybinds(MethodNode methodNode) {
        InsnList list = new InsnList();
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class), "onPlace", "()V"));

        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            final AbstractInsnNode node = methodNode.instructions.get(i);

            if (node instanceof JumpInsnNode) {
                final AbstractInsnNode lastNode = methodNode.instructions.get(i - 1);

                if (lastNode instanceof MethodInsnNode lastWrappedNode) {
                    if (lastWrappedNode.name.equals(Mapping.get(LocalPlayer.class, "isUsingItem", "()Z"))) { // isUsingItem
                        final AbstractInsnNode aload_0 = methodNode.instructions.get(i - 3);
                        methodNode.instructions.insertBefore(aload_0, list);
                    }
                }
            }
        }
    }
}
