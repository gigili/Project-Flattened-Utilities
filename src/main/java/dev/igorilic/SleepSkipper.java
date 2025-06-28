package dev.igorilic;

import dev.igorilic.worldgen.dimension.ModDimensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = projectflattenedutilities.MOD_ID)
public class SleepSkipper {
    private static final ResourceLocation VOID_DIM = ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void");

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();
        ServerLevel level = server.getLevel(ModDimensions.PFDIM_VOID_LEVEL_KEY);

        if (level == null || !level.dimension().location().equals(VOID_DIM)) return;

        // Check if all players are sleeping
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        boolean allSleeping = players.stream().allMatch(ServerPlayer::isSleeping);

        if (allSleeping) {
            String command = "time set day";

            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack()
                            .withLevel(level)
                            .withSuppressedOutput(),
                    command
            );
        }
    }
}
