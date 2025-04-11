package shop.xmz.lol.loratadine.combat;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.ItemStack;

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

/*            mc.player.connection.send(event.getPacket());
            event.setCancelled(true);*/
        }
    }
}