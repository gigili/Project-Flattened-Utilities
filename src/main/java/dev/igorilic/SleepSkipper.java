package dev.igorilic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = projectflattenedutilities.MOD_ID)
public class SleepSkipper {
    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();

        if (level.isClientSide()) return;

        // Get a list of dimension strings from config
        List<String> configuredDims = List.of(projectflattenedutilities.MOD_ID + ":void");

        // Convert strings to ResourceLocations and check if the current dimension matches any
        boolean isConfiguredDim = configuredDims.stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .anyMatch(dim -> dim.equals(level.dimension().location()));

        if (!isConfiguredDim) return;

        MinecraftServer server = player.server;

        long currentTime = level.getDayTime();
        long newTime = ((currentTime / 24000) + 1) * 24000;

        String command = "time set " + newTime;

        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack()
                        .withLevel(level)
                        .withSuppressedOutput(),
                command
        );
    }
}
