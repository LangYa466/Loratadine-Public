package shop.xmz.lol.loratadine.modules.impl.misc;

import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;

import java.util.*;

public class AntiBot extends Module {
    private final BooleanSetting entityID = new BooleanSetting("EntityID", this, false);
    private final BooleanSetting sleep = new BooleanSetting("Sleep", this, false);
    private final BooleanSetting noArmor = new BooleanSetting("NoArmor", this, false);
    private final BooleanSetting height = new BooleanSetting("Height", this, false);
    private final BooleanSetting ground = new BooleanSetting("Ground", this, false);
    private final BooleanSetting dead = new BooleanSetting("Dead", this, false);
    private final BooleanSetting health = new BooleanSetting("Health", this, false);
    private final BooleanSetting hypixel = new BooleanSetting("Hypixel", this, false);
    private final BooleanSetting matrix = new BooleanSetting("Heypixel", this, true);

    private final List<Integer> groundBotList = new ArrayList<>();
    private final Set<UUID> bots = new HashSet<>();

    public AntiBot() {
        super("AntiBot", "防假人", Category.MISC);
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        clearAll();
    }

    private void clearAll() {
        bots.clear();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (mc.player == null || mc.level == null) return;

        if (packet instanceof ClientboundMoveEntityPacket wrapper && ground.getValue()) {
            Entity entity = wrapper.getEntity(mc.level);

            if (entity instanceof Player) {
                if (wrapper.isOnGround() && !groundBotList.contains(entity.getId())) {
                    groundBotList.add(entity.getId());
                }
            }
        }

        if (matrix.getValue()) {
            mc.level.entitiesForRendering().forEach(player -> {
                if (packet instanceof ClientboundPlayerInfoUpdatePacket wrapper) {
                    for (ClientboundPlayerInfoUpdatePacket.Entry data : wrapper.entries()) {
                        if (wrapper.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) &&
                                data.profile().getProperties().isEmpty()
                                && wrapper.entries().size() == 1
                                && mc.getConnection() != null && mc.getConnection().getPlayerInfo(data.profile().getName()) != null) {
                            bots.add(data.profile().getId());
                        }
                    }
                }
            });
        }
    }

    public boolean isServerBot(Entity entity) {
        if (Loratadine.INSTANCE.getModuleManager().getModule(AntiBot.class).isEnabled()) {
            if (entity instanceof Player) {
                if (height.getValue() && (entity.getBbHeight() <= 0.5 || ((Player) entity).isSleeping() || entity.tickCount < 80)) {
                    return true;
                }
                if (dead.getValue() && ((Player) entity).isDeadOrDying()) {
                    return true;
                }
                if (health.getValue() && ((Player) entity).getHealth() == 0.0F) {
                    return true;
                }
                if (sleep.getValue() && ((Player) entity).isSleeping()) {
                    return true;
                }
                if (entityID.getValue() && (entity.getId() >= 1000000000 || entity.getId() <= -1)) {
                    return true;
                }
                if (hypixel.getValue() && mc.getConnection().getPlayerInfo(entity.getUUID()) == null) {
                    return true;
                }
                if (ground.getValue() && !groundBotList.contains(entity.getId())) {
                    return true;
                }
                if (matrix.getValue()) {
                    return bots.contains(entity.getUUID());
                }
                return noArmor.getValue() && (((Player) entity).getInventory().getArmor(0).isEmpty()
                        && ((Player) entity).getInventory().getArmor(1).isEmpty()
                        && ((Player) entity).getInventory().getArmor(2).isEmpty()
                        && ((Player) entity).getInventory().getArmor(3).isEmpty());
            }
        }
        return false;
    }
}