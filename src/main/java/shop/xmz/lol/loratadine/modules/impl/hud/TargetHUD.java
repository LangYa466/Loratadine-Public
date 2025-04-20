package shop.xmz.lol.loratadine.modules.impl.hud;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.EaseBackIn;
import shop.xmz.lol.loratadine.ui.targethud.TargetManager;

/**
 * @author Jon_awa / DSJ_ / DreamDev
 * @since 13/2/2025
 */
public class TargetHUD extends Module {
    private final ModeSetting mode = new ModeSetting("TargetMode", this, new String[]{"Loratadine","Chill","LSD", "Modern", "Simple", "Exhibition","Modern Remix"}, "Loratadine");
    private final NumberSetting xPos = new NumberSetting("X", this, 20, -1000, 1000, 10);
    private final NumberSetting yPos = new NumberSetting("Y", this, 20, -1000, 1000, 10);
    private final Animation animation = new EaseBackIn(500, 1.0, 1.8f);
    public static final TimerUtils targetTimer = new TimerUtils();

    public TargetHUD() {
        super("TargetHUD", "目标信息", Category.RENDER);
    }

    @Override
    public void onEnable() {
        targetTimer.reset();
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

    }
}
