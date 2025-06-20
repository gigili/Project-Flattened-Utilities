package dev.igorilic;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = projectflattenedutilities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_HOUSE_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue REQUIRES_ITEM_TO_TELEPORT;
    public static final ForgeConfigSpec.BooleanValue CONSUME_ITEM_TO_TELEPORT;
    public static final ForgeConfigSpec.ConfigValue<String> REQUIRED_ITEM_TO_TELEPORT;

    static {
        BUILDER.push("Permissions");

        SPAWN_HOUSE_WHITELIST = BUILDER
                .comment("List of player UUIDs allowed to use /spawnHouse. Leave empty to allow everyone.")
                .defineListAllowEmpty(
                        "spawnHouseWhitelist",
                        List.of(), // default: empty list
                        entry -> {
                            try {
                                UUID.fromString((String) entry);
                                return true;
                            } catch (Exception e) {
                                return false;
                            }
                        });

        BUILDER.pop();

        BUILDER.push("Settings");

        REQUIRES_ITEM_TO_TELEPORT = BUILDER
                .comment("If a player needs to hold an item in order to use the Teleporter")
                .define("requireItemToTeleport", false);

        CONSUME_ITEM_TO_TELEPORT = BUILDER
                .comment("If the required item gets consumed when teleporting")
                .define("consumeItemToTeleport", false);

        REQUIRED_ITEM_TO_TELEPORT = BUILDER
                .comment("Item that needs to be held in order for the teleporter to work")
                .define("requiredItemToTeleport", "minecraft:stone_pickaxe");

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {

    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String;
    }
}
