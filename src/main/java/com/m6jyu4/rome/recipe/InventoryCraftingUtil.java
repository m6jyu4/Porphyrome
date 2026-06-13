package com.m6jyu4.rome.recipe;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

public class InventoryCraftingUtil {

    // 1. Check if the player has all required ingredients
    public static boolean hasIngredients(Inventory playerInv, List<SizedIngredient> requirements) {
        for (SizedIngredient requirement : requirements) {
            int foundCount = 0;
            for (int i = 0; i < playerInv.getContainerSize(); i++) {
                ItemStack stack = playerInv.getItem(i);
                if (requirement.ingredient().test(stack)) {
                    foundCount += stack.getCount();
                }
            }
            if (foundCount < requirement.count()) {
                return false; // Missing enough of this specific ingredient
            }
        }
        return true;
    }

    // 2. Consume the ingredients from the player's inventory
    public static void consumeIngredients(Inventory playerInv, List<SizedIngredient> requirements) {
        for (SizedIngredient requirement : requirements) {
            int amountNeeded = requirement.count();

            for (int i = 0; i < playerInv.getContainerSize(); i++) {
                if (amountNeeded <= 0) break; // Finished consuming this ingredient

                ItemStack stack = playerInv.getItem(i);
                if (requirement.ingredient().test(stack)) {
                    int amountToTake = Math.min(stack.getCount(), amountNeeded);
                    stack.shrink(amountToTake);
                    amountNeeded -= amountToTake;

                    if (stack.isEmpty()) {
                        playerInv.setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
