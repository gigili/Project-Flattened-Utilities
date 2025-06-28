package dev.igorilic.worldgen.dimension;

import dev.igorilic.projectflattenedutilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ModDimensions {
    public static final ResourceKey<Level> PFDIM_VOID_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static final ResourceKey<DimensionType> PFDIM_VOID_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(projectflattenedutilities.MOD_ID, "void"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {

    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {

    }
}
