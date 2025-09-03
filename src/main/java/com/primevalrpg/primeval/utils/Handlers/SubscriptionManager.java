package com.primevalrpg.primeval.utils.Handlers;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.utils.API.LicenseApiService;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;

public class SubscriptionManager {
    private static boolean premiumEnabled = false;

    /** Call this early in onEnable() */
    public static void initialize() {
        PrimevalRPG plugin = PrimevalRPG.getInstance();
        String token = plugin.getConfig()
                .getString("subscription.token", "");

        LicenseApiService licenseService = new LicenseApiService(token);
        premiumEnabled = licenseService.validateServerSubscription();

        if (premiumEnabled) {
            RPGLogger.get().info("[Subscription] Premium features enabled.");
        } else {
            RPGLogger.get().error("[Subscription] Invalid or missing token; premium features disabled.");
        }
    }

    /** Returns true if premium features (e.g. in-game GUI) should be active */
    public static boolean isPremiumEnabled() {
        return premiumEnabled;
    }
}