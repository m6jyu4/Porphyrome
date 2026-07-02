package com.m6jyu4.rome.block;

import com.m6jyu4.rome.blockentity.ForumBlockEntity;
import com.m6jyu4.rome.blockentity.PalaceBlockEntity;
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

public class ForumBlock extends ConstructableBlock {

    public ForumBlock(Properties properties) {
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

            // Check if this location is already inside an existing city
            if (manager.getCityAt(pos) == null) {
                String cityName = placer != null ? placer.getName().getString() + "'s Colonia" : "New Rome";
                manager.registerCity(cityName, pos);

                // Get the newly created city so we can read its radius
                City newCity = manager.getCityAt(pos);

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

            // Get the city from the global manager
            CityManager manager = CityManager.get((ServerLevel) level);
            City city = manager.getCityAt(pos);

            if (city != null) {
                // Open the menu, and WRITE the custom data to the network buffer!
                List<ForumMenu.BuildingEntry> activeBuildings = new ArrayList<>();
                for (BlockPos bPos : city.getBuildings()) {
                    BlockState bState = level.getBlockState(bPos);
                    if (bState.getBlock() instanceof ConstructableBlock) {
                        activeBuildings.add(new ForumMenu.BuildingEntry(bPos, new ItemStack(bState.getBlock())));
                    }
                }

                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new ForumMenu(id, inv, city, activeBuildings),
                                Component.literal(city.getName())
                        ),
                        buf -> {
                            buf.writeUtf(city.getName());
                            buf.writeBlockPos(city.getForumPos()); // NEW: Must match constructor order!
                            buf.writeInt(city.getTier());
                            buf.writeInt(city.getRadius());

                            buf.writeInt(activeBuildings.size());
                            for (ForumMenu.BuildingEntry entry : activeBuildings) {
                                buf.writeBlockPos(entry.pos());
                                ItemStack.STREAM_CODEC.encode(buf, entry.icon());
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
