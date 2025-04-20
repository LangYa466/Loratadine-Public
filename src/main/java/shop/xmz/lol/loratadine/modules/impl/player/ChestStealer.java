package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.WorldEvent;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import cn.lzq.injection.leaked.invoked.MotionEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.item.InventoryUtils;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cool / DSJ_
 * @since 2025/2/19
 */
@Getter
public class ChestStealer extends Module {
    private final List<BlockPos> openedChests = new ArrayList<>();
    private final TimerUtils closeContainer = new TimerUtils();
    private final TimerUtils stopwatch = new TimerUtils();
    private long lastClickTime = 0; // 用于记录上次点击时间
    private float yaw, pitch;
    private long nextClick;
    private int lastClick;
    private int lastSteal;

    private final NumberSetting delay = new NumberSetting("StealDelay",this,100, 0, 1000, 10);

    private final BooleanSetting aura = new BooleanSetting("Aura",this, false);

    private final BooleanSetting throughWalls = (BooleanSetting) new BooleanSetting("Through Walls",this, true)
                .setVisibility(aura::getValue);
    private final NumberSetting auraRange = (NumberSetting) new NumberSetting("Aura Range",this,  3, 1, 6, 1)
                .setVisibility(aura::getValue);

    private final BooleanSetting chest = new BooleanSetting("Chest",this,true);
    public final BooleanSetting furnace = new BooleanSetting("Furnace",this,true);
    public final BooleanSetting brewingStand = new BooleanSetting("BrewingStand",this,true);
    private final BooleanSetting trash = new BooleanSetting("PickTrash",this,false);

    public ChestStealer() {
        super("ChestStealer", "箱子小偷" ,Category.PLAYER, GLFW.GLFW_KEY_B);
    }

