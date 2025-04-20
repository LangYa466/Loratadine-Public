package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.MotionEvent;
import cn.lzq.injection.leaked.invoked.TickEvent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.NumberSetting;
import shop.xmz.lol.loratadine.ui.notification.NotificationManager;
import shop.xmz.lol.loratadine.ui.notification.NotificationType;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import net.minecraft.world.phys.Vec3;

/**
 * @author DSJ_
 * @since 2025/2/14
 */
public class NoFall extends Module {
    private final ModeSetting mode_Value = new ModeSetting("Mode", this, new String[]{"Heypixel", "MLG", "Vanilla"},"Vanilla");
    private final ModeSetting rotation_Value = new ModeSetting("Rotation Mode", this, new String[]{"Legit", "Silent"},"Silent");
    private final BooleanSetting rotationBack = new BooleanSetting("Rotation Back", this, true);
    private final ModeSetting rotationBack_Value = new ModeSetting("RotationBack Mode", this, new String[]{"After Recycle", "No Recycle"},"No Recycle");
    private final BooleanSetting recycleBuckets = new BooleanSetting("Legit Recycle", this, true);
    private final NumberSetting distance = new NumberSetting("Distance", this, 4, 1, 10, 1); // 触发高度
    private final NumberSetting rotationSpeed = new NumberSetting("Rotation Speed", this, 10, 1, 10, 1); // 转头速度
    private final NumberSetting placeHeight = new NumberSetting("Place Height", this, 2, 1, 3, 0.1); // 放置高度（距离地面）
    private final BooleanSetting notification = new BooleanSetting("Notification", this, true);
    // Vanilla模式设置
    private final NumberSetting predictionTicks = new NumberSetting("Prediction Ticks", this, 2, 1, 5, 1); // 预测tick数
    private final BooleanSetting debugInfo = new BooleanSetting("Debug Info", this, false); // 调试信息

    private int previousSlot = -1;
    private boolean falling;
    private float originalPitch = 0; // 初始视角俯仰角
    public static boolean isPlacing = false; // 是否正在放置水桶
    private boolean isResetting = false; // 是否正在恢复视角

    // Vanilla模式变量
    private boolean willLandIn2Ticks = false; // 2tick后是否会落地
    private boolean willLandIn1Tick = false; // 1tick后是否会落地
    private boolean isSneaking = false; // 是否正在下蹲
    private int sneakTimer = 0; // 下蹲计时器
    private Vec3 lastPos; // 上一tick位置
    private Vec3 lastVelocity; // 上一tick速度
    private int tickCounter = 0; // 自定义tick计数器

    public NoFall() {
        super("NoFall", "自动落地水", Category.PLAYER);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null || mc.level == null || event.post) return;

