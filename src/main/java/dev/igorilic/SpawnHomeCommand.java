package dev.igorilic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.stream.IntStream;

@Mod(projectflattenedutilities.MODID)
public class SpawnHomeCommand {
    private static final Logger LOGGER = LogManager.getLogger();
    static String NBT_HOUSE_KEY = "pf_schematics:used";

    public SpawnHomeCommand() {
        new File(FMLPaths.CONFIGDIR.get().toFile(), "pf_schematics").mkdirs();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("spawnHouse")
                .then(Commands.argument("filename", StringArgumentType.string())
                        .executes(ctx -> spawnHouse(
                                ctx.getSource().getPlayerOrException(),
                                StringArgumentType.getString(ctx, "filename")
                        ))
                )
                .executes(ctx -> spawnHouse(
                        ctx.getSource().getPlayerOrException(),
                        "default_base"
                ))
        );

        dispatcher.register(
                Commands.literal("clearHouse")
                        .requires(cs ->
                                cs.isPlayer()
                                        && cs.getEntity() instanceof ServerPlayer
                                        && cs.hasPermission(2)
                        )
                        .then(Commands.argument("player", StringArgumentType.string())
                                .executes(ctx -> clearHouseNBT(
                                        ctx,
                                        StringArgumentType.getString(ctx, "player")
                                ))
                        )
                        .executes(ctx -> {
                            int level = IntStream.iterate(4, i -> i >= 0, i -> i - 1).filter(i -> ctx.getSource().hasPermission(i)).findFirst().orElse(0);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Your permission level is: " + level), false);

                            return clearHouseNBT(
                                    ctx,
                                    ctx.getSource().getPlayer().getName().getString()
                            );
                        })
        );
    }

    private int clearHouseNBT(CommandContext<CommandSourceStack> context, String playerArg) {
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerArg);
        assert player != null;
        CompoundTag tag = player.getPersistentData().getCompound(ServerPlayer.PERSISTED_NBT_TAG);

        if (tag.getBoolean(NBT_HOUSE_KEY)) {
            tag.remove(NBT_HOUSE_KEY);
        }
        player.sendSystemMessage(Component.literal("You can now place a new house!"));

        return 1;
    }

    private int spawnHouse(ServerPlayer player, String filename) {
        CompoundTag tag = player.getPersistentData().getCompound(ServerPlayer.PERSISTED_NBT_TAG);


        if (tag.getBoolean(NBT_HOUSE_KEY)) {
            player.sendSystemMessage(Component.literal("You've already spawned your house!"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        File structureFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "pf_schematics/" + filename + ".nbt");
        if (!structureFile.exists()) {
            player.sendSystemMessage(Component.literal("File not found: " + structureFile.getAbsolutePath()));
            return 0;
        }

        try {
            // Load the structure
            CompoundTag nbt = NbtIo.readCompressed(new FileInputStream(structureFile));
            StructureTemplate template = new StructureTemplate();
            HolderGetter<Block> blockGetter = level.registryAccess().lookupOrThrow(Registries.BLOCK);
            template.load(blockGetter, nbt);

            // Get actual structure size
            Vec3i size = template.getSize();
            StructurePlaceSettings settings = new StructurePlaceSettings();

            // Lightweight area-clear check before placing
            BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

            outer:
            for (int dx = 0; dx < size.getX(); dx++) {
                for (int dy = 0; dy < size.getY(); dy++) {
                    for (int dz = 0; dz < size.getZ(); dz++) {
                        checkPos.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                        Block block = level.getBlockState(checkPos).getBlock();

                        String id = block.builtInRegistryHolder().key().location().getPath();

                        if (!id.equals("air") && !id.equals("grass_block") && !id.equals("dirt")) {
                            player.sendSystemMessage(Component.literal("The area isn't clear for building — pick a more open spot."));
                            return 0;
                        }
                    }
                }
            }

            // ✅ Spawn the structure
            template.placeInWorld(level, pos, pos, settings, level.getRandom(), 2);
            player.sendSystemMessage(Component.literal("Spawned " + filename + " at " + pos.toShortString()));
        } catch (Exception e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to spawn: " + e.getMessage()));
            return 0;
        }

        tag.putBoolean(NBT_HOUSE_KEY, true);
        return 1;
    }
}
