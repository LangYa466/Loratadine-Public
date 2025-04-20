package shop.xmz.lol.loratadine.modules.impl.render;

import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;

public class ItemPhysic extends Module {
    public NumberSetting rotateSpeed = new NumberSetting("Rotate Speed", this, 300, 0, 500, 50);

    public static ItemPhysic INSTANCE;

    public ItemPhysic() {
        super("ItemPhysic", "物理掉落", Category.RENDER);
        INSTANCE = this;
    }
}