        if (mode_Value.getValue().equals("MLG")) {

            // 阶段1: 下落时触发
            if (mc.player.fallDistance > distance.getValue().intValue() && !mc.player.onGround() && !isPlacing && !isResetting) {
                startMLG();
            }

            // 阶段2: 正在执行放置动作
            if (isPlacing) {
                processRotation();
            }

            // 阶段3: 恢复状态
            if (rotationBack_Value.getValue().equals("After Recycle")) {
                if (isResetting && mc.player.onGround() && !mc.player.isInWater()) {
                    resetState();
                }
            } else {
                if (isResetting) {
                    resetState();
                }
            }
            // 检查是否落地且没有水桶
            if (!mc.player.onGround() && !isPlacing && !isResetting && mc.player.fallDistance > distance.getValue().intValue()) {
                checkWaterBucket();
            }
        } else if (mode_Value.getValue().equals("Vanilla")) {
            // Vanilla模式预判处理
            handleVanillaMode(event);
        }
    }

    private void handleVanillaMode(MotionEvent event) {
        if (mc.player == null || mc.level == null) return;

        // Vanilla模式现在使用直接发包来取消摔落伤害
        // 仍然保留预测逻辑，用于辅助判断和调试信息

        // 当玩家在空中且有摔落伤害风险
        if (!mc.player.onGround() && mc.player.fallDistance > 2.0F) {
            // 计算当前速度
            Vec3 currentPos = mc.player.position();
            Vec3 currentVelocity = null;

            if (lastPos != null) {
                currentVelocity = currentPos.subtract(lastPos);
            }

            // 预测未来位置
            if (currentVelocity != null) {
                // 保存当前位置和速度用于下一tick
                lastVelocity = currentVelocity;

                // 预测下一tick的位置（考虑重力）
                Vec3 nextTickPos = currentPos.add(currentVelocity.x, currentVelocity.y - 0.08, currentVelocity.z);
                Vec3 nextTickVelocity = new Vec3(currentVelocity.x * 0.91, (currentVelocity.y - 0.08) * 0.98, currentVelocity.z * 0.91);

                // 预测两tick后的位置
                Vec3 twoTicksPos = nextTickPos.add(nextTickVelocity.x, nextTickVelocity.y - 0.08, nextTickVelocity.z);

                // 检查1tick和2tick后是否会落地
                willLandIn1Tick = willLandAtPosition(nextTickPos);
                willLandIn2Ticks = willLandAtPosition(twoTicksPos);

                // 调试信息
                if (debugInfo.getValue()) {
                    NotificationManager.add(
                            NotificationType.INFO,
                            "NoFall Debug",
                            "1Tick: " + willLandIn1Tick + ", 2Ticks: " + willLandIn2Ticks +
                                    ", FallDist: " + String.format("%.2f", mc.player.fallDistance)
                    );
                }

                // 如果预测到即将落地，直接发送一个onGround=true的包来取消摔落伤害
                if (willLandIn1Tick || willLandIn2Ticks) {
                    if (mc.player.fallDistance > 3.0F) { // 只在有实际摔落伤害风险时干预
                        // 这一部分逻辑在onPacketEvent中处理，不在这里直接发包
                        // 而是在发包事件中截取并修改

                        // 在预判即将落地时做记录，方便在包处理时检查
                        falling = true;
                    }
                }
            }

            // 更新位置记录
            lastPos = currentPos;
        } else {
            // 玩家在地面上，重置状态
            falling = false;
            lastPos = mc.player.position();
        }
    }

    private boolean willLandAtPosition(Vec3 position) {
        // 获取玩家位置下方的方块
        BlockPos blockPos = new BlockPos((int)position.x, (int)position.y, (int)position.z);

        // 检查脚下是否有方块
        return !mc.level.getBlockState(blockPos.below()).isAir();
    }

    @EventTarget
    private void onTickEvent(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (mode_Value.is("Heypixel")) {
            falling = mc.player.fallDistance > distance.getValue().intValue();

            if (!mc.player.onGround() && falling) {
                // 获取玩家的当前位置和旋转
                double x = mc.player.getX();
                double y = mc.player.getY() + 1.0E-9; // 添加微小偏移
                double z = mc.player.getZ();
                float yaw = mc.player.getYRot(); // 水平旋转
                float pitch = mc.player.getXRot(); // 垂直旋转
                boolean onGround = true; // 设置为在地面上

                // 生成一个包含位置和旋转的数据包
                ServerboundMovePlayerPacket packet = new ServerboundMovePlayerPacket.PosRot(
                        x, y, z, yaw, pitch, onGround
                );

                // 发送数据包
                mc.getConnection().send(packet);
            }
        } else if (mode_Value.is("Vanilla")) {
            // Vanilla模式现在主要依赖包处理来取消摔落伤害

            // 如果摔落距离超过阈值，直接发送一个onGround=true的包，防止蓄积过多的摔落距离
            if (mc.player.fallDistance > 3.0F && !mc.player.onGround()) {
                // 创建一个简单的计数器来控制发包频率
                if (tickCounter % 10 == 0) { // 每10个tick一次
                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            true // 告诉服务器我们在地面上
                    ));

                    // 紧接着发送真实位置以避免橡皮筋效应
                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            false // 实际未着地
                    ));

                    if (debugInfo.getValue()) {
                        NotificationManager.add(
                                NotificationType.INFO,
                                "NoFall",
                                "Sent anti-fall packet"
                        );
                    }
                }
            }

            // 确保落地后重置摔落距离
            if (mc.player.onGround() && mc.player.fallDistance > 0) {
                mc.player.fallDistance = 0;
            }

            // 增加计数器
            tickCounter++;
            if (tickCounter > 1000) tickCounter = 0; // 防止溢出
        }
    }

    @EventTarget
    private void onPacketEvent(PacketEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (mode_Value.is("Heypixel")) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
                if (packet.isOnGround() && falling) {
                    falling = false;
                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                            mc.player.xo,
                            mc.player.yo + 1.0E-9,
                            mc.player.zo,
                            false // onGround
                    ));
                }
            }
        } else if (mode_Value.is("Vanilla")) {
            // 使用发包方式来取消摔落伤害
            if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
                // 如果玩家有摔落伤害风险，修改发送给服务器的包
                if (mc.player.fallDistance > 2.0F) {
                    if (packet.isOnGround()) {
                        // 在玩家即将接触地面时，发送一个假的"未着地"状态
                        event.setCancelled(true);
                        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                                mc.player.getX(),
                                mc.player.getY() + 0.5, // 轻微提高位置
                                mc.player.getZ(),
                                false // 告诉服务器我们没有在地面上
                        ));

                        // 紧接着发送正确的着地包，但不包含摔落伤害信息
                        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                                mc.player.getX(),
                                mc.player.getY(),
                                mc.player.getZ(),
                                true // 此时告诉服务器我们已经在地面上
                        ));

                        // 重置摔落距离
                        mc.player.fallDistance = 0;

                        if (notification.getValue()) {
                            NotificationManager.add(
                                    NotificationType.SUCCESS,
                                    "NoFall",
                                    "Fall damage canceled!"
                            );
                        }
                    }
                }
            }
        }
    }

    private void startMLG() {
        int waterSlot = findWaterBucketSlot();
        if (waterSlot == -1) return;

        // 记录初始状态
        originalPitch = mc.player.getXRot();
        previousSlot = mc.player.getInventory().selected;
        isPlacing = true;

        // 切换到水桶
        mc.player.getInventory().selected = waterSlot;
    }

    private void processRotation() {
        // 平滑低头（目标角度90度）
        float targetPitch = 90.0f;
        float currentPitch = mc.player.getXRot();

        if (currentPitch < targetPitch) {
            // 每帧增加角度（限制最大不超过目标值）
            if (!rotation_Value.getValue().equals("Silent")) {
                mc.player.setXRot(Math.min(currentPitch + (10 * rotationSpeed.getValue().intValue()), targetPitch));
            } else {
                RotationUtils.setRotation(new Rotation(mc.player.getYRot(), Math.min(currentPitch + (10 * rotationSpeed.getValue().intValue()), targetPitch)), 20);
            }
        }

        // 检查是否接近地面
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos groundPos = findGroundBelowPlayer(playerPos);

        if (groundPos != null && playerPos.getY() - groundPos.getY() <= placeHeight.getValue().intValue()) {

            // 放置水桶
            mc.options.keyUse.setDown(true);
            if (notification.getValue()) NotificationManager.add(NotificationType.SUCCESS, "MLG", "Successfully placed the bucket!");

            // 回收水桶
            if (recycleBuckets.getValue() && mc.player.onGround() && mc.player.isInWater()) {
                mc.options.keyUse.setDown(false);
                mc.options.keyUse.setDown(true);
            }

            // 标记放置完成，开始恢复视角，取消放置水桶
            if (rotationBack.getValue()) {
                isResetting = true;
            }
            isPlacing = false;
        }
    }

    private void resetState() {
        // 平滑恢复视角
        float currentPitch = mc.player.getXRot();
        if (currentPitch > originalPitch) {
            mc.player.setXRot(Math.max(currentPitch - (10 * rotationSpeed.getValue().intValue()), originalPitch));
        } else {
            // 恢复物品栏
            if (previousSlot != -1) {
                mc.player.getInventory().selected = previousSlot;
                previousSlot = -1;
            }
            mc.options.keyUse.setDown(false);
            mc.player.fallDistance = 0;
            isResetting = false;
        }
    }

    private BlockPos findGroundBelowPlayer(BlockPos playerPos) {
        for (int y = playerPos.getY(); y > 0; y--) {
            BlockPos pos = new BlockPos(playerPos.getX(), y, playerPos.getZ());
            if (!mc.level.getBlockState(pos).isAir()) {
                return pos;
            }
        }
        return null;
    }

    private int findWaterBucketSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                return i;
            }
        }
        return -1;
    }

    private void checkWaterBucket() {
        int waterSlot = findWaterBucketSlot();
        if (waterSlot == -1) {
            // 没有水桶，发送通知
            if (notification.getValue()) NotificationManager.add(NotificationType.WARNING, "MLG", "No water bucket found!");
        }
    }

    @Override
    public void onDisable() {
        // 确保禁用时取消下蹲
        if (isSneaking && mc.options != null) {
            mc.options.keyShift.setDown(false);
            isSneaking = false;
        }
        super.onDisable();
    }
}