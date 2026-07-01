package com.m6jyu4.rome.blockentity;

import com.m6jyu4.rome.gui.Workstation.WorkstationMenu;
import com.m6jyu4.rome.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WorkstationBlockEntity extends BlockEntity implements MenuProvider {

    public WorkstationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.WORKSTATION_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.rome.workstation");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new WorkstationMenu(id, playerInventory, ContainerLevelAccess.create(this.level, this.worldPosition));
    }

}
