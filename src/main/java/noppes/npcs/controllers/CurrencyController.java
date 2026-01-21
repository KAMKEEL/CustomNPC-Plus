package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.controllers.data.PlayerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Controller for managing currency operations including:
 * - Claims system (for auction refunds, NPC payments, etc.)
 * - Transaction history
 * - Currency administration
 *
 * Delegates actual balance operations to VaultHelper.
 */
public class CurrencyController {
    private static final Logger logger = LogManager.getLogger(CustomNpcs.class);

    public static CurrencyController Instance;

    // Pending claims per player UUID - claims that can be collected
    private Map<UUID, List<CurrencyClaim>> pendingClaims = new HashMap<>();

    // File path for persistence
    private String filePath = "";

    public CurrencyController() {
        Instance = this;
        load();
    }

    /**
     * Get player's current balance
     */
    public long getBalance(EntityPlayer player) {
        return VaultHelper.Instance.getBalance(player);
    }

    /**
     * Deposit currency directly to player's balance
     */
    public boolean deposit(EntityPlayer player, long amount) {
        return VaultHelper.Instance.deposit(player, amount);
    }

    /**
     * Withdraw currency from player's balance
     */
    public boolean withdraw(EntityPlayer player, long amount) {
        return VaultHelper.Instance.withdraw(player, amount);
    }

    /**
     * Check if player can afford an amount
     */
    public boolean canAfford(EntityPlayer player, long amount) {
        return VaultHelper.Instance.canAfford(player, amount);
    }

    /**
     * Transfer currency between players
     */
    public boolean transfer(EntityPlayer from, EntityPlayer to, long amount) {
        return VaultHelper.Instance.transfer(from, to, amount);
    }

    /**
     * Format amount for display
     */
    public String formatAmount(long amount) {
        return VaultHelper.Instance.formatAmount(amount);
    }

    // ==================== CLAIMS SYSTEM ====================

    /**
     * Add a claim for a player (e.g., auction refund, NPC payment)
     * Claims can be collected later by the player.
     *
     * @param playerUUID The player's UUID
     * @param amount     The amount to claim
     * @param source     Description of where this claim came from
     * @param sourceId   Optional ID of the source (e.g., auction ID)
     */
    public void addClaim(UUID playerUUID, long amount, String source, String sourceId) {
        if (amount <= 0) {
            return;
        }

        CurrencyClaim claim = new CurrencyClaim();
        claim.amount = amount;
        claim.source = source;
        claim.sourceId = sourceId;
        claim.timestamp = System.currentTimeMillis();
        claim.expiresAt = claim.timestamp + (ConfigMarket.ClaimExpirationDays * 24L * 60L * 60L * 1000L);

        List<CurrencyClaim> claims = pendingClaims.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        claims.add(claim);

        save();
        logger.info("Added claim of {} for player {} from {}", amount, playerUUID, source);
    }

    /**
     * Get all pending claims for a player
     */
    public List<CurrencyClaim> getPendingClaims(EntityPlayer player) {
        return getPendingClaims(player.getUniqueID());
    }

    /**
     * Get all pending claims for a player by UUID
     */
    public List<CurrencyClaim> getPendingClaims(UUID playerUUID) {
        List<CurrencyClaim> claims = pendingClaims.get(playerUUID);
        if (claims == null) {
            return new ArrayList<>();
        }

        // Filter out expired claims
        long now = System.currentTimeMillis();
        claims.removeIf(claim -> claim.expiresAt > 0 && claim.expiresAt < now);

        return new ArrayList<>(claims);
    }

    /**
     * Get total pending claim amount for a player
     */
    public long getTotalPendingClaims(EntityPlayer player) {
        return getTotalPendingClaims(player.getUniqueID());
    }

    /**
     * Get total pending claim amount for a player by UUID
     */
    public long getTotalPendingClaims(UUID playerUUID) {
        List<CurrencyClaim> claims = getPendingClaims(playerUUID);
        long total = 0;
        for (CurrencyClaim claim : claims) {
            total += claim.amount;
        }
        return total;
    }

    /**
     * Claim all pending claims for a player
     * @return Amount successfully claimed
     */
    public long claimAll(EntityPlayer player) {
        List<CurrencyClaim> claims = getPendingClaims(player);
        if (claims.isEmpty()) {
            return 0;
        }

        long totalClaimed = 0;

        for (CurrencyClaim claim : claims) {
            if (deposit(player, claim.amount)) {
                totalClaimed += claim.amount;
            }
        }

        // Clear claims after successful deposit
        pendingClaims.remove(player.getUniqueID());
        save();

        return totalClaimed;
    }

    /**
     * Claim a specific claim by index
     * @return true if successful
     */
    public boolean claimSingle(EntityPlayer player, int claimIndex) {
        List<CurrencyClaim> claims = pendingClaims.get(player.getUniqueID());
        if (claims == null || claimIndex < 0 || claimIndex >= claims.size()) {
            return false;
        }

        CurrencyClaim claim = claims.get(claimIndex);
        if (deposit(player, claim.amount)) {
            claims.remove(claimIndex);
            if (claims.isEmpty()) {
                pendingClaims.remove(player.getUniqueID());
            }
            save();
            return true;
        }

        return false;
    }

    /**
     * Check if player has any pending claims
     */
    public boolean hasPendingClaims(EntityPlayer player) {
        return !getPendingClaims(player).isEmpty();
    }

