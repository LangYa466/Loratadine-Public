package shop.xmz.lol.loratadine.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.RenderNameplateEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author Jon_awa / Cool / DSJ_
 * @since 13/2/2025
 */
public class NameTags extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"Modern", "Old","Simple"}, "Modern");
    private final NumberSetting nameTagScale = new NumberSetting("Scale",this,0.35, 0.01, 0.5, 0.1);

    public NameTags() {
        super("NameTags", "名称标签", Category.RENDER);
    }

    @EventTarget
    public void onPostRenderPlayerEvent(RenderNameplateEvent event) {
        if (mc.player == null || mc.level == null || event.getPoseStack() == null || event.getEntity() == null || (event.getEntity() == mc.player && mc.options.getCameraType().isFirstPerson())) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            PoseStack poseStack = event.getPoseStack();
            Font font = mc.font;

            double distance = mc.player.distanceTo(player);

            if (distance > 100.0) {
                return;
            }

            renderNameTag(poseStack, player, player.getName(), font, distance);
        }

        event.setCancelled(true);
    }

    private void renderNameTag(PoseStack poseStack, LivingEntity entity, Component nameComponent, Font font, double distance) {
        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPosition().x;
        double y = entity.getY() + entity.getEyeHeight() + 3.4 - mc.getEntityRenderDispatcher().camera.getPosition().y;
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPosition().z;
        FontManager fontManager = Loratadine.INSTANCE.getFontManager();
        String text = nameComponent.getString() + " [" + String.format("%.1f", distance) + "m]" + " [" + String.format("%.1f", entity.getHealth()) + "HP]";

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(mc.getEntityRenderDispatcher().camera.rotation());

        // 根据距离调整 scale
        float scale = (float) (nameTagScale.getValue().floatValue() / (20.0F / (distance + 10.0F))); // 假设最大距离为20m，最小为10m
        poseStack.scale(-scale, -scale, scale);

        switch (mode.getValue()) {
            case "Old" -> {
                float halfWidth = fontManager.zw20.getStringWidth(text) / 2.0F;
                float height = font.lineHeight;
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderUtils.renderNameTagRoundedQuad(poseStack, -halfWidth - 2, 0, halfWidth + 2, height + 20, 5, 100, new Color(25, 22, 22, 169));
                RenderSystem.disableBlend();
                fontManager.zw20.drawString(poseStack, text, -halfWidth, 10, Color.WHITE.getRGB());
                RenderSystem.enableDepthTest();
            }

            case "Simple" -> {
                float halfWidth = fontManager.zw20.getStringWidth(text) / 2.0F;
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                fontManager.zw20.drawString(poseStack, text, -halfWidth, 10, Color.WHITE.getRGB());
                RenderSystem.enableDepthTest();
            }

            case "Modern" -> {
                final DecimalFormat decimalFormat = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                float width = Math.max(fontManager.zw26.getStringWidth(entity.getName().getString()), fontManager.ax18.getStringWidth(decimalFormat.format(entity.getHealth()) + " HP")) + 10;
                float height = fontManager.zw26.getHeight() + fontManager.ax18.getHeight() + 17;
                RenderSystem.disableDepthTest();
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                RenderUtils.drawRectangle(poseStack, -(width / 2), -height, width, height, new Color(20, 20, 20, 80).getRGB());
                RenderUtils.drawRectangle(poseStack, -(width / 2), -3, width * (entity.getHealth() / entity.getMaxHealth()), 3, -1);
                RenderUtils.drawRectangle(poseStack, -(width / 2), -3, width, 3, new Color(20, 20, 20, 80).getRGB());
                fontManager.zw26.drawString(poseStack, entity.getName().getString(), -(width / 2) + 5, -height + 5, -1);
                fontManager.ax18.drawString(poseStack, decimalFormat.format(entity.getHealth()) + " HP", -(width / 2) + 5, -height + 8 + fontManager.zw26.getHeight(), -1);
                RenderSystem.enableDepthTest();
            }
        }

        poseStack.popPose();
    }
}