    public void setRotations(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void onDisable() {
        openedChests.clear();
    }
    @EventTarget
    public void onWorld(WorldEvent event){
        openedChests.clear();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null
                || mc.gameMode == null
                || KillAura.target != null
                || !this.stopwatch.hasTimeElapsed(this.nextClick)
                || !mc.player.isAlive()
                || mc.player.isDeadOrDying()
                || mc.player.isSpectator()
                || event.post) return;

        if (aura.getValue()) {
            final int radius = auraRange.getValue().intValue();
            boolean canWallPass = throughWalls.getValue(); // 控制是否允许穿墙
            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {
                    for (int z = -radius; z < radius; z++) {
                        final BlockPos pos = new BlockPos((int) (mc.player.getX() + x), (int) mc.player.getY() + y, (int) mc.player.getZ() + z);

                        // 确保该方块是箱子并且未打开过
                        if (mc.level.getBlockState(pos).getBlock() == Blocks.CHEST && !openedChests.contains(pos)) {
                            // 判断是否能穿墙
                            if (canWallPass || mc.level.getBlockState(pos.offset(0, -1, 0)).isSolid()) {
                                // 构建点击位置（箱子中心）
                                Vec3 hitVec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                                BlockHitResult blockHitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);

                                // 模拟点击延迟，避免被反作弊检测到
                                if (System.currentTimeMillis() - lastClickTime > 200) { // 设置合理的点击间隔（200ms）
                                    lastClickTime = System.currentTimeMillis();

                                    // 直接调用 useItemOn（正确用法）
                                    InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);

                                    // 确保交互成功
                                    if (result.consumesAction()) {
                                        mc.player.swing(InteractionHand.MAIN_HAND);
                                        final float[] rotations = RotationUtils.getFacingRotations(pos.getX(), pos.getY(), pos.getZ());
                                        this.setRotations(rotations[0], rotations[1]);
                                        RotationUtils.setVisualRotations(rotations[0], rotations[1]);
                                        openedChests.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mc.player.containerMenu instanceof ChestMenu container && chest.getValue()) {
            this.lastSteal++;

            if (isChestEmpty(container) && closeContainer.hasTimeElapsed(100)) {
                mc.player.closeContainer();
                return;
            }

            for (int i = 0; i < container.getContainer().getContainerSize(); ++i) {
                if (!container.getContainer().getItem(i).isEmpty()) {

                    if (this.lastSteal <= 1) {
                        continue;
                    }

                    if ((isItemUseful(container, i) || trash.getValue())) {
                        this.nextClick = Math.round(MathUtils.getRandomFloat(this.delay.getValue().intValue(), this.delay.getValue().intValue() + 5));

                        mc.gameMode.handleInventoryMouseClick(container.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);

                        stopwatch.reset();
                        closeContainer.reset();

                        this.lastClick = 0;
                        if (this.nextClick > 0) return;
                    }
                }
            }
        }

        if (mc.player.containerMenu instanceof FurnaceMenu container && furnace.getValue()) {
            this.lastSteal++;

            try {
                // 使用反射获取 AbstractFurnaceMenu 中的私有字段 "container"
                Field containerField = AbstractFurnaceMenu.class.getDeclaredField("f_38955_"); //container
                containerField.setAccessible(true); // 设置字段可访问
                Container internalContainer = (Container) containerField.get(container); // 获取字段值

                if (isFurnaceEmpty(container) && closeContainer.hasTimeElapsed(100)) {
                    mc.player.closeContainer();
                    return;
                }

                // 遍历容器内部物品槽
                for (int i = 0; i < internalContainer.getContainerSize(); ++i) {
                    if (!internalContainer.getItem(i).isEmpty()) {

                        if (this.lastSteal <= 1) {
                            continue; // 如果 lastSteal <= 1，跳过本次循环
                        }

                        // 设置下一次点击的延迟
                        this.nextClick = Math.round(MathUtils.getRandomFloat(this.delay.getValue().intValue(), this.delay.getValue().intValue() + 5));

                        // 处理物品快速移动点击
                        mc.gameMode.handleInventoryMouseClick(container.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);

                        // 重置计时器
                        stopwatch.reset();
                        closeContainer.reset();

                        this.lastClick = 0;
                        if (this.nextClick > 0) return; // 如果 nextClick 大于 0，结束方法
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace(); // 如果反射失败，打印错误信息
            }
        }
        if (mc.player.containerMenu instanceof BrewingStandMenu container && brewingStand.getValue()) {
            this.lastSteal++;

            try {
                // 使用反射获取 BrewingStandMenu 中的私有字段 "container"
                Field containerField = BrewingStandMenu.class.getDeclaredField("f_39086_"); //brewingStand
                containerField.setAccessible(true); // 设置字段可访问
                Container internalContainer = (Container) containerField.get(container); // 获取字段值

                if (isBrewingStandEmpty(container) && closeContainer.hasTimeElapsed(100)) {
                    mc.player.closeContainer();
                    return;
                }

                // 遍历容器内部物品槽
                for (int i = 0; i < internalContainer.getContainerSize(); ++i) {
                    if (!internalContainer.getItem(i).isEmpty()) {

                        if (this.lastSteal <= 1) {
                            continue; // 如果 lastSteal <= 1，跳过本次循环
                        }

                        // 设置下一次点击的延迟
                        this.nextClick = Math.round(MathUtils.getRandomFloat(this.delay.getValue().intValue(), this.delay.getValue().intValue() + 5));

                        // 处理物品快速移动点击
                        mc.gameMode.handleInventoryMouseClick(container.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);

                        // 重置计时器
                        stopwatch.reset();
                        closeContainer.reset();

                        this.lastClick = 0;
                        if (this.nextClick > 0) return; // 如果 nextClick 大于 0，结束方法
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace(); // 如果反射失败，打印错误信息
            }
        }
    }

    private boolean isChestEmpty(ChestMenu c) {
        for (int i = 0; i < c.getContainer().getMaxStackSize(); ++i) {
            if (!c.getContainer().getItem(i).isEmpty()) {
                if (isItemUseful(c, i) || trash.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFurnaceEmpty(FurnaceMenu c) {
        try {
            // 使用反射获取 FurnaceMenu 中的私有字段 "tileFurnace"
            Field tileFurnaceField = AbstractFurnaceMenu.class.getDeclaredField("f_38955_"); //container
            tileFurnaceField.setAccessible(true);
            Container tileFurnace = (Container) tileFurnaceField.get(c); // 获取字段值

            // 遍历容器槽位检查是否为空
            for (int i = 0; i < tileFurnace.getContainerSize(); ++i) {
                if (!tileFurnace.getItem(i).isEmpty()) {
                    return false; // 如果有物品，返回 false
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // 如果反射失败，打印错误信息
            return false; // 反射失败时默认返回 false
        }

        return true; // 所有槽位为空，返回 true
    }

    private boolean isBrewingStandEmpty(BrewingStandMenu c) {
        try {
            // 使用反射获取 BrewingStandMenu 中的私有字段 "tileBrewingStand"
            Field tileBrewingStandField = BrewingStandMenu.class.getDeclaredField("f_39086_"); //brewingStand
            tileBrewingStandField.setAccessible(true);
            Container tileBrewingStand = (Container) tileBrewingStandField.get(c); // 获取字段值

            // 遍历容器槽位检查是否为空
            for (int i = 0; i < tileBrewingStand.getContainerSize(); ++i) {
                if (!tileBrewingStand.getItem(i).isEmpty()) {
                    return false; // 如果有物品，返回 false
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // 如果反射失败，打印错误信息
            return false; // 反射失败时默认返回 false
        }

        return true; // 所有槽位为空，返回 true
    }

    private boolean isItemUseful(ChestMenu c, int i) {
        ItemStack itemStack = c.getSlot(i).getItem();
        Item item = itemStack.getItem();

        if (item instanceof AxeItem || item instanceof PickaxeItem) {
            return true;
        }

        if (itemStack.getItem().getFoodProperties() != null)
            return true;

        if (itemStack.getItem() instanceof FishingRodItem
                || itemStack.getItem() instanceof SnowballItem
                || itemStack.getItem() instanceof EggItem
                || itemStack.getItem() instanceof PotionItem) {
            return true; // 钓鱼竿、雪球、鸡蛋和药水都保留
        }

        if (item instanceof BowItem || item == Items.ARROW)
            return true;

        if (item instanceof PotionItem)
            return true;

        if (item instanceof SwordItem && InventoryUtils.isBestSword(c, itemStack))
            return true;
        if (item instanceof ArmorItem && InventoryUtils.isBestArmor(c, itemStack))
            return true;
        if (item instanceof BlockItem)
            return true;
        if (item == Items.SLIME_BALL)
            return true;

        if (item instanceof CrossbowItem)
            return true;

        if (item == Items.WATER_BUCKET)
            return true;

        if (item == Items.TOTEM_OF_UNDYING) return true;

        if (item == Items.FIRE_CHARGE) return true;

        return item == Items.ENDER_PEARL;
    }
}
