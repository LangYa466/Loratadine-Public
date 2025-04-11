package shop.xmz.lol.loratadine.combat;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.*;
import shop.xmz.lol.loratadine.player.Stuck;

import java.util.LinkedList;
import java.util.Queue;

@JNICInclude
public class Velocity extends Module {

    private static String[] modes = {"", ""};
    public static Velocity INSTANCE;
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Vanilla", "Grim Attack", "Jump Reset", "Auto Stuck"}, "Vanilla");

    private final BooleanSetting attacked_FlightObject = (BooleanSetting) new BooleanSetting("Attacked FlightObject", this, false)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final NumberSetting targetMotion = (NumberSetting) new NumberSetting("Target Motion", this, 0.1, 0.01, 1, 0.001)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final NumberSetting counter = (NumberSetting) new NumberSetting("Counter", this, 1, 1, 10, 1)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final BooleanSetting rayCast = (BooleanSetting) new BooleanSetting("Ray Cast", this, true)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final BooleanSetting sprintOnly = (BooleanSetting) new BooleanSetting("Sprint Only", this, true)
            .setVisibility(() -> mode_Value.is("Grim Attack"));
    private final NumberSetting range = (NumberSetting) new NumberSetting("Range", this, 3, 2, 8, 0.1)
            .setVisibility(() -> mode_Value.is("Auto Stuck"));

    private final BooleanSetting onlyMove = new BooleanSetting("OnlyMove", this, false);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround", this, false);

    private final BooleanSetting flagCheckValue = new BooleanSetting("FlagCheck", this, false);
    private final BooleanSetting flagDisable = new BooleanSetting("FlagDisable", this, false);
    public final NumberSetting flagTickValue = new NumberSetting("FlagTicks", this, 6, 0, 30, 1);
    public final BooleanSetting flagDebugMessageValue = new BooleanSetting("FlagDebugMessage", this, false);
    public final BooleanSetting debugMessageValue = new BooleanSetting("DebugMessage", this, false);
    int flags;

    private final Queue<Packet<?>> packets = new LinkedList<>();
    public boolean attackedFlightObject = false;
    private boolean slowdownTicks = false;
    public boolean velocityInput = false;
    public boolean attacked = false;
    double reduceXZ;

    public boolean shouldDelay = false;

    public Velocity() {
        super("Velocity", "反击退", Category.COMBAT);
        INSTANCE = this;
        this.setEnabled(true);
    }


    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onEnable() {
        reset();
    }

    public void reset() {
        shouldDelay = false;
        velocityInput = false;
        attacked = false;
        attackedFlightObject = false;
        reduceXZ = 0;
        packets.clear();
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        reset();
    }

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (flagCheckValue.getValue()) {
            if (flags > 0) flags--;
        }

        if (mc.player.isDeadOrDying() || mc.player.getHealth() <= 0) return;

        switch (mode_Value.getValue()) {
            case "Grim Attack" -> {
                while (!packets.isEmpty()) {
                    mc.player.connection.send(packets.poll());
                }

                Vec3 deltaMovement = mc.player.getDeltaMovement();

                if (slowdownTicks) {
                    WrapperUtils.setSkipTicks(1);
                    slowdownTicks = false;
                }

                if (velocityInput) {
                    if (attacked) {
                        mc.player.setDeltaMovement(deltaMovement.x * reduceXZ, deltaMovement.y, deltaMovement.z * reduceXZ);
                        attacked = false;
                    }
                    if (attackedFlightObject) {
                        mc.player.setDeltaMovement(deltaMovement.x * reduceXZ, deltaMovement.y, deltaMovement.z * reduceXZ);
                        attackedFlightObject = false;
                    }
                    if (mc.player.hurtTime == 0) reset();
                }
            }

            case "Auto Stuck" -> {
                if (KillAura.INSTANCE.getTarget() != null
                        && KillAura.INSTANCE.getTarget() != mc.player
                        && RotationUtils.getDistanceToEntity(KillAura.INSTANCE.getTarget()) <= range.getValue().floatValue()
                        && mc.player.onGround() && mc.player.hurtTime > 0) {
                    Stuck stuck = (Stuck) Loratadine.INSTANCE.getModuleManager().getModule(Stuck.class);
                    stuck.setEnabled(true);
                }

                if (KillAura.INSTANCE.getTarget() != null && KillAura.INSTANCE.getTarget() != mc.player && RotationUtils.getDistanceToEntity(KillAura.INSTANCE.getTarget()) >= range.getValue().floatValue()) {
                    Stuck stuck = (Stuck) Loratadine.INSTANCE.getModuleManager().getModule(Stuck.class);
                    stuck.setEnabled(false);
                }
            }

            case "Jump Reset" -> {
                if (mc.player.onGround() && mc.player.hurtTime > 0) {
                    mc.player.setSprinting(false);
                    mc.player.input.jumping = true;
                }
            }
        }

