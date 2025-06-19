package dev.igorilic.worldgen.portal;

import dev.igorilic.block.ModBlocks;
import dev.igorilic.block.custom.TeleporterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class ModTeleporter implements ITeleporter {
    private static final Logger LOGGER = LogManager.getLogger();
    public static BlockPos thisPos = BlockPos.ZERO;
    public static boolean insideDimension = true;

    public ModTeleporter(BlockPos pos, boolean insideDim) {
        thisPos = pos;
        insideDimension = insideDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        int estimatedY = destinationWorld.getHeight(Heightmap.Types.WORLD_SURFACE, thisPos.getX(), thisPos.getZ());
        BlockPos destinationPos = new BlockPos(thisPos.getX(), estimatedY, thisPos.getZ());
        BlockPos ground = findNearestValidGround(destinationWorld, destinationPos);

        LOGGER.info("Ground found at {}", ground);
        boolean doSetBlock = true;
        for (BlockPos checkPos : BlockPos.betweenClosed(
                ground.below(5).west(50).north(50),  // min corner
                ground.above(15).east(50).south(50)  // max corner
        )) {
            //LOGGER.info("Check pos {}", checkPos);
            if (destinationWorld.getBlockState(checkPos).getBlock() instanceof TeleporterBlock) {
                LOGGER.info("Found teleporter at {}", checkPos);
                ground = checkPos;
                doSetBlock = false;
                break;
            }
        }

        if (doSetBlock) {
            LOGGER.info("Placing teleporter at {}", ground);
            destinationWorld.setBlock(ground, ModBlocks.TELEPORTER_BLOCK.get().defaultBlockState(), 3);
        }

        // Set the position directly without using getCenter() to ensure precise placement
        Vec3 spawnPos = new Vec3(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5);
        entity.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);

        // Force the position before calling repositionEntity
        entity = repositionEntity.apply(false);

        entity.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);

        LOGGER.info("Final entity position: {}", entity.position());
        return entity;
    }

    private BlockPos findNearestValidGround(ServerLevel world, BlockPos center) {
        int horizontalRange = 20;
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();

        LOGGER.info("minY: {}", minY);
        LOGGER.info("maxY: {}", maxY);

        for (int y = minY; y <= maxY; y++) {
            for (int xOffset = -horizontalRange; xOffset <= horizontalRange; xOffset++) {
                for (int zOffset = -horizontalRange; zOffset <= horizontalRange; zOffset++) {
                    BlockPos checkPos = new BlockPos(center.getX() + xOffset, y, center.getZ() + zOffset);
                    if (isValidGround(world, checkPos)) {
                        return checkPos;
                    }
                }
            }
        }

        // Last resort — fallback to center heightmap
        return world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center);
    }


    private boolean isValidGround(ServerLevel world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        BlockState below = world.getBlockState(pos.below());
        return !below.isAir() && above.isAir();
    }
}
