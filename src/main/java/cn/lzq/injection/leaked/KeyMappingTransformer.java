package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.KeyMapping;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.movement.InvMove;

import static org.objectweb.asm.Opcodes.*;

public class KeyMappingTransformer extends ASMTransformer {
    public KeyMappingTransformer() {
        super(KeyMapping.class);
    }

    @Inject(method = "isDown", desc = "()Z")
    public void isDown(MethodNode node) {
        InsnList insnList = new InsnList();

        // 调用静态方法
        insnList.add(new MethodInsnNode(
                INVOKESTATIC,
                Type.getInternalName(KeyMappingTransformer.class),
                "guiMove",
                "()Z",
                false
        ));

        // 创建跳转标签
        LabelNode elseLabel = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, elseLabel));

        // true分支
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new FieldInsnNode(
                GETFIELD,
                "net/minecraft/client/KeyMapping",
                Mapping.get(KeyMapping.class, "isDown", null),
                "Z"
        ));
        insnList.add(new InsnNode(IRETURN));

        // false分支
        insnList.add(elseLabel);

        // 插入到方法最前面
        node.instructions.insert(insnList);
    }

    public static boolean guiMove() {
        return Loratadine.INSTANCE != null
                && Loratadine.INSTANCE.getModuleManager() != null
                && Loratadine.INSTANCE.getModuleManager().getModule(InvMove.class) != null
                && Loratadine.INSTANCE.getModuleManager().getModule(InvMove.class).isEnabled();
    }
}