        this.setSuffix(mode_Value.getValue());
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.level == null || mc.player == null || mc.getConnection() == null) return;

        Packet<?> packet = event.getPacket();

        if ((onlyGround.getValue() && !mc.player.onGround()) || (onlyMove.getValue() && !MoveUtils.isMoving()) || flags != 0) {
            return;
        }

        if (packet instanceof ClientboundSetEntityMotionPacket packetEntityVelocity) {
            if (packetEntityVelocity.getId() != mc.player.getId()) return;

            if (debugMessageValue.getValue())
                ClientUtils.log("[Debug]Entity Velocity >> " + MathUtils.getRandomNumber(1000, 10000) + " <<");

            switch (mode_Value.getValue()) {
                case "Vanilla" -> event.setCancelled(true);

                case "Watch Dog" -> {
                    event.setCancelled(true);
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, packetEntityVelocity.getYa() / 8000.0D, mc.player.getDeltaMovement().z);
                }

                case "Grim Attack" -> {
                    //Entity
                    if (KillAura.INSTANCE.getTarget() != null && KillAura.INSTANCE.getTarget() != mc.player && !mc.player.onClimbable()) {
                        final HitResult hitResult = RayCastUtil.rayCast(new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast()), 3.0);
                        if (rayCast.getValue() && hitResult != null) {
                            if (hitResult.getType() != HitResult.Type.ENTITY || !KillAura.INSTANCE.getTarget().equals(((EntityHitResult) hitResult).getEntity())) {
                                return;
                            }
                        }
                        boolean state = WrapperUtils.getWasSprinting();

                        if (!sprintOnly.getValue() || state) {

                            if (attacked) return;

                            velocityInput = true;

                            reduceXZ = 1;

                            if (!state) {
                                packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                                packets.offer(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));
                                slowdownTicks = true;
                            }

                            final double motionX = packetEntityVelocity.getXa() / 8000.0;
                            final double motionZ = packetEntityVelocity.getZa() / 8000.0;
                            double velocityDistance = Math.sqrt(motionX * motionX + motionZ * motionZ);

                            int counter = 0;
                            while (velocityDistance * reduceXZ > targetMotion.getValue().floatValue() && counter <= this.counter.getValue().intValue()) {
                                packets.offer(ServerboundInteractPacket.createAttackPacket(KillAura.INSTANCE.getTarget(), mc.player.isShiftKeyDown()));
                                packets.offer(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                reduceXZ *= 0.6;
                                counter++;
                            }

                            if (!state) {
                                packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                            }

                            attacked = true;
                        }
                    }

                    //Projectile
                    if (attacked_FlightObject.getValue()) {
                        for (Entity entity : mc.level.entitiesForRendering()) {
                            if (entity != null
                                    && entity != mc.player
                                    && entity instanceof Projectile
                                    && !((AntiBot) Loratadine.INSTANCE.getModuleManager().getModule(AntiBot.class)).isServerBot(entity)
                                    && RotationUtils.getDistanceToEntity(entity) > 6.0) {
                                final HitResult hitResult = RayCastUtil.rayCast(new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast()), 3.0);
                                if (rayCast.getValue() && hitResult != null) {
                                    if (hitResult.getType() != HitResult.Type.ENTITY || !entity.equals(((EntityHitResult) hitResult).getEntity())) {
                                        return;
                                    }
                                }

                                if (attackedFlightObject) return;

                                if (entity.onGround()) continue;

                                velocityInput = true;

                                boolean state = WrapperUtils.getWasSprinting();

                                reduceXZ = 1;

                                if (!state) {
                                    packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                                    packets.offer(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));
                                    slowdownTicks = true;
                                }

                                final double motionX = packetEntityVelocity.getXa() / 8000.0;
                                final double motionZ = packetEntityVelocity.getZa() / 8000.0;
                                double velocityDistance = Math.sqrt(motionX * motionX + motionZ * motionZ);

                                int counter = 0;
                                while (velocityDistance * reduceXZ > targetMotion.getValue().floatValue() && counter <= this.counter.getValue().intValue()) {
                                    packets.offer(ServerboundInteractPacket.createAttackPacket(entity, mc.player.isShiftKeyDown()));
                                    packets.offer(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                    reduceXZ *= 0.6;
                                    counter++;
                                }

                                if (!state) {
                                    packets.offer(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                                }

                                attackedFlightObject = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (packet instanceof ClientboundPlayerPositionPacket) {
            if (flagCheckValue.getValue()) {
                flags = flagTickValue.getValue().intValue();
                if (flagDebugMessageValue.getValue()) ClientUtils.log("[Velocity]Debug Flags.");
            }
            if (flagDisable.getValue()) {
                setEnabled(false);
                if (flagDebugMessageValue.getValue()) ClientUtils.log("[Velocity]Auto Disabled.");
            }
            while (!inBound.isEmpty()) {
                inBound.poll().handle(mc.player.connection);
            }
        }
    }
}