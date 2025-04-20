package shop.xmz.lol.loratadine.config.impl.module;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.config.Config;
import shop.xmz.lol.loratadine.config.ConfigManager;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.Setting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class SwitchModuleConfig extends Config {
    public SwitchModuleConfig(File file) {
        super(file);
    }

    @Override
    public void read() throws Throwable {
        if (this.file.exists()) {
            final JsonObject moduleConfig = (JsonObject) JsonParser.parseString(Files.readString(file.toPath()));

            if (Loratadine.INSTANCE == null) return;

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
            write();
        }
    }

    @Override
    public boolean write() throws Throwable {
        final JsonObject moduleConfig = new JsonObject();

        if (Loratadine.INSTANCE == null) return false;

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

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            ConfigManager.GSON.toJson(moduleConfig, bufferedWriter);
        }

        return true;
    }
}