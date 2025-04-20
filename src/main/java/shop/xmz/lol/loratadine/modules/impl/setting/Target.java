package shop.xmz.lol.loratadine.modules.impl.setting;

import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;

public class Target extends Module {
    public static Target INSTANCE;
    public final BooleanSetting players = new BooleanSetting("Players", this, true);
    public final BooleanSetting dead = new BooleanSetting("Dead", this, false);
    public final BooleanSetting invisible = new BooleanSetting("Invisible", this, false);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", this, false);
    public final BooleanSetting animals = new BooleanSetting("Animals", this, false);

    public Target() {
        super("Target", "目标", Category.SETTING);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        NotificationManager.add(NotificationType.INFO, "Target", "你不用打开这个模块");
        setEnabled(false);
    }
}
