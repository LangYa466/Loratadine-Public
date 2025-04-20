package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.AttackEvent;
import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import cn.lzq.injection.leaked.invoked.MotionEvent;
import lombok.Getter;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.impl.player.Scaffold;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.item.InventoryUtils;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.misc.EntityUtils;
import org.lwjgl.glfw.GLFW;
import shop.xmz.lol.loratadine.utils.player.PlayerUtil;
import shop.xmz.lol.loratadine.utils.player.RayCastUtil;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
    public static KillAura INSTANCE;
    private final ModeSetting mode_Value = new ModeSetting("Target Mode",this,new String[]{"Single", "Switch", "Multiple"},"Switch");
    private final NumberSetting switchDelay_Value = (NumberSetting) new NumberSetting("Switch Delay",this,0,0,1000,10)
            .setVisibility(() -> mode_Value.is("Switch"));

    private final NumberSetting cps_Value = new NumberSetting("CPS",this,10, 1, 20, 1);
    private final NumberSetting range_Value = new NumberSetting("Range",this,3.5, 1, 6.0, 0.1);

    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls",this,false);
    private final NumberSetting throughWalls_Value = (NumberSetting) new NumberSetting("ThroughWalls Range",this,2.5, 1, 6.0, 0.1)
            .setVisibility(throughWalls::getValue);
    private final NumberSetting fov_Value = new NumberSetting("Fov",this,180.0,0.0,360.0, 10);

    private final BooleanSetting rayCast_Value = new BooleanSetting("RayCast",this,true);
    private final NumberSetting rayCastExpand_Value = (NumberSetting) new NumberSetting("RayCast Expand",this,0,0,0.1,0.01)
            .setVisibility(rayCast_Value::getValue);

    private final ModeSetting rayCastRotation_Value = (ModeSetting) new ModeSetting("RayCast Mode",this,new String[]{"Normal", "Entity", "Last"},"Normal")
            .setVisibility(rayCast_Value::getValue);

    private final ModeSetting priority_Value = new ModeSetting("Priority Mode",this,new String[]{"Distance","Health","EasyToKill","ThreatLevel","LivingTime","Armor"},"Distance");
    private final ModeSetting autoBlock_Value = new ModeSetting("AutoBlock Mode",this,new String[]{"None","Fake","UseItem"},"None");
    private final ModeSetting rotation_Value = new ModeSetting("Rotation Mode",this,new String[]{"Simple", "Normal", "HvH"},"Normal");
    private final BooleanSetting moveFix_Value = new BooleanSetting("Move Fix",this,true);
    private final ModeSetting moveFixMode_Value = (ModeSetting) new ModeSetting("MoveFix Mode",this,new String[]{"Silent", "Strict", "Length"},"Silent")
            .setVisibility(moveFix_Value::getValue);
    private final NumberSetting length_Value = (NumberSetting) new NumberSetting("Length Value",this,10,0, 20, 1)
            .setVisibility(() -> moveFixMode_Value.is("Length"));

    private final BooleanSetting keepSprint = new BooleanSetting("KeepSprint",this,false);
    private final BooleanSetting silentRotation = new BooleanSetting("SilentRotation",this,true);

    @Getter
    private final List<LivingEntity> targets = new ArrayList<>();
    public static LivingEntity target = null;
    private boolean silentRotation_ = false;
    TimerUtils switchDelay = new TimerUtils();
    TimerUtils timer = new TimerUtils();
    public float[] rotation_ = null;
    public boolean blocking = false;

    public KillAura() {
        super("KillAura", "杀戮光环" , Category.COMBAT, GLFW.GLFW_KEY_R);
        INSTANCE = this;
    }

    @Override
    protected void onEnable() {
        reset();
    }

    @Override
    protected void onDisable() {
        reset();
    }

    public void reset() {
        // 重置目标和旋转
        target = null;
        targets.clear();
        silentRotation_ = false;
        rotation_ = null;
        // 重置自动防砍
        if (blocking) {
            switch (autoBlock_Value.getValue()) {
                case "Fake" -> blocking = false;

                case "UseItem" -> {
                    mc.options.keyUse.setDown(false);
                    blocking = false;
                }
            }
        }
    }

    public Rotation getRayCastRotationMode() {
        if (rotation_ == null) return new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast());

        switch (rayCastRotation_Value.getValue()) {
            case "Normal" -> {
                return new Rotation(rotation_[0], rotation_[1]);
            }
            case "Entity" -> {
                return RotationUtils.getRotationForEntity(target);
            }
            case "Last" -> {
                return new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast());
            }
        }
        return new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast());
    }

    @EventTarget
    public void onUpdateTargetAndAttack(LivingUpdateEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (Scaffold.INSTANCE.isEnabled()) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity) {
                if (filter(livingEntity)) {
                    targets.add(livingEntity);
                }
            }
        }

        sortTargets();

        if (targets.isEmpty()
                || (target != null && (RotationUtils.getDistanceToEntity(target) > range_Value.getValue().floatValue() || target.isDeadOrDying() || !target.isAlive() || target.getHealth() <= 0))
                || (mc.player.isDeadOrDying() || !mc.player.isAlive() || mc.player.getHealth() <= 0)) {
            reset();
        } else {
            switch (mode_Value.getValue()) {
                case "Single" -> target = targets.get(0);
                case "Switch", "Multiple" -> {
                    if (switchDelay.delay(switchDelay_Value.getValue().longValue())) {
                        target = targets.get(MathUtils.getRandomNumberUsingNextInt(0, targets.size() - 1));
                        switchDelay.reset();
                    }
                }
            }
        }

        if (target == null) return;

        switch (rotation_Value.getValue()) {
            case "Simple" -> rotation_ = RotationUtils.getSimpleRotations(target);

            case "Normal" -> {
                Rotation rotations = RotationUtils.getAngles(target);
                rotation_ = new float[]{rotations.getYaw(), rotations.getPitch()};
            }

            case "HvH" -> rotation_ = RotationUtils.getHVHRotation(target);
        }

        if (silentRotation.getValue()) {
            if (moveFix_Value.getValue()) {
                RotationUtils.setRotation(new Rotation(rotation_[0], rotation_[1]), moveFixMode_Value.is("Strict") ? 20 : moveFixMode_Value.is("Length") ? length_Value.getValue().intValue() : 0);
            } else {
                silentRotation_ = true;
            }
        } else {
            Rotation rotation = RotationUtils.getAngles(target);
            rotation.toPlayer(mc.player);
        }

        if (rotation_ == null) return;

        switch (mode_Value.getValue()) {
            case "Single", "Switch" -> {
                if (rayCast_Value.getValue()) {
                    final HitResult hitResult = RayCastUtil.rayCast(getRayCastRotationMode(), 3.0, rayCastExpand_Value.getValue().floatValue());
                    if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY && target.equals(((EntityHitResult) hitResult).getEntity())) {
                        attack(target);
                    }
                } else {
                    attack(target);
                }
            }
            case "Multiple" -> {
                if (!targets.isEmpty()) {
                    if (rayCast_Value.getValue()) {
                        for (LivingEntity entity : targets) {
                            final HitResult hitResult = RayCastUtil.rayCast(getRayCastRotationMode(), 3.0, rayCastExpand_Value.getValue().floatValue());
                            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY && entity.equals(((EntityHitResult) hitResult).getEntity())) {
                                attack(entity);
                            }
                        }
                    } else {
                        targets.forEach(this::attack);
                    }
                }
            }
        }
        this.setSuffix(mode_Value.getValue());
    }

    public void sortTargets() {
        if (mc.player == null) return;

        switch (priority_Value.getValue()) {
            case "Distance":
                targets.sort(Comparator.comparingInt(a -> (int) RotationUtils.getDistanceToEntity(a)));
                break;
            case "Health":
                targets.sort(Comparator.comparingInt(a -> (int) a.getHealth()));
                break;
            case "LivingTime":
                targets.sort((a, b) -> Integer.compare(b.tickCount, a.tickCount));
                break;
            case "Armor":
                targets.sort((a, b) -> Integer.compare(PlayerUtil.getTotalArmorValue(b), PlayerUtil.getTotalArmorValue(a)));
                break;
            case "EasyToKill":
                targets.sort((a, b) -> {
                    double scoreA = a.getHealth() / (PlayerUtil.getTotalArmorValue(a) + 1);
                    double scoreB = b.getHealth() / (PlayerUtil.getTotalArmorValue(b) + 1);
                    return Double.compare(scoreA, scoreB);
                });
                break;
            case "ThreatLevel":
                double playerAttack = InventoryUtils.getItemDamage(mc.player.getMainHandItem());
                targets.sort((a, b) -> {
                    double threatScoreA = (a.getHealth() + PlayerUtil.getTotalArmorValue(a)) / playerAttack;
                    double threatScoreB = (b.getHealth() + PlayerUtil.getTotalArmorValue(b)) / playerAttack;
                    return Double.compare(threatScoreA, threatScoreB);
                });
                break;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!event.post && silentRotation_ && rotation_ != null) {
            event.setYaw(rotation_[0]);
            event.setPitch(rotation_[1]);
            silentRotation_ = false;
            rotation_ = null;
        }
    }

    public void attack(LivingEntity target) {
        if (mc.player == null
                || mc.level == null
                || mc.getConnection() == null
                || mc.gameMode == null) return;

        if (timer.delay(800 / cps_Value.getValue().intValue())) {
            if (keepSprint.getValue()) {
                mc.gameMode.tick();

                final AttackEvent attackEvent = new AttackEvent(target);
                Loratadine.INSTANCE.getEventManager().call(attackEvent);

                if (attackEvent.isCancelled()) return;

                mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(target, mc.player.isShiftKeyDown()));
                mc.player.swing(InteractionHand.MAIN_HAND);

                if (mc.player.fallDistance > 0
                        && !mc.player.onGround()
                        && !mc.player.onClimbable()
                        && !mc.player.isInWater()
                        && !mc.player.hasEffect(MobEffects.BLINDNESS)
                        && mc.player.getVehicle() == null) {
                    mc.player.crit(target);
                }

                if (EnchantmentHelper.getDamageBonus(mc.player.getMainHandItem(), target.getMobType()) > 0F)
                    mc.player.magicCrit(target);
            } else {
                final AttackEvent attackEvent = new AttackEvent(target);
                Loratadine.INSTANCE.getEventManager().call(attackEvent);

                if (attackEvent.isCancelled()) return;

                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            timer.reset();
        }

        if (!blocking) {
            switch (autoBlock_Value.getValue()) {
                case "Fake" -> blocking = true;

                case "UseItem" -> {
                    mc.options.keyUse.setDown(true);
                    blocking = true;
                }
            }
        }
    }

    public boolean filter(LivingEntity entity) {
        if (mc.player == null) return false;

        if (RotationUtils.getDistanceToEntity(entity)
                > (!throughWalls.getValue() ? range_Value.getValue().floatValue()
                : mc.player.hasLineOfSight(entity) ? range_Value.getValue().floatValue() : throughWalls_Value.getValue().floatValue())
                || !EntityUtils.isSelected(entity, true)
                || !RotationUtils.isInViewRange(fov_Value.getValue().floatValue(), entity)) {
            return false;
        }

        if (!mc.player.hasLineOfSight(entity) && !throughWalls.getValue()) return false;

        return !entity.isDeadOrDying() && !(entity.getHealth() <= 0);
    }
}