package shop.xmz.lol.loratadine.modules.impl.setting;

import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;

public class MoveFix extends Module {
    public static MoveFix INSTANCE;
    public final BooleanSetting renderRotation = new BooleanSetting("Render Rotation", this, true);
    public final BooleanSetting strictValue = new BooleanSetting("Strict", this, false);

    public MoveFix() {
        super("MoveFix", "移动修复设置", Category.SETTING);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        NotificationManager.add(NotificationType.INFO, "MoveFix", "你不用打开这个模块");
        setEnabled(false);
    }
}
