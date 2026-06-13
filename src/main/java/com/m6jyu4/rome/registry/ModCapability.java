package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.capability.INetworkNode;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapability {
    public static final BlockCapability<INetworkNode, Direction> NETWORK_NODE =
            BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(Constant.MOD_ID, "network_node"), INetworkNode.class);

//    public static final BlockCapability<IFoodProvider, @Nullable Direction> FOOD_PROVIDER =
//            BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(Constant.MOD_ID, "food_provider"), IFoodProvider.class);
//
//    public static final BlockCapability<IPietyProvider, @Nullable Direction> PIETY_PROVIDER =
//            BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(Constant.MOD_ID, "piety_provider"), IPietyProvider.class);

    // Call this in your FML event bus subscriber
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(NETWORK_NODE, ModBlockEntity.FARM_BE.get(), (be, side) -> (INetworkNode) be);
//        event.registerBlockEntity(FOOD_PROVIDER, ModBlockEntity.FARM_BE.get(), (be, side) -> (IFoodProvider) be);
//        event.registerBlockEntity(PIETY_PROVIDER, ModBlockEntity.FARM_BE.get(), (be, side) -> (IPietyProvider) be);

//        event.registerBlockEntity(NETWORK_NODE, ModBlockEntity.HOUSE_BE.get(), (be, side) -> (INetworkNode) be);
//        event.registerBlockEntity(FOOD_PROVIDER, ModBlockEntity.HOUSE_BE.get(), (be, side) -> (IFoodProvider) be);
//        event.registerBlockEntity(PIETY_PROVIDER, ModBlockEntity.HOUSE_BE.get(), (be, side) -> (IPietyProvider) be);
//
//        event.registerBlockEntity(NETWORK_NODE, ModBlockEntity.TOWN_BE.get(), (be, side) -> (INetworkNode) be);
//        event.registerBlockEntity(FOOD_PROVIDER, ModBlockEntity.TOWN_BE.get(), (be, side) -> (IFoodProvider) be);
//        event.registerBlockEntity(PIETY_PROVIDER, ModBlockEntity.TOWN_BE.get(), (be, side) -> (IPietyProvider) be);
    }
}
