package noppes.npcs.util;

import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;

/**
 * Shared formatting utilities for the auction system.
 * Consolidates duplicated format methods from GUI classes.
 */
public class AuctionFormatUtil {
    // Time constants in milliseconds
    public static final long MS_PER_SECOND = 1000L;
    public static final long MS_PER_MINUTE = 60L * MS_PER_SECOND;
    public static final long MS_PER_HOUR = 60L * MS_PER_MINUTE;
    public static final long MS_PER_DAY = 24L * MS_PER_HOUR;

    /**
     * Format currency amount with commas (e.g., 1000 -> "1,000").
     * Does NOT append currency name.
     */
    public static String formatCurrency(long amount) {
        if (amount < 1000) return "" + amount;
        StringBuilder sb = new StringBuilder();
        String str = "" + amount;
        int count = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) sb.insert(0, ',');
            sb.insert(0, str.charAt(i));
            count++;
        }
        return sb.toString();
    }

    /**
     * Format currency amount with commas and append currency name.
     * (e.g., 1000 -> "1,000 Gold")
     */
    public static String formatCurrencyWithName(long amount) {
        return formatCurrency(amount) + " " + AuctionClientConfig.getCurrencyName();
    }

    /**
     * Format time remaining as a compact string (e.g., "2d 5h 30m").
     * Uses localized "Ended" text when time is <= 0.
     */
    public static String formatTimeRemaining(long ms) {
        if (ms <= 0) return StatCollector.translateToLocal("auction.ended");

        long seconds = (ms / MS_PER_SECOND) % 60;
        long minutes = (ms / MS_PER_MINUTE) % 60;
        long hours = (ms / MS_PER_HOUR) % 24;
        long days = ms / MS_PER_DAY;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m");
        else sb.append(seconds).append("s");
        return sb.toString();
    }

    /**
     * Check if time remaining is urgent (less than 1 hour).
     */
    public static boolean isTimeUrgent(long ms) {
        return ms > 0 && ms < MS_PER_HOUR;
    }
}
