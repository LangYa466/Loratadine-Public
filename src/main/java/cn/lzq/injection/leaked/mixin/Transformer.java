package cn.lzq.injection.leaked.mixin;

import shop.xmz.lol.loratadine.utils.NativeUtils;
import cn.lzq.injection.leaked.mapping.Mapping;
import cn.lzq.injection.leaked.mixin.transformer.Operation;
import cn.lzq.injection.leaked.mixin.transformer.impl.InjectOperation;
import cn.lzq.injection.leaked.mixin.utils.ASMTransformer;
import cn.lzq.injection.leaked.mixin.utils.ASMUtils;
import lombok.Getter;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Transformer {
    private final ArrayList<Operation> operations;
    private final ArrayList<ASMTransformer> transformers;

    public Transformer() {
        this.operations = new ArrayList<>();
        this.transformers = new ArrayList<>();
        this.operations.add(new InjectOperation());
    }

    public void addTransformer(ASMTransformer transformer) {
        transformers.add(transformer);
    }

    public Map<String, byte[]> transform() {
        Map<String, byte[]> classMap = new HashMap<>();

        for (ASMTransformer transformer : transformers) {
            if (transformer.getTarget() == null) {
                System.out.println("Transformer " + transformer.getClass().getName() + " has no target class, skipping.");
                continue;
            }
            String name = transformer.getTarget().getName().replace('/', '.');
            byte[] bytes = classMap.get(name);
            ClassNode targetNode;
            if (bytes == null) {
                ClassNode node = null;
                while (node == null) {
                    try {
                        bytes = NativeUtils.getClassesBytes(transformer.getTarget());
                        node = ASMUtils.node(bytes);
                    } catch (Throwable ignored) {
                    }
                }
                targetNode = node;
            } else targetNode = ASMUtils.node(bytes);
            for (Method method : transformer.getClass().getDeclaredMethods()) {
                method.setAccessible(true);
                if (method.getParameterCount() != 1) continue;
                ASMTransformer.Inject annotation = method.getAnnotation(ASMTransformer.Inject.class);
                if (annotation == null) continue;
                MethodNode node = Operation.findTargetMethod(targetNode.methods, targetNode.name, Mapping.get(transformer.getTarget(), annotation.method(), annotation.desc()), annotation.desc());
                try {
                    method.invoke(transformer, node);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    if(MixinLoader.debugging) ClientUtils.throwableBug(targetNode.name, e);
                }
            }
            byte[] class_bytes = ASMUtils.rewriteClass(targetNode);
            classMap.put(name, class_bytes);
        }
        return classMap;
    }
}
