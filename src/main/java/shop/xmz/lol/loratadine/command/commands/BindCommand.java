package shop.xmz.lol.loratadine.command.commands;

import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.command.Command;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class BindCommand extends Command {
    private Map<String,Integer> bindMap = new HashMap<>();

    public BindCommand() {
        super("bind","b");

        bindMap.put("none",0);

        for (Field field : GLFW.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("GLFW_KEY_")) {
                field.setAccessible(true);
                try {
                    bindMap.put(field.getName().substring(9).toLowerCase(),(Integer) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void execute(String[] params) {
        if (params.length == 2) {
            Module module = Loratadine.INSTANCE.getModuleManager().findModule(params[0]);
            if (module != null) {
                Integer key = bindMap.get(params[1]);
                if (key != null) {
                    module.setKey(key);
                    NotificationManager.add(NotificationType.INFO, "Bind", "Bound module " + module.getName() + " to " + params[1] + ".", 5);
                } else {
                    module.setKey(GLFW.GLFW_KEY_UNKNOWN);
                    ClientUtils.log("Error: Invalid key (default set to None)");
                }
            } else {
                NotificationManager.add(NotificationType.WARNING, "Bind", "Error: " + params[0] + " not found", 5);
            }
        } else {
            ClientUtils.log("Usage: .bind <module> <key>");
        }
    }
}
