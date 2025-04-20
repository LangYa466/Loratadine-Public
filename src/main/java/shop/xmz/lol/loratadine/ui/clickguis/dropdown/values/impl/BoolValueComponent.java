package shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.impl;

import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.Setting;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.ModuleRenderer;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.Component;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.awt.*;

public class BoolValueComponent extends Component implements Wrapper {
    private final BooleanSetting booleanValue;

    public BoolValueComponent(Setting<?> value, ModuleRenderer parent, int offset) {
        super(value, parent, offset);
        if (!(value instanceof BooleanSetting)) {
            throw new IllegalArgumentException("BooleanSetting required");
        }
        this.booleanValue = (BooleanSetting) value;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height) {
        super.render(guiGraphics, mouseX, mouseY, delta, x, y, width, height);

        String text = String.valueOf(booleanValue.getValue());
        TrueTypeFont font = getCurrentFont();

        // Draw the value with color indication
        font.drawString(guiGraphics.pose(), text,
                x + 7 + font.getStringWidth(booleanValue.getName() + ": "),
                y + (height / 2F - font.getHeight() / 2F),
                booleanValue.getValue() ? Color.GREEN.getRGB() : Color.RED.getRGB());

        // Draw the colon
        font.drawString(guiGraphics.pose(), ": ",
                x + 7 + font.getStringWidth(booleanValue.getName()),
                y + (height / 2F - font.getHeight() / 2F), -1);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY, parent.parent.x, parent.parent.y + parent.offset + offset,
                parent.parent.width, parent.parent.height) && mouseButton == 0) {
            booleanValue.setValue(!booleanValue.getValue());
            // Refresh state
            parent.parent.refreshModules();
        }
    }

    private TrueTypeFont getCurrentFont() {
        boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                .languageValue.getValue().equals("English");
        return isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity20 :
                Loratadine.INSTANCE.getFontManager().zw20;
    }
}