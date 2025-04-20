package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.invoked.MoveInputEvent;
import cn.lzq.injection.leaked.mapping.Mapping;
import net.minecraft.client.player.Input;
import shop.xmz.lol.loratadine.Loratadine;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import net.minecraft.client.player.KeyboardInput;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class KeyboardInputTransformer extends ASMTransformer {
    public KeyboardInputTransformer() {
        super(KeyboardInput.class);
    }

    public static MoveInputEvent onMoveInput(float forwardImpulse, float leftImpulse, boolean keyJump, boolean keyShift) {
        return (MoveInputEvent) Loratadine.INSTANCE.getEventManager().call(new MoveInputEvent(forwardImpulse, leftImpulse, keyJump, keyShift));
    }

    @Inject(method = "tick", desc = "(ZF)V")
    public void tick(MethodNode methodNode) {
        InsnList list = new InsnList();

        // Get the current input values
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "forwardImpulse", null), "F"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "leftImpulse", null), "F"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "jumping", null), "Z"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "shiftKeyDown", null), "Z"));

        // Call our event method
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(KeyboardInputTransformer.class), "onMoveInput", "(FFZZ)L" + MoveInputEvent.class.getName().replace(".", "/") + ";"));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3)); // Using local variable 3 for the event

        // Apply the potentially modified values back
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, MoveInputEvent.class.getName().replace(".", "/"), "forwardImpulse", "F"));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "forwardImpulse", null), "F"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, MoveInputEvent.class.getName().replace(".", "/"), "leftImpulse", "F"));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "leftImpulse", null), "F"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, MoveInputEvent.class.getName().replace(".", "/"), "keyJump", "Z"));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "jumping", null), "Z"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, MoveInputEvent.class.getName().replace(".", "/"), "keyShift", "Z"));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/player/Input", Mapping.get(Input.class, "shiftKeyDown", null), "Z"));

        // Find the appropriate injection point - after setting input values but before the conditional scaling
        boolean foundTarget = false;
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode node = methodNode.instructions.get(i);

            // Look for the field assignment to shiftKeyDown, which is the last input value set before the conditional
            if (node instanceof FieldInsnNode && node.getOpcode() == Opcodes.PUTFIELD) {
                FieldInsnNode fieldNode = (FieldInsnNode) node;
                if (fieldNode.name.equals(Mapping.get(Input.class, "shiftKeyDown", null))) {
                    // Insert our code after this instruction
                    methodNode.instructions.insert(node, list);
                    foundTarget = true;
                    break;
                }
            }
        }

        if (!foundTarget) {
            // Fallback: find the instruction right before the if-statement
            for (int i = 0; i < methodNode.instructions.size(); ++i) {
                AbstractInsnNode node = methodNode.instructions.get(i);

                if (node instanceof JumpInsnNode) {
                    // This is likely the if-condition jump
                    AbstractInsnNode prev = node.getPrevious();
                    if (prev != null) {
                        methodNode.instructions.insertBefore(prev, list);
                        break;
                    }
                }
            }
        }
    }
}
