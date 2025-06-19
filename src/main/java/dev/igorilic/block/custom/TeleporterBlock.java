package dev.igorilic.block.custom;

import dev.igorilic.Config;
import dev.igorilic.worldgen.dimension.ModDimensions;
import dev.igorilic.worldgen.portal.ModTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TeleporterBlock extends Block {
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean requiresItem = false;
    private Item requiredItem = null;

    public TeleporterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pPlayer.canChangeDimensions()) {
            return InteractionResult.CONSUME;
        }

        // Check config for item requirement
        if (Config.REQUIRES_ITEM_TO_TELEPORT.get()) {
            String requiredItemName = Config.REQUIRED_ITEM_TO_TELEPORT.get();

            if (requiredItemName.isEmpty()) {
                LOGGER.error("REQUIRED_ITEM_TO_TELEPORT config is empty. Cannot teleport.");
                if (!pLevel.isClientSide) {
                    pPlayer.sendSystemMessage(Component.literal("Teleportation requires an item to be held, but that items wasn't specified"));
                }
                return InteractionResult.CONSUME;
            }

            ResourceLocation requiredItemId = ResourceLocation.tryParse(requiredItemName);
            if (requiredItemId == null || !ForgeRegistries.ITEMS.containsKey(requiredItemId)) {
                LOGGER.error("Invalid item ID in REQUIRED_ITEM_TO_TELEPORT: {}", requiredItemName);
                if (!pLevel.isClientSide) {
                    pPlayer.sendSystemMessage(Component.literal("Can't find the specified Item in config. Item required: " + requiredItemName));
                }
                return InteractionResult.CONSUME;
            }

            Item requiredItem = ForgeRegistries.ITEMS.getValue(requiredItemId);

            if (requiredItem == null) {
                LOGGER.error("requiredItem IS NULL: {}", requiredItemName);
                return InteractionResult.CONSUME;
            }

            this.requiresItem = true;
            this.requiredItem = requiredItem;

            boolean hasItemInHand = pPlayer.getMainHandItem().is(requiredItem) || pPlayer.getOffhandItem().is(requiredItem);

            if (!hasItemInHand) {
                LOGGER.info("Player does not hold required item ({}) in either hand", requiredItemName);
                if (!pLevel.isClientSide) {
                    pPlayer.sendSystemMessage(Component.literal("You need to hold " + requiredItem.getDescription().getString() + " in your hand to teleport"));
                }
                return InteractionResult.CONSUME;
            }
        }

        handleTeleport(pPlayer, pPos);
        return InteractionResult.SUCCESS;
    }

    private void handleTeleport(Entity player, BlockPos pPos) {
        if (player.level() instanceof ServerLevel serverLevel) {
            MinecraftServer minecraftserver = serverLevel.getServer();
            ResourceKey<Level> resourcekey = player.level().dimension() == ModDimensions.PFDIM_LEVEL_KEY ? Level.OVERWORLD : ModDimensions.PFDIM_LEVEL_KEY;

            ServerLevel portalDimension = minecraftserver.getLevel(resourcekey);
            if (portalDimension != null && !player.isPassenger()) {
                if (resourcekey == ModDimensions.PFDIM_LEVEL_KEY) {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos, true));
                } else {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos, false));
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable BlockGetter pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        if (this.requiresItem) {
            pTooltip.add(Component.literal("You need to hold a " + requiredItem.getDescription().getString() + " in your hand to teleport"));
        }
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
