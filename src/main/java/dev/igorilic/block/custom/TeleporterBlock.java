package dev.igorilic.block.custom;

import dev.igorilic.Config;
import dev.igorilic.worldgen.dimension.ModDimensions;
import dev.igorilic.worldgen.portal.ModTeleporter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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
                    pPlayer.sendSystemMessage(
                            Component.translatable("messages.projectflattenedutilities.item_required_but_not_specified")
                                    .plainCopy()
                                    .withStyle(ChatFormatting.RED)
                    );
                }
                return InteractionResult.CONSUME;
            }

            ResourceLocation requiredItemId = ResourceLocation.tryParse(requiredItemName);
            if (requiredItemId == null || !ForgeRegistries.ITEMS.containsKey(requiredItemId)) {
                LOGGER.error("Invalid item ID in REQUIRED_ITEM_TO_TELEPORT: {}", requiredItemName);
                if (!pLevel.isClientSide) {
                    pPlayer.sendSystemMessage(
                            Component.translatable("messages.projectflattenedutilities.cant_find_required_item", requiredItemName)
                                    .plainCopy()
                                    .withStyle(ChatFormatting.RED)
                    );
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
                    MutableComponent component = Component.translatable("tooltip.projectflattenedutilities.teleporter_block_required_item", requiredItem.getDescription().getString())
                            .plainCopy()
                            .withStyle(ChatFormatting.YELLOW);

                    pPlayer.sendSystemMessage(component);
                }
                return InteractionResult.CONSUME;
            }

            if (Config.CONSUME_ITEM_TO_TELEPORT.get()) {
                if (pPlayer.getMainHandItem().is(requiredItem)) {
                    LOGGER.info("Consumed item {} in main hand", requiredItemName);
                    pPlayer.getMainHandItem().shrink(1);
                }

                if (pPlayer.getOffhandItem().is(requiredItem)) {
                    LOGGER.info("Consumed item {} in off hand", requiredItemName);
                    pPlayer.getMainHandItem().shrink(1);
                }
            }
        }

        handleTeleport(pPlayer, pPos);
        return InteractionResult.SUCCESS;
    }

    private void handleTeleport(Entity player, BlockPos pPos) {
        if (player.level() instanceof ServerLevel serverLevel) {
            MinecraftServer minecraftserver = serverLevel.getServer();
            ResourceKey<Level> resourcekey = player.level().dimension() == ModDimensions.PFDIM_LEVEL_KEY ? ModDimensions.PFDIM_VOID_LEVEL_KEY : ModDimensions.PFDIM_LEVEL_KEY;

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if (this.requiresItem) {
            MutableComponent component = Component.translatable("tooltip.projectflattenedutilities.teleporter_block_required_item", requiredItem.getDescription().getString())
                    .plainCopy()
                    .withStyle(ChatFormatting.YELLOW);

            pTooltip.add(component);

            if (Config.CONSUME_ITEM_TO_TELEPORT.get()) {
                MutableComponent warningComponent = Component.translatable("tooltip.projectflattenedutilities.teleporter_block_consumes_item")
                        .plainCopy()
                        .withStyle(ChatFormatting.RED);
                pTooltip.add(warningComponent);
            }
        }
    }
}