    // ==================== PERSISTENCE ====================

    public void load() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }

        filePath = saveDir.getAbsolutePath();
        pendingClaims.clear();

        try {
            File file = new File(saveDir, "currency_claims.dat");
            if (file.exists()) {
                loadFromFile(file);
            }
        } catch (Exception e) {
            logger.error("Error loading currency claims", e);
            try {
                File file = new File(saveDir, "currency_claims.dat_old");
                if (file.exists()) {
                    loadFromFile(file);
                }
            } catch (Exception ee) {
                logger.error("Error loading backup currency claims", ee);
            }
        }

        // Clean expired claims
        cleanExpiredClaims();
    }

    private void loadFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
        fis.close();

        NBTTagList claimsList = compound.getTagList("Claims", 10);
        for (int i = 0; i < claimsList.tagCount(); i++) {
            NBTTagCompound playerCompound = claimsList.getCompoundTagAt(i);
            UUID playerUUID = UUID.fromString(playerCompound.getString("UUID"));

            List<CurrencyClaim> playerClaims = new ArrayList<>();
            NBTTagList playerClaimsList = playerCompound.getTagList("PlayerClaims", 10);
            for (int j = 0; j < playerClaimsList.tagCount(); j++) {
                NBTTagCompound claimCompound = playerClaimsList.getCompoundTagAt(j);
                CurrencyClaim claim = new CurrencyClaim();
                claim.readFromNBT(claimCompound);
                playerClaims.add(claim);
            }

            if (!playerClaims.isEmpty()) {
                pendingClaims.put(playerUUID, playerClaims);
            }
        }

        logger.info("Loaded {} player claim records", pendingClaims.size());
    }

    public void save() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }

        try {
            File file = new File(saveDir, "currency_claims.dat");
            File backup = new File(saveDir, "currency_claims.dat_old");

            if (file.exists()) {
                if (backup.exists()) {
                    backup.delete();
                }
                file.renameTo(backup);
            }

            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList claimsList = new NBTTagList();

            for (Map.Entry<UUID, List<CurrencyClaim>> entry : pendingClaims.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    continue;
                }

                NBTTagCompound playerCompound = new NBTTagCompound();
                playerCompound.setString("UUID", entry.getKey().toString());

                NBTTagList playerClaimsList = new NBTTagList();
                for (CurrencyClaim claim : entry.getValue()) {
                    NBTTagCompound claimCompound = new NBTTagCompound();
                    claim.writeToNBT(claimCompound);
                    playerClaimsList.appendTag(claimCompound);
                }
                playerCompound.setTag("PlayerClaims", playerClaimsList);

                claimsList.appendTag(playerCompound);
            }

            compound.setTag("Claims", claimsList);

            file = new File(saveDir, "currency_claims.dat");
            FileOutputStream fos = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(compound, fos);
            fos.close();
        } catch (Exception e) {
            logger.error("Error saving currency claims", e);
        }
    }

    /**
     * Clean up expired claims
     */
    public void cleanExpiredClaims() {
        long now = System.currentTimeMillis();
        boolean changed = false;

        Iterator<Map.Entry<UUID, List<CurrencyClaim>>> it = pendingClaims.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, List<CurrencyClaim>> entry = it.next();
            List<CurrencyClaim> claims = entry.getValue();

            int before = claims.size();
            claims.removeIf(claim -> claim.expiresAt > 0 && claim.expiresAt < now);

            if (claims.size() != before) {
                changed = true;
            }

            if (claims.isEmpty()) {
                it.remove();
            }
        }

        if (changed) {
            save();
        }
    }

    // ==================== CLAIM DATA CLASS ====================

    public static class CurrencyClaim {
        public long amount;
        public String source;       // e.g., "Auction Refund", "NPC Payment", "Quest Reward"
        public String sourceId;     // e.g., auction ID
        public long timestamp;      // When the claim was created
        public long expiresAt;      // When the claim expires (0 = never)

        public void writeToNBT(NBTTagCompound compound) {
            compound.setLong("Amount", amount);
            compound.setString("Source", source != null ? source : "");
            compound.setString("SourceId", sourceId != null ? sourceId : "");
            compound.setLong("Timestamp", timestamp);
            compound.setLong("ExpiresAt", expiresAt);
        }

        public void readFromNBT(NBTTagCompound compound) {
            amount = compound.getLong("Amount");
            source = compound.getString("Source");
            sourceId = compound.getString("SourceId");
            timestamp = compound.getLong("Timestamp");
            expiresAt = compound.getLong("ExpiresAt");
        }

        /**
         * Get formatted time remaining until expiration
         */
        public String getTimeRemaining() {
            if (expiresAt <= 0) {
                return "Never";
            }

            long remaining = expiresAt - System.currentTimeMillis();
            if (remaining <= 0) {
                return "Expired";
            }

            long days = remaining / (24L * 60L * 60L * 1000L);
            long hours = (remaining % (24L * 60L * 60L * 1000L)) / (60L * 60L * 1000L);

            if (days > 0) {
                return days + " days, " + hours + " hours";
            } else if (hours > 0) {
                return hours + " hours";
            } else {
                long minutes = remaining / (60L * 1000L);
                return minutes + " minutes";
            }
        }
    }
}
