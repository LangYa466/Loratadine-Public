package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.impl.setting.MoveFix;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.animations.Animation;
import shop.xmz.lol.loratadine.utils.animations.impl.DecelerateAnimation;
import shop.xmz.lol.loratadine.utils.font.FontManager;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.item.SpoofItemUtil;
import shop.xmz.lol.loratadine.utils.math.MathUtils;
import shop.xmz.lol.loratadine.utils.player.FallingPlayer;
import shop.xmz.lol.loratadine.utils.player.MoveUtils;
import shop.xmz.lol.loratadine.utils.player.PlayerUtil;
import shop.xmz.lol.loratadine.utils.render.ColorUtils;
import shop.xmz.lol.loratadine.utils.render.RenderUtils;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.awt.*;
import java.util.List;
import java.util.*;

import static shop.xmz.lol.loratadine.utils.player.MoveUtils.isMoving;


public class Scaffold extends Module {
    public static Scaffold INSTANCE;
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Snap", "Telly", "Watchdog Telly"}, "Snap");
    private final BooleanSetting keepRotation = new BooleanSetting("Keep Rotation", this, true);
    private final BooleanSetting spoof = new BooleanSetting("Spoof Item", this, true);
    private final BooleanSetting silent = new BooleanSetting("Silent Swing", this, true);
    private final BooleanSetting keepY = new BooleanSetting("KeepY", this, false);
    private final BooleanSetting telly = new BooleanSetting("Telly", this, true);
    private final BooleanSetting auto3rdPerson = new BooleanSetting("Auto 3rd Person", this, false);
    private final ModeSetting sneak_Value = new ModeSetting("Sneak Mode", this, new String[]{"None", "Legit", "Fast"}, "None");
    private final ModeSetting jump_Value = new ModeSetting("Jump Mode", this, new String[]{"None", "Legit", "Fast"}, "None");
    private final ModeSetting count_Value = new ModeSetting("Count", this, new String[]{"None", "Modern", "Basic", "Simple"}, "None");
    private final Animation animation = new DecelerateAnimation(250, 1);
    private int ticksOnAir, sneakingTicks;
    private boolean lastJumpPressed;
    public int bigVelocityTick = 0;
    private int offGroundTicks = 0;
    private boolean onKeepY = false;
    boolean canTellyPlace;
    private int prevItem = 0;
    private int baseY = -1;
    private int slot;

    public Scaffold() {
        super("Scaffold", "自动搭路", Category.PLAYER, GLFW.GLFW_KEY_G);
        INSTANCE = this;
    }
}