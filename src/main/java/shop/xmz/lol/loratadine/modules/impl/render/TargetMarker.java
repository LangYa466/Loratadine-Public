package shop.xmz.lol.loratadine.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.Render3DEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.combat.KillAura;
import shop.xmz.lol.loratadine.modules.impl.hud.HUD;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.utils.misc.EntityUtils;
import shop.xmz.lol.loratadine.utils.render.ESPUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;

import java.awt.*;

public class TargetMarker extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"Nursultan", "Jello"}, "Nursultan");
    private final BooleanSetting onlyKillAura = new BooleanSetting("Only KillAura", this, false);
    private final NumberSetting range = new NumberSetting("Range", this, 4, 0.1, 6, 0.1);
    private float circleStep = 0;
    public TargetMarker() {
        super("TargetMarker", "目标指示器", Category.RENDER);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.level == null || event.poseStack() == null) return;
        PoseStack poseStack = event.poseStack();
        Color mainColor = HUD.INSTANCE.getColor(1);
        Color secondColor = HUD.INSTANCE.getColor(8);

        if (onlyKillAura.getValue()) {
            final Entity entity = KillAura.target;
            if (entity != null) {
                poseStack.pushPose();
                switch (mode.getValue()) {
                    case "Nursultan" -> ESPUtils.drawTextureOnEntity(poseStack, -24, -24, 48, 48, 48, 48, entity, Loratadine.INSTANCE.getCLIENT_TARGET_PNG(), true, mainColor, mainColor, secondColor, secondColor);
                    case "Jello" -> {
                        ESPUtils.drawJello(poseStack, entity, 1, circleStep);
                        circleStep += 0.08f;
                    }
                }
                poseStack.popPose();
            }
        } else {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (EntityUtils.isSelected(entity, true) && mc.player.distanceTo(entity) <= range.getValue().floatValue()) {
                    poseStack.pushPose();
                    switch (mode.getValue()) {
                        case "Nursultan" -> ESPUtils.drawTextureOnEntity(poseStack, -24, -24, 48, 48, 48, 48, entity, Loratadine.INSTANCE.getCLIENT_TARGET_PNG(), true, mainColor, mainColor, secondColor, secondColor);
                        case "Jello" -> ESPUtils.drawJello(poseStack, entity, 1, circleStep);
                    }
                    poseStack.popPose();
                }
            }
            if (mode.is("Jello")) circleStep += 0.08f;
        }
    }
}
