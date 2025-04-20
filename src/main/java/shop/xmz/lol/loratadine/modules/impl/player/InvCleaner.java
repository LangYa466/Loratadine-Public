package shop.xmz.lol.loratadine.modules.impl.player;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import cn.lzq.injection.leaked.invoked.MotionEvent;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.item.InventoryUtils;
import shop.xmz.lol.loratadine.utils.player.BlockUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InvCleaner extends Module {

    public InvCleaner() {
        super("InvCleaner", "背包整理" ,Category.PLAYER, GLFW.GLFW_KEY_B);
    }
    final ModeSetting autoOffHand = new ModeSetting("Mode",this,new String[]{"Gapple","Snowball/Egg","None"}, "None");

    private final NumberSetting delay = new NumberSetting("Delay",this,5, 0, 300, 10);
    private final NumberSetting armorDelay = new NumberSetting("Armor Delay",this,20, 0, 300, 10);
    public final NumberSetting slotWeapon = new NumberSetting("Weapon Slot",this,1, 1, 9, 1);
    public final NumberSetting slotPick = new NumberSetting("Pickaxe Slot",this,7, 1, 9, 1);
    public final NumberSetting slotAxe = new NumberSetting("Axe Slot",this,8, 1, 9, 1);
    public final NumberSetting slotGapple = new NumberSetting("Gapple Slot", this, 3, 1, 9, 1);
    public final NumberSetting slotWater = new NumberSetting("Water Slot",this,5, 1, 9, 1);
    public final NumberSetting slotBow = new NumberSetting("Bow Slot",this,4, 1, 9, 1);
    public final NumberSetting slotBlock = new NumberSetting("Block Slot",this,2, 1, 9, 1);
    public final NumberSetting slotPearl = new NumberSetting("Pearl Slot",this,9, 1, 9, 1);

    public final String[] serverItems = {"选择游戏", "加入游戏", "职业选择菜单", "离开对局", "再来一局", "selector", "tracking compass", "(right click)", "tienda ", "perfil", "salir", "shop", "collectibles", "game", "profil", "lobby", "show all", "hub", "friends only", "cofre", "(click", "teleport", "play", "exit", "hide all", "jeux", "gadget", " (activ", "emote", "amis", "bountique", "choisir", "choose "};

    private final int[] bestArmorPieces = new int[6];
    private final List<Integer> trash = new ArrayList<>();
    private final int[] bestToolSlots = new int[2];
    private final List<Integer> gappleStackSlots = new ArrayList<>();
    private int bestSwordSlot;
    private int bestPearlSlot;
    private int bestBowSlot;
    private int bestWaterSlot;

    private int ticksSinceLastClick;

    private boolean nextTickCloseInventory;
    private boolean serverOpen;
    private boolean clientOpen;
    private final TimerUtils timer = new TimerUtils();


    @EventTarget
    private void onPacket(PacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof ClientboundOpenScreenPacket) {
            this.clientOpen = false;
            this.serverOpen = false;
        }
        if (packet instanceof ServerboundPlayerCommandPacket wrapper) {
            if (wrapper.getData() == mc.player.getId() && wrapper.getAction() == ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY) {
                this.clientOpen = true;
                this.serverOpen = true;
            }
        } else if (packet instanceof ServerboundContainerClosePacket wrapper) {

            if (wrapper.getContainerId() == mc.player.inventoryMenu.containerId) {
                this.clientOpen = false;
                this.serverOpen = false;
            }
        } else if (packet instanceof ServerboundContainerClickPacket && !mc.player.isUsingItem()) {
            this.ticksSinceLastClick = 0;
        }
    }


    private boolean dropItem(final List<Integer> listOfSlots) {

        if (!listOfSlots.isEmpty()) {
            int slot = listOfSlots.remove(0);

/*            if (Client.channel == null || !Client.channel.isActive() || !Fucker.login) {
                for (;;) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 1, ClickType.THROW, mc.player);
                    PacketUtils.sendPacketNoEvent(new ServerboundSwingPacket((InteractionHand) null));
                }
            }*/
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 1, ClickType.THROW, mc.player);
            return true;
        }
        return false;
    }


    @EventTarget
    private void onMotion(MotionEvent event) {
        if (mc.player == null
                || mc.gameMode == null
                || KillAura.target != null
                || mc.player.isSpectator()
                || !mc.player.isAlive()
                || mc.player.isDeadOrDying()) return;

        if (!mc.player.isUsingItem()) {
            this.ticksSinceLastClick++;

            if (this.ticksSinceLastClick < Math.floor(this.delay.getValue().doubleValue() / 50)) return;

            if (mc.screen instanceof InventoryScreen) {
                this.clear();

                for (int slot = InventoryUtils.INCLUDE_ARMOR_BEGIN; slot < InventoryUtils.END; slot++) {
                    final ItemStack stack = mc.player.containerMenu.getSlot(slot).getItem();
                    AbstractContainerMenu handler = mc.player.containerMenu;

                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof SwordItem && InventoryUtils.isBestSword(handler, stack)) {
                            this.bestSwordSlot = slot;
                        } else if (stack.getItem() instanceof DiggerItem && InventoryUtils.isBestTool(handler, stack)) {
                            final int toolType = InventoryUtils.getToolType(stack);
                            if (toolType != -1 && slot != this.bestToolSlots[toolType])
                                this.bestToolSlots[toolType] = slot;
                        } else if (stack.getItem() instanceof ArmorItem armor && InventoryUtils.isBestArmor(handler, stack)) {
                            EquipmentSlot armorSlot = armor.getEquipmentSlot();
                            int index = armorSlot.ordinal();

                            if (index >= 1 && index < bestArmorPieces.length + 2) {
                                int pieceSlot = this.bestArmorPieces[index];
                                if (pieceSlot == -1 || slot != pieceSlot) {
                                    this.bestArmorPieces[index] = slot;
                                }
                            }
                        } else if (stack.getItem() instanceof BowItem && InventoryUtils.isBestBow(handler, stack)) {
                            if (slot != this.bestBowSlot)
                                this.bestBowSlot = slot;
                        } else if (stack.getItem() == Items.GOLDEN_APPLE) {
                            this.gappleStackSlots.add(slot);
                        } else if (stack.getItem() == Items.ENDER_PEARL) {
                            this.bestPearlSlot = slot;
                        } else if (stack.getItem() == Items.WATER_BUCKET) {
                            if (slot != this.bestWaterSlot) this.bestWaterSlot = slot;
                        } else if (!this.trash.contains(slot) && !isValidStack(stack)) {
                            this.trash.add(slot);
                        }
                    }
                }

                final boolean busy = (!this.trash.isEmpty()) || this.equipArmor(false) || this.sortItems(false);

                if (!busy) {
                    if (this.nextTickCloseInventory) {
                        this.close();
                        this.nextTickCloseInventory = false;
                    } else {
                        this.nextTickCloseInventory = true;
                    }
                    return;
                } else {
                    boolean waitUntilNextTick = !this.serverOpen;

                    this.open();

                    if (this.nextTickCloseInventory)
                        this.nextTickCloseInventory = false;

                    if (waitUntilNextTick) return;
                }

                if (timer.hasTimeElapsed(this.armorDelay.getValue().longValue()) && this.equipArmor(true))
                    return;

                if (this.dropItem(this.trash)) return;

                this.sortItems(true);

            }
        }
    }

    private boolean sortItems(final boolean moveItems) {
        int goodSwordSlot = this.slotWeapon.getValue().intValue() + 35;

        if (this.bestSwordSlot != -1) {
            if (this.bestSwordSlot != goodSwordSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodSwordSlot, this.bestSwordSlot);
                    this.bestSwordSlot = goodSwordSlot;
                }

                return true;
            }
        }
        int goodBowSlot = this.slotBow.getValue().intValue() + 35;

        if (this.bestBowSlot != -1) {
            if (this.bestBowSlot != goodBowSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodBowSlot, this.bestBowSlot);
                    this.bestBowSlot = goodBowSlot;
                }
                return true;
            }
        }

        int goodWaterSlot = this.slotWater.getValue().intValue() + 35;

        if (this.bestWaterSlot != -1) {
            if (this.bestWaterSlot != goodWaterSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodWaterSlot, this.bestWaterSlot);
                    this.bestWaterSlot = goodWaterSlot;
                }
                return true;
            }
        }

        if (autoOffHand.is("Gapple")) {
            if (!this.gappleStackSlots.isEmpty()) {
                this.gappleStackSlots.sort(Comparator.comparingInt(slot -> -mc.player.containerMenu.getSlot(slot).getItem().getCount()));
                final int bestGappleSlot = this.gappleStackSlots.get(0);
                if (bestGappleSlot != 45) {
                    if (moveItems) {
                        this.putItemInSlot(45, bestGappleSlot);
                        this.gappleStackSlots.set(0, 45);
                    }
                    return true;
                }
            }
        } else {
            // 新增的非Gapple模式处理（自定义槽位）
            if (!this.gappleStackSlots.isEmpty()) {
                int targetSlot = slotGapple.getValue().intValue() + 35;
                this.gappleStackSlots.sort(Comparator.comparingInt(slot -> -mc.player.containerMenu.getSlot(slot).getItem().getCount()));
                final int bestGappleSlot = this.gappleStackSlots.get(0);

                // 检查目标槽位是否已经有金苹果
                ItemStack targetStack = mc.player.containerMenu.getSlot(targetSlot).getItem();
                boolean needsMove = true;

                if (bestGappleSlot == targetSlot) {
                    needsMove = false;
                } else if (targetStack.getItem() == Items.GOLDEN_APPLE) {
                    int currentCount = mc.player.containerMenu.getSlot(bestGappleSlot).getItem().getCount();
                    if (targetStack.getCount() >= currentCount) {
                        needsMove = false;
                    }
                }

                if (needsMove) {
                    if (moveItems) {
                        this.putItemInSlot(targetSlot, bestGappleSlot);
                        this.gappleStackSlots.set(0, targetSlot);
                    }
                    return true;
                }
            }
        }

        // 新增的雪球/鸡蛋处理逻辑
        if (autoOffHand.is("Snowball/Egg")) {
            int maxSnowballSlot = -1;
            int maxSnowballCount = 0;
            int maxEggSlot = -1;
            int maxEggCount = 0;

            // 遍历所有有效槽位（排除装备槽）
            for (int slot = 0; slot < 45; slot++) {
                ItemStack stack = mc.player.containerMenu.getSlot(slot).getItem();
                if (stack.isEmpty()) continue;

                if (stack.getItem() == Items.SNOWBALL) {
                    if (stack.getCount() > maxSnowballCount) {
                        maxSnowballCount = stack.getCount();
                        maxSnowballSlot = slot;
                    }
                } else if (stack.getItem() == Items.EGG) {
                    if (stack.getCount() > maxEggCount) {
                        maxEggCount = stack.getCount();
                        maxEggSlot = slot;
                    }
                }
            }

            // 确定目标物品
            int targetSlot = -1;
            if (maxSnowballCount > maxEggCount) {
                targetSlot = maxSnowballSlot;
            } else if (maxEggCount > maxSnowballCount) {
                targetSlot = maxEggSlot;
            } else if (maxSnowballCount > 0) { // 数量相同时优先雪球
                targetSlot = maxSnowballSlot;
            }

            // 检查是否需要移动
            if (targetSlot != -1) {
                ItemStack offhandStack = mc.player.containerMenu.getSlot(45).getItem();
                boolean shouldMove = offhandStack.isEmpty() ||
                        (offhandStack.getItem() != Items.SNOWBALL && offhandStack.getItem() != Items.EGG) ||
                        (offhandStack.getCount() < (targetSlot == maxSnowballSlot ? maxSnowballCount : maxEggCount));

                if (shouldMove) {
                    if (moveItems) {
                        this.putItemInSlot(45, targetSlot);
                    }
                    return true;
                }
            }
        }


        final int[] toolSlots = {
                slotPick.getValue().intValue() + 35,
                slotAxe.getValue().intValue() + 35};

        for (final int toolSlot : this.bestToolSlots) {
            if (toolSlot != -1) {
                final int type = InventoryUtils.getToolType(mc.player.containerMenu.getSlot(toolSlot).getItem());

                if (type != -1) {
                    if (toolSlot != toolSlots[type]) {
                        if (moveItems) {
                            this.putToolsInSlot(type, toolSlots);
                        }
                        return true;
                    }
                }
            }
        }

        int goodBlockSlot = this.slotBlock.getValue().intValue() + 35;
        int mostBlocksSlot = getMostBlocks();
        if (mostBlocksSlot != -1 && mostBlocksSlot != goodBlockSlot) {
            Slot dss = mc.player.containerMenu.getSlot(goodBlockSlot);
            ItemStack dsis = dss.getItem();
            if (!(!dsis.isEmpty() && dsis.getItem() instanceof BlockItem && dsis.getCount() >= mc.player.containerMenu.getSlot(mostBlocksSlot).getItem().getCount())) {
                this.putItemInSlot(goodBlockSlot, mostBlocksSlot);
            }
        }

        int goodPearlSlot = this.slotPearl.getValue().intValue() + 35;

        if (this.bestPearlSlot != -1) {
            if (this.bestPearlSlot != goodPearlSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodPearlSlot, this.bestPearlSlot);
                    this.bestPearlSlot = goodPearlSlot;
                }
                return true;
            }
        }
        return false;
    }

    public int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.player.containerMenu.getSlot(i);
            ItemStack is = slot.getItem();
            if (!is.isEmpty() && is.getItem() instanceof BlockItem && is.getCount() > stack) {
                boolean noneMatch = true;
                String itemName = is.getItem().getName(is).getString().toLowerCase();
                for (String serverItem : serverItems) {
                    if (itemName.contains(serverItem.toLowerCase())) {
                        noneMatch = false;
                        break;
                    }
                }
                if (noneMatch) {
                    stack = is.getCount();
                    biggestSlot = i;
                }
            }
        }
        return biggestSlot;
    }

    private boolean equipArmor(boolean moveItems) {
        for (int i = 0; i < this.bestArmorPieces.length; i++) {
            final int piece = this.bestArmorPieces[i];

            if (piece != -1) {
                // Adjust slot calculation based on EquipmentSlot
                int armorPieceSlot = getArmorSlot(EquipmentSlot.values()[i]);

                // Check if the slot is within valid range
                if (armorPieceSlot < 0 || armorPieceSlot >= mc.player.containerMenu.slots.size()) {
                    continue;
                }

                final ItemStack stack = mc.player.containerMenu.getSlot(armorPieceSlot).getItem();
//                System.out.println(armorPieceSlot + " " + stack.getItem().getName(stack).getString());
                if (!stack.isEmpty()) {
                    continue;
                }

                if (moveItems) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, piece, 0, ClickType.QUICK_MOVE, mc.player);
                }

                timer.reset();
                return true;
            }
        }

        return false;
    }

    private int getArmorSlot(EquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return 5; // Adjust according to your slot mapping
            case CHEST:
                return 6; // Adjust according to your slot mapping
            case LEGS:
                return 7; // Adjust according to your slot mapping
            case FEET:
                return 8; // Adjust according to your slot mapping
            default:
                return -1; // Invalid slot
        }
    }

    private void putItemInSlot(final int slot, final int slotIn) {
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slotIn,
                slot == 45 ? 40 : (slot - 36),
                ClickType.SWAP, mc.player);
    }

    private void putToolsInSlot(final int tool, final int[] toolSlots) {
        final int toolSlot = toolSlots[tool];

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, this.bestToolSlots[tool],
                toolSlot - 36,
                ClickType.SWAP, mc.player);

        this.bestToolSlots[tool] = toolSlot;
    }

    private int findEmptyBackpackSlot() {
        for (int i = 9; i < 45; i++) { // 遍历背包槽
            Slot slot = mc.player.containerMenu.getSlot(i);
            if (!slot.hasItem()) {
                return i; // 找到第一个空槽
            }
        }
        return -1; // 没有空槽
    }

    private static boolean isValidStack(final ItemStack stack) {
        if (stack.getItem() instanceof FishingRodItem || stack.getItem() instanceof SnowballItem || stack.getItem() instanceof EggItem || stack.getItem() instanceof PotionItem) {
            return true; // 钓鱼竿、雪球、鸡蛋和药水都保留
        }

        if (stack.getItem() instanceof BlockItem && BlockUtils.isValidBlock(((BlockItem) stack.getItem()).getBlock())) {
            return true;
        } else if (stack.getItem() instanceof PotionItem && InventoryUtils.isBuffPotion(stack)) {
            return true;
        } else if (stack.getItem().getFoodProperties() != null && InventoryUtils.isGoodFood(stack)) {
            return true;
        } else if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
            return true;
        } else {
            return InventoryUtils.isGoodItem(stack);
        }
    }


    @Override
    public void onEnable() {
        this.ticksSinceLastClick = 0;
        this.clientOpen = mc.screen instanceof InventoryScreen;
        this.serverOpen = this.clientOpen;
    }

    @Override
    public void onDisable() {
        this.clear();
    }

    private void open() {
        if (!this.clientOpen && !this.serverOpen) {
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
            this.serverOpen = true;
        }
    }

    private void close() {
        if (!this.clientOpen && this.serverOpen) {
            mc.getConnection().send((new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId)));
            this.serverOpen = false;
        }
    }

    private void clear() {
        this.trash.clear();
        this.bestBowSlot = -1;
        this.bestSwordSlot = -1;
        this.bestWaterSlot = -1;
        this.gappleStackSlots.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
    }
}
