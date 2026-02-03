package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Base container for all auction GUIs.
 * Provides player inventory at the correct position for the 256x256 auction texture.
 * Player inventory is display-only - no modifications allowed.
 */
public class ContainerAuction extends ContainerNpcInterface {
    public final EntityNPCInterface npc;

    // Player inventory layout for 256x256 auction texture
    // Top-left slot background at (47, 163)
    // Item renders at +1 offset inside the 18x18 slot background
    public static final int PLAYER_INV_X = 48;  // 47 + 1 for item offset
    public static final int PLAYER_INV_Y = 164; // 163 + 1 for item offset
    public static final int HOTBAR_Y = 222;     // PLAYER_INV_Y + (3 * 18) + 4 gap

    // Player inventory slots are added first (36 slots: 0-35)
    public static final int PLAYER_INV_SLOT_COUNT = 36;

    public ContainerAuction(EntityNPCInterface npc, EntityPlayer player) {
        super(player);
        this.npc = npc;
        this.player = player;

        addPlayerInventory();
    }

    protected void addPlayerInventory() {
        // Main inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9; // Slots 9-35
                int x = PLAYER_INV_X + col * 18;
                int y = PLAYER_INV_Y + row * 18;
                addSlotToContainer(new Slot(player.inventory, slotIndex, x, y));
            }
        }

        // Hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            int x = PLAYER_INV_X + col * 18;
            addSlotToContainer(new Slot(player.inventory, col, x, HOTBAR_Y));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        return null; // Disable shift-clicking
    }

    @Override
    public ItemStack slotClick(int slotIndex, int mouseButton, int mode, EntityPlayer player) {
        // Block all slot interactions by default in auction containers
        // This prevents picking up, placing, and swapping items
        // Subclasses can override to allow specific interactions
        return null;
    }

    /**
     * Check if the slot index is a player inventory slot.
     */
    public boolean isPlayerInventorySlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < PLAYER_INV_SLOT_COUNT;
    }

    /**
     * Get the ItemStack in a player inventory slot.
     * @param containerSlotIndex The container slot index (0-35 for player inv)
     */
    public ItemStack getPlayerInventoryStack(int containerSlotIndex) {
        if (!isPlayerInventorySlot(containerSlotIndex)) return null;
        Slot slot = (Slot) inventorySlots.get(containerSlotIndex);
        return slot != null ? slot.getStack() : null;
    }
}
