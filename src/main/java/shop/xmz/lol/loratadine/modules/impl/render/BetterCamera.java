package shop.xmz.lol.loratadine.modules.impl.render;

import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;

public class BetterCamera extends Module {
    public final BooleanSetting motionCamera = new BooleanSetting("Motion Camera", this, true);
    public final NumberSetting interpolation = new NumberSetting("Motion Interpolation",this, 0.15f, 0.01f, 0.35f, 0.01f);
    public final BooleanSetting noHurtShake = new BooleanSetting("No Hurt Shake", this, true);
    public final BooleanSetting npClip = new BooleanSetting("No Clip", this, true);
    public final NumberSetting cameraDistance = new NumberSetting("No Clip 3rd Camera Distance", this, 4, 0.1, 10, 0.1);

    public static BetterCamera INSTANCE;

    public BetterCamera() {
        super("BetterCamera", "更好的相机", Category.RENDER);
        INSTANCE = this;
    }
}
