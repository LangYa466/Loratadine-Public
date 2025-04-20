package cn.lzq.injection.leaked;

import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.impl.CancellableEvent;
import shop.xmz.lol.loratadine.event.impl.RenderNameplateEvent;

public class EntityRendererTransformer extends ASMTransformer {
    public EntityRendererTransformer() {
        super(EntityRenderer.class);
    }

    public static RenderNameplateEvent onRenderNameplate(Entity entity, EntityRenderer<?> entityRenderer, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        return (RenderNameplateEvent) Loratadine.INSTANCE.getEventManager().call(new RenderNameplateEvent(entity, entityRenderer, partialTicks, poseStack, multiBufferSource, packedLight));
    }

    @Inject(method = "render", desc = "(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void render(MethodNode methodNode) {
        // First, analyze the method to find the highest used local variable index
        int maxVarIndex = 6; // Start with the known parameters (0-6)
        for (AbstractInsnNode insn : methodNode.instructions) {
            if (insn instanceof VarInsnNode varInsn) {
                maxVarIndex = Math.max(maxVarIndex, varInsn.var);
            }
        }

        // Use the next available index for our event variable
        int eventVarIndex = maxVarIndex + 1;

        InsnList insnNodes = new InsnList();

        // Load all parameters for the onRenderNameplate call
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 1)); // p_114485_ (Entity)
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this (EntityRenderer)
        insnNodes.add(new VarInsnNode(Opcodes.FLOAD, 3)); // var3 (float partialTicks)
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 4)); // p_114488_ (PoseStack)
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, 5)); // p_114489_ (MultiBufferSource)
        insnNodes.add(new VarInsnNode(Opcodes.ILOAD, 6)); // p_114490_ (int packedLight) - Note: changed ALOAD to ILOAD for int parameter

        // Call the static onRenderNameplate method
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(EntityRendererTransformer.class),
                "onRenderNameplate",
                "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/EntityRenderer;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)L" + RenderNameplateEvent.class.getName().replace(".", "/") + ";",
                false));

        // Store the event in a new local variable to avoid conflict
        insnNodes.add(new VarInsnNode(Opcodes.ASTORE, eventVarIndex));

        // Load the event to check if it's cancelled
        insnNodes.add(new VarInsnNode(Opcodes.ALOAD, eventVarIndex));

        // Call isCancelled method
        insnNodes.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(CancellableEvent.class),
                "isCancelled",
                "()Z",
                false));

        // If not cancelled, continue to the original method; otherwise return early
        LabelNode continueLabel = new LabelNode();
        insnNodes.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
        insnNodes.add(new InsnNode(Opcodes.RETURN));
        insnNodes.add(continueLabel);

        // Insert our instructions at the beginning of the method
        methodNode.instructions.insert(insnNodes);
    }
}
