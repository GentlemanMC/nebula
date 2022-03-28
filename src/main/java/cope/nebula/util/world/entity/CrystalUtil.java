package cope.nebula.util.world.entity;

import cope.nebula.client.Nebula;
import cope.nebula.util.Globals;
import cope.nebula.util.world.BlockUtil;
import cope.nebula.util.world.entity.player.rotation.AngleUtil;
import cope.nebula.util.world.entity.player.rotation.Rotation;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * Helps with end crystals
 *
 * @author aesthetical
 * @since 3/19/22
 */
public class CrystalUtil implements Globals {
    /**
     * Checks if we can place an end crystal here
     * @param blockPos The position to place at
     * @param protocol If we should take into account being on a newer server
     * @return if we can place an and crystal at this block
     */
    public static boolean canPlaceAt(BlockPos blockPos, boolean protocol) {
        // if the block is not bedrock/obsidian
        if (!canCrystalBePlacedOn(blockPos)) {
            return false;
        }

        // the position of where crystals will be
        BlockPos possibleCrystalPos = blockPos.add(0, 1, 0);

        // if the block where a crystal can placed is not air, we cannot place there
        if (!mc.world.isAirBlock(possibleCrystalPos)) {
            return false;
        }

        // check if any end crystals are at this spot
        if (!mc.world.getEntitiesWithinAABB(Entity.class,
                new AxisAlignedBB(possibleCrystalPos),
                (c) -> !(c instanceof EntityEnderCrystal) && !c.isDead).isEmpty()) {

            return false;
        }

        // if we are not on an updated server and the block above where a crystal can be placed is not air, it is not a valid placement
        if (!protocol && !mc.world.isAirBlock(blockPos.add(0, 2, 0))) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the position is bedrock or obsidian
     * @param pos the position
     * @return if we can place at that position
     */
    public static boolean canCrystalBePlacedOn(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block.equals(Blocks.BEDROCK) || block.equals(Blocks.OBSIDIAN);
    }

    /**
     * Places the crystal at this location
     * @param pos The position
     * @param hand The hand to place with
     * @param strictDirection If to place with a stricter direction
     * @param swing If to swing
     * @param rotate If to rotate
     */
    public static void placeAt(BlockPos pos, EnumHand hand, boolean strictDirection, boolean swing, boolean rotate) {
        EnumFacing facing = EnumFacing.UP;
        Vec3d vec = new Vec3d(0.0, 0.0, 0.0);

        if (strictDirection) {
            if (pos.getY() > mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()) {
                RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0f), new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

                if (result != null) {
                    facing = result.sideHit == null ? EnumFacing.DOWN : result.sideHit;

                    if (result.hitVec != null) {
                        vec = result.hitVec;
                    }
                }
            }
        }

        if (rotate) {
            Rotation rotation = AngleUtil.toBlock(pos, facing);
            if (rotation.isValid()) {
                Nebula.getInstance().getRotationManager().setRotation(rotation);
            }
        }

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                pos, facing, hand,
                (float) (vec.x - pos.getX()),
                (float) (vec.y - pos.getY()),
                (float) (vec.z - pos.getZ())));

        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }
    }

    /**
     * Attacks an end crystal
     * @param crystal The crystal
     * @param hand The hand to attack with
     * @param swing If to swing your hand
     */
    public static void attack(EntityEnderCrystal crystal, EnumHand hand, boolean swing) {
        if (crystal == null) {
            return;
        }

        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        if (swing) {
            mc.player.swingArm(hand);
        } else {
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        }
    }
}
