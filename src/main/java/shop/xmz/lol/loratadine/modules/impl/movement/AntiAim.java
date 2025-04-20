package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.*;
import lombok.Getter;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;

public class AntiAim extends Module {
    public static AntiAim INSTANCE;
    private final NumberSetting spinSpeed = new NumberSetting("Spin Speed", this, 10.0, 1.0, 50.0, 1.0);
    private final BooleanSetting renderOnly = new BooleanSetting("Render Only", this, true);
    private final BooleanSetting stopOnMove = new BooleanSetting("Stop On Move", this, true);

    @Getter
    private float currentRotation = 0f;

    public AntiAim() {
        super("AntiAim", "反自瞄", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            currentRotation = mc.player.getYRot();
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) return;

        // 更新旋转角度
        currentRotation += spinSpeed.getValue().floatValue();
        currentRotation %= 360f;

        // 在非renderOnly模式下，根据stopOnMove设置来判断是否在移动时停止旋转
        if (!renderOnly.getValue() && (!stopOnMove.getValue() || !MoveUtils.isMoving())) {
            RotationUtils.setRotation(new Rotation(currentRotation, mc.player.getXRot()));
        }
    }

    @EventTarget
    public void onRenderer(RenderPlayerEvent event) {
        if (!this.isEnabled()) return;
        event.rotationYaw = currentRotation;
        event.rotationPitch = 90f; // 视觉上固定低头
    }

    @EventTarget
    public void onPitchRender(PitchRenderEvent event) {
        if (!this.isEnabled()) return;
        event.pitch = 90f; // 视觉上固定低头
    }
}
