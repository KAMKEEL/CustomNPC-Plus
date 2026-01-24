package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual inventory for displaying auction listings and claims.
 * This inventory holds ItemStacks from AuctionListings/Claims for display purposes.
 */
public class InventoryAuctionDisplay implements IInventory {
    private final ItemStack[] items;
    private final List<AuctionListing> listings;
    private final List<AuctionClaim> claims;
    private final int size;

    public InventoryAuctionDisplay(int size) {
        this.size = size;
        this.items = new ItemStack[size];
        this.listings = new ArrayList<>(size);
        this.claims = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            listings.add(null);
            claims.add(null);
        }
    }

    public void setListing(int slot, AuctionListing listing) {
        if (slot >= 0 && slot < size) {
            listings.set(slot, listing);
            claims.set(slot, null);
            items[slot] = listing != null ? listing.item : null;
        }
    }

    public AuctionListing getListing(int slot) {
        if (slot >= 0 && slot < size) {
            return listings.get(slot);
        }
        return null;
    }

    /**
     * Set a claim item in the inventory.
     * For ITEM claims, shows the item.
     * For CURRENCY/REFUND claims, shows a gold coin as visual representation.
     */
    public void setClaimItem(int slot, AuctionClaim claim) {
        if (slot >= 0 && slot < size) {
            claims.set(slot, claim);
            listings.set(slot, null);
            if (claim != null) {
                if (claim.type == EnumClaimType.ITEM && claim.item != null) {
                    items[slot] = claim.item;
                } else {
                    // For currency claims, show a gold coin
                    items[slot] = new ItemStack(CustomItems.coinGold);
                }
            } else {
                items[slot] = null;
            }
        }
    }

    public AuctionClaim getClaim(int slot) {
        if (slot >= 0 && slot < size) {
            return claims.get(slot);
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            items[i] = null;
            listings.set(i, null);
            claims.set(i, null);
        }
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < size) {
            return items[slot];
        }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return null; // Display only
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot >= 0 && slot < size) {
            items[slot] = stack;
        }
    }

    @Override
    public String getInventoryName() {
        return "Auction Display";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return false;
    }
}
