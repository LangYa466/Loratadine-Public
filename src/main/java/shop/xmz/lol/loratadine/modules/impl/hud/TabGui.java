package shop.xmz.lol.loratadine.modules.impl.hud;

import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render2DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.ui.tabgui.CheckKeyUtils;
import shop.xmz.lol.loratadine.ui.tabgui.TabGUI;

/**
 * @author DSJ_
 * @since 13/2/2025
 */
public class TabGui extends Module {
    public TabGui() {
        super("TabGUI", "选项卡", Category.RENDER);
    }
    @EventTarget
    public void onRender2D(Render2DEvent event) {
        CheckKeyUtils.checkKeyPress(); // 在每帧渲染时检查按键
        TabGUI.drawExhibitionTabGui(event.poseStack());
    }
}
