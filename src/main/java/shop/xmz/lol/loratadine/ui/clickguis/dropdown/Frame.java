package shop.xmz.lol.loratadine.ui.clickguis.dropdown;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame implements Wrapper {
    public static float offsetY; // Vertical offset
    public int x, y, dragX, dragY, width, height;
    public Category category;
    public boolean dragging, extended;
    private final List<ModuleRenderer> renderers;
    private float openProgress;
    public static final int ANIMATION_DURATION = 1000; // Milliseconds
    public long lastToggleTime;
    private final ResourceLocation categoryIcon;

    public Frame(int x, int y, int width, int height, Category category) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        this.dragging = false;
        this.extended = true;
        this.openProgress = 0f;
        this.categoryIcon = Loratadine.INSTANCE.getResourcesManager().loadIconFromAbsolutePath(
                Loratadine.INSTANCE.getResourcesManager().resources.getAbsolutePath() +
                        "\\icon\\category\\" + category.name().toLowerCase() + "_icon.png"
        );
        renderers = new ArrayList<>();

        int offset = height;
        for (Module mod : Loratadine.INSTANCE.getModuleManager().getModule(category)) {
            renderers.add(new ModuleRenderer(mod, this, offset));
            offset += height;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Update animation
        updateAnimation();

        // Draw Module Category Background
        RenderUtils.drawGradientRectL2R(guiGraphics.pose(), x, y, width, height - 2,
                HUD.INSTANCE.getColor(0).getRGB(),
                HUD.INSTANCE.getColor(4).getRGB()
        );

        // Draw Module Category ICON
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(categoryIcon, x + 5, y + 3, 0, 0, 12, 12, 12, 12);

        // Draw Module Category NAME
        HUD hud = (HUD) Loratadine.INSTANCE.getModuleManager().getModule(HUD.class);
        boolean isEnglish = hud.languageValue.getValue().equals("English");
        String moduleName = isEnglish ? category.name : category.cnName;
        TrueTypeFont font = isEnglish ?
                Loratadine.INSTANCE.getFontManager().tenacity20 :
                Loratadine.INSTANCE.getFontManager().zw20;
        font.drawString(guiGraphics.pose(), moduleName, x + 22, y + 4, -1);

        // Draw Module Animation
        if (openProgress > 0) {
            int yOffset = height;
            for (ModuleRenderer renderer : renderers) {
                int moduleHeight = (int) (renderer.getHeight() * openProgress);
                if (moduleHeight > 0) {
                    renderer.render(guiGraphics, mouseX, mouseY, delta, x, y + yOffset, width, moduleHeight);
                    yOffset += moduleHeight;
                }
            }
        }
    }


    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (mouseButton == 1) {
                extended = !extended;
                lastToggleTime = System.currentTimeMillis();
                refreshModules();
            }
        }

        if (extended) {
            int yOffset = height;
            for (ModuleRenderer renderer : renderers) {
                int moduleHeight = (int) (renderer.getHeight() * openProgress);
                if (mouseY >= y + yOffset && mouseY <= y + yOffset + moduleHeight) {
                    renderer.mouseClicked(mouseX, mouseY, mouseButton);
                    break;
                }
                yOffset += moduleHeight;
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        for (ModuleRenderer renderer : renderers) {
            renderer.mouseReleased(mouseX, mouseY, mouseButton);
        }
        if (mouseButton == 0 && dragging) dragging = false;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    public void updateButtons() {
        int offset = height;
        for (ModuleRenderer renderer : renderers) {
            renderer.offset = offset;
            offset += renderer.getHeight();
        }
    }

    public void refreshModules() {
        // Save current module expanded states
        Map<Module, Boolean> moduleStates = new HashMap<>();
        for (ModuleRenderer renderer : this.renderers) {
            moduleStates.put(renderer.module, renderer.extended);
        }

        // Save current frame expanded state
        boolean wasExtended = this.extended;

        // Reinitialize renderers
        renderers.clear();
        int offset = height;
        for (Module mod : Loratadine.INSTANCE.getModuleManager().getModule(category)) {
            ModuleRenderer renderer = new ModuleRenderer(mod, this, offset);

            // Restore module expanded state
            if (moduleStates.containsKey(mod)) {
                renderer.extended = moduleStates.get(mod);
                renderer.openProgress = renderer.extended ? 1f : 0f;
            }
            renderers.add(renderer);
            offset += height;
        }

        // Restore frame expanded state
        this.extended = wasExtended;
        this.lastToggleTime = System.currentTimeMillis();

        // Immediately expand or collapse without animation
        for (ModuleRenderer renderer : renderers) {
            renderer.openProgress = renderer.extended ? 1f : 0f;
        }
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float targetProgress = extended ? 1f : 0f;

        // Use interpolation for smooth animation
        float delta = (currentTime - lastToggleTime) / (float) ANIMATION_DURATION;
        openProgress = extended ?
                Math.min(1, openProgress + delta) :
                Math.max(0, openProgress - delta);

        // Add elastic effect
        if (Math.abs(openProgress - targetProgress) > 0.01f) {
            openProgress += (targetProgress - openProgress) * 0.2f;
        }
    }
}