package shop.xmz.lol.loratadine.command.commands;

import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.command.Command;
import shop.xmz.lol.loratadine.config.impl.module.SwitchModuleConfig;
import shop.xmz.lol.loratadine.utils.ClientUtils;

import java.io.File;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", "cfg");
    }

    @Override
    public void execute(String[] params) {
        if (params.length == 1) {
            if (params[0].equals("list")) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Config list:");
                File[] files = Loratadine.INSTANCE.getConfigManager().configFile.listFiles();
                if (files == null || files.length == 0) {
                    sb.append("empty;");
                } else {
                    boolean needShort = false;
                    for (File file : files) {
                        sb.append(needShort ? ", " : "").append(file.getName().replaceAll(".ini", ""));
                        needShort = true;
                    }
                    sb.append(";");
                }
                ClientUtils.log(sb.toString());
            } else {
                ClientUtils.log("Usage: .config/cfg <create/list/load/save>");
            }
        } else if (params.length == 2) {
            final SwitchModuleConfig switchModuleConfig = new SwitchModuleConfig(new File(Loratadine.INSTANCE.getConfigManager().configFile, params[1] + ".ini"));
            try {
                if (params[0].equals("load")) {
                    switchModuleConfig.read();
                    ClientUtils.log("Load config " + params[1] + " successful.");
                } else if (params[0].equals("create") || params[0].equals("save")) {
                    if (switchModuleConfig.write())
                        ClientUtils.log(params[0].toLowerCase() + " " + params[1] + " config successful.");
                    else
                        ClientUtils.log(params[0].toLowerCase() + " " + params[1] + " config failed.");
                }
            } catch (Throwable ex) {
                ClientUtils.log("Error: failed.");
            }
        } else {
            ClientUtils.log("Usage: .config/cfg <create/list/load/save>");
        }
    }
}
