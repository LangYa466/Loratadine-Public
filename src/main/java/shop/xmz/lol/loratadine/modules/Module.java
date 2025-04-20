package shop.xmz.lol.loratadine.modules;

import lombok.Getter;
import lombok.Setter;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.impl.hud.NotificationHUD;
import shop.xmz.lol.loratadine.modules.setting.Setting;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.DecelerateAnimation;
import shop.xmz.lol.loratadine.utils.sound.SoundUtil;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.ArrayList;

@Getter
public abstract class Module implements Wrapper {
    private final String name;
    private final String cnName;
    private final Category category;
    private final ArrayList<Setting<?>> settings;
    @Getter
    private final Animation animation = new DecelerateAnimation(300, 1).setDirection(Direction.BACKWARDS);
    @Setter
    private int key;
    private boolean enabled;
    @Setter
    private String suffix;
    @Setter
    private boolean expanded;

    public Module(String name, String cnName, Category category, int key) {
        this.name = name;
        this.cnName = cnName;
        this.category = category;
        this.key = key;
        this.settings = new ArrayList<>();
    }

    public Module(String name, String cnName, Category category) {
        this.name = name;
        this.cnName = cnName;
        this.category = category;
        this.key = 0;
        this.settings = new ArrayList<>();
    }

    public Setting<?> findSetting(String name) {
        for (Setting<?> setting : getSettings()) {
            if (setting.getName().replace(" ", "").equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public String getModuleName() {
        if (Loratadine.INSTANCE == null || Loratadine.INSTANCE.getModuleManager().getModule(HUD.class) == null)
            return null;

        return ((HUD) (Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))).languageValue.getValue().equals("English") ? getName() : getCnName();
    }

    private String getEnableText() {
        if (Loratadine.INSTANCE == null || Loratadine.INSTANCE.getModuleManager().getModule(HUD.class) == null)
            return null;

        return ((HUD) (Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))).languageValue.getValue().equals("English") ? " was §2Enabled" : "开启了";
    }

    private String getDisableText() {
        if (Loratadine.INSTANCE == null || Loratadine.INSTANCE.getModuleManager().getModule(HUD.class) == null)
            return null;

        return ((HUD) (Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))).languageValue.getValue().equals("English") ? " was §4Disabled" : "关闭了";
    }

    private String getTitleName() {
        if (Loratadine.INSTANCE == null || Loratadine.INSTANCE.getModuleManager().getModule(HUD.class) == null)
            return null;

        return ((HUD) (Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))).languageValue.getValue().equals("English") ? "Module" : "模块";
    }

    public void setEnabledWhenConfigChange(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            if (mc.player != null && mc.level != null) onEnable();
            Loratadine.INSTANCE.getEventManager().register(this);
        } else {
            if (mc.player != null && mc.level != null) Loratadine.INSTANCE.getEventManager().unregister(this);
            onDisable();
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;
        if (enabled) {
            if (mc.player != null
                    && mc.level != null
                    && (Loratadine.INSTANCE != null
                    && Loratadine.INSTANCE.getModuleManager() != null
                    && Loratadine.INSTANCE.getModuleManager().getModule(NotificationHUD.class) != null
                    && NotificationHUD.notiStyle != null
                    && !NotificationHUD.notiStyle.equals("LSD"))) {
                NotificationManager.add(NotificationType.SUCCESS, getTitleName(), getModuleName() + getEnableText());
            } else if (mc.player != null && mc.level != null) {
                NotificationManager.add(NotificationType.SUCCESS, getTitleName(), getModuleName());
            }

            if (mc.player != null && mc.level != null) onEnable();
            Loratadine.INSTANCE.getEventManager().register(this);
            if (mc.player != null) SoundUtil.simplePlaySound(Loratadine.INSTANCE.getResourcesManager().resources.getAbsolutePath() + "\\sound\\enable.wav",.8f);
        } else {
            if (mc.player != null
                    && mc.level != null
                    && (Loratadine.INSTANCE != null
                    && Loratadine.INSTANCE.getModuleManager() != null
                    && Loratadine.INSTANCE.getModuleManager().getModule(NotificationHUD.class) != null
                    && NotificationHUD.notiStyle != null && !NotificationHUD.notiStyle.equals("LSD"))) {
                NotificationManager.add(NotificationType.DISABLE, getTitleName(), getModuleName() + getDisableText());
            } else if (mc.player != null && mc.level != null) {
                NotificationManager.add(NotificationType.DISABLE, getTitleName(), getModuleName());
            }

            Loratadine.INSTANCE.getEventManager().unregister(this);
            if (mc.player != null && mc.level != null) onDisable();
            if (mc.player != null) SoundUtil.simplePlaySound(Loratadine.INSTANCE.getResourcesManager().resources.getAbsolutePath() + "\\sound\\disable.wav",.8f);
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
