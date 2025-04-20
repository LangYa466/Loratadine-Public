package shop.xmz.lol.loratadine.utils.item;

import com.google.common.collect.Multimap;
import lombok.experimental.UtilityClass;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.*;


@UtilityClass
public final class InventoryUtils implements Wrapper {

    public static final int INCLUDE_ARMOR_BEGIN = 5;
    public static final int EXCLUDE_ARMOR_BEGIN = 9;
    public static final int ONLY_HOT_BAR_BEGIN = 36;
    public static final int END = 45;

    private static final Set<MobEffect> GOOD_STATUS_EFFECTS = new HashSet<>(Set.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.HEAL,
            MobEffects.DAMAGE_BOOST,
            MobEffects.JUMP,
            MobEffects.REGENERATION,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING,
            MobEffects.NIGHT_VISION,
            MobEffects.HEALTH_BOOST,
            MobEffects.ABSORPTION,
            MobEffects.SATURATION,
            MobEffects.LUCK,
            MobEffects.SLOW_FALLING,
            MobEffects.CONDUIT_POWER,
            MobEffects.DOLPHINS_GRACE,
            MobEffects.HERO_OF_THE_VILLAGE,
            MobEffects.INVISIBILITY
    ));

    public static boolean isBestCrossBow(InventoryMenu handler,
                                         final ItemStack itemStack) {
        double bestBowDmg = -1.0;
        ItemStack bestBow = ItemStack.EMPTY;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof CrossbowItem) {
                final double damage = getCrossBowDamage(stack);

                if (damage > bestBowDmg) {
                    bestBow = stack;
                    bestBowDmg = damage;
                }
            }
        }

        return itemStack.equals(bestBow) || getBowDamage(itemStack) > bestBowDmg;
    }

    public static boolean isBestBow(AbstractContainerMenu handler,
                                    final ItemStack itemStack) {
        double bestBowDmg = -1.0;
        ItemStack bestBow = ItemStack.EMPTY;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = handler.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof BowItem) {
                final double damage = getBowDamage(stack);

                if (damage > bestBowDmg) {
                    bestBow = stack;
                    bestBowDmg = damage;
                }
            }
        }

        return itemStack.equals(bestBow) || getBowDamage(itemStack) > bestBowDmg;
    }

    public static double getCrossBowDamage(ItemStack stack) {
        double damage = 0.0;

        if (stack.getItem() instanceof CrossbowItem && stack.isEnchanted())
            damage += getLevel(Enchantments.POWER_ARROWS, stack);

        return damage;
    }

    public static int findItem(final int startSlot, final int endSlot, final Item item) {
        for (int i = startSlot; i < endSlot; i++) {
            final ItemStack stack = mc.player.getInventory().getItem(i);

            if (!stack.isEmpty() && stack.getItem().equals(item)) return i;
        }
        return -1;
    }

    public static double getBowDamage(ItemStack stack) {
        double damage = 0.0;

        if (stack.getItem() instanceof BowItem && stack.isEnchanted())
            damage += getLevel(Enchantments.POWER_ARROWS, stack);

        return damage;
    }

    public static boolean isBuffPotion(final ItemStack stack) {
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);

        for (MobEffectInstance effect : effects) {
            if (!effect.getEffect().isBeneficial()) {
                return false;
            }
        }

        return true;
    }


    public static boolean isBestTool(AbstractContainerMenu handler, final ItemStack itemStack) {
        final int type = getToolType(itemStack);

        Tool bestTool = new Tool(-1, -1, ItemStack.EMPTY);

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = handler.getSlot(i).getItem();

            if (!stack.isEmpty() && stack.getItem() instanceof DiggerItem && type == getToolType(stack)) {
                final double efficiency = getToolScore(stack);
                if (efficiency > getToolScore(bestTool.getItem()))
                    bestTool = new Tool(i, efficiency, stack);
            }
        }

        return bestTool.getItem().equals(itemStack) ||
                getToolScore(itemStack) > getToolScore(bestTool.getItem());
    }

    public static float getToolScore(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof DiggerItem tool) {
            if (item instanceof PickaxeItem) {
                score = tool.getDestroySpeed(stack, Blocks.STONE.defaultBlockState());
            } else {
                if (!(item instanceof AxeItem)) return 1;
                score = tool.getDestroySpeed(stack, Blocks.DARK_OAK_LOG.defaultBlockState());
            }
            score += getLevel(Enchantments.BLOCK_EFFICIENCY, stack) * 0.0075F;
            score += getLevel(Enchantments.BLOCK_EFFICIENCY, stack) / 100F;
            score += getLevel(Enchantments.SHARPNESS, stack) * 1F;
        }
        return score;
    }


    public static float getToolEfficiency(final ItemStack itemStack) {
        final DiggerItem tool = (DiggerItem) itemStack.getItem();
        // 1.20.1 变化：使用 Tier 接口的 getSpeed 方法
        float efficiency = tool.getTier().getSpeed();

        final int lvl = getLevel(Enchantments.BLOCK_EFFICIENCY, itemStack);

        if (efficiency > 1.0F && lvl > 0)
            efficiency += lvl * lvl + 1;

        return efficiency;
    }

    public static boolean isGoodFood(final ItemStack stack) {
        if (stack.getItem() == Items.GOLDEN_APPLE)
            return true;

        FoodProperties component = stack.getItem().getFoodProperties(stack, null);
        if (component == null) return false;

        return component.getNutrition() >= 4 && component.getSaturationModifier() >= 0.3F;
    }

    public static boolean isGoodItem(final ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.FIRE_CHARGE || item == Items.ENDER_PEARL || item == Items.ARROW || item == Items.WATER_BUCKET || item == Items.SLIME_BALL || item == Items.TNT || (item instanceof CrossbowItem && isBestCrossBow(mc.player.inventoryMenu, stack));
    }

    public static boolean isBestSword(final AbstractContainerMenu c,
                                      final ItemStack itemStack) {
        double damage = 0.0;
        ItemStack bestStack = ItemStack.EMPTY;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = c.getSlot(i).getItem();

            if (!stack.isEmpty() && stack.getItem() instanceof SwordItem) {
                double newDamage = getItemDamage(stack);

                if (newDamage > damage) {
                    damage = newDamage;
                    bestStack = stack;
                }
            }
        }

        return bestStack.equals(itemStack) || getItemDamage(itemStack) > damage;
    }


    public static boolean isBestArmor(final AbstractContainerMenu c,
                                      final ItemStack itemStack) {
        final ArmorItem itemArmor = (ArmorItem) itemStack.getItem();

        double reduction = 0.0;
        ItemStack bestStack = ItemStack.EMPTY;

        for (int i = INCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = c.getSlot(i).getItem();

            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem stackArmor) {
                if (stackArmor.getEquipmentSlot() == itemArmor.getEquipmentSlot()) {
                    final double newReduction = getDamageReduction(stack);

                    if (newReduction > reduction) {
                        reduction = newReduction;
                        bestStack = stack;
                    }
                }
            }
        }

        return bestStack.equals(itemStack) || getDamageReduction(itemStack) > reduction;
    }

    public static double getDamageReduction(final ItemStack stack) {
        double reduction = 0.0;

        if (!(stack.getItem() instanceof ArmorItem)) {
            return 0.0;
        }

        final ArmorItem armor = (ArmorItem) stack.getItem();

        reduction += armor.getDefense();

        if (stack.isEnchanted())
            reduction += getLevel(Enchantments.ALL_DAMAGE_PROTECTION, stack) * 0.25;

        return reduction;
    }


    public static double getItemDamage(final ItemStack stack) {
        double damage = 0.0;

        // ------------------- 基础属性计算 -------------------
        final Multimap<Attribute, AttributeModifier> attributes = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);

        // 获取所有攻击伤害属性修饰符并累加
        for (AttributeModifier modifier : attributes.get(Attributes.ATTACK_DAMAGE)) {
            damage += modifier.getAmount(); // 累加所有修饰符值
        }

        // ------------------- 附魔计算 -------------------
        final int sharpnessLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);
        final int smiteLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, stack);
        final int baneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, stack);

        // 根据原版机制计算附魔加成
        damage += sharpnessLevel * 1.0;    // 锋利：每级 +1
        damage += smiteLevel * 2.5;        // 亡灵杀手：每级 +2.5（对亡灵生物）
        damage += baneLevel * 2.5;         // 节肢杀手：每级 +2.5（对节肢动物）

        return Math.max(damage, 0.0); // 确保非负数
    }

    private static int getLevel(Enchantment registryKey, ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(registryKey, itemStack);
    }

    public static int getToolType(final ItemStack stack) {
        final DiggerItem tool = (DiggerItem) stack.getItem();

        if (tool instanceof PickaxeItem) return 0;
        else if (tool instanceof AxeItem) return 1;
        else return -1;
    }


    public static List<ItemStack> getItemStacks(Player player) {
        List<ItemStack> result = new ArrayList<>();

        for (Slot slot : player.inventoryMenu.slots) {
            if (!slot.getItem().isEmpty()) {
                result.add(slot.getItem());
            }
        }

        return result;
    }

    public static float getPlayerArmorScore(Player player) {
        float score = 0f;

        for (int armorSlot = 5; armorSlot < 9; armorSlot++) {
            ItemStack stack = player.inventoryMenu.getSlot(armorSlot).getItem();
            if (stack.isEmpty()) {
                continue;
            }
            score += (float) getDamageReduction(stack);
        }

        return score;
    }

    public static boolean isArmorBetter(Player player) {
        return getPlayerArmorScore(player) < getPlayerArmorScore(mc.player);
    }


    public enum BlockAction {
        PLACE, REPLACE, PLACE_ON
    }

    private static class Tool {
        private final int slot;
        private final double efficiency;
        private final ItemStack stack;

        public Tool(int slot, double efficiency, ItemStack stack) {
            this.slot = slot;
            this.efficiency = efficiency;
            this.stack = stack;
        }

        public int getSlot() {
            return slot;
        }

        public double getEfficiency() {
            return efficiency;
        }

        public ItemStack getItem() {
            return stack;
        }
    }
}