package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;

import java.util.ArrayList;
import java.util.List;

/**
 * Global auction management container.
 * Uses a 9x5 display grid plus staged/detail slots on the right panel and player inventory below the grid.
 */
public class ContainerManageAuction extends ContainerNpcInterface {
    public enum DisplayMode {
        LISTINGS,
        CLAIMS
    }

    public static final int DISPLAY_COLS = 9;
    public static final int DISPLAY_ROWS = 5;
    public static final int DISPLAY_SLOT_COUNT = DISPLAY_COLS * DISPLAY_ROWS;

    public static final int DISPLAY_X = 8;
    public static final int DISPLAY_Y = 16;

    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 113;
    public static final int HOTBAR_Y = 171;
    public static final int PLAYER_SLOT_COUNT = 36;

    public static final int CREATE_SLOT_X = 188;
    public static final int CREATE_SLOT_Y = 156;
    public static final int DETAIL_SLOT_X = 194;
    public static final int DETAIL_SLOT_Y = 56;
    private static final int MAX_STACK = 64;

    public static final int DISPLAY_SLOT_START = 0;
    public static final int PLAYER_SLOT_START = DISPLAY_SLOT_START + DISPLAY_SLOT_COUNT;
    public static final int DETAIL_SLOT_INDEX = PLAYER_SLOT_START + PLAYER_SLOT_COUNT;
    public static final int CREATE_SLOT_INDEX = DETAIL_SLOT_INDEX + 1;

    private static final int HIDDEN_SLOT_X = -1000;
    private static final int HIDDEN_SLOT_Y = -1000;

    public final InventoryAuctionDisplay displayInventory;
    private final IInventory detailInventory;
    private final IInventory createInventory;
    private DisplayMode displayMode = DisplayMode.LISTINGS;
    private Slot detailSlot;
    private Slot createSlot;
    private int hiddenDisplaySlot = -1;

