package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.gui.WorkstationMenu;
import com.m6jyu4.rome.util.Constant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuType {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Constant.MOD_ID);

    // --- MENU ---
    public static final Supplier<MenuType<WorkstationMenu>> WORKSTATION_MENU = MENUS.register("workstation", () -> IMenuTypeExtension.create(WorkstationMenu::new));
}
