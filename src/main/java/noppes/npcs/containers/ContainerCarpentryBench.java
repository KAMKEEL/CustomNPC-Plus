package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.scripted.event.RecipeScriptEvent;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;

public class ContainerCarpentryBench extends Container {
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 4, 4);
    public IInventory craftResult = new InventoryCraftResult();
    private EntityPlayer player;
    private World worldObj;
    private int posX;
    private int posY;
    private int posZ;

    private RecipeCarpentry currentRecipe;
    private SlotCarpentryResult resultSlot;
    private boolean resultCanPickup = true;

    public ContainerCarpentryBench(InventoryPlayer par1InventoryPlayer, World par2World, int par3, int par4, int par5) {
        this.worldObj = par2World;
        this.posX = par3;
        this.posY = par4;
        this.posZ = par5;
        this.player = par1InventoryPlayer.player;
        this.resultSlot = new SlotCarpentryResult(this, par1InventoryPlayer.player, this.craftMatrix, this.craftResult, 0, 133, 41);
        this.addSlotToContainer(this.resultSlot);
        int var6;
        int var7;

        for (var6 = 0; var6 < 4; ++var6) {
            for (var7 = 0; var7 < 4; ++var7) {
                this.addSlotToContainer(new Slot(this.craftMatrix, var7 + var6 * 4, 17 + var7 * 18, 14 + var6 * 18));
            }
        }

        for (var6 = 0; var6 < 3; ++var6) {
            for (var7 = 0; var7 < 9; ++var7) {
                this.addSlotToContainer(new Slot(par1InventoryPlayer, var7 + var6 * 9 + 9, 8 + var7 * 18, 98 + var6 * 18));
            }
        }

        for (var6 = 0; var6 < 9; ++var6) {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, var6, 8 + var6 * 18, 156));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    public int getMetadata() {
        return worldObj.getBlockMetadata(posX, posY, posZ);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    @Override
    public void onCraftMatrixChanged(IInventory par1IInventory) {
        if (!this.worldObj.isRemote) {
            RecipeCarpentry recipe = RecipeController.Instance.findMatchingRecipe(this.craftMatrix);
            this.currentRecipe = recipe;

            ItemStack item = null;
            boolean canPickup = true;
            if (recipe != null && recipe.availability.isAvailable(player)) {
                item = recipe.getCraftingResult(this.craftMatrix);

                ItemStack[] items = new ItemStack[this.craftMatrix.getSizeInventory()];
                for (int i = 0; i < items.length; i++) {
                    items[i] = this.craftMatrix.getStackInSlot(i);
                }

                RecipeScriptEvent.Pre pre = EventHooks.onRecipeScriptPre(player, recipe.getScriptHandler(), recipe, items);
                canPickup = !pre.isCanceled();
                item = EventHooks.onRecipeScriptPost(player, recipe.getScriptHandler(), recipe, items, item);
            }

            this.craftResult.setInventorySlotContents(0, item);
            this.resultCanPickup = canPickup;
            if (this.resultSlot != null) {
                this.resultSlot.setCanPickup(canPickup);
            }
            EntityPlayerMP plmp = (EntityPlayerMP) player;
            plmp.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(this.windowId, 0, item));
        }
    }

    /**
     * Callback for when the crafting gui is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);

        if (!this.worldObj.isRemote) {
            for (int var2 = 0; var2 < 16; ++var2) {
                ItemStack var3 = this.craftMatrix.getStackInSlotOnClosing(var2);

                if (var3 != null) {
                    par1EntityPlayer.dropPlayerItemWithRandomChoice(var3, false);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
        return this.worldObj.getBlock(this.posX, this.posY, this.posZ) != CustomItems.carpentyBench ? false : par1EntityPlayer.getDistanceSq((double) this.posX + 0.5D, (double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par1) {
        ItemStack var2 = null;
        Slot var3 = (Slot) this.inventorySlots.get(par1);

        if (var3 != null && var3.getHasStack()) {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();

            if (par1 == 0) {
                SlotCarpentryResult resultSlot = (SlotCarpentryResult) var3;
                if (!resultSlot.canPickup()) {
                    return null;
                }
                if (!this.mergeItemStack(var4, 17, 53, true)) {
                    return null;
                }

                var3.onSlotChange(var4, var2);
            } else if (par1 >= 17 && par1 < 44) {
                if (!this.mergeItemStack(var4, 44, 53, false)) {
                    return null;
                }
            } else if (par1 >= 44 && par1 < 53) {
                if (!this.mergeItemStack(var4, 17, 44, false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(var4, 17, 53, false)) {
                return null;
            }

            if (var4.stackSize == 0) {
                var3.putStack((ItemStack) null);
            } else {
                var3.onSlotChanged();
            }

            if (var4.stackSize == var2.stackSize) {
                return null;
            }

            var3.onPickupFromSlot(par1EntityPlayer, var4);
        }

        // Update client-side inventory
        if (!this.worldObj.isRemote && par1EntityPlayer instanceof EntityPlayerMP) {
            ((EntityPlayerMP) par1EntityPlayer).sendContainerToPlayer(this);
        }

        return var2;
    }

    @Override
    public boolean func_94530_a(ItemStack stack, Slot slotIn) {
        return slotIn.inventory != this.craftResult && super.func_94530_a(stack, slotIn);
    }

    // --- Sync Methods ---
    @Override
    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        listener.sendProgressBarUpdate(this, 0, this.resultCanPickup ? 1 : 0);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafterObj : this.crafters) {
            ((ICrafting) crafterObj).sendProgressBarUpdate(this, 0, this.resultCanPickup ? 1 : 0);
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0 && this.resultSlot != null) {
            this.resultSlot.setCanPickup(data != 0);
        }
    }
    // --- End Sync Methods ---

    // Custom result slot to handle recipe scripts
    private class SlotCarpentryResult extends SlotCrafting {
        private final ContainerCarpentryBench container;
        private boolean canPickup = true;

        public SlotCarpentryResult(ContainerCarpentryBench container, EntityPlayer player, InventoryCrafting matrix, IInventory result, int index, int x, int y) {
            super(player, matrix, result, index, x, y);
            this.container = container;
        }

        public void setCanPickup(boolean value) {
            this.canPickup = value;
        }

        public boolean canPickup() {
            return this.canPickup;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return canPickup && super.canTakeStack(player);
        }

        @Override
        public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
            if (!canPickup)
                return;
            super.onPickupFromSlot(player, stack);
        }
    }
}
