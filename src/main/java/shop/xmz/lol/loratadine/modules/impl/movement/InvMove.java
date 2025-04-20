package shop.xmz.lol.loratadine.modules.impl.movement;

import cn.lzq.injection.leaked.invoked.UpdateEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;

import java.util.Arrays;
import java.util.List;

public class InvMove extends Module {
    final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"Vanilla", "Heypixel"}, "Vanilla");
    private final BooleanSetting noJump = new BooleanSetting("No Jump", this, false);
    private boolean wasInContainer = false;
    private boolean wasSprinting = false;

    private static final List<KeyMapping> BASE_KEYS = Arrays.asList(
            mc.options.keyUp,
            mc.options.keyDown,
            mc.options.keyLeft,
            mc.options.keyRight,
            mc.options.keySprint
    );

    private List<KeyMapping> getKeys() {
        if (noJump.getValue()) {
            return BASE_KEYS;
        } else {
            return Arrays.asList(
                    mc.options.keyUp,
                    mc.options.keyDown,
                    mc.options.keyLeft,
                    mc.options.keyRight,
                    mc.options.keyJump,
                    mc.options.keySprint
            );
        }
    }

    public InvMove() {
        super("InvMove", "背包移动", Category.MOVEMENT);
    }

    @EventTarget
    public void onMotion(UpdateEvent event) {
        this.setSuffix(mode.getValue());

        // 检查Minecraft客户端实例及玩家是否有效
        if (mc == null || mc.player == null) {
            return;
        }

        // 只有在屏幕打开时才需要处理
        if (mc.screen != null) {
            if (mode.is("Heypixel")) {
                if (mc.screen instanceof AbstractContainerScreen) {
                    // 记录状态
                    if (!wasInContainer) {
                        wasSprinting = mc.player.isSprinting();
                        wasInContainer = true;
                    }

                    // 取消疾跑（如果需要）
                    if (mc.player.isSprinting()) {
                        mc.player.setSprinting(false);
                    }

                    // 更新键位状态
                    forceUpdateStates();
                } else {
                    // 退出容器时恢复疾跑
                    if (wasInContainer) {
                        if (wasSprinting) {
                            mc.player.setSprinting(wasSprinting);
                        }
                        wasInContainer = false;
                        wasSprinting = false; // 重置状态
                    }
                    // 对于非容器屏幕，也要更新键位
                    forceUpdateStates();
                }
            } else {
                // Vanilla模式，直接更新键位
                forceUpdateStates();
            }
        } else {
            // 屏幕关闭时，如果我们之前在容器中，恢复疾跑状态
            if (wasInContainer) {
                if (wasSprinting) {
                    mc.player.setSprinting(wasSprinting);
                }
                wasInContainer = false;
                wasSprinting = false;
            }
        }
    }

    // 重命名为forceUpdateStates，确保强制设置键位状态
    public void forceUpdateStates() {
        if (mc.screen != null) {
            for (KeyMapping k : getKeys()) {
                InputConstants.Key key = k.getKey();
                // 直接检查键是否被按下
                boolean isKeyDown = InputConstants.isKeyDown(mc.getWindow().getWindow(), key.getValue());

                // 强制设置键位状态
                KeyMapping.set(key, isKeyDown);

                // 确保按键被处理
                if (isKeyDown) {
                    KeyMapping.click(key);
                }
            }

            // 如果开启了禁止跳跃选项，强制取消跳跃键
            if (noJump.getValue()) {
                KeyMapping.set(mc.options.keyJump.getKey(), false);
            }
        }
    }
}