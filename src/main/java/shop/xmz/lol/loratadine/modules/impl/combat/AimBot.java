package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.misc.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AimBot extends Module {
    // 设置项
    private final NumberSetting maxSpeed = new NumberSetting("Max Rotation Speed", this, 5, 1, 10, 1);
    private final NumberSetting minSpeed = new NumberSetting("Min Rotation Speed", this, 5, 1, 10, 1);
    private final NumberSetting range = new NumberSetting("Range", this, 5, 3, 30, 0.1);
    private final NumberSetting fovValue = new NumberSetting("Fov", this, 180, 0, 360, 1); // 新增FOV设置

    private final BooleanSetting holdLeftClick = new BooleanSetting("On Click", this, true);
    private Rotation rotation, lastRotation;
    private boolean wasLeftClicking = false;

    public AimBot() {
        super("AimBot", "自动瞄准", Category.COMBAT);
    }

    @EventTarget
    public void onMotion(LivingUpdateEvent event) {
        List<LivingEntity> targets = new ArrayList<>();
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof LivingEntity livingEntity) {
                    if (filter(livingEntity)) {
                        targets.add(livingEntity);
                    }
                }
            }
        }

        // 按准星角度差排序（优先准星附近的目标）
        targets.sort((e1, e2) -> {
            float angleDiff1 = getAngleDifferenceToCrosshair(e1);
            float angleDiff2 = getAngleDifferenceToCrosshair(e2);
            return Float.compare(angleDiff1, angleDiff2);
        });

        LivingEntity target = null;
        if (!targets.isEmpty()) {
            target = targets.get(0);
        }

        if (target == null) {
            lastRotation = rotation = null;
        } else {
            lastRotation = rotation;
            rotation = RotationUtils.getAngles(target);
        }
    }

    private float getAngleDifferenceToCrosshair(Entity entity) {
        Rotation targetRot = RotationUtils.getAngles(entity);
        float entityYaw = targetRot.getYaw();
        float entityPitch = targetRot.getPitch();

        float playerYaw = mc.player.getYRot();
        float playerPitch = mc.player.getXRot();

        // 计算 Yaw 和 Pitch 的角度差
        float deltaYaw = MathUtils.wrapAngleTo180(entityYaw - playerYaw);
        float deltaPitch = MathUtils.wrapAngleTo180(entityPitch - playerPitch);

        // 返回总角度差（可以根据需要调整权重）
        return Math.abs(deltaYaw) + Math.abs(deltaPitch);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        final boolean isLeftClicking = mc.mouseHandler.isLeftPressed();

        if (holdLeftClick.getValue() && wasLeftClicking && !isLeftClicking) {
            lastRotation = rotation = null;
        }
        wasLeftClicking = isLeftClicking;

        final boolean shouldAim = !holdLeftClick.getValue() || isLeftClicking;

        if (!shouldAim ||
                mc.player == null ||
                mc.level == null ||
                rotation == null ||
                lastRotation == null
        ) {
            return;
        }

        // 视角插值计算（平滑过渡）
        final float partialTicks = mc.getFrameTime();
        Rotation rotations = new Rotation(
                lastRotation.getYaw() + (rotation.getYaw() - lastRotation.getYaw()) * partialTicks,
                lastRotation.getPitch() + (rotation.getPitch() - lastRotation.getPitch()) * partialTicks
        );

        // 速度计算（使用随机强度避免检测）
        final float strength = (float) MathUtils.getRandomNumber(
                minSpeed.getValue().intValue() * 30,
                maxSpeed.getValue().intValue() * 30
        );

        // 灵敏度计算（适配不同灵敏度设置）
        final double sensitivity = mc.options.sensitivity().get() * 0.6F + 0.2F;
        final double gcd = sensitivity * sensitivity * sensitivity * 8.0F;

        // 实际视角转动
        float deltaYaw = (float) ((rotations.getYaw() - mc.player.getYRot()) * (strength / 100) * gcd);
        float deltaPitch = (float) ((rotations.getPitch() - mc.player.getXRot()) * (strength / 100) * gcd);

        mc.player.turn(deltaYaw, deltaPitch);
    }

    private boolean filter(LivingEntity entity) {
        // 基础过滤条件
        if (RotationUtils.getDistanceToEntity(entity) > range.getValue().floatValue() || !EntityUtils.isSelected(entity, true)) {
            return false;
        }

        // 视野检测
        if (!mc.player.hasLineOfSight(entity)) return false;

        // FOV过滤
        if (!isInFOV(entity)) return false;

        // 生命状态检测
        return !entity.isDeadOrDying() && !(entity.getHealth() <= 0);
    }

    private boolean isInFOV(LivingEntity entity) {
        float currentFOV = fovValue.getValue().floatValue();
        if (currentFOV >= 360) return true;

        Rotation targetRot = RotationUtils.getAngles(entity);
        float entityYaw = targetRot.getYaw();
        float playerYaw = mc.player.getYRot();

        // 计算标准化角度差
        float deltaYaw = MathUtils.wrapAngleTo180(entityYaw - playerYaw);
        deltaYaw = Math.abs(deltaYaw);

        return deltaYaw <= currentFOV / 2;
    }
}