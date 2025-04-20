package shop.xmz.lol.loratadine.ui.tabgui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.ModuleManager;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.Direction;
import shop.xmz.lol.loratadine.utils.animations.impl.DecelerateAnimation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.font.TrueTypeFont;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static shop.xmz.lol.loratadine.utils.wrapper.Wrapper.mc;

/**
 * @author DSJ_
 * @since 13/2/2025
 */
public class TabGUI {
    private static final Category[] categories = Category.values();
    private static int mainIndex = 0;
    private static int subIndex = 0;
    private static boolean isSubMenuOpen = false;
    private static long lastMoveTime = 0; // 防止快速按键问题

    private static final float x = 3.5F;
    private static final int y = 30;
    private static final int width = 110;
    private static final int height = 18;

    private static final FontManager fontManager = Loratadine.INSTANCE.getFontManager();
    private static final ModuleManager moduleManager = Loratadine.INSTANCE.getModuleManager();
    private static final Animation subMenuAnimation = new DecelerateAnimation(400, 1).setDirection(Direction.BACKWARDS);
    private static final Animation subMenuSlideAnimation = new DecelerateAnimation(200, x + width + 105).setDirection(Direction.BACKWARDS);

    private static final Set<Module> enabledModules = new HashSet<>(); // 存储已开启的模块

    /**
     * 绘制 Exhibition 的 TabGui
     * @author DSJ_
     */
    public static void drawExhibitionTabGui(PoseStack poseStack) {
        // **定义颜色**
        Color darkest = new Color(0, 0, 0);
        Color lineColor = new Color(104, 104, 104);
        Color dark = new Color(70, 70, 70);

        // **计算滑动动画偏移量**
        float slideProgress = (float) subMenuSlideAnimation.getOutput();
        float subX = x - 100 + (int) (slideProgress); // **从主菜单左侧 (-100px) 滑到主菜单右侧 + 5px**

        // **先绘制二级菜单**
        float animProgress = (float) subMenuAnimation.getOutput();
        if (isSubMenuOpen || animProgress > 0.05) {
            List<Module> modules = moduleManager.getModulesByCategory(categories[mainIndex]);
            if (!modules.isEmpty()) {
                int subY = y + mainIndex * height;
                int alpha = (int) (180 * Math.min(1.0f, Math.max(0.0f, animProgress)));

                // **绘制二级菜单背景**
                RenderUtils.drawRectangle(poseStack, subX, subY, width, height * modules.size(), new Color(0, 0, 0, alpha).getRGB());
                RenderUtils.drawRectangle(poseStack, subX + 0.5F, subY + 0.5F, width - 1, height * modules.size() - 1, new Color(104, 104, 104, alpha).getRGB());
                RenderUtils.drawRectangle(poseStack, subX + 1.5F, subY + 1.5F, width - 3, height * modules.size() - 3, new Color(0, 0, 0, alpha).getRGB());
                RenderUtils.drawRectangle(poseStack, subX + 2, subY + 2, width - 4, height * modules.size() - 4, new Color(70, 70, 70, alpha).getRGB());

                for (int i = 0; i < modules.size(); i++) {
                    Module module = modules.get(i);
                    boolean isEnabled = module.isEnabled();
                    String moduleText = module.getModuleName() + (isEnabled ? " [Open]" : ""); // **已启用模块显示 "✔"**

                    float textAlpha = (i == subIndex) ? 1.0f : 0.6f;
                    int textAlphaValue = (int) (255 * Math.min(1.0f, Math.max(0.0f, textAlpha * animProgress)));
                    Color textColor = new Color(255, 255, 255, textAlphaValue);
                    Matrix4f matrix = poseStack.last().pose();
                    MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                    if(HUD.INSTANCE.fontValue.is("Minecraft")){
                        //mc.font.drawInBatch(moduleText, subX + 10, subY + i * height + 6, 0, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, textColor.getRGB());

                        WrapperUtils.draw(poseStack, moduleText, subX + 10, subY + i * height + 6, textColor.getRGB());
                    } else {
                        TrueTypeFont font = fontManager.zw19;
                        font.drawString(poseStack, moduleText, subX + 10, subY + i * height + 5, textColor.getRGB());
                    }
                }
            }
        }

        // **再绘制主菜单，确保它在上层**
        RenderUtils.drawRectangle(poseStack, x, y, width, height * categories.length, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 0.5F, y + 0.5F, width - 1, height * categories.length - 1, lineColor.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 1.5F, y + 1.5F, width - 3, height * categories.length - 3, darkest.getRGB());
        RenderUtils.drawRectangle(poseStack, x + 2, y + 2, width - 4, height * categories.length - 4, dark.getRGB());

        for (int i = 0; i < categories.length; i++) {
            float textAlpha = (i == mainIndex) ? 1.0f : 0.6f;
            int textColorAlpha = (int) (255 * Math.min(1.0f, Math.max(0.0f, textAlpha)));
            Color textColor = new Color(255, 255, 255, textColorAlpha);
            Matrix4f matrix = poseStack.last().pose();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            if(HUD.INSTANCE.fontValue.is("Minecraft")) {
                //mc.font.drawInBatch(HUD.INSTANCE.languageValue.is("English") ? categories[i].name : categories[i].cnName, x + 1, y, 0, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, textColor.getRGB());
                WrapperUtils.draw(poseStack, HUD.INSTANCE.languageValue.is("English") ? categories[i].name : categories[i].cnName, x + 10, y + i * height + 6, textColor.getRGB());
            } else {
                TrueTypeFont font = fontManager.zw19;
                font.drawString(poseStack, HUD.INSTANCE.languageValue.is("English") ? categories[i].name : categories[i].cnName, x + 10, y + i * height + 5, textColor.getRGB());
            }
        }
    }

