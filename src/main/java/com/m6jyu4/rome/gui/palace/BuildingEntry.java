package com.m6jyu4.rome.gui.palace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public record BuildingEntry(BlockPos pos, ItemStack icon, boolean isOwned, int color, BlockPos ownerForumPos, boolean allowAnnex){}
