package com.m6jyu4.rome.recipe;

import com.m6jyu4.rome.gui.Workstation.WorkstationMenu;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

public class RecipeResultSlot extends Slot {
    private final WorkstationMenu menu;

    public RecipeResultSlot(WorkstationMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        WorkstationRecipe recipe = menu.getSelectedRecipe();
        if (recipe == null) return false;

        return InventoryCraftingUtil.hasIngredients(player.getInventory(), recipe.sizedIngredients());
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (!player.level().isClientSide) {
            WorkstationRecipe recipe = menu.getSelectedRecipe();
            if (recipe != null) {

                // 2. Consume the ingredients
                InventoryCraftingUtil.consumeIngredients(player.getInventory(), recipe.sizedIngredients());

                // 3. Save inventory changes
                player.getInventory().setChanged();


                // 5. Manually force the single clean update
                menu.updateOutputSlot();

                // --- NEW: PLAY CRAFTING SOUND ON THE SERVER ---
                // We play the sound on the server side so everyone nearby can hear the crafting action!
                // Using the Anvil Use sound for a satisfying "hammering" impact.
                player.level().playSound(
                        null, // Target player (null means everyone around the block hears it)
                        player.blockPosition(), // Where the sound originates (the player's position near the block)
                        SoundEvents.VILLAGER_WORK_FLETCHER, // The Vanilla Sound event
                        net.minecraft.sounds.SoundSource.BLOCKS, // Sound Category
                        0.3F, // Volume (0.0 to 1.0)
                        1.0F  // Pitch (1.0 is normal, higher is faster/squeakier)
                );
            }
        }
        super.onTake(player, stack);
    }

}

