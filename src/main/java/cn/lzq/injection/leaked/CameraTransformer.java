package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.Camera;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.modules.impl.render.BetterCamera;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class CameraTransformer extends ASMTransformer implements Wrapper {
    public CameraTransformer() {
        super(Camera.class);
    }

    public static boolean doNoClip() {
        return BetterCamera.INSTANCE != null && BetterCamera.INSTANCE.isEnabled() && BetterCamera.INSTANCE.npClip.getValue() && mc.player != null && mc.level != null;
    }

    @Inject(method = "getMaxZoom", desc = "(D)D")
    private void getMaxZoom(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        LabelNode continueLabel = new LabelNode();

        instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        Type.getInternalName(CameraTransformer.class),
                        "doNoClip",
                        "()Z",
                        false
                )
        );

        instructions.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        instructions.add(new VarInsnNode(Opcodes.DLOAD, 1));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CameraTransformer.class), "noClip", "(D)D", false));
        instructions.add(new InsnNode(Opcodes.DRETURN));
        instructions.add(continueLabel);

        methodNode.instructions.insert(instructions);
    }

    public static double noClip(double ignore) {
        return BetterCamera.INSTANCE.cameraDistance.getValue().doubleValue();
    }
}
