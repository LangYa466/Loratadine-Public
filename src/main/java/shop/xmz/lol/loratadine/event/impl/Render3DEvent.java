package shop.xmz.lol.loratadine.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;

public record Render3DEvent(float partialTick, PoseStack poseStack) implements Event {
}
