package dev.igorilic.worldgen;

import dev.igorilic.block.ModBlocks;
import dev.igorilic.projectflattenedutilities;
import dev.igorilic.worldgen.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class ModPlatformSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation JOINED_PLATFORM_TAG = ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "spawned_platform");
    private static final ResourceLocation PLATFORM_CORDS_TAG = ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "platform_cords");

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        // if starting dimension is not custom dimension don't override the spawn and teleport player
        if (player.serverLevel().dimensionTypeId() != ModDimensions.PFDIM_VOID_DIMENSION_TYPE) {
            return;
        }

        ServerLevel targetDim = player.server.getLevel(ModDimensions.PFDIM_VOID_LEVEL_KEY);

        if (targetDim == null) return;

        teleportAndSpawnPlatform(player, targetDim, true);
    }

    @SubscribeEvent
    public static void onEnterVoidDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (event.getTo() != ModDimensions.PFDIM_VOID_LEVEL_KEY) return;

        ServerLevel targetDim = player.server.getLevel(ModDimensions.PFDIM_VOID_LEVEL_KEY);
        if (targetDim == null) return;

        teleportAndSpawnPlatform(player, targetDim, false);
    }

    private static void teleportAndSpawnPlatform(ServerPlayer player, ServerLevel dimension, boolean forceSpawn) {
        BlockPos platformPos = getPlatformPositionForPlayer(player); // : new BlockPos(player.getOnPos().getX(), 64, player.getOnPos().getZ());
        StructureTemplate template = dimension.getServer().getStructureManager()
                .get(ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "start_platform"))
                .orElse(null);


        CompoundTag persistentData = player.getPersistentData();
        CompoundTag forgeData = persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG);

        if (template != null && !forgeData.contains(JOINED_PLATFORM_TAG.toString())) {
            forgeData.putBoolean(JOINED_PLATFORM_TAG.toString(), true);
            persistentData.put(ServerPlayer.PERSISTED_NBT_TAG, forgeData);

            template.placeInWorld(
                    dimension,
                    platformPos,
                    platformPos,
                    new StructurePlaceSettings(),
                    dimension.getRandom(),
                    2
            );
        }

        player.teleportTo(dimension, platformPos.getX(), 68, platformPos.getZ(), 0F, 0F);
        BlockPos spawnPos = new BlockPos(platformPos.getX(), 68, platformPos.getZ());
        if (forceSpawn) {
            player.setRespawnPosition(ModDimensions.PFDIM_VOID_LEVEL_KEY, spawnPos, 0, true, false);
        } else {
            BlockPos oldTeleporterPos = new BlockPos(spawnPos.getX(), -64, spawnPos.getZ());
            BlockPos newTeleporterPos = new BlockPos(spawnPos.getX(), 67, spawnPos.getZ());

            if (dimension.getBlockState(oldTeleporterPos).is(ModBlocks.TELEPORTER_BLOCK.get())) {
                dimension.removeBlock(oldTeleporterPos, false);
                LOGGER.info("[PFU] Removed old teleporter block at {}", oldTeleporterPos);
            }

            dimension.setBlock(newTeleporterPos, ModBlocks.TELEPORTER_BLOCK.get().defaultBlockState(), 3);
            LOGGER.info("[PFU] Placed new teleporter block at {}", newTeleporterPos);
        }
    }


    private static BlockPos getPlatformPositionForPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        int spread = 64;

        int index = Math.abs(uuid.hashCode() % 10000);
        int gridX = index % 100;
        int gridZ = index / 100;

        return new BlockPos(gridX * spread, 64, gridZ * spread);
    }

}
