package shop.xmz.lol.loratadine.modules.impl.misc;

import cn.lzq.injection.leaked.invoked.AttackEvent;
import cn.lzq.injection.leaked.invoked.UpdateEvent;
import cn.lzq.injection.leaked.invoked.WorldEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.LivingEntity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.misc.EntityUtils;

public class Insults extends Module {
    public Insults() {
        super("Insults", "击杀喊话", Category.MISC);
    }
    private LivingEntity attackEntity = null;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;

        if (attackEntity != null && attackEntity != mc.player && !attackEntity.isAlive()) {
            if (EntityUtils.isAnimal(attackEntity) || EntityUtils.isMob(attackEntity) || (Loratadine.INSTANCE.getModuleManager().getModule(Teams.class).isEnabled()
                    && ((Teams) Loratadine.INSTANCE.getModuleManager().getModule(Teams.class)).isSameTeam(attackEntity))) return;

            int randomText = MathUtils.getRandomNumber(1, 2);

            switch (randomText) {
                case 1 -> mc.player.sendSystemMessage(Component.literal("!" + attackEntity.getName().getString() + "正试图逃跑，结果被我无情击毙"));
                case 2 -> mc.player.sendSystemMessage(Component.literal("!" + attackEntity.getName().getString() + "已被我无情痛打，不要扣字我没有下载Dog language翻译器"));
            }

            attackEntity = null;
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        attackEntity = null;
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof LivingEntity livingEntity) {
            attackEntity = livingEntity;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null) return;

        Packet<?> packet = event.getPacket();

        // Since you mentioned there's no ClientboundChatPacket,
        // I've commented out the chat packet handling
        // You'll need to replace this with the appropriate packet
        // handling method for your specific mod's event system

        /*
        if (packet instanceof ServerboundChatPacket wrapper) {
            String input = wrapper.getMessage();
            String regex = "床被摧毁 > (.+?) 破坏了 (.+?) 的床！";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            // 1破坏者 // 2破坏的队伍
            if (matcher.find()) {
                if (matcher.group(1).contains(mc.player.getName().getString())) {

                    int randomText = MathUtils.getRandomNumber(1, 3);

                    switch (randomText) {
                        case 1 -> mc.player.sendSystemMessage(Component.literal("!" + matcher.group(2) + "老弟，本皇已给你床拆掉咯，转人工"));
                        case 2 -> mc.player.sendSystemMessage(Component.literal("!就这啊? " + matcher.group(2) + "老弟，回去练练你的守家技术吧"));
                        case 3 -> mc.player.sendSystemMessage(Component.literal("一般一般，回去练练你的守家技术吧"));
                    }
                }
            }
        }
        */
    }
}