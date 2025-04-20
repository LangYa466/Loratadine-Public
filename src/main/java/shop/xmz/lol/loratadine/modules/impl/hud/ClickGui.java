package shop.xmz.lol.loratadine.modules.impl.hud;

import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.DropdownClickGUI;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends Module {
    public static ClickGui INSTANCE;
    public final ModeSetting mode = new ModeSetting("Mode", this, new String[]{
            "Compact",
            "Dropdown"
    }, "Dropdown");

    public ClickGui() {
        super("ClickGui", "点击界面" ,Category.RENDER, GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
    }

    @Override
    protected void onEnable() {
        if (Loratadine.INSTANCE == null || Loratadine.INSTANCE.getConfigManager() == null || mc == null || mc.level == null || mc.player == null) return;

        mc.setScreen(DropdownClickGUI.INSTANCE);

        Loratadine.INSTANCE.getConfigManager().save();
        setEnabled(false);
    }
}
