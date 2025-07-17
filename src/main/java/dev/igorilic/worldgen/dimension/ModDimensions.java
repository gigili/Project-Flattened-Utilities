package dev.igorilic.worldgen.dimension;

import com.mojang.datafixers.util.Pair;
import dev.igorilic.projectflattenedutilities;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

public class ModDimensions {
    public static final ResourceKey<Level> PFDIM_VOID_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static final ResourceKey<DimensionType> PFDIM_VOID_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static final ResourceKey<LevelStem> PFDIM_VOID_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static final ResourceKey<NoiseGeneratorSettings> VOID_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static final ResourceKey<WorldPreset> VOID_WORLD_PRESET = ResourceKey.create(Registries.WORLD_PRESET,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static final ResourceKey<StructureSet> VOID_STRUCTURE_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "start_platform"));

    public static final ResourceKey<Structure> START_PLATFORM = ResourceKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "start_platform"));

    public static final ResourceKey<StructureTemplatePool> START_PLATFORM_POOL = ResourceKey.create(Registries.TEMPLATE_POOL,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "start_platform_pool"));


    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(PFDIM_VOID_DIMENSION_TYPE, new DimensionType(
                OptionalLong.empty(), //of(12000), // fixedTime
                true, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                true, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                -64, // minY
                384, // height
                384, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, true, ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.THE_VOID)),
                noiseGenSettings.getOrThrow(VOID_NOISE_SETTINGS)
        );

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.PFDIM_VOID_DIMENSION_TYPE), wrappedChunkGenerator);

        context.register(PFDIM_VOID_STEM_KEY, stem);
    }

    public static void bootstrapStructure(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(START_PLATFORM, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE
                ),
                pools.getOrThrow(START_PLATFORM_POOL),
                7, // max depth
                UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(0)), // start height
                false // expand bounds
        ));
    }

    public static void bootstrapStructurePool(BootstapContext<StructureTemplatePool> context) {
        ResourceKey<StructureTemplatePool> emptyKey = ResourceKey.create(
                Registries.TEMPLATE_POOL,
                ResourceLocation.fromNamespaceAndPath("minecraft", "empty")
        );

        Holder<StructureTemplatePool> fallback = context.lookup(Registries.TEMPLATE_POOL).getOrThrow(emptyKey);

        // Create the template pool with your element
        StructureTemplatePool pool = new StructureTemplatePool(
                fallback,
                List.of(Pair.of(SinglePoolElement.single("projectflattenedutilities:start_platform"), 1)), // element with weight 1
                StructureTemplatePool.Projection.RIGID
        );

        // Register the pool
        context.register(
                ResourceKey.create(
                        Registries.TEMPLATE_POOL,
                        ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "start_platform_pool")
                ),
                pool
        );
    }

    public static void bootstrapStructureSets(BootstapContext<StructureSet> context) {
        // Get the holder for your structure
        Holder<Structure> existingStructure = context.lookup(Registries.STRUCTURE)
                .getOrThrow(START_PLATFORM);

        // Register your structure set
        context.register(VOID_STRUCTURE_SET, new StructureSet(
                List.of(
                        new StructureSet.StructureSelectionEntry(existingStructure, 1)
                ),
                new RandomSpreadStructurePlacement(
                        10, // spacing
                        5,  // separation
                        RandomSpreadType.LINEAR,
                        123456 // salt
                )
        ));
    }

    public static void bootstrapNoiseSettings(BootstapContext<NoiseGeneratorSettings> context) {
        context.register(VOID_NOISE_SETTINGS, new NoiseGeneratorSettings(
                new NoiseSettings(
                        0, // minY
                        256, // height
                        2, // noiseSizeHorizontal
                        1 // noiseSizeVertical
                ),
                Blocks.AIR.defaultBlockState(), // defaultBlock
                Blocks.AIR.defaultBlockState(), // defaultFluid
                overworldNoiseWithoutStructures(),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)),
                                SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState())
                        )
                ),
                List.of(), // spawnTarget
                0, // seaLevel
                false, // disableMobGeneration
                false, // aquifersEnabled
                false, // oreVeinsEnabled
                false // useLegacyRandomSource
        ));
    }

    public static void bootstrapWorldPreset(BootstapContext<WorldPreset> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        HolderGetter<StructureSet> structureSets = context.lookup(Registries.STRUCTURE_SET);
        Holder<StructureSet> emptyStructures = structureSets.getOrThrow(VOID_STRUCTURE_SET);

        // Create flat level settings for overworld
        FlatLevelGeneratorSettings flatSettings = new FlatLevelGeneratorSettings(
                Optional.of(HolderSet.direct(emptyStructures)),
                biomes.getOrThrow(Biomes.THE_VOID),
                List.of()
        );
        flatSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.AIR));
        flatSettings.updateLayers();

        // Create level stems for each dimension
        LevelStem overworldStem = new LevelStem(
                dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                new FlatLevelSource(flatSettings)
        );

        LevelStem netherStem = new LevelStem(
                dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromPreset(
                                context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                                        .getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)
                        ),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER) // already a holder
                )
        );

        LevelStem endStem = new LevelStem(
                dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                new NoiseBasedChunkGenerator(
                        TheEndBiomeSource.create(biomes),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.END)
                )
        );

        // Create and register the world preset
        context.register(VOID_WORLD_PRESET, new WorldPreset(
                Map.of(
                        LevelStem.OVERWORLD, overworldStem,
                        LevelStem.NETHER, netherStem,
                        LevelStem.END, endStem
                )
        ));
    }

    private static NoiseRouter overworldNoiseWithoutStructures() {
        return new NoiseRouter(
                DensityFunctions.zero(), // barrierNoise
                DensityFunctions.zero(), // fluidLevelFloodednessNoise
                DensityFunctions.zero(), // fluidLevelSpreadNoise
                DensityFunctions.zero(), // lavaNoise
                DensityFunctions.zero(), // temperature
                DensityFunctions.zero(), // vegetation
                DensityFunctions.zero(), // continents
                DensityFunctions.zero(), // erosion
                DensityFunctions.zero(), // depth
                DensityFunctions.zero(), // ridges
                DensityFunctions.zero(), // initialDensityWithoutJaggedness
                DensityFunctions.zero(), // finalDensity
                DensityFunctions.zero(), // veinToggle
                DensityFunctions.zero(), // veinRidged
                DensityFunctions.zero()  // veinGap
        );
    }
}
