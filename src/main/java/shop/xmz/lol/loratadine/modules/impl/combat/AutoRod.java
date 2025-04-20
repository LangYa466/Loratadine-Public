package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.TimerUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoRod extends Module {
    private final NumberSetting useItemDelay = new NumberSetting("UseItemDelay",this,300,100,1000,1);
    private final NumberSetting switchDelay = new NumberSetting("SwitchDelay",this,300,100,1000,1);
    private final NumberSetting range = new NumberSetting("Range",this,3.5, 1, 6.0, 0.1);
    private final BooleanSetting rotation = new BooleanSetting("Rotation",this,true);
    private int originalSlot = -1;
    private boolean isActive = false;
    private final List<LivingEntity> targets = new ArrayList<>();
    public static LivingEntity target = null;
    TimerUtils useItemTimer = new TimerUtils();
    TimerUtils switchTimer = new TimerUtils();

    public AutoRod() {
        super("AutoRod", "自动鱼竿", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        reset();
    }
    
    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.player == null || mc.level == null || !mc.player.isAlive()) return;

        if (KillAura.target != null) {
            reset();
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity == mc.player || livingEntity.isDeadOrDying() || !livingEntity.isAlive()) continue;

                if (RotationUtils.getDistanceToEntity(livingEntity) <= range.getValue().floatValue()) targets.add(livingEntity);
            }
        }

        targets.sort(Comparator.comparingInt(a -> (int) RotationUtils.getDistanceToEntity(a)));

        if (targets.isEmpty()
                || (target != null
                && (RotationUtils.getDistanceToEntity(target) > range.getValue().floatValue() || target.isDeadOrDying() || !target.isAlive() || target.getHealth() <= 0))
                || (mc.player.isDeadOrDying() || !mc.player.isAlive() || mc.player.getHealth() <= 0)) {
            reset();
        } else {
            target = targets.get(0);
        }

        if (target == null) {
            if (switchTimer.delay(switchDelay.getValue().intValue())) {
                resetState();
                switchTimer.reset();
            }
            return;
        }

        if (useItemTimer.delay(useItemDelay.getValue().intValue())){
            handleAttackLogic();
            useItemTimer.reset();
        }
    }

    public void reset() {
        target = null;
        targets.clear();
    }

    private void handleAttackLogic() {
        if (mc.player == null || mc.level == null) return;

        int rodSlot = findFishingRodSlot();
        if (rodSlot == -1) return;

        if (originalSlot == -1) {
            originalSlot = mc.player.getInventory().selected;
        }
        mc.player.getInventory().selected = rodSlot;
        WrapperUtils.ensureHasSentCarriedItem(mc.gameMode);

        if (rotation.getValue()) rotateToNearestEntity();

        useFishingRod();
        isActive = true;
    }

    private void resetState() {
        if (mc.player == null || mc.level == null) return;

        if (isActive && originalSlot != -1) {
            mc.player.getInventory().selected = originalSlot;
            WrapperUtils.ensureHasSentCarriedItem(mc.gameMode);
            originalSlot = -1;
            isActive = false;
        }
    }

    private int findFishingRodSlot() {
        if (mc.player == null || mc.level == null) return 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.FISHING_ROD) return i;
        }
        return -1;
    }

    private void rotateToNearestEntity() {
        if (mc.player == null || mc.level == null || target == null) return;

        RotationUtils.setRotation(RotationUtils.getAngles(target), 0);
    }

    private void useFishingRod() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        InteractionHand hand = mc.player.getUsedItemHand();
        mc.gameMode.useItem(mc.player, hand);
    }
}