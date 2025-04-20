package shop.xmz.lol.loratadine.modules.impl.combat;

import cn.lzq.injection.leaked.invoked.AttackEvent;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.ItemStack;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.utils.item.InventoryUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

public class AutoWeapon extends Module {
    public AutoWeapon() {
        super("AutoWeapon", "自动武器", Category.COMBAT);
    }
    private final BooleanSetting itemTool = new BooleanSetting("ItemTool", this, true);
    private boolean attackEnemy = false;

    @EventTarget
    public void onAttack(AttackEvent event) {
        attackEnemy = true;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.gameMode == null) return;

        if (event.getPacket() instanceof ServerboundInteractPacket wrapper && WrapperUtils.isAttackAction(wrapper) && attackEnemy) {
            attackEnemy = false;

            int slot = -1;
            double maxDamage = 0;

            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof TieredItem && itemTool.getValue()) {
                    double damage = InventoryUtils.getItemDamage(stack);

                    if (damage > maxDamage) {
                        maxDamage = damage;
                        slot = i;
                    }
                }
            }

            if (slot != -1) {
                mc.player.getInventory().selected = slot;
            }

            mc.player.connection.send(event.getPacket());
            event.setCancelled(true);
        }
    }
}