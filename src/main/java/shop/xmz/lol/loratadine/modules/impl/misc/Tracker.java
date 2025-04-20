package shop.xmz.lol.loratadine.modules.impl.misc;

import cn.lzq.injection.leaked.invoked.TickEvent;
import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;
import shop.xmz.lol.loratadine.utils.ClientUtils;
import shop.xmz.lol.loratadine.utils.player.PlayerTrackerUtils;

import java.util.ArrayList;
import java.util.List;

public class Tracker extends Module {
    public BooleanSetting lightning = new BooleanSetting("Lightning",this,true);
    public BooleanSetting allPlayer = new BooleanSetting("AllPlayer Check",this,true);

    public BooleanSetting bannedCheck = new BooleanSetting("Banned Check",this,true);
    public BooleanSetting autoHub = (BooleanSetting) new BooleanSetting("Auto Hub",this,true)
            .setVisibility(() -> bannedCheck.getValue());
    public BooleanSetting autoDisconnected = (BooleanSetting) new BooleanSetting("Auto Disconnected",this,true)
            .setVisibility(() -> bannedCheck.getValue());

    public static List<Entity> flaggedEntity = new ArrayList<>();
    public static int banned = 0;

    public Tracker() {
        super("Tracker","检测玩家行为", Category.MISC);
    }

    @Override
    public void onDisable() {
        flaggedEntity.clear();
        super.onDisable();
    }

    @EventTarget
    public void onWorld(WorldEvent e) {
        flaggedEntity.clear();
    }

    @EventTarget
    public void onChatReceived(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundSystemChatPacket wrapper) {

            if (wrapper.content().getString().isEmpty()) return;

            if (bannedCheck.getValue()) {
                // 获取聊天消息内容
                String message = wrapper.content().getString();
                String playerName = mc.player.getGameProfile().getName(); // 获取自己的名字

                // 检测是否为自己发送的消息
                if (message.startsWith("<" + playerName + ">")) {
                    return; // 忽略自己发送的消息
                }

                // 检测是否包含“违规”
                if (message.contains("违规")) {

                    banned++; // 增加 banned 计数
                    ClientUtils.log("检测到违规信息！有一个黑客被妖猫击落了，本局游戏封禁人数：" + banned);

                    // 自动逃逸
                    if (autoHub.getValue() && bannedCheck.getValue())
                        mc.gui.getChat().addMessage(Component.literal("/hub"));

                    // 自动退出服务器
                    if (mc.getConnection() != null && autoDisconnected.getValue() && bannedCheck.getValue()) {
                        Connection networkManager = mc.getConnection().getConnection();
                        networkManager.disconnect(Component.literal("检测到违规信息，自动退出服务器"));
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacketEvent(PacketEvent e) {
        if (mc.player == null || mc.level == null) return;

        if (lightning.getValue()) {
            if (e.getPacket() instanceof ClientboundAddEntityPacket packet) {
                // 获取实体类型，不是閃電就跳過
                if (packet.getType() != EntityType.LIGHTNING_BOLT) {
                    return;
                }

                // 获取坐标
                final Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());

                final Vec3 playerPos = mc.player.position();
                final double distance = playerPos.distanceTo(pos);

                // 发送聊天消息
                ClientUtils.log("闪电击中 | " + "X: " +
                        String.format("%.2f", pos.x) + ", " + "Y: " +
                        String.format("%.2f", pos.y) + ", " + "Z: " +
                        String.format("%.2f", pos.z) + " | 距离玩家 (" +
                        (int) distance + " 米)"
                );
            }
        }
    }


    @EventTarget
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.level == null || mc.level.players().isEmpty()) return;

        if (mc.player.tickCount % 6 != 0) return;

        for (Player player : mc.level.players()) {

            boolean me = player != mc.player;
            boolean flagged = flaggedEntity.contains(player);
            boolean sameTeam = Teams.INSTANCE.isSameTeam(player);

            if (!allPlayer.getValue() && me && !sameTeam && !flagged) {
                checkAndFlag(player);
            }

            if (allPlayer.getValue() && !flagged) {
                checkAndFlag(player);
            }

        }
    }

    private void checkAndFlag(Player player) {
        if (PlayerTrackerUtils.isStrength(player) > 0) {
            flagPlayer(player, "攻击伤害异常");
        }
        if (PlayerTrackerUtils.isRegen(player) > 0) {
            flagPlayer(player, "恢复速度异常");
        }
        if (PlayerTrackerUtils.isHoldingGodAxe(player)) {
            flagPlayer(player, "持有秒人斧");
        }
        if (PlayerTrackerUtils.isHoldingSlimeball(player)) {
            flagPlayer(player, "持有击退球");
        }
        if (PlayerTrackerUtils.isHoldingTotemo(player)) {
            flagPlayer(player, "持有不死图腾");
        }
        if (PlayerTrackerUtils.isHoldingCrossbow(player)) {
            flagPlayer(player, "持有弩");
        }
        if (PlayerTrackerUtils.isHoldingBow(player)) {
            flagPlayer(player, "持有弓");
        }
        if (PlayerTrackerUtils.isHoldingFireCharge(player)) {
            flagPlayer(player, "持有火焰弹");
        }
    }

    private void flagPlayer(Player player, String message) {
        flaggedEntity.add(player);
        NotificationManager.add(NotificationType.WARNING, HUD.INSTANCE.languageValue.is("Chinese") ? "检测" : "Tracker", player.getName().getString() + " " + message, 10);
    }

}