    public static void onKeyPress(int key) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime < 100) return; // 防止快速按键

        switch (key) {
            case GLFW.GLFW_KEY_DOWN -> {
                if (isSubMenuOpen) {
                    List<Module> modules = moduleManager.getModulesByCategory(categories[mainIndex]);
                    if (!modules.isEmpty()) {
                        subIndex = (subIndex + 1) % modules.size();
                    }
                } else {
                    mainIndex = (mainIndex + 1) % categories.length;
                }
            }
            case GLFW.GLFW_KEY_UP -> {
                if (isSubMenuOpen) {
                    List<Module> modules = moduleManager.getModulesByCategory(categories[mainIndex]);
                    if (!modules.isEmpty()) {
                        subIndex = (subIndex - 1 + modules.size()) % modules.size();
                    }
                } else {
                    mainIndex = (mainIndex - 1 + categories.length) % categories.length;
                }
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                List<Module> modules = moduleManager.getModulesByCategory(categories[mainIndex]);
                if (!modules.isEmpty() && !isSubMenuOpen) {
                    isSubMenuOpen = true;
                    subIndex = 0;
                    subMenuAnimation.setDirection(Direction.FORWARDS);
                    subMenuSlideAnimation.setDirection(Direction.FORWARDS);
                }
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (isSubMenuOpen) {
                    isSubMenuOpen = false;
                    subIndex = 0;
                    subMenuAnimation.setDirection(Direction.BACKWARDS);
                    subMenuSlideAnimation.setDirection(Direction.BACKWARDS);
                }
            }
            case GLFW.GLFW_KEY_ENTER -> {
                if (isSubMenuOpen) {
                    List<Module> modules = moduleManager.getModulesByCategory(categories[mainIndex]);
                    if (!modules.isEmpty()) {
                        Module selectedModule = modules.get(subIndex);
                        selectedModule.toggle();
                        if (enabledModules.contains(selectedModule)) {
                            enabledModules.remove(selectedModule);
                        } else {
                            enabledModules.add(selectedModule);
                        }
                    }
                }
            }
        }

        lastMoveTime = currentTime;
    }
}