    public ContainerManageAuction(EntityPlayer player) {
        super(player);
        this.player = player;

        displayInventory = new InventoryAuctionDisplay(DISPLAY_SLOT_COUNT);
        detailInventory = new InventoryBasic("GlobalAuctionDetail", false, 1);
        createInventory = new InventoryBasic("GlobalAuctionCreate", false, 1);

        // 9x5 display grid (listings / global claims)
        for (int row = 0; row < DISPLAY_ROWS; row++) {
            for (int col = 0; col < DISPLAY_COLS; col++) {
                int slotIndex = col + row * DISPLAY_COLS;
                int x = DISPLAY_X + col * 18;
                int y = DISPLAY_Y + row * 18;
                addSlotToContainer(new SlotAuctionDisplay(displayInventory, slotIndex, x, y));
            }
        }

        // Player inventory below display grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int playerIndex = col + row * 9 + 9;
                int x = PLAYER_INV_X + col * 18;
                int y = PLAYER_INV_Y + row * 18;
                addSlotToContainer(new Slot(player.inventory, playerIndex, x, y));
            }
        }
        for (int col = 0; col < 9; col++) {
            int x = PLAYER_INV_X + col * 18;
            addSlotToContainer(new Slot(player.inventory, col, x, HOTBAR_Y));
        }

        // Listing details slot (right panel)
        detailSlot = new SlotAuctionDisplay(detailInventory, 0, DETAIL_SLOT_X, DETAIL_SLOT_Y);
        addSlotToContainer(detailSlot);

        // Staged fake listing slot
        createSlot = new Slot(createInventory, 0, CREATE_SLOT_X, CREATE_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer player) {
                return false;
            }
        };
        addSlotToContainer(createSlot);

        setDetailSlotVisible(false);
        setCreateSlotVisible(false);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        return null;
    }

    @Override
    public ItemStack slotClick(int slotIndex, int mouseButton, int mode, EntityPlayer player) {
        // Block native container interactions. GUI handles all click behaviors.
        return null;
    }

    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode != null ? mode : DisplayMode.LISTINGS;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void clearDisplay() {
        displayInventory.clear();
    }

    public void setListingsPage(List<AuctionListing> listings) {
        displayMode = DisplayMode.LISTINGS;
        clearHiddenDisplaySlot();
        displayInventory.clear();
        if (listings == null) return;

        for (int i = 0; i < listings.size() && i < DISPLAY_SLOT_COUNT; i++) {
            displayInventory.setListing(i, listings.get(i));
        }
    }

    public void setClaimsPage(List<AuctionClaim> claims) {
        displayMode = DisplayMode.CLAIMS;
        clearHiddenDisplaySlot();
        displayInventory.clear();
        if (claims == null) return;

        for (int i = 0; i < claims.size() && i < DISPLAY_SLOT_COUNT; i++) {
            displayInventory.setClaimItem(i, claims.get(i));
        }
    }

    /**
     * Claims page can contain both global claims and active global listings.
     * Order mirrors My Trades:
     * sold claims, outbid claims, won claims, active listings, expired claims.
     */
    public void setClaimsAndListingsPage(List<AuctionClaim> claims, List<AuctionListing> listings) {
        displayMode = DisplayMode.CLAIMS;
        clearHiddenDisplaySlot();
        displayInventory.clear();

        List<AuctionClaim> soldClaims = new ArrayList<AuctionClaim>();
        List<AuctionClaim> outbidClaims = new ArrayList<AuctionClaim>();
        List<AuctionClaim> wonClaims = new ArrayList<AuctionClaim>();
        List<AuctionClaim> expiredClaims = new ArrayList<AuctionClaim>();
        if (claims != null) {
            for (AuctionClaim claim : claims) {
                if (claim == null) continue;
                if (claim.type == EnumClaimType.CURRENCY) {
                    soldClaims.add(claim);
                } else if (claim.type == EnumClaimType.REFUND) {
                    outbidClaims.add(claim);
                } else if (claim.type == EnumClaimType.ITEM) {
                    if (claim.isReturned) {
                        expiredClaims.add(claim);
                    } else {
                        wonClaims.add(claim);
                    }
                } else {
                    wonClaims.add(claim);
                }
            }
        }

        int slot = 0;
        slot = fillClaims(slot, soldClaims);
        slot = fillClaims(slot, outbidClaims);
        slot = fillClaims(slot, wonClaims);

        if (listings != null) {
            for (int i = 0; i < listings.size() && slot < DISPLAY_SLOT_COUNT; i++) {
                displayInventory.setListing(slot, listings.get(i));
                slot++;
            }
        }

        fillClaims(slot, expiredClaims);
    }

    private int fillClaims(int slot, List<AuctionClaim> claims) {
        if (claims == null) return slot;
        for (int i = 0; i < claims.size() && slot < DISPLAY_SLOT_COUNT; i++) {
            displayInventory.setClaimItem(slot, claims.get(i));
            slot++;
        }
        return slot;
    }

    public boolean isDisplaySlot(int containerSlotIndex) {
        return containerSlotIndex >= DISPLAY_SLOT_START && containerSlotIndex < DISPLAY_SLOT_START + DISPLAY_SLOT_COUNT;
    }

    public int toDisplayIndex(int containerSlotIndex) {
        if (!isDisplaySlot(containerSlotIndex)) return -1;
        return containerSlotIndex - DISPLAY_SLOT_START;
    }

    public AuctionListing getListingAtDisplay(int displayIndex) {
        if (displayIndex < 0 || displayIndex >= DISPLAY_SLOT_COUNT) return null;
        return displayInventory.getListing(displayIndex);
    }

    public AuctionClaim getClaimAtDisplay(int displayIndex) {
        if (displayIndex < 0 || displayIndex >= DISPLAY_SLOT_COUNT) return null;
        return displayInventory.getClaim(displayIndex);
    }

    public boolean isPlayerSlot(int containerSlotIndex) {
        return containerSlotIndex >= PLAYER_SLOT_START && containerSlotIndex < PLAYER_SLOT_START + PLAYER_SLOT_COUNT;
    }

    public boolean isCreateSlot(int containerSlotIndex) {
        return containerSlotIndex == CREATE_SLOT_INDEX;
    }

    public boolean isDetailSlot(int containerSlotIndex) {
        return containerSlotIndex == DETAIL_SLOT_INDEX;
    }

    public void setDetailItem(ItemStack stack) {
        detailInventory.setInventorySlotContents(0, stack != null ? stack.copy() : null);
    }

    public void clearDetailItem() {
        detailInventory.setInventorySlotContents(0, null);
    }

    public void setDetailSlotVisible(boolean visible) {
        if (detailSlot == null) return;
        detailSlot.xDisplayPosition = visible ? DETAIL_SLOT_X : HIDDEN_SLOT_X;
        detailSlot.yDisplayPosition = visible ? DETAIL_SLOT_Y : HIDDEN_SLOT_Y;
    }

    public void setCreateSlotVisible(boolean visible) {
        if (createSlot == null) return;
        createSlot.xDisplayPosition = visible ? CREATE_SLOT_X : HIDDEN_SLOT_X;
        createSlot.yDisplayPosition = visible ? CREATE_SLOT_Y : HIDDEN_SLOT_Y;
    }

    public void setHiddenDisplaySlot(int displayIndex) {
        if (displayIndex < 0 || displayIndex >= DISPLAY_SLOT_COUNT) {
            clearHiddenDisplaySlot();
            return;
        }
        if (hiddenDisplaySlot == displayIndex) return;

        clearHiddenDisplaySlot();
        hiddenDisplaySlot = displayIndex;
        displayInventory.setInventorySlotContents(displayIndex, null);
    }

    public void clearHiddenDisplaySlot() {
        if (hiddenDisplaySlot < 0 || hiddenDisplaySlot >= DISPLAY_SLOT_COUNT) {
            hiddenDisplaySlot = -1;
            return;
        }
        restoreDisplaySlot(hiddenDisplaySlot);
        hiddenDisplaySlot = -1;
    }

    public int getHiddenDisplaySlot() {
        return hiddenDisplaySlot;
    }

    private void restoreDisplaySlot(int displayIndex) {
        if (displayIndex < 0 || displayIndex >= DISPLAY_SLOT_COUNT) return;

        AuctionClaim claim = displayInventory.getClaim(displayIndex);
        if (claim != null) {
            displayInventory.setClaimItem(displayIndex, claim);
            return;
        }

        AuctionListing listing = displayInventory.getListing(displayIndex);
        if (listing != null) {
            displayInventory.setListing(displayIndex, listing);
            return;
        }

        displayInventory.setInventorySlotContents(displayIndex, null);
    }

    public ItemStack getCreateItem() {
        return createInventory.getStackInSlot(0);
    }

    public void clearCreateSlot() {
        createInventory.setInventorySlotContents(0, null);
    }

    public void addToCreateSlot(int containerPlayerSlot, boolean fullStack) {
        if (!isPlayerSlot(containerPlayerSlot)) return;

        Slot slot = (Slot) inventorySlots.get(containerPlayerSlot);
        if (slot == null) return;
        ItemStack source = slot.getStack();
        if (source == null) return;

        ItemStack staged = createInventory.getStackInSlot(0);
        int toAdd = fullStack ? source.stackSize : 1;

        if (staged == null) {
            ItemStack copy = source.copy();
            copy.stackSize = Math.min(MAX_STACK, toAdd);
            createInventory.setInventorySlotContents(0, copy);
            return;
        }

        if (!itemsMatch(staged, source)) {
            ItemStack copy = source.copy();
            copy.stackSize = Math.min(MAX_STACK, toAdd);
            createInventory.setInventorySlotContents(0, copy);
            return;
        }

        int space = MAX_STACK - staged.stackSize;
        int add = Math.min(space, toAdd);
        if (add > 0) {
            staged.stackSize += add;
        }
    }

    public void removeFromCreateSlot(boolean removeAll) {
        ItemStack staged = createInventory.getStackInSlot(0);
        if (staged == null) return;

        if (removeAll || staged.stackSize <= 1) {
            createInventory.setInventorySlotContents(0, null);
        } else {
            staged.stackSize--;
        }
    }

    public int countItemInPlayerInventory(ItemStack target) {
        if (target == null || player == null || player.inventory == null) return 0;
        int total = 0;
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null && itemsMatch(target, stack)) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    private boolean itemsMatch(ItemStack a, ItemStack b) {
        return NoppesUtilPlayer.compareItems(a, b, false, false);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        clearHiddenDisplaySlot();
        clearDetailItem();
        clearCreateSlot();
    }
}
