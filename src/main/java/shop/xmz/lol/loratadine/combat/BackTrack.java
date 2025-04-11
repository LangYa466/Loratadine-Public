package shop.xmz.lol.loratadine.combat;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.LinkedBlockingQueue;

public class BackTrack extends Module {
    LivingEntity target;
    private final NumberSetting amount = new NumberSetting("Amount", this,3.0, 1.0,7.0,0.1);
    private final NumberSetting range = new NumberSetting("Range", this,3.0, 2.0, 7.0, 0.1);
    private final NumberSetting interval = new NumberSetting("Interval Tick", this,4, 0, 10, 1);
    private final BooleanSetting clientSideSetPos = new BooleanSetting("ClientSide SetPos", this,true);
    private final BooleanSetting onlyKillAura = new BooleanSetting("Only KillAura", this,true);
    private final BooleanSetting rayCast = new BooleanSetting("RayCast", this, true);
    private final BooleanSetting debug = new BooleanSetting("Debug", this,false);

    private Vec3 realTargetPosition = new Vec3(0.0D, 0.0D, 0.0D);
    private final LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new LinkedBlockingQueue<>();
    private boolean disableLogger;
    private boolean canBackTrack;
    int tick = 0;

    public BackTrack() {
        super("BackTrack", "回溯" ,Category.COMBAT);
    }

    @EventTarget
    public void onAttack(AttackEvent e) {
        if (e == null || e.getTarget() == null) return;

        if (target == null) target = (LivingEntity) e.getTarget();
    }

    @EventTarget
    public void onTick(TickEvent e) {
        if (mc.player == null) return;

        if (onlyKillAura.getValue() && Loratadine.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() && KillAura.INSTANCE.getTarget() != null) {
            target = KillAura.INSTANCE.getTarget();
        }

        if (target != null
                && (RotationUtils.getDistanceToEntity(target) > range.getValue().floatValue() || target.isDeadOrDying() || !target.isAlive() || target.getHealth() <= 0)
                || (mc.player.isDeadOrDying() || !mc.player.isAlive() || mc.player.getHealth() <= 0)) {
            target = null;
        }

        if (target == null) return;

        final HitResult hitResult = RayCastUtil.rayCast(new Rotation(WrapperUtils.getYRotLast(), WrapperUtils.getXRotLast()), 3.0);
        if (rayCast.getValue() && hitResult != null) {
            if (hitResult.getType() != HitResult.Type.ENTITY || !target.equals(((EntityHitResult) hitResult).getEntity())) {
                return;
            }
        }

        if (target != null
                && RotationUtils.getDistanceToEntity(target) <= range.getValue().floatValue()
                && new Vec3(target.getX(), target.getY(), target.getZ()).distanceTo(realTargetPosition) < amount.getValue().floatValue()
                && tick >= interval.getValue().intValue()) {
            canBackTrack = true;
            if (clientSideSetPos.getValue()) target.setPos(target.xo, target.yo, target.zo);
            if (debug.getValue()) ClientUtils.log("["+getModuleName()+"]: 触发回溯 tick:"+tick+"/"+interval.getValue());
            tick = 0;
        } else {
            canBackTrack = false;
            stop();
            tick++;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent e) {
        if (mc.player == null || mc.level == null || e.getPacket() == null || target == null || disableLogger || e.getSide() == Event.Side.POST) return;

        Packet<?> packet = e.getPacket();

        if (packet instanceof ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {
            if (clientboundTeleportEntityPacket.getId() == target.getId()) {
                realTargetPosition = new Vec3(clientboundTeleportEntityPacket.getX(), clientboundTeleportEntityPacket.getY(), clientboundTeleportEntityPacket.getZ());

                e.setCancelled(true);
                packets.add((Packet<ClientGamePacketListener>) packet);
            }
        }
        if (packet instanceof ClientboundMoveEntityPacket clientboundMoveEntityPacket && canBackTrack) {
            if (clientboundMoveEntityPacket.getEntity(mc.level) == target) {
                realTargetPosition.add(clientboundMoveEntityPacket.getXa(),clientboundMoveEntityPacket.getYa(),clientboundMoveEntityPacket.getZa());

                e.setCancelled(true);
                packets.add((Packet<ClientGamePacketListener>) packet);
            }
        }
    }

    public void onDisable() {
        target = null;
        tick = 0;
        stop();
    }

    private void stop() {
        if (mc.player == null) return;

        try {
            disableLogger = true;

            while (!packets.isEmpty()) {
                packets.take().handle(mc.player.connection);
            }

            disableLogger = false;
        } catch (final Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }
    }
}
