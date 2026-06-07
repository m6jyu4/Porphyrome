package com.m6jyu4.rome.capability;

import net.minecraft.core.BlockPos;

public interface INetworkNode {
    int getTier(); // 0 = Farm, 1 = House, 2 = Town, 3 = Kingdom, etc.

    BlockPos getParentPos();
    void setParentPos(BlockPos pos);

    void onChildUpdated();
    void dismantle(); // Cleanly breaks connections when destroyed
}