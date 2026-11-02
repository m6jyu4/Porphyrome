package com.m6jyu4.rome.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class Localisation {

    public static String getLanguageCode(LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.clientInformation().language();
        }
        return "en_us";
    }

    public static MutableComponent translatable(String baseKey, int count, LivingEntity livingEntity) {
        char form = PluralRules.getPluralForm(getLanguageCode(livingEntity), count);
        String fullKey = baseKey + "." + form;

        return Component.translatable(fullKey, count);
    }

    private static class PluralRules {

        public static char getPluralForm(String languageCode, int count) {
            if (count == 0) return 'z';
            return switch (languageCode.toLowerCase()) {
                case "zh_tw", "zh_cn" -> 'a';
                case "pl_pl"                   -> getPolishForm(count);
                default                        -> count == 1 ? 'a' : 'b'; // English default
            };
        }

        private static char getPolishForm(int count) {
            if (count == 1) return 'a';
            if (count % 10 >= 2 && count % 10 <= 4 && !(count % 100 >= 12 && count % 100 <= 14)) {
                return 'b'; // 2-4 form
            }
            return 'c'; // 5+ and teens
        }
    }
}
