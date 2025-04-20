package shop.xmz.lol.loratadine.modules.impl.render;

import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render3DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.setting.Theme;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.misc.EntityUtils;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.ESPUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class ESP extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"2D", "3D", "Outline", "Orbital", "Spiral"}, "3D");
    private final BooleanSetting rainbow = new BooleanSetting("Rainbow", this, true);

    // 3D模式设置
    private final BooleanSetting outLine = (BooleanSetting) new BooleanSetting("Out Line", this, true)
            .setVisibility(() -> mode.is("3D"));
    private final BooleanSetting fullBox = (BooleanSetting) new BooleanSetting("Full Box", this, true)
            .setVisibility(() -> mode.is("3D"));

    // 通用设置
    private final NumberSetting lineWidth = (NumberSetting) new NumberSetting("Line Width", this, 3.5f, 0.5f, 10.0f, 0.5f)
            .setVisibility(() -> mode.is("Outline") || mode.is("Orbital") || mode.is("Spiral"));

    // Orbital特定设置
    private final NumberSetting orbitCount = (NumberSetting) new NumberSetting("Orbit Count", this, 3, 1, 5, 1)
            .setVisibility(() -> mode.is("Orbital"));
    private final NumberSetting orbitRadius = (NumberSetting) new NumberSetting("Orbit Size", this, 2.0f, 1.0f, 5.0f, 0.5f)
            .setVisibility(() -> mode.is("Orbital"));

    // Spiral特定设置
    private final NumberSetting spiralRadius = (NumberSetting) new NumberSetting("Spiral Size", this, 1.8f, 1.0f, 4.0f, 0.5f)
            .setVisibility(() -> mode.is("Spiral"));

    private final List<LivingEntity> needRenderEntity = new ArrayList<>();

    public ESP() {
        super("ESP", "透视方框", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
        setEnabled(true);
    }

    @Override
    protected void onEnable() {
        needRenderEntity.clear();
    }

    @Override
    protected void onDisable() {
        needRenderEntity.clear();
    }

    @EventTarget
    public void onMotionPre(LivingUpdateEvent event) {
        if (mc.player == null || mc.level == null) return;

        needRenderEntity.clear();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity) {
                if (EntityUtils.isSelected(entity, true))
                    needRenderEntity.add(livingEntity);
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        PoseStack poseStack = event.poseStack();
        int primaryColor = rainbow.getValue() ? ColorUtils.rainbow(10, 1).getRGB() : Theme.INSTANCE.firstColor.getRGB();

        // 为双色螺旋准备辅助颜色 - 使用互补色
        Color rainbowColor = new Color(primaryColor);
        int secondaryColor = new Color(
                255 - rainbowColor.getRed(),
                255 - rainbowColor.getGreen(),
                255 - rainbowColor.getBlue(),
                rainbowColor.getAlpha()
        ).getRGB();

        for (LivingEntity entity : needRenderEntity) {
            switch (mode.getValue()) {
                case "3D" -> {
                    ESPUtils.render3DEntityBoundingBox(poseStack, entity, primaryColor, true, fullBox.getValue(), outLine.getValue(), 200);
                }

                case "2D" -> {
                    ESPUtils.renderEntityBoundingBox(poseStack, entity, primaryColor, true);
                }

                case "Outline" -> {
                    // 判断是否为玩家，决定使用哪种轮廓方法
                    if (entity instanceof Player) {
                        ESPUtils.renderPlayerDetailedOutline(poseStack, (Player) entity, primaryColor, lineWidth.getValue().floatValue(), true);
                    } else {
                        ESPUtils.renderEntityOutlineESP(poseStack, entity, primaryColor, lineWidth.getValue().floatValue(), true);
                    }
                }

                case "Orbital" -> {
                    // 轨道描边效果 - 总是穿墙可见
                    ESPUtils.renderEntityOrbitalOutline(
                            poseStack,
                            entity,
                            primaryColor,
                            lineWidth.getValue().floatValue(), // 使用更粗的线条
                            orbitCount.getValue().intValue(),
                            orbitRadius.getValue().floatValue(),
                            true // 总是穿墙
                    );
                }

                case "Spiral" -> {
                    // 双螺旋描边效果 - 总是穿墙可见
                    ESPUtils.renderEntitySpiralOutline(
                            poseStack,
                            entity,
                            primaryColor,
                            secondaryColor,
                            lineWidth.getValue().floatValue(), // 使用更粗的线条
                            spiralRadius.getValue().floatValue(),
                            true // 总是穿墙
                    );
                }
            }
        }
    }
}