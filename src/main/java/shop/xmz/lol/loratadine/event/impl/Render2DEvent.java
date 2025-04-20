package shop.xmz.lol.loratadine.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public record Render2DEvent(float partialTick, GuiGraphics guiGraphics, PoseStack poseStack) implements Event {
}
