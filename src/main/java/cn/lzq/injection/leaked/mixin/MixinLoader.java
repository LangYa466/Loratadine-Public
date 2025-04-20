package cn.lzq.injection.leaked.mixin;

import cn.lzq.injection.leaked.*;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.NativeUtils;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class MixinLoader {
    private static boolean loaded = false;
    public static final boolean debugging = false;
    public static final int ASM_API = Opcodes.ASM5;
    public static ClassLoader classLoader;
    public static Transformer transformer;
    public static Map<String, byte[]> classBytesMap;

    public static void init() {
        if (loaded) return;
        loaded = true;

        classLoader = findTargetClassLoader();
        transformer = new Transformer();

        try {
            transformer.addTransformer(new LocalPlayerTransformer());
            transformer.addTransformer(new HumanoidModelTransformer());
            transformer.addTransformer(new ItemInHandRendererTransformer());
            transformer.addTransformer(new KeyMappingTransformer());
            transformer.addTransformer(new ItemEntityRendererTransformer());
            transformer.addTransformer(new ClientLevelTransformer());
            transformer.addTransformer(new CameraTransformer());
            transformer.addTransformer(new ConnectionTransformer());
            transformer.addTransformer(new PacketUtilsTransformer());
            transformer.addTransformer(new ClientPacketListenerTransformer());
            transformer.addTransformer(new EntityRendererTransformer());
            transformer.addTransformer(new ItemInHandLayerTransformer());
            transformer.addTransformer(new GuiTransformer());
            transformer.addTransformer(new KeyboardHandlerTransformer());
            transformer.addTransformer(new GameRendererTransformer());
            transformer.addTransformer(new EntityTransformer());
            transformer.addTransformer(new PlayerRendererTransformer());
            transformer.addTransformer(new LivingEntityTransformer());
            transformer.addTransformer(new MultiPlayerGameModeTransformer());
            transformer.addTransformer(new KeyboardInputTransformer());
            transformer.addTransformer(new MinecraftTransformer());

            classBytesMap = transformer.transform();
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if ("Render thread".equalsIgnoreCase(thread.getName())) {
                    for (Map.Entry<String, byte[]> entry : classBytesMap.entrySet()) {
                        try {
                            NativeUtils.redefineClasses(Class.forName(entry.getKey()), entry.getValue());
                            if (debugging) Files.write(new File(Loratadine.INSTANCE.getConfigManager().mainFile, entry.getKey() + ".class").toPath(), entry.getValue());
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                            System.out.println("Failed to reload class:" + entry.getKey() + "\n" + ex.getMessage());
                        }
                    }
                    break;
                }
            }

            System.out.println("Mixin inject success. Rewrite " + classBytesMap.size() + " classes.");
        } catch (Throwable ex) {
            System.out.println("Mixin inject failed.");
            ex.printStackTrace();
        }
    }

    private static ClassLoader findTargetClassLoader() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if ("Render thread".equalsIgnoreCase(thread.getName())) {
                return thread.getContextClassLoader();
            }
        }
        return null;
    }
}