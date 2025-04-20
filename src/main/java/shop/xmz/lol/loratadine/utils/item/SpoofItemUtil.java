package shop.xmz.lol.loratadine.utils.item;

import net.minecraft.world.item.ItemStack;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class SpoofItemUtil implements Wrapper {
    private static int spoofSlot = 0;
    public static boolean spoofing = false;

    public static void startSpoof(int slot) {
        if (!spoofing) {
            spoofSlot = slot;
            spoofing = true;
        }
    }

    public static void stopSpoof() {
        for (int i = 0; i < 9; ++i) {
            if (i == spoofSlot && mc.player != null && mc.gameMode != null) {
                mc.player.getInventory().selected = i;
                mc.gameMode.tick();
            }
        }
        spoofing = false;
    }

    public static int getSlot() {
        if (mc.player != null) {
            return spoofing ? spoofSlot : mc.player.getInventory().selected;
        } else return 0;
    }

    public static ItemStack getStack() {
        if (mc.player != null) {
            return spoofing ? mc.player.getInventory().getItem(spoofSlot) : mc.player.getMainHandItem();
        } else return null;
    }

    public void setSlot(int slot) {
        spoofSlot = slot;
    }
}
