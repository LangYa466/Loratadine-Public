package shop.xmz.lol.loratadine.modules.impl.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;

public class Teams extends Module {
    public static Teams INSTANCE;
    private final BooleanSetting armorColorValue = new BooleanSetting("Armor Color",this,true);
    private final BooleanSetting colorValue = new BooleanSetting("Color",this,true);
    private final BooleanSetting gommeSWValue = new BooleanSetting("Gomme SW",this,false);
    private final BooleanSetting scoreboardTeamValue = new BooleanSetting("Scoreboard Team",this,false);

    public Teams() {
        super("Teams", "队伍" ,Category.MISC);
        INSTANCE = this;
    }

    public boolean isSameTeam(LivingEntity entity) {
        if (mc.player == null) return false;

        if (scoreboardTeamValue.getValue() && mc.player.getTeam() != null && entity.getTeam() != null &&
                mc.player.getTeam().isAlliedTo(entity.getTeam())) {
            return true;
        }

        Component displayName = mc.player.getDisplayName();

        if (armorColorValue.getValue()) {
            if (entity instanceof Player entityPlayer) {
                ItemStack myHead = mc.player.getInventory().armor.get(3);
                ItemStack entityHead = entityPlayer.getInventory().armor.get(3);

                if (!myHead.isEmpty() && !entityHead.isEmpty()) {
                    if (String.valueOf(getArmorColor(entityHead)).equals("10511680")) {
                        return true;
                    }
                    if (myHead.getItem() instanceof ArmorItem && entityHead.getItem() instanceof ArmorItem) {
                        return getArmorColor(myHead) == getArmorColor(entityHead);
                    }
                }
            }
        }

        if (gommeSWValue.getValue() && !displayName.getString().isEmpty() && !entity.getDisplayName().getString().isEmpty()) {
            String targetName = entity.getDisplayName().getString().replace("§r", "");
            String clientName = displayName.getString().replace("§r", "");

            if (targetName.startsWith("T") && clientName.startsWith("T")) {
                if (Character.isDigit(targetName.charAt(1)) && Character.isDigit(clientName.charAt(1))) {
                    return targetName.charAt(1) == clientName.charAt(1);
                }
            }
        }

        if (colorValue.getValue() && !displayName.getString().isEmpty() && !entity.getDisplayName().getString().isEmpty()) {
            String targetName = entity.getDisplayName().getString().replace("§r", "");
            String clientName = displayName.getString().replace("§r", "");
            return targetName.startsWith("§" + clientName.charAt(1));
        }

        return false;
    }

    private static int getArmorColor(ItemStack stack) {
        if (stack.getItem() instanceof DyeableLeatherItem) {
            return ((DyeableLeatherItem) stack.getItem()).getColor(stack);
        }
        return -1;
    }
}