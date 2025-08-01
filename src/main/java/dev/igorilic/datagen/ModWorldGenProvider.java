package dev.igorilic.datagen;

import dev.igorilic.projectflattenedutilities;
import dev.igorilic.worldgen.dimension.ModDimensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.TEMPLATE_POOL, ModDimensions::bootstrapStructurePool)
            .add(Registries.STRUCTURE, ModDimensions::bootstrapStructure)
            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem)
            .add(Registries.NOISE_SETTINGS, ModDimensions::bootstrapNoiseSettings)
            .add(Registries.STRUCTURE_SET, ModDimensions::bootstrapStructureSets)
            .add(Registries.WORLD_PRESET, ModDimensions::bootstrapWorldPreset);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(projectflattenedutilities.MOD_ID));
    }
}
