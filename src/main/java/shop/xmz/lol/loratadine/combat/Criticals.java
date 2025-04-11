package shop.xmz.lol.loratadine.combat;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Criticals extends Module {
    // 模式设置
    private final ModeSetting modeValue = new ModeSetting("Mode", this, new String[]{"GrimStuck", "GrimTimer"}, "GrimStuck");

    // GrimStuck 专用设置
    private final BooleanSetting fire = (BooleanSetting) new BooleanSetting("Fire", this, false)
            .setVisibility(() -> modeValue.is("GrimStuck"));

    // GrimTimer 专用设置
    private final NumberSetting idleTime_Value = (NumberSetting) new NumberSetting("Idle time", this, 250, 10, 500, 10)
            .setVisibility(() -> modeValue.is("GrimTimer"));
    private final NumberSetting downHeight_Value = (NumberSetting) new NumberSetting("Down Height", this, 2, 1, 10, 1)
            .setVisibility(() -> modeValue.is("GrimTimer"));
    private final BooleanSetting autoJump = (BooleanSetting) new BooleanSetting("Auto Jump", this, true)
            .setVisibility(() -> modeValue.is("GrimTimer"));
    private final BooleanSetting C03 = (BooleanSetting) new BooleanSetting("Send C03", this, true)
            .setVisibility(() -> modeValue.is("GrimTimer"));

    private boolean start = false;
    private final TimerUtils msTimer = new TimerUtils();

    public Criticals() {
        super("Criticals", "刀爆", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        start = false;
        WrapperUtils.setMsPerTick(WrapperUtils.getTimer(), 50F); // 重置计时器
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundInteractPacket wrapper && WrapperUtils.isAttackAction(wrapper)) {
            start = true;
        }
    }

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        if (mc.player == null || mc.level == null) return;

        switch (modeValue.getValue()) {
            case "GrimTimer" -> handleGrimTimerLogic();
            case "GrimStuck" -> handleGrimStuckLogic();
        }
    }

    private void handleGrimStuckLogic() {
        // 火焰粒子效果
        if (fire.getValue() && start && WrapperUtils.getOffGroundTicks() > 3) {
            if (msTimer.delay(500L)) {
                for (int i = 0; i <= 8; i++) {
                    if (KillAura.INSTANCE.getTarget() != null) {
                        WrapperUtils.spawnLegacyParticles(KillAura.INSTANCE.getTarget(), ParticleTypes.FLAME);
                    }
                }
                msTimer.reset();
            }
        }

        // 位置同步逻辑
        if (KillAura.INSTANCE.getTarget() != null && start) {
            if (mc.player.fallDistance > 0 || WrapperUtils.getOffGroundTicks() > 3) {
                double d0 = mc.player.getX() - WrapperUtils.getXLast();
                double d1 = mc.player.getY() - WrapperUtils.getYLast();
                double d2 = mc.player.getZ() - WrapperUtils.getZLast();
                double d3 = (KillAura.INSTANCE.rotation_[0] - WrapperUtils.getYRotLast());
                double d4 = (KillAura.INSTANCE.rotation_[1] - WrapperUtils.getXRotLast());

                boolean positionChanged = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D;
                boolean rotationChanged = d3 != 0.0D || d4 != 0.0D;

                if (positionChanged && rotationChanged) {
                    sendPositionPacket(ServerboundMovePlayerPacket.PosRot.class,
                            mc.player.getX(), mc.player.getY() - 0.03, mc.player.getZ(),
                            KillAura.INSTANCE.rotation_[0], KillAura.INSTANCE.rotation_[1]);
                } else if (positionChanged) {
                    sendPositionPacket(ServerboundMovePlayerPacket.Pos.class,
                            mc.player.getX(), mc.player.getY() - 0.03, mc.player.getZ());
                } else if (rotationChanged) {
                    sendPositionPacket(ServerboundMovePlayerPacket.Rot.class,
                            KillAura.INSTANCE.rotation_[0], KillAura.INSTANCE.rotation_[1]);
                } else {
                    mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(false));
                }
                WrapperUtils.setSkipTicks(WrapperUtils.getSkipTicks() + 1);
            }
        } else {
            start = false;
        }
    }

    private void handleGrimTimerLogic() {
        // 地面自动跳跃
        if (KillAura.INSTANCE.getTarget() != null && mc.player.onGround()) {
            if (autoJump.getValue()) {
                KeyMapping.set(mc.options.keyJump.getKey(), true);
            }
            WrapperUtils.setMsPerTick(WrapperUtils.getTimer(), 50F);
        }

        // 空中速度控制
        if (KillAura.INSTANCE.getTarget() != null && !mc.player.onGround()) {
            KeyMapping.set(mc.options.keySprint.getKey(), false);
            double fallDistance = mc.player.fallDistance;
            double maxFallDistance = PlayerUtil.getMaxFallDistance();

            if (fallDistance > 0 && fallDistance < maxFallDistance / 2) {
                WrapperUtils.setMsPerTick(WrapperUtils.getTimer(), idleTime_Value.getValue().intValue());
                if (C03.getValue()) sendC03Packet(true);
            } else if (fallDistance >= maxFallDistance / downHeight_Value.getValue().intValue()) {
                WrapperUtils.setMsPerTick(WrapperUtils.getTimer(), 50F);
            }
            if (C03.getValue()) sendC03Packet(false);
        }

        // 状态恢复
        if (mc.player.onGround() || mc.player.hurtTime > 0) {
            WrapperUtils.setMsPerTick(WrapperUtils.getTimer(), 50F);
        }
    }

    private void sendC03Packet(boolean onGround) {
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                mc.player.getYRot(),
                mc.player.getXRot(),
                onGround
        ));
    }

    private void sendPositionPacket(Class<?> packetType, Object... params) {
        if (packetType == ServerboundMovePlayerPacket.PosRot.class) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    (Double)params[0], (Double)params[1], (Double)params[2],
                    (Float)params[3], (Float)params[4], false));
        } else if (packetType == ServerboundMovePlayerPacket.Pos.class) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
                    (Double)params[0], (Double)params[1], (Double)params[2], false));
        } else if (packetType == ServerboundMovePlayerPacket.Rot.class) {
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                    (Float)params[0], (Float)params[1], false));
        }
    }
}