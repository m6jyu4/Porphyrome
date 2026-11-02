package com.m6jyu4.rome.block;


import com.m6jyu4.rome.util.Localisation;
import com.m6jyu4.rome.blockentity.ForumBlockEntity;
import com.m6jyu4.rome.city.City;
import com.m6jyu4.rome.city.CityData;
import com.m6jyu4.rome.city.CityManager;
import com.m6jyu4.rome.gui.forum.ForumMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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

import java.util.List;

public class ForumBlock extends ConstructableBlock {

    private static final Component hasCityError    = Component.translatable("city.rome.has_city").withColor(ChatFormatting.RED.getColor());
    private static final Component ruinedCity      = Component.translatable("city.rome.ruined_city").withColor(ChatFormatting.RED.getColor());
    private static final String possessiveCity     = "city.rome.name"; //
    private static final String cityAnnexBuildings = "city.rome.annex_building_count"; //.withColor

    public ForumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ForumBlockEntity(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);

        if(level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        /* IS PLACED INSIDE CITY? */
        CityManager cityManager = CityManager.get(serverLevel);
        boolean hasCity = cityManager.getCityAt(blockPos) != null;
        if(hasCity){
            if (livingEntity instanceof Player player) player.displayClientMessage(hasCityError, true);
            return;
        }

        /* CREATE NEW CITY */
        java.util.UUID ownerUUID = livingEntity instanceof Player player ? player.getUUID() : null;
        City newCity = cityManager.foundNewCity(createNameForCity(livingEntity, itemStack), blockPos, ownerUUID);

        /* MESSAGE FOR ANNEXING BUILDINGS */
        int buildingsFound = cityManager.annexPreExistingBuildings(newCity, serverLevel);
        if (livingEntity instanceof Player player) player.displayClientMessage(Localisation.translatable(cityAnnexBuildings, buildingsFound, livingEntity).withColor(ChatFormatting.GOLD.getColor()), true);

    }

    private String createNameForCity(LivingEntity blockPlacer, ItemStack forumItem){
        String cityName = Component.translatable("block.rome.forum").getString();

        if(blockPlacer != null) cityName = Component.translatable(possessiveCity, blockPlacer.getName().getString()).getString();
        if(forumItem.has(DataComponents.CUSTOM_NAME)) cityName = forumItem.getHoverName().getString();

        return cityName;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) CityManager.get(serverLevel).removeCity(pos);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        CityManager cityManager = CityManager.get(serverLevel);
        City thisCity = cityManager.getCityAt(pos);

        /* NO CITY? */
        if (thisCity == null) {
            serverPlayer.displayClientMessage(ruinedCity, true);
            return InteractionResult.SUCCESS;
        }

        /* OPEN CITY MENU */
        List<ForumMenu.BuildingEntry> displayBuildings = cityManager.getMenuBuildingEntries(thisCity, serverLevel);
        openMenu(serverPlayer, thisCity, displayBuildings);

        return InteractionResult.SUCCESS;
    }

    private void openMenu(ServerPlayer serverPlayer, City thisCity, List<ForumMenu.BuildingEntry> displayBuildings) {

        CityData cityData = CityData.from(thisCity, displayBuildings);
        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (id, inv, p) -> new ForumMenu(id, inv, thisCity, displayBuildings),
                        Component.literal(thisCity.getName())
                ),
                buf -> CityData.write(buf, cityData)
        );
    }
}