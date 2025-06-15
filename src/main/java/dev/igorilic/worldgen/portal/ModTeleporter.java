package dev.igorilic.worldgen.portal;

import dev.igorilic.block.ModBlocks;
import dev.igorilic.block.custom.TeleporterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ModTeleporter implements ITeleporter {
    public static BlockPos thisPos = BlockPos.ZERO;
    public static boolean insideDimension = true;

    public ModTeleporter(BlockPos pos, boolean insideDim) {
        thisPos = pos;
        insideDimension = insideDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);

        int estimatedY = destinationWorld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, thisPos.getX(), thisPos.getZ());
        BlockPos searchCenter = new BlockPos(thisPos.getX(), estimatedY, thisPos.getZ());
        BlockPos destinationPos = findNearestValidGround(destinationWorld, searchCenter);

        if (insideDimension) {
            boolean doSetBlock = true;
            for (BlockPos checkPos : BlockPos.betweenClosed(destinationPos.below(256).west(20),
                    destinationPos.above(256).east(20))) {
                if (destinationWorld.getBlockState(checkPos).getBlock() instanceof TeleporterBlock) {
                    doSetBlock = false;
                    break;
                }
            }
            if (doSetBlock) {
                destinationWorld.setBlock(destinationPos.below(), ModBlocks.TELEPORTER_BLOCK.get().defaultBlockState(), 3);
            }
        }

        entity.setPos(destinationPos.getX() + 0.5, destinationPos.getY(), destinationPos.getZ() + 0.5);
        return entity;
    }

    private BlockPos findNearestValidGround(ServerLevel world, BlockPos center) {
        int horizontalRange = 20;
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();

        for (int y = minY; y <= maxY; y++) {
            for (int xOffset = -horizontalRange; xOffset <= horizontalRange; xOffset++) {
                for (int zOffset = -horizontalRange; zOffset <= horizontalRange; zOffset++) {
                    BlockPos checkPos = new BlockPos(center.getX() + xOffset, y, center.getZ() + zOffset);
                    if (isValidGround(world, checkPos)) {
                        return checkPos.above();
                    }
                }
            }
        }

        // Last resort — fallback to center heightmap
        return world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center).above();
    }


    private boolean isValidGround(ServerLevel world, BlockPos pos) {
        BlockState below = world.getBlockState(pos);
        BlockState above = world.getBlockState(pos.above());
        return below.isSolid() && above.isAir();
    }
}
