package shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.impl;

import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.ModuleRenderer;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.Component;
import shop.xmz.lol.loratadine.modules.setting.Setting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.math.MathUtils;

import java.awt.*;

public class NumberValueComponent extends Component implements Wrapper {
    private final NumberSetting numValue;
    private boolean sliding = false;

    public NumberValueComponent(Setting<?> value, ModuleRenderer parent, int offset) {
        super(value, parent, offset);
        if (!(value instanceof NumberSetting)) {
            throw new IllegalArgumentException("NumberSetting required");
        }
        this.numValue = (NumberSetting) value;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height) {
        super.render(guiGraphics, mouseX, mouseY, delta, x, y, width, height);

        // Adjust parameters: right shift 3 pixels and adjust width
        final int xOffset = 3;
        final int adjustedWidth = width - xOffset;

        // Progress bar calculation
        float progress = (numValue.getValue().floatValue() - numValue.getMinValue().floatValue())
                / (numValue.getMaxValue().floatValue() - numValue.getMinValue().floatValue());
        int renderWidth = (int) (progress * adjustedWidth);

        // Draw progress bar
        RenderUtils.fill(guiGraphics.pose(),
                x + xOffset,
                y + height,
                x + xOffset + renderWidth,
                y,
                new Color(255, 255, 255, 100).getRGB()
        );

        // Sliding logic
        if (sliding) {
            double diff = MathUtils.clamp_double(mouseX - x, 0, width);
            float newValue = (float) (diff / width)
                    * (numValue.getMaxValue().floatValue() - numValue.getMinValue().floatValue())
                    + numValue.getMinValue().floatValue();

            // Round to integer or decimal based on min/max values
            if (numValue.getMinValue() instanceof Integer && numValue.getMaxValue() instanceof Integer) {
                numValue.setValue(Math.round(newValue));
            } else {
                numValue.setValue(MathUtils.roundToPlace(newValue, 2));
            }
        }

        // Format current value
        float currentValue = numValue.getValue().floatValue();
        String text = currentValue == (int) currentValue ?
                String.valueOf((int) currentValue) :
                String.valueOf(MathUtils.roundToPlace(currentValue, 2));

        // Text rendering
        TrueTypeFont font = getCurrentFont();
        font.drawString(guiGraphics.pose(), text,
                x + 7 + font.getStringWidth(numValue.getName() + ": "),
                y + (height / 2F - font.getHeight() / 2F), -1);

        font.drawString(guiGraphics.pose(), ": ",
                x + 7 + font.getStringWidth(numValue.getName()),
                y + (height / 2F - font.getHeight() / 2F), -1);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY, parent.parent.x, parent.parent.y + parent.offset + offset,
                parent.parent.width, parent.parent.height) && mouseButton == 0) {
            sliding = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        sliding = false;
    }

    private TrueTypeFont getCurrentFont() {
        boolean isEnglish = ((HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class))
                .languageValue.getValue().equals("English");
        return isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity20 :
                Loratadine.INSTANCE.getFontManager().zw20;
    }
}