package shop.xmz.lol.loratadine.ui.clickguis.dropdown;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.List;

public class DropdownClickGUI extends Screen implements Wrapper {
    public static final DropdownClickGUI INSTANCE = new DropdownClickGUI();
    private final List<Frame> frames;

    // 动画
    private float animProgress = 0f;
    private boolean isClosing = false;
    private static final float ANIM_DURATION = 0.5f;
    private static final float OVERSHOOT_INTENSITY = 1.2f;

    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 15;
    private boolean reverseScroll = true;

    protected DropdownClickGUI() {
        super(Component.nullToEmpty("Click GUI"));
        frames = new ArrayList<>();
        int offset = 18;

        for (Category category : Category.values()) {
            frames.add(new Frame(offset, 20, 150, 20, category));
            offset += 155;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (mc == null || mc.player == null || mc.level == null) return;

        updateAnimation(partialTicks);

        float bgAlpha = Mth.lerp(animProgress, 0f, 0.4f);
        renderBackgroundFade(guiGraphics, bgAlpha);

        float scale = calculateScale();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(screenWidth / 2f, screenHeight / 2f, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate(-screenWidth / 2f, -screenHeight / 2f, 0);
        guiGraphics.pose().translate(0, scrollOffset, 0);

        for (Frame frame : frames) {
            frame.render(guiGraphics,
                    (int) ((mouseX - screenWidth / 2f) / scale + screenWidth / 2f),
                    (int) ((mouseY - screenHeight / 2f) / scale + screenHeight / 2f - scrollOffset),
                    partialTicks
            );
            frame.updatePosition(mouseX, mouseY - scrollOffset);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.pose().popPose();
    }

    private void updateAnimation(float partialTicks) {
        float delta = partialTicks / 20f;
        if (isClosing) {
            animProgress = Mth.clamp(animProgress - delta / ANIM_DURATION, 0f, 1f); // Decrease progress when closing
            if (animProgress <= 0f) {
                super.onClose();
            }
        } else {
            animProgress = Mth.clamp(animProgress + delta / ANIM_DURATION, 0f, 1f); // Increase progress when opening
        }
    }

    private float calculateScale() {
        return easeOutBack(animProgress);
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f * OVERSHOOT_INTENSITY;
        float c3 = c1 + 1f;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private void renderBackgroundFade(GuiGraphics guiGraphics, float alpha) {
        int alphaValue = (int) (Mth.clamp(alpha, 0f, 0.4f) * 255);
        guiGraphics.fill(0, 0, this.width, this.height, (alphaValue << 24));
    }

    @Override
    public void onClose() {
        if (!isClosing) {
            isClosing = true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;
        scrollOffset += (delta > 0 ? (reverseScroll ? SCROLL_AMOUNT : -SCROLL_AMOUNT) : (reverseScroll ? -SCROLL_AMOUNT : SCROLL_AMOUNT));
        scrollOffset = (int) (scrollOffset * 0.9);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY - scrollOffset, button); // Adjust mouse Y coordinate
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mc == null || mc.player == null || mc.level == null || !isAnimationFinished()) return false;

        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY - scrollOffset, button); // Adjust mouse Y coordinate
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isAnimationFinished() {
        return isClosing ? animProgress <= 0f : animProgress >= 1f;
    }

    @Override
    protected void init() {
        if (mc == null || mc.player == null || mc.level == null) return;

        animProgress = 0f;
        isClosing = false;
        scrollOffset = 0;
        super.init();
    }
}