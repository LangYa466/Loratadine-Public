package shop.xmz.lol.loratadine.ui.clickguis.dropdown;

import net.minecraft.client.gui.GuiGraphics;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.Setting;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.Component;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.impl.BoolValueComponent;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.impl.ModeValueComponent;
import shop.xmz.lol.loratadine.ui.clickguis.dropdown.values.impl.NumberValueComponent;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleRenderer implements Wrapper {
    public Module module;
    public Frame parent;
    public int offset;
    public List<Component> components;
    public boolean extended;
    private static final int COMPONENT_HEIGHT = 20;
    float openProgress;
    private long lastToggleTime;

    public ModuleRenderer(Module module, Frame parent, int offset) {
        this.module = module;
        this.parent = parent;
        this.offset = offset;
        this.extended = false;
        this.openProgress = 0f;
        this.lastToggleTime = 0;
        components = new ArrayList<>();

        int valueOffset = COMPONENT_HEIGHT;
        for (Setting<?> value : module.getSettings()) {
            if (!value.shouldRender()) continue;

            if (value instanceof BooleanSetting) {
                components.add(new BoolValueComponent(value, this, valueOffset));
            } else if (value instanceof ModeSetting) {
                components.add(new ModeValueComponent(value, this, valueOffset));
            } else if (value instanceof NumberSetting) {
                components.add(new NumberValueComponent(value, this, valueOffset));
            }
            valueOffset += COMPONENT_HEIGHT;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height) {
        // Update animation progress
        updateAnimation();

        // Draw module background
        RenderUtils.drawRectangle(guiGraphics.pose(), x, y, width, height, ColorUtils.color(0, 0, 0, 200));

        // Determine font based on language
        HUD hud = (HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class);
        boolean isEnglish = hud.languageValue.getValue().equals("English");
        TrueTypeFont font = isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity20 :
                Loratadine.INSTANCE.getFontManager().zw20;

        // Prepare module name
        int textOffset = (COMPONENT_HEIGHT / 2 - font.getHeight() / 2);
        String moduleName = isEnglish ? module.getName() : module.getCnName();
        float scaleFactor = 1.0f;

        // Adjust scale if name is too long
        int moduleNameWidth = (int) font.getStringWidth(moduleName);
        if (moduleNameWidth > width - 30) {
            scaleFactor = (float) (width - 30) / moduleNameWidth;
        }

        // Draw module name with scaling
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + textOffset, y + textOffset, 0);
        guiGraphics.pose().scale(scaleFactor, scaleFactor, 1.0f);

        // Choose color based on module state
        int textColor = module.isEnabled() ? -1 : new Color(128, 134, 141, 255).getRGB();
        font.drawString(guiGraphics.pose(), moduleName, 0, 0, textColor);
        guiGraphics.pose().popPose();

        // Draw expand/collapse button
        if (!components.isEmpty()) {
            Loratadine.INSTANCE.getFontManager().tenacity20.drawString(
                    guiGraphics.pose(),
                    extended ? "-" : "+",
                    x + width - 10,
                    y + textOffset,
                    -1
            );
        }

        // Render components
        if (openProgress > 0) {
            int componentY = y + COMPONENT_HEIGHT;
            for (Component component : components) {
                if (componentY + COMPONENT_HEIGHT <= y + height) {
                    component.render(guiGraphics, mouseX, mouseY, delta, x, componentY, width, COMPONENT_HEIGHT);
                    componentY += COMPONENT_HEIGHT;
                } else {
                    break;
                }
            }
        }
    }

    private void updateAnimation() {
        openProgress = extended ? 1f : 0f;
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY, parent.x, parent.y + offset, parent.width, COMPONENT_HEIGHT)) {
            if (mouseButton == 0) {
                module.setEnabled(!module.isEnabled());
            } else if (mouseButton == 1 && !components.isEmpty()) {
                extended = !extended;
                lastToggleTime = System.currentTimeMillis();
                parent.updateButtons();
            }
        }
        if (extended) {
            int componentY = parent.y + offset + COMPONENT_HEIGHT;
            for (Component component : components) {
                if (mouseY > componentY && mouseY < componentY + COMPONENT_HEIGHT) {
                    component.mouseClicked(mouseX, mouseY, mouseButton);
                }
                componentY += COMPONENT_HEIGHT;
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (extended) {
            int componentY = parent.y + offset + COMPONENT_HEIGHT;
            for (Component component : components) {
                if (mouseY > componentY && mouseY < componentY + COMPONENT_HEIGHT) {
                    component.mouseReleased(mouseX, mouseY, mouseButton);
                }
                componentY += COMPONENT_HEIGHT;
            }
        }
    }

    public boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public int getHeight() {
        return COMPONENT_HEIGHT + Math.round((extended ? components.size() * COMPONENT_HEIGHT : 0) * openProgress);
    }
}