package shop.xmz.lol.loratadine.utils.wrapper;

import net.minecraft.client.Minecraft;
import shop.xmz.lol.loratadine.Loratadine;

public interface Wrapper {
    //请勿使用Minecraft.getInstance(), 妖猫给Minecraft.getInstance()写了stack检测
    Minecraft mc = Loratadine.INSTANCE.getMinecraft();
}
