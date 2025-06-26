package dev.igorilic.worldgen.dimension;

import com.mojang.datafixers.util.Pair;
import dev.igorilic.projectflattenedutilities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.OptionalLong;

public class ModDimensions {
    public static final ResourceKey<LevelStem> PFDIM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "pfdim"));
    public static final ResourceKey<Level> PFDIM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "pfdim"));
    public static final ResourceKey<Level> PFDIM_VOID_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));
    public static final ResourceKey<DimensionType> PF_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "pfdim"));
    public static final ResourceKey<NoiseGeneratorSettings> PF_NOISE = ResourceKey.create(Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "pfdim"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(PF_DIM_TYPE, new DimensionType(
                OptionalLong.empty(), //of(12000), // fixedTime
                true, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                true, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                -64, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        /*NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.PLAINS)),
                noiseGenSettings.getOrThrow(PF_NOISE)
        );*/

        NoiseBasedChunkGenerator noiseBasedChunkGenerator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(
                                List.of(
                                        Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.PLAINS)),
                                        Pair.of(Climate.parameters(0.1F, 0.1F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.DARK_FOREST)),
                                        Pair.of(Climate.parameters(0.2F, 0.1F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.SUNFLOWER_PLAINS)),
                                        Pair.of(Climate.parameters(0.1F, 0.1F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.GROVE)),
                                        Pair.of(Climate.parameters(0.1F, 0.1F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.CHERRY_GROVE)),
                                        Pair.of(Climate.parameters(0.2F, 0.05F, 0.0F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.SWAMP)),
                                        Pair.of(Climate.parameters(0.2F, 0.09F, 0.0F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.MANGROVE_SWAMP)),
                                        Pair.of(Climate.parameters(0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.05F), biomeRegistry.getOrThrow(Biomes.STONY_PEAKS)),
                                        Pair.of(Climate.parameters(0.095F, 0.01F, 0.0F, 0.0F, 0.0F, 0.0F, 0.05F), biomeRegistry.getOrThrow(Biomes.FROZEN_PEAKS)),
                                        Pair.of(Climate.parameters(0.01F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.01F), biomeRegistry.getOrThrow(Biomes.DEEP_FROZEN_OCEAN))
                                )
                        )
                ), noiseGenSettings.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS));

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.PF_DIM_TYPE), noiseBasedChunkGenerator);

        /*NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.PLAINS)),
                noiseGenSettings.getOrThrow(PF_NOISE)
        );
        LevelStem stem = new LevelStem(dimTypes.getOrThrow(PF_DIM_TYPE), wrappedChunkGenerator);*/

        context.register(PFDIM_KEY, stem);
    }
}
