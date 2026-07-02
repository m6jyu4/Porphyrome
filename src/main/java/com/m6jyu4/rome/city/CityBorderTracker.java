package com.m6jyu4.rome.city;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// Listens to the standard Forge GAME bus
@EventBusSubscriber(modid = "rome", bus = EventBusSubscriber.Bus.GAME)
public class CityBorderTracker {

    // A temporary memory of where each player is currently standing
    private static final Map<UUID, String> lastCityTracker = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // 1. Optimization: Only run on the server, and only check once per second (every 20 ticks)
        if (player.level().isClientSide() || player.tickCount % 20 != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        CityManager manager = CityManager.get(level);

        // 2. Figure out which city the player is standing in right now
        City currentCity = manager.getCityAt(player.blockPosition());
        String currentCityName = (currentCity != null) ? currentCity.getName() : null;

        // 3. Look up where they were 1 second ago
        String lastCityName = lastCityTracker.get(player.getUUID());

        // 4. Did they cross a border?
        if (!Objects.equals(currentCityName, lastCityName)) {

            if (currentCityName != null) {
                // Entering a City
                player.displayClientMessage(Component.literal("§6§lEntering: §e" + currentCityName), true); // 'true' puts it on the Action Bar!
            } else if (lastCityName != null) {
                // Leaving a City
                player.displayClientMessage(Component.literal("§7Leaving " + lastCityName), true);
            }

            // 5. Update their memory
            lastCityTracker.put(player.getUUID(), currentCityName);
        }
    }
}
