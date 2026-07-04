package com.m6jyu4.rome.block;

import com.m6jyu4.rome.blockentity.ForumBlockEntity;
import com.m6jyu4.rome.city.City;
import com.m6jyu4.rome.city.CityManager;
import com.m6jyu4.rome.gui.forum.ForumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ForumBlockBackup extends ConstructableBlock {

    public ForumBlockBackup(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ForumBlockEntity(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            CityManager manager = CityManager.get(serverLevel);

            if (manager.getCityAt(pos) == null) {
                // --- NEW: ANVIL NAMING LOGIC ---
                String cityName = placer != null ? placer.getName().getString() + "'s Colonia" : "New Rome";

                if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                    cityName = stack.getHoverName().getString();
                }

                manager.registerCity(cityName, pos);

                // Get the newly created city so we can read its radius
                City newCity = manager.getCityAt(pos);

                if (newCity != null && placer instanceof Player player) {
                    newCity.setOwnerUUID(player.getUUID()); // Record the Mayor!
                }

                if (newCity != null) {
                    int r = newCity.getRadius();
                    int buildingsFound = 0;

                    // PERFORMANCE: We limit the Y-scan to 64 blocks up and down so it doesn't
                    // cause a massive lag spike by scanning the entire vertical sky limit.
                    int minY = Math.max(serverLevel.getMinBuildHeight(), pos.getY() - 64);
                    int maxY = Math.min(serverLevel.getMaxBuildHeight() - 1, pos.getY() + 64);

                    BlockPos minPos = pos.offset(-r, 0, -r).atY(minY);
                    BlockPos maxPos = pos.offset(r, 0, r).atY(maxY);

                    // Scan the newly claimed area
                    for (BlockPos scanPos : BlockPos.betweenClosed(minPos, maxPos)) {
                        BlockState scanState = serverLevel.getBlockState(scanPos);

                        if (scanState.getBlock() instanceof ConstructableBlock) {
                            // --- NEW: ONLY ANNEX IF IT IS UNCLAIMED ---
                            if (manager.getCityOwningBuilding(scanPos) == null) {
                                newCity.addBuilding(scanPos.immutable());
                                buildingsFound++;
                            }
                        }
                    }

                    // Save the newly annexed buildings to the disk
                    manager.setDirty();

                    if (placer instanceof Player player) {
                        if (buildingsFound > 0) {
                            player.displayClientMessage(
                                    Component.literal("§6Colonia established! §eAnnexed " + buildingsFound + " existing buildings."),
                                    false
                            );
                        } else {
                            player.displayClientMessage(
                                    Component.literal("§6Colonia established! Grid coordinates registered."),
                                    false
                            );
                        }
                    }
                }
            } else {
                if (placer instanceof Player player) {
                    player.displayClientMessage(Component.literal("§cThis area is already claimed by an existing settlement!"), false);
                }
            }
        }
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                // Abandon the settlement
                CityManager.get(serverLevel).removeCity(pos);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel serverLevel = (ServerLevel) level;
            CityManager manager = CityManager.get(serverLevel);

            // NOTE: Use getCityExact to prevent overlapping border issues!
            City city = manager.getCityAt(pos);

            if (city != null) {
                int r = city.getRadius() + 1;
                List<ForumMenu.BuildingEntry> displayBuildings = new ArrayList<>();

                // Performance-optimized scan space around the Forum
                int minY = Math.max(serverLevel.getMinBuildHeight(), pos.getY() - 32);
                int maxY = Math.min(serverLevel.getMaxBuildHeight() - 1, pos.getY() + 32);

                BlockPos minPos = pos.offset(-r, 0, -r).atY(minY);
                BlockPos maxPos = pos.offset(r, 0, r).atY(maxY);

                // Scan all blocks within borders
                for (BlockPos scanPos : BlockPos.betweenClosed(minPos, maxPos)) {
                    BlockState bState = serverLevel.getBlockState(scanPos);

                    if (bState.getBlock() instanceof ConstructableBlock) {
                        BlockPos immutablePos = scanPos.immutable();

                        // Check who actually owns this building
                        City ownerCity = manager.getCityOwningBuilding(immutablePos);

                        ItemStack iconStack = new ItemStack(bState.getBlock());
                        if (iconStack.isEmpty()) {
                            iconStack = new ItemStack(net.minecraft.world.item.Items.BRICKS);
                        }

                        if (ownerCity != null) {
                            // 1. It is owned by a city (either us or a neighbor)
                            boolean isOwned = ownerCity.getForumPos().equals(city.getForumPos());
                            displayBuildings.add(new ForumMenu.BuildingEntry(
                                    immutablePos,
                                    iconStack,
                                    isOwned,
                                    ownerCity.getColour(),
                                    ownerCity.getForumPos(),
                                    ownerCity.isAllowAnnex()
                            ));
                        } else {
                            // 2. --- NEW: UNOWNED/NEUTRAL BUILDING ON THE BORDER ---
                            // If no city owns it, we display it as a neutral/foreign building (allows annexation by default)
                            displayBuildings.add(new ForumMenu.BuildingEntry(
                                    immutablePos,
                                    iconStack,
                                    false, // Not owned by us
                                    0x888888, // Neutral Grey color
                                    city.getForumPos(), // Temporary owner assignment so it draws
                                    true // Allows annexation
                            ));
                        }
                    }
                }

                // Open the Menu and write to the network buffer
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new ForumMenu(id, inv, city, displayBuildings),
                                Component.literal(city.getName())
                        ),
                        buf -> {
                            buf.writeUtf(city.getName());
                            buf.writeBlockPos(city.getForumPos());
                            buf.writeInt(city.getTier());
                            buf.writeInt(city.getRadius());

                            // --- FIX 2: Write missing variables (allowAnnex and ownerUUID) ---
                            buf.writeBoolean(city.isAllowAnnex());
                            if (city.getOwnerUUID() != null) {
                                buf.writeBoolean(true);
                                buf.writeUUID(city.getOwnerUUID());
                            } else {
                                buf.writeBoolean(false);
                            }

                            buf.writeInt(displayBuildings.size());
                            for (ForumMenu.BuildingEntry entry : displayBuildings) {
                                buf.writeBlockPos(entry.pos());

                                // --- FIX 3: Use OPTIONAL_STREAM_CODEC! ---
                                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, entry.icon());

                                buf.writeBoolean(entry.isOwned());
                                buf.writeInt(entry.color());
                                buf.writeBlockPos(entry.ownerForumPos());

                                // --- FIX 4: Write missing entry boolean ---
                                buf.writeBoolean(entry.allowAnnex());
                            }
                        }
                );
            } else {
                serverPlayer.displayClientMessage(Component.literal("§cThis Forum is ruined. Break and replace it to found a new city."), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

}
