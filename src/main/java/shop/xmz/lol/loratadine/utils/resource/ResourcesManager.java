package shop.xmz.lol.loratadine.utils.resource;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ResourcesManager implements Wrapper {
    public File resources = new File(Loratadine.INSTANCE.getConfigManager().mainFile, "resources");
    public File font = new File(resources, "font");
    public final Map<String, byte[]> res = new HashMap<>();

    public void init() {
        if (!resources.exists()) {
            if (!resources.mkdirs()) {
                System.err.println("Failed to create resource directory: " + resources.getAbsolutePath());
            } else {
                System.out.println("Resource directory created: " + resources.getAbsolutePath());
            }
        }
        if (!font.exists()) {
            if (!font.mkdirs()) {
                System.err.println("Failed to create font directory: " + font.getAbsolutePath());
            } else {
                System.out.println("Font directory created: " + font.getAbsolutePath());
            }
        }
    }

    public ResourceLocation loadIconFromAbsolutePath(String absolutePath) {
        File file = new File(absolutePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + absolutePath);
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            NativeImage nativeImage = NativeImage.read(inputStream);
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            return mc.getTextureManager().register("custom_icon_" + file.getName(), dynamicTexture);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResourceLocation loadFile(String absolutePath) {
        File file = new File(absolutePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + absolutePath);
        }
        return null;
    }

    public byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try (InputStream input = inStream;
             ByteArrayOutputStream output = outStream) {
            while ((len = input.read(buffer)) != -1)
                output.write(buffer, 0, len);
            return output.toByteArray();
        }
    }

    public InputStream getStream(String name) {
        if (res.containsKey(name))
            return new ByteArrayInputStream(res.get(name));
        File file = new File(resources, name);
        try {
            if (file.exists())
                return Files.newInputStream(file.toPath());
        } catch (Throwable ignored) {}
        return ResourcesManager.class.getResourceAsStream("/" + name);
    }

    public byte[] get(String name) {
        InputStream stream = getStream(name);
        if (stream != null) {
            try {
                return readStream(stream);
            } catch (Throwable ignored) {}
        }
        return null;
    }
}
