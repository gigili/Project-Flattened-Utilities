package dev.igorilic.worldgen;

import dev.igorilic.projectflattenedutilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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
    private static final ResourceLocation JOINED_PLATFORM_TAG = new ResourceLocation(projectflattenedutilities.MOD_ID, "spawned_platform");

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel targetDim = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(projectflattenedutilities.MOD_ID, "void")));

        if (targetDim == null) return;

        CompoundTag persistentData = player.getPersistentData();
        CompoundTag forgeData = persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG);

        if (!forgeData.contains(JOINED_PLATFORM_TAG.toString())) {
            forgeData.putBoolean(JOINED_PLATFORM_TAG.toString(), true);
            persistentData.put(ServerPlayer.PERSISTED_NBT_TAG, forgeData);

            teleportAndSpawnPlatform(player, targetDim);
        }
    }

    private static void teleportAndSpawnPlatform(ServerPlayer player, ServerLevel dimension) {
        BlockPos platformPos = getPlatformPositionForPlayer(player, dimension);
        StructureTemplate template = dimension.getServer().getStructureManager()
                .get(new ResourceLocation(projectflattenedutilities.MOD_ID, "start_platform"))
                .orElse(null);

        if (template != null) {
            template.placeInWorld(
                    dimension,
                    platformPos,
                    platformPos,
                    new StructurePlaceSettings(),
                    dimension.getRandom(),
                    2
            );
        }

        player.teleportTo(dimension, platformPos.getX() + 0.5, 68, platformPos.getZ() + 0.5, 0F, 0F);
    }


    private static BlockPos getPlatformPositionForPlayer(ServerPlayer player, ServerLevel dimension) {
        UUID uuid = player.getUUID();
        int spread = 64;

        int index = Math.abs(uuid.hashCode() % 10000);
        int gridX = index % 100;
        int gridZ = index / 100;

        return new BlockPos(gridX * spread, 64, gridZ * spread);
    }

}
