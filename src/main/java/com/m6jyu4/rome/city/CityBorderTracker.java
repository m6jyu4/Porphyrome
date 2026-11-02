package com.m6jyu4.rome.city;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CityBorderTracker {

    private record HorizontalPos(int x, int z) {}

    private static final Map<UUID, String> lastCityTracker            = new HashMap<>();
    private static final Map<UUID, HorizontalPos> lastPositionTracker = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;

        UUID uuid = player.getUUID();
        BlockPos currentPos = player.blockPosition();
        HorizontalPos lastHorizontalPos = lastPositionTracker.get(uuid);

        if (lastHorizontalPos != null &&
                lastHorizontalPos.x() == currentPos.getX() &&
                lastHorizontalPos.z() == currentPos.getZ()) {
            return;
        }

        // Update horizontal position cache
        lastPositionTracker.put(uuid, new HorizontalPos(currentPos.getX(), currentPos.getZ()));

        ServerLevel level = (ServerLevel) player.level();
        CityManager manager = CityManager.get(level);

        // Fetch the city at their horizontal coordinate
        City currentCity = manager.getCityAt(currentPos);
        String currentCityName = (currentCity != null) ? currentCity.getName() : null;
        String lastCityName = lastCityTracker.get(uuid);
        if (!Objects.equals(currentCityName, lastCityName)) {
            if (currentCityName != null) {
                player.displayClientMessage(Component.literal("§e" + currentCityName), true);
            } else if (lastCityName != null) {
                player.displayClientMessage(Component.literal("Wilderness"), true);
            }
            lastCityTracker.put(uuid, currentCityName);
        }
    }

}
