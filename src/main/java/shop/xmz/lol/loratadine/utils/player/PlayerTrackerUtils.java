package shop.xmz.lol.loratadine.utils.player;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

public class PlayerTrackerUtils {

    // 检测是否在大厅
    public static boolean isInLobby() {
        ClientLevel world = mc.level;
        if (world == null) return false;

        return world.players().stream().anyMatch(e -> e.getName().getString().contains("问题反馈")
                || e.getName().getString().contains("练习场")
                || e.getName().getString().contains("单人模式")
        );
    }

    // 检测正副手是否持有金斧
    public static boolean isHoldingGodAxe(Player player) {
        return isGodAxe(player.getMainHandItem()) || isGodAxe(player.getOffhandItem());
    }

    // 检测是否是秒人斧
    public static boolean isGodAxe(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.GOLDEN_AXE)) return false;

        // 计算耐久度（剩余耐久）
        int durability = stack.getMaxDamage() - stack.getDamageValue();

        // 获取锋利附魔等级
        int sharpnessLevel = getEnchantmentLevel(stack);

        // 只有当 耐久 ≤ 2 且 锋利 > 20 时，才判定为“特殊金斧”
        return durability <= 2 && sharpnessLevel > 20;
    }

    // 获取附魔等级
    private static int getEnchantmentLevel(ItemStack stack) {
        ListTag enchantmentTagList = stack.getEnchantmentTags();

        for (int i = 0; i < enchantmentTagList.size(); i++) {
            CompoundTag nbt = enchantmentTagList.getCompound(i);
            if (nbt.contains("id") && nbt.contains("lvl") &&
                    nbt.getString("id").equals("minecraft:sharpness")) {
                return nbt.getInt("lvl");
            }
        }
        return 0; // 如果没有找到锋利附魔，返回 0
    }

    // 检测是否持有史莱姆球
    public static boolean isHoldingSlimeball(Player player) {
        return player.getMainHandItem().is(Items.SLIME_BALL) || player.getOffhandItem().is(Items.SLIME_BALL);
    }

    // 检测是否持有不死图腾
    public static boolean isHoldingTotemo(Player player) {
        return player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
    }

    // 检测是否持有弩
    public static boolean isHoldingCrossbow(Player player) {
        return player.getMainHandItem().is(Items.CROSSBOW) || player.getOffhandItem().is(Items.CROSSBOW);
    }

    // 检测是否持有弓
    public static boolean isHoldingBow(Player player) {
        return player.getMainHandItem().is(Items.BOW) || player.getOffhandItem().is(Items.BOW);
    }

    // 检测是否持有火焰球
    public static boolean isHoldingFireCharge(Player player) {
        return player.getMainHandItem().is(Items.FIRE_CHARGE) || player.getOffhandItem().is(Items.FIRE_CHARGE);
    }

    // 检测恢复速度
    public static int isRegen(Player player) {
        MobEffectInstance regenPotion = player.getEffect(MobEffects.REGENERATION);
        return regenPotion == null ? -1 : regenPotion.getDuration();
    }

    // 检测力量
    public static int isStrength(Player player) {
        MobEffectInstance strengthPotion = player.getEffect(MobEffects.DAMAGE_BOOST);
        return strengthPotion == null ? -1 : strengthPotion.getDuration();
    }
}
