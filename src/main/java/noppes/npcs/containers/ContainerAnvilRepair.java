package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.world.World;
import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeAnvil;

public class ContainerAnvilRepair extends Container {
    // A 2-slot crafting matrix: slot 0 = damaged item, slot 1 = repair material.
    public InventoryCrafting anvilMatrix = new InventoryCrafting(this, 2, 1);
    // Single-slot output inventory.
    public InventoryCraftResult anvilResult = new InventoryCraftResult();

    private EntityPlayer player;
    private World worldObj;
    private int posX, posY, posZ;

    // These fields are used for syncing the repair cost message.
    public String repairStatus = "";
    public int repairCost = 0;
    // How many repair material items are used during the repair calculation.
    public int repairMaterialConsumed = 0;

    public ContainerAnvilRepair(InventoryPlayer playerInv, World world, int x, int y, int z) {
        this.worldObj = world;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.player = playerInv.player;

        // Output slot (index 0) using our custom SlotAnvilOutput.
        this.addSlotToContainer(new SlotAnvilOutput(this, anvilResult, 0, 133, 47));

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

        // For testing: clear recipes and add a test recipe.
        RecipeController.Instance.anvilRecipes.clear();
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "Test",
            new ItemStack(Items.iron_pickaxe),
            new ItemStack(Items.paper),
            50,
            23));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "Okay",
            new ItemStack(Items.diamond_pickaxe),
            new ItemStack(Items.paper),
            21,
            10));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "Bob",
            new ItemStack(Items.golden_pickaxe),
            new ItemStack(Items.paper),
            1200,
            40));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "Dude",
            new ItemStack(Items.iron_chestplate),
            new ItemStack(Items.paper),
            40000,
            23));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "Oas",
            new ItemStack(Items.golden_pickaxe),
            new ItemStack(Items.diamond),
            12,
            1));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "awd",
            new ItemStack(Items.golden_sword),
            new ItemStack(Items.gold_ingot),
            80,
            14));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "fawd",
            new ItemStack(Items.bow),
            new ItemStack(Items.string),
            4,
            3));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "cxzs",
            new ItemStack(Items.bow),
            new ItemStack(Items.diamond),
            4,
            3));
        RecipeController.Instance.addAnvilRecipe(new RecipeAnvil(
            "awdawdwa",
            new ItemStack(Items.iron_sword),
            new ItemStack(Items.blaze_rod),
            4,
            3));
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

            ItemStack output = null;
            if (matchingRecipe != null) {
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

            anvilResult.setInventorySlotContents(0, output);
        }
    }




    // --- Sync Methods ---
    @Override
    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        listener.sendProgressBarUpdate(this, 0, this.repairCost);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafterObj : this.crafters) {
            ((ICrafting) crafterObj).sendProgressBarUpdate(this, 0, this.repairCost);
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            this.repairCost = data;
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
        return player.getDistanceSq((double)this.posX + 0.5D,
            (double)this.posY + 0.5D,
            (double)this.posZ + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();
            if (index == 0) { // output slot
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
        private ContainerAnvilRepair container;

        public SlotAnvilOutput(ContainerAnvilRepair container, IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.container = container;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return player.experienceTotal >= container.repairCost;
        }

        @Override
        public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
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
            super.onPickupFromSlot(player, stack);
        }
    }
}
