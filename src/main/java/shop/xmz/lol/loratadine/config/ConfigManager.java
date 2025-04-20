package shop.xmz.lol.loratadine.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.Setting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class ConfigManager {
    public final File mainFile = new File(System.getProperty("user.home"), ".moran" + File.separator + "Loratadine");
    public final File configFile = new File(mainFile, "Configs");
    public final File moduleConfigFile = new File(mainFile, "module.ini");

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void init() {
        if (mc == null || Loratadine.INSTANCE == null) return;

        try {
            if (!mainFile.exists()) {
                if (!mainFile.mkdirs()) {
                    System.err.println("Failed to create main directory: " + mainFile.getAbsolutePath());
                } else {
                    System.out.println("Main directory created: " + mainFile.getAbsolutePath());
                }
            }

            if (!configFile.exists()) {
                if (!configFile.mkdirs()) {
                    System.err.println("Failed to create config directory: " + configFile.getAbsolutePath());
                } else {
                    System.out.println("Config directory created: " + configFile.getAbsolutePath());
                }
            }

            if (this.moduleConfigFile.exists()) {
                final JsonObject moduleConfig = (JsonObject) JsonParser.parseString(Files.readString(moduleConfigFile.toPath()));

                for (Module module : Loratadine.INSTANCE.getModuleManager().getModules()) {
                    final String name = module.getName();

                    if (moduleConfig.has(name)) {
                        final JsonObject singleModule = moduleConfig.getAsJsonObject(name);

                        if (singleModule.has("State"))
                            module.setEnabledWhenConfigChange(singleModule.get("State").getAsBoolean());
                        if (singleModule.has("KeyBind"))
                            module.setKey(singleModule.get("KeyBind").getAsInt());
                        if (singleModule.has("Settings")) {
                            final JsonObject settingsConfig = singleModule.get("Settings").getAsJsonObject();

                            for (Setting<?> setting : module.getSettings())
                                if (settingsConfig.has(setting.getName()))
                                    setting.formJson(settingsConfig.get(setting.getName()));
                        }
                    }
                }
            } else {
                save();
            }
        } catch (Throwable ignored) {}
    }

    public void save() {
        if (mc == null || Loratadine.INSTANCE == null) return;

        try {
            final JsonObject moduleConfig = new JsonObject();

            for (Module module : Loratadine.INSTANCE.getModuleManager().getModules()) {
                final JsonObject singleModule = new JsonObject();

                singleModule.addProperty("State", module.isEnabled());
                singleModule.addProperty("KeyBind", module.getKey());
                final JsonObject settingConfig = new JsonObject();
                for (Setting<?> setting : module.getSettings())
                    setting.toJson(settingConfig);
                singleModule.add("Settings", settingConfig);

                moduleConfig.add(module.getName(), singleModule);
            }

            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(moduleConfigFile))) {
                ConfigManager.GSON.toJson(moduleConfig, bufferedWriter);
            }
        } catch (Throwable ignored) {}
    }
}
