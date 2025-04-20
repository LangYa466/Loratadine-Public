package shop.xmz.lol.loratadine.modules.impl.render;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;

public class Animations extends Module {
    public final NumberSetting rightX = new NumberSetting("Right-Item-X",this,0.0, -1.0, 1.0,1);
    public final NumberSetting rightY = new NumberSetting("Right-Item-Y",this,0.0, -1.0, 1.0,1.0);
    public final NumberSetting rightZ = new NumberSetting("Right-Item-Z",this,0.0, -1.0, 1.0,1.0);
    public final NumberSetting leftX = new NumberSetting("Left-Item-X",this,0.0, -1.0, 1.0,1);
    public final NumberSetting leftY = new NumberSetting("Left-Item-Y",this,0.0, -1.0, 1.0,1.0);
    public final NumberSetting leftZ = new NumberSetting("Left-Item-Z",this,0.0, -1.0, 1.0,1.0);
    public final NumberSetting itemScale = new NumberSetting("Item-scale",this,1.0, 0.01, 1.0,1.0);
    public final BooleanSetting everythingBlock = new BooleanSetting("Everything can block",this,false);
    public final ModeSetting swordValue = new ModeSetting("Sword Animation",this,new String[]{
            "1.7",
            "SideDown",
            "SigmaOld" ,
            "Zoom",
            "WindMill",
            "Slide",
            "Slide2",
            "SpinnyBoi",
            "Boop",
            "Smooth"
    }, "1.7");

    public static Animations INSTANCE;

    public Animations() {
        super("Animations","动画", Category.RENDER);
        INSTANCE = this;
        setEnabled(true);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setSuffix(swordValue.getValue());
    }
}