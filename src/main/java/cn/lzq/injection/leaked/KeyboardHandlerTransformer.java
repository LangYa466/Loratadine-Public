package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.KeyEvent;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import shop.xmz.lol.loratadine.Loratadine;

public class KeyboardHandlerTransformer extends ASMTransformer {
    public KeyboardHandlerTransformer() {
        super(KeyboardHandler.class);
    }

    public static void onKey(int key, int action) {
        if (action == GLFW.GLFW_PRESS/* && key != GLFW.GLFW_KEY_UNKNOWN*/) {
            Loratadine.INSTANCE.getEventManager().call(new KeyEvent(key));
        }
    }

    @Inject(method = "keyPress", desc = "(JIIII)V")
    public void keyPress(MethodNode methodNode) {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ILOAD, 3));
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(KeyboardHandlerTransformer.class), "onKey", "(II)V"));

        methodNode.instructions.insert(list);
    }
}
