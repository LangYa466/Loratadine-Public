package shop.xmz.lol.loratadine.ui.clickguis.dropdown.values;

import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.Setting;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.ModuleRenderer;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;

public class Component implements Wrapper {
    public Setting<?> value;
    public ModuleRenderer parent;
    public int offset;

    public Component(Setting<?> value, ModuleRenderer parent, int offset) {
        this.value = value;
        this.parent = parent;
        this.offset = offset;
    }

    public boolean shouldRender() {
        return value.getVisibilityCondition().get();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height) {
        if (!shouldRender()) return;

        RenderUtils.drawRectangle(guiGraphics.pose(), x, y, width, height, ColorUtils.color(0, 0, 0, 180));
        RenderUtils.drawGradientRectL2R(guiGraphics.pose(), x + 2, y, 1, height,
                HUD.INSTANCE.getColor(1).getRGB(),
                HUD.INSTANCE.getColor(4).getRGB()
        );

        HUD hud = (HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class);
        boolean isEnglish = hud.languageValue.is("English");
        TrueTypeFont font = isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity20 :
                Loratadine.INSTANCE.getFontManager().zw20;

        int textOffset = (height / 2 - font.getHeight() / 2);

        font.drawString(guiGraphics.pose(), value.getName(), x + 7, y + textOffset, -1);
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
    }

    public boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}