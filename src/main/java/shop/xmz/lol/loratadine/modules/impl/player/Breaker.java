package shop.xmz.lol.loratadine.modules.impl.player;

import cn.lzq.injection.leaked.invoked.BlockDamageEvent;
import cn.lzq.injection.leaked.invoked.LivingUpdateEvent;
import cn.lzq.injection.leaked.invoked.MouseOverEvent;
import cn.lzq.injection.leaked.invoked.TeleportEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.event.impl.PacketEvent;
import shop.xmz.lol.loratadine.event.impl.Priorities;
import shop.xmz.lol.loratadine.modules.Category;
import shop.xmz.lol.loratadine.modules.Module;
import shop.xmz.lol.loratadine.modules.setting.impl.BooleanSetting;
import shop.xmz.lol.loratadine.modules.setting.impl.ModeSetting;
import shop.xmz.lol.loratadine.utils.RotationUtils;
import shop.xmz.lol.loratadine.utils.helper.Rotation;
import shop.xmz.lol.loratadine.utils.player.PlayerUtil;
import shop.xmz.lol.loratadine.utils.player.RayCastUtil;
import shop.xmz.lol.loratadine.utils.helper.Vector3d;
import shop.xmz.lol.loratadine.utils.wrapper.WrapperUtils;

import java.util.ArrayList;
import java.util.List;

public class Breaker extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", this, new String[]{"Normal", "Instant"}, "Normal");

    public final BooleanSetting keep = new BooleanSetting("Keep Break", this, true);
    public final BooleanSetting velocity = new BooleanSetting("Cancel VelocityPacket", this, true);

    public final BooleanSetting throughWalls = new BooleanSetting("Through Walls", this, true);
    private final BooleanSetting emptySurrounding = new BooleanSetting("Empty Surrounding", this, false/*, () -> !throughWalls.getValue()*/);

    public final BooleanSetting rotations = new BooleanSetting("Rotation", this, true);
    public final BooleanSetting importantRotationsOnly = new BooleanSetting("Only ImportantRotations", this, true);
    public final BooleanSetting slowDownInAir = new BooleanSetting("Slow Down In Air", this, true);
    public final BooleanSetting whiteListOwnBed = new BooleanSetting("Whitelist Own Bed", this, true);
    public final BooleanSetting dontBreakWhenAttacking = new BooleanSetting("Without Attack", this, true);
    private Vector3d block, lastBlock, home;
    private int delay;
    private boolean down;
    private float damage;

    public Breaker() {
        super("Breaker", "挖床", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        block = null;
        damage = 0;
        delay = 0;
    }

    @Override
    public void onDisable() {
        block = null;

        if (down) {
            mc.options.keyAttack.setDown(false);
            down = false;
        }
    }

/*    @EventLink()
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (block == null) return;

        Vector3i pos = new Vector3i((int) Math.floor(block.getX()), (int) Math.floor(block.getY()), (int) Math.floor(block.getZ()));
        damageAnimation.run(damage);

        getLayer(BLOOM).add(() -> {
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GL11.glDepthMask(false);

            RenderUtil.color(getTheme().getFirstColor());
            RenderUtil.drawBoundingBox(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1 * damageAnimation.getValue(), pos.getZ() + 1));

            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GL11.glDepthMask(true);
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
            GlStateManager.resetColor();
        });
    };*/

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || !velocity.getValue() || block == null) return;

        final Packet<?> p = event.getPacket();

        if (p instanceof ClientboundSetEntityMotionPacket wrapper) {

            if (wrapper.getId() == mc.player.getId()) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget(value = Priorities.VERY_HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        damage = WrapperUtils.getDestroyProgress();
    }

    @EventTarget(value = Priorities.VERY_HIGH)
    public void onPreUpdate(LivingUpdateEvent event) {
        if (mc.player == null) return;

        delay--;

        if (delay > 0 || (dontBreakWhenAttacking.getValue() )) return;

        if (block == null || PlayerUtil.getDistance(block.getX(), block.getY(), block.getZ()) > 4 ||
                PlayerUtil.block(block.getX(), block.getY(), block.getZ()) instanceof AirBlock) {
            this.updateBlock();

            if (down) {
                mc.options.keyAttack.setDown(false);
                down = false;
            }

            if (block == null) return;
        }

        this.destroy();
    }

    public void updateBlock() {
        if (mc.player == null) return;

        if (!(this.block == null || PlayerUtil.block(this.block.x, this.block.y, this.block.z) instanceof AirBlock
                || PlayerUtil.getDistance(this.block.x, this.block.y - mc.player.getEyeHeight(), this.block.z) > 4.5)) {
            return;
        }
        if (this.lastBlock != null && !keep.getValue()) {
            WrapperUtils.setDestroyProgress(0);
        }

        lastBlock = block;
        block = this.block();
    }

    public void rotate() {
        if (mc.player == null || mc.level == null) return;

        BlockPos blockPos = new BlockPos((int) block.getX(), (int) block.getY(), (int) block.getZ());

        float blockHardness = mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.level, blockPos);

        if (importantRotationsOnly.getValue() && (WrapperUtils.getDestroyProgress() != 0 && WrapperUtils.getDestroyProgress() <= 1 - blockHardness - 0.001)) {
            return;
        }

        if (!this.rotations.getValue()) return;

        RotationUtils.setRotation(getRotations(), 1);
    }

    public Vector3d block() {
        if (mc.player == null || mc.level == null) return null;

        if (home != null && mc.player.distanceToSqr(home.getX(), home.getY(), home.getZ()) < 35 * 35 && whiteListOwnBed.getValue()) {
            return null;
        }

        int beds = 0;

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {

                    final Block block = PlayerUtil.blockRelativeToPlayer(x, y, z);
                    final Vector3d position = new Vector3d(mc.player.getX() + x, mc.player.getY() + y, mc.player.getZ() + z);

                    if (!(block instanceof BedBlock)) {
                        continue;
                    }

                    beds++;

                    if (beds <= 1) continue;

                    /* Grab moving object position */
                    final HitResult movingObjectPosition = RayCastUtil.rayCast(RotationUtils.calculate(position), 4.5f);
                    if (movingObjectPosition == null || movingObjectPosition.getLocation().distanceTo(new Vec3(mc.player.getX(), mc.player.getY() - mc.player.getEyeHeight(), mc.player.getZ())) > 4.5) {
                        continue;
                    }

                    if (!throughWalls.getValue()) {
                        final BlockPos blockPos = ((BlockHitResult) movingObjectPosition).getBlockPos();
                        if (!PlayerUtil.equalsVector(blockPos, position)) {
                            continue;
                        }
                    } else if (emptySurrounding.getValue()) {
                        Vector3d addVec = position;
                        double hardness = Double.MAX_VALUE;
                        boolean empty = false;

                        for (int addX = -4; addX <= 4; addX++) {
                            for (int addY = 0; addY <= 1; addY++) {
                                for (int addZ = -4; addZ <= 4; addZ++) {
                                    BlockPos pos = new BlockPos((int) (position.getX() + addX), (int) (position.getY() + addY), (int) (position.getZ() + addZ));
                                    Block possibleBlock = PlayerUtil.getBlock(pos);

                                    if (possibleBlock instanceof BedBlock) {
                                        continue;
                                    }

                                    if (empty || (mc.player.distanceToSqr(position.getX() + addX, position.getY() + addY, position.getZ()) + addZ) > 4.5)
                                        continue;

                                    if (getNeighbours(position.add(addX, addY, addZ)).stream().noneMatch(neighbour -> neighbour instanceof BedBlock)) {
                                        continue;
                                    }

                                    if (possibleBlock instanceof AirBlock || possibleBlock instanceof LiquidBlock) {
                                        empty = true;
                                        continue;
                                    }

                                    if (PlayerUtil.getDistance(position.getX() + addX, position.getY() + addY - mc.player.getEyeHeight(), position.getZ() + addZ) > 4.5) {
                                        continue;
                                    }

                                    double possibleHardness = PlayerUtil.getBlockHardness(mc.level, pos);

                                    if (possibleHardness < hardness) {
                                        hardness = possibleHardness;
                                        addVec = position.add(addX, addY, addZ);
                                    }
                                }
                            }
                        }

                        if (!empty) {
                            if (addVec.equals(position)) {
                                return null;
                            } else {
                                return addVec;
                            }
                        }
                    }

                    return position;
                }
            }
        }

        return null;
    }

    public List<Block> getNeighbours(Vector3d blockPos) {
        List<Block> neighbours = new ArrayList<>();
        for (Direction enumFacing : Direction.values()) {
            if (enumFacing == Direction.UP) continue;
            Vector3d neighbourPos = blockPos.add(new Vector3d(enumFacing.getNormal().getX(), enumFacing.getNormal().getY(), enumFacing.getNormal().getZ()));
            neighbours.add(PlayerUtil.block(neighbourPos));
        }
        return neighbours;
    }

    public void updateHardnessAndCallEvent(BlockPos blockPos) {
        final BlockDamageEvent bdEvent = new BlockDamageEvent(this.mc.player, this.mc.level, blockPos);
        Loratadine.INSTANCE.getEventManager().call(bdEvent);
    }

    public void destroy() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        boolean slowDownInAir = this.slowDownInAir.getValue();
        boolean ground = mc.player.onGround();

        if (!slowDownInAir) mc.player.setOnGround(true);

        BlockPos blockPos = new BlockPos((int) block.getX(), (int) block.getY(), (int) block.getZ());

        WrapperUtils.setDestroyProgress(damage);

        switch (mode.getValue()) {
            case "Instant":
                this.updateHardnessAndCallEvent(blockPos);
                this.rotate();

                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));

                block = null;
                delay = 20;

                mc.gameMode.destroyBlock(blockPos);
                break;

            case "Normal":
                this.updateHardnessAndCallEvent(blockPos);
                this.rotate();

                if (block != null) {
                    if (damage == 0) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        // 使用ServerboundPlayerActionPacket来发送开始破坏包
                        mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                    }

                    damage += mc.level.getBlockState(blockPos).getDestroyProgress(mc.player, mc.level, blockPos);

                    if (damage >= 1.0F) {
                        mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                        mc.gameMode.destroyBlock(blockPos); // Note: ensure using the correct method to destroy block
                        damage = 0.0F;
                        block = null;
                    }

                    mc.player.swing(InteractionHand.MAIN_HAND);

                    // Optionally, you might want to send progress visually to other players (if applicable)
                    mc.level.destroyBlockProgress(mc.player.getId(), blockPos, (int) (damage * 10.0F) - 1);
                }

                /*mc.options.keyAttack.setDown(true);
                down = true;*/
                break;
        }

        mc.player.setOnGround(ground);
    }

    public Rotation getRotations() {
        return RotationUtils.calculate(new Vector3d(Math.floor(block.getX()) + 0.5 + (Math.random() - 0.5) / 4, Math.floor(block.getY()) + 0.1, Math.floor(block.getZ()) + 0.5 + (Math.random() - 0.5) / 4));
    }

    @EventTarget
    public void onMouseOver(MouseOverEvent event) {
        if (mc.player == null || mc.level == null || block == null) return;

        BlockPos blockPos = new BlockPos((int) block.getX(), (int) block.getY(), (int) block.getZ());
        final Vec3 eyes = mc.player.getEyePosition(1);

        Rotation rotations = importantRotationsOnly.getValue() ? getRotations() : new Rotation(mc.player.getYRot(), mc.player.getXRot());
        final Vec3 rotationVector = RotationUtils.getVectorForRotation(rotations);

        final double range = 4.5;
        final Vec3 forward = eyes.add(rotationVector.x * range, rotationVector.y * range, rotationVector.z * range);

        HitResult movingObjectPosition = PlayerUtil.collisionRayTrace(mc.level, blockPos, mc.player.getEyePosition(1), forward);
        event.setMovingObjectPosition(movingObjectPosition);
    }

    @EventTarget
    public void onTeleport(TeleportEvent event) {
        if (mc.player == null) return;

        final double distance = Mth.sqrt((float) mc.player.distanceToSqr(event.getPosX(), event.getPosY(), event.getPosZ()));

        if (distance > 40) {
            home = new Vector3d(event.getPosX(), event.getPosY(), event.getPosZ());
        }
    }
}