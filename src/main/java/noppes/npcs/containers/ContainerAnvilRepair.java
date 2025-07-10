package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.EventHooks;
import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.scripted.event.RecipeScriptEvent;

public class ContainerAnvilRepair extends Container {
    // A 2-slot crafting matrix: slot 0 = damaged item, slot 1 = repair material.
    public InventoryCrafting anvilMatrix = new InventoryCrafting(this, 2, 1);
    // Single-slot output inventory.
    public InventoryCraftResult anvilResult = new InventoryCraftResult();

    private final EntityPlayer player;
    private final World worldObj;
    private final int posX;
    private final int posY;
    private final int posZ;

    public int repairCost = 0;
    public int repairMaterialConsumed = 0;

    private RecipeAnvil currentRecipe;
    private SlotAnvilOutput resultSlot;
    private boolean resultCanPickup = true;

    public ContainerAnvilRepair(InventoryPlayer playerInv, World world, int x, int y, int z) {
        this.worldObj = world;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.player = playerInv.player;

        // Output slot (index 0) using our custom SlotAnvilOutput.
        this.resultSlot = new SlotAnvilOutput(this, anvilResult, 0, 133, 47);
        this.addSlotToContainer(this.resultSlot);

        // Input slot 0: damaged item. We restrict its stack size to 1.
        this.addSlotToContainer(new Slot(anvilMatrix, 0, 26, 47) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
        });
        // Input slot 1: repair material. Override onSlotChanged so that changes to the stack size trigger an update.
        this.addSlotToContainer(new Slot(anvilMatrix, 1, 75, 47) {
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                ContainerAnvilRepair.this.onCraftMatrixChanged(anvilMatrix);
            }
        });

        // Add player inventory slots.
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 98 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 156));
        }

        updateRepairResult();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        updateRepairResult();
        detectAndSendChanges();
    }

    /**
     * Updates the output slot (and repair cost fields) based on the two input slots.
     * Instead of forcing the damaged item’s stack to 1 (which deletes extra items),
     * we create a copy with stackSize 1 for calculations. Then, when the repair is picked up,
     * we subtract one from the original stack.
     */
    public void updateRepairResult() {
        if (!this.worldObj.isRemote) {
            repairMaterialConsumed = 0;
            ItemStack input0 = anvilMatrix.getStackInRowAndColumn(0, 0);
            ItemStack input1 = anvilMatrix.getStackInRowAndColumn(1, 0);

            // If either input is missing, clear output.
            if (input0 == null || input1 == null) {
                repairCost = 0;
                anvilResult.setInventorySlotContents(0, null);
                return;
            }

            // Create a copy of the damaged item for calculation (stack size 1)
            ItemStack calcItem = input0.copy();
            calcItem.stackSize = 1;

            // If the item is damageable and already fully repaired, show a message.
            if (calcItem.isItemStackDamageable() && calcItem.getItemDamage() <= 0) {
                repairCost = 0;
                anvilResult.setInventorySlotContents(0, null);
                return;
            }

            // Look for a matching recipe.
            RecipeAnvil matchingRecipe = null;
            for (IAnvilRecipe recipe : RecipeController.Instance.getAnvilList()) {
                if (recipe.matches(input0, input1)) {
                    matchingRecipe = (RecipeAnvil) recipe;
                    break;
                }
            }

            currentRecipe = matchingRecipe;

            ItemStack output = null;
            boolean canPickup = true;
            if (matchingRecipe != null) {
                if (!matchingRecipe.availability.isAvailable(player)) {
                    return;
                }

                int baseXpCost = matchingRecipe.getXpCost();
                // Assume repairPercentage is given as a percent (e.g. 20 for 20% per material)
                float repairPercentage = matchingRecipe.getRepairPercentage() / 100.0f;

                int maxDamage = calcItem.getMaxDamage();
                int currentDamage = calcItem.getItemDamage();

                // Fixed repair amount per material (at least 1 point)
                int repairPerMaterial = (int) Math.floor(maxDamage * repairPercentage);
                if (repairPerMaterial < 1) {
                    repairPerMaterial = 1;
                }

                int availableMaterials = input1.stackSize;
                int materialsUsed = 0;

                while (currentDamage > 0 && materialsUsed < availableMaterials) {
                    int repairThisIteration = repairPerMaterial;
                    if (repairThisIteration > currentDamage) {
                        repairThisIteration = currentDamage;
                    }
                    currentDamage -= repairThisIteration;
                    materialsUsed++;

                    // Stop if item is fully repaired.
                    if (currentDamage <= 0) {
                        break;
                    }
                }
                int xpCost = baseXpCost * materialsUsed;

                calcItem.setItemDamage(currentDamage);
                if (player.experienceTotal >= xpCost) {
                    output = calcItem;
                }
                repairMaterialConsumed = materialsUsed;
                repairCost = xpCost;
            } else {
                repairCost = 0;
            }
            if (matchingRecipe != null) {
                ItemStack[] items = new ItemStack[]{input0, input1};
                RecipeScriptEvent.Pre pre = EventHooks.onRecipeScriptPre(player, matchingRecipe.getScriptHandler(), matchingRecipe, items);
                canPickup = !pre.isCanceled();
                output = EventHooks.onRecipeScriptPost(player, matchingRecipe.getScriptHandler(), matchingRecipe, items, output);
            }

            anvilResult.setInventorySlotContents(0, output);
            this.resultCanPickup = canPickup;
            if (this.resultSlot != null) {
                this.resultSlot.setCanPickup(canPickup);
            }
        }
    }


    // --- Sync Methods ---
    @Override
    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        listener.sendProgressBarUpdate(this, 0, this.repairCost);
        listener.sendProgressBarUpdate(this, 1, this.resultCanPickup ? 1 : 0);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafterObj : this.crafters) {
            ((ICrafting) crafterObj).sendProgressBarUpdate(this, 0, this.repairCost);
            ((ICrafting) crafterObj).sendProgressBarUpdate(this, 1, this.resultCanPickup ? 1 : 0);
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            this.repairCost = data;
        } else if (id == 1 && this.resultSlot != null) {
            this.resultSlot.setCanPickup(data != 0);
        }
    }
    // --- End Sync Methods ---

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!this.worldObj.isRemote) {
            for (int i = 0; i < this.anvilMatrix.getSizeInventory(); i++) {
                ItemStack stack = this.anvilMatrix.getStackInSlotOnClosing(i);
                if (stack != null) {
                    player.dropPlayerItemWithRandomChoice(stack, false);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq((double) this.posX + 0.5D,
            (double) this.posY + 0.5D,
            (double) this.posZ + 0.5D) <= 64.0D;
    }

    public boolean canPickupResult() {
        return this.resultCanPickup;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = null;
        Slot slot = (Slot) this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();
            if (index == 0) { // output slot
                SlotAnvilOutput resultSlot = (SlotAnvilOutput) slot;
                if (!resultSlot.canPickup()) {
                    return null;
                }
                if (player.experienceTotal < this.repairCost) {
                    return null;
                }
                if (!this.mergeItemStack(stack, 3, this.inventorySlots.size(), true)) {
                    return null;
                }
                slot.onSlotChange(stack, copy);
            } else if (index >= 1 && index <= 2) { // input slots
                if (!this.mergeItemStack(stack, 3, this.inventorySlots.size(), false)) {
                    return null;
                }
            } else { // player inventory
                if (!this.mergeItemStack(stack, 1, 3, false)) {
                    return null;
                }
            }
            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (stack.stackSize == copy.stackSize) {
                return null;
            }
            slot.onPickupFromSlot(player, stack);
        }
        if (!this.worldObj.isRemote && player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).sendContainerToPlayer(this);
        }
        return copy;
    }

    @Override
    public boolean func_94530_a(ItemStack stack, Slot slotIn) {
        return slotIn.inventory != this.anvilResult && super.func_94530_a(stack, slotIn);
    }

    // Custom output slot: only allows pickup if the player has enough XP and,
    // when picked up, consumes the required number of repair material items and
    // subtracts one from the damaged item stack.
    public class SlotAnvilOutput extends Slot {
        private final ContainerAnvilRepair container;
        private boolean canPickup = true;

        public SlotAnvilOutput(ContainerAnvilRepair container, IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.container = container;
        }

        public void setCanPickup(boolean value) {
            this.canPickup = value;
        }

        public boolean canPickup() {
            return this.canPickup;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return canPickup && player.experienceTotal >= container.repairCost;
        }

        @Override
        public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
            if (!canPickup) {
                return;
            }
            if (player.experienceTotal < container.repairCost) {
                container.updateRepairResult();
                return;
            }
            if (!player.capabilities.isCreativeMode) {
                player.addExperience(-container.repairCost);
            }

            ItemStack repairMat = container.anvilMatrix.getStackInRowAndColumn(1, 0);
            if (repairMat != null) {
                if (repairMat.stackSize > container.repairMaterialConsumed) {
                    repairMat.stackSize -= container.repairMaterialConsumed;
                } else {
                    repairMat = null;
                }
                container.anvilMatrix.setInventorySlotContents(1, repairMat);
            }

            ItemStack input0 = container.anvilMatrix.getStackInRowAndColumn(0, 0);
            if (input0 != null) {
                if (input0.stackSize > 1) {
                    input0.stackSize -= 1;
                } else {
                    input0 = null;
                }
                container.anvilMatrix.setInventorySlotContents(0, input0);
            }
            container.repairCost = 0;
            container.updateRepairResult();
            canPickup = true;
            super.onPickupFromSlot(player, stack);
        }
    }
}
