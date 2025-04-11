package shop.xmz.lol.loratadine.player;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.glfw.GLFW;

/**
 * @author Cool / DSJ_
 * @since 2025/2/28
 */
public class Blink extends Module {

    public Blink() {
        super("Blink", "瞬移" ,Category.PLAYER, GLFW.GLFW_KEY_V);
    }

    private final Animation animation = new EaseBackIn(500, 1.0, 1.8f);
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    public static final TimerUtils targetTimer = new TimerUtils();
    public static RemotePlayer fakePlayer = null;
    private boolean disableLogger;

    @Override
    public void onEnable() {
        if (mc.level == null || mc.player == null) return;

        fakePlayer = new RemotePlayer(mc.level, mc.player.getGameProfile());

        fakePlayer.yHeadRot = mc.player.yHeadRot;
        fakePlayer.copyPosition(mc.player);

        fakePlayer.setUUID(UUID.randomUUID());
        WrapperUtils.addEntity(mc.level,fakePlayer.getId(),fakePlayer);
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.level == null || fakePlayer == null)
            return;

        blink();
        mc.level.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
        fakePlayer = null;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        int y = mc.getWindow().getGuiScaledHeight() / 2 + 70;

        if (!packets.isEmpty()) {
            targetTimer.reset();
            this.animation.setDirection(Direction.FORWARDS);
        }

        if (packets.size() > 55){
            this.animation.setDirection(Direction.BACKWARDS);
        }

        if (!packets.isEmpty() && !animation.finished(Direction.BACKWARDS)) {
            event.poseStack().pushPose();
            switch (HUD.INSTANCE.count_Value.getValue()) {
                case "Simple" ->
                        ProgressbarManager.drawSimpleCountInfo(event.poseStack(), "Blink now...", packets.size() / 40F, packets.size(), y);
                case "Modern" ->
                        ProgressbarManager.drawModernCountInfo(event.poseStack(), animation,packets.size() / 40F, y);
                case "Basic" ->
                        ProgressbarManager.drawBasicCountInfo(event.poseStack(), packets.size() / 40F, y);
            }
            event.poseStack().popPose();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (mc.player == null || mc.level == null || disableLogger || event.getSide() == Event.Side.PRE) return;

        event.setCancelled(true);
        packets.add(packet);
    }

    @EventTarget
    public void onUpdate(LivingUpdateEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (packets.size() >= 40F){
            setEnabled(false);
        }
        setSuffix(String.valueOf(packets.size()));
    }

    private void blink() {
        if (mc.player == null) return;

        try {
            disableLogger = true;

            while (!packets.isEmpty()) {
                mc.player.connection.getConnection().send(packets.take());
            }
            
            disableLogger = false;
        } catch (final Exception e) {
            e.printStackTrace();
            disableLogger = false;
        }
    }
}