package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IAnvilRecipe;

public class RecipeAnvil implements IAnvilRecipe {
    public int id = -1;
    public String name = "";
    public Availability availability = new Availability();

    public boolean ignoreRepairItemNBT = false;
    public boolean ignoreRepairMaterialNBT = false;
    public boolean ignoreRepairMaterialDamage = false;

    public ItemStack itemToRepair;
    public ItemStack repairMaterial;

    public int xpCost;
    public float repairPercentage;

    private boolean isAnvil = true;

    public RecipeAnvil() {
    }

    public RecipeAnvil(String name, ItemStack itemToRepair, ItemStack repairMaterial, int xpCost, float repairPercentage) {
        this.name = name;
        this.itemToRepair = itemToRepair;
        this.repairMaterial = repairMaterial;
        this.xpCost = xpCost;
        this.repairPercentage = repairPercentage;
    }

    public static RecipeAnvil read(NBTTagCompound compound) {
        RecipeAnvil recipe = new RecipeAnvil();
        recipe.id = compound.getInteger("ID");
        recipe.name = compound.getString("Name");
        recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
        recipe.xpCost = compound.getInteger("XPCost");
        recipe.repairPercentage = compound.getFloat("RepairPercentage");
        recipe.itemToRepair = NoppesUtilServer.readItem(compound.getCompoundTag("ItemToRepair"));
        recipe.repairMaterial = NoppesUtilServer.readItem(compound.getCompoundTag("RepairMaterial"));
        recipe.ignoreRepairMaterialNBT = compound.getBoolean("IgnoreRepairMatNBT");
        recipe.ignoreRepairItemNBT = compound.getBoolean("IgnoreRepairItemNBT");
        recipe.ignoreRepairMaterialDamage = compound.getBoolean("IgnoreRepairMatDamage");
        recipe.isAnvil = true;
        return recipe;
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("ID", id);
        compound.setString("Name", name);
        compound.setTag("Availability", availability.writeToNBT(new NBTTagCompound()));
        compound.setInteger("XPCost", xpCost);
        compound.setFloat("RepairPercentage", repairPercentage);
        if (itemToRepair != null) {
            compound.setTag("ItemToRepair", NoppesUtilServer.writeItem(itemToRepair, new NBTTagCompound()));
        }
        if (repairMaterial != null) {
            compound.setTag("RepairMaterial", NoppesUtilServer.writeItem(repairMaterial, new NBTTagCompound()));
        }
        compound.setBoolean("IgnoreRepairMatNBT", ignoreRepairMaterialNBT);
        compound.setBoolean("IgnoreRepairItemNBT", ignoreRepairItemNBT);
        compound.setBoolean("IgnoreRepairMatDamage", ignoreRepairMaterialDamage);
        compound.setBoolean("IsAnvil", true);
        return compound;
    }

    @Override
    public int getXpCost() {
        return xpCost;
    }

    @Override
    public float getRepairPercentage() {
        return repairPercentage;
    }

    @Override
    public boolean matches(ItemStack inputItem, ItemStack inputRepairMaterial) {
        if (inputItem == null || inputRepairMaterial == null) return false;
        if (!NoppesUtilPlayer.compareItems(this.itemToRepair, inputItem, true, this.ignoreRepairItemNBT)) {
            return false;
        }
        if (!NoppesUtilPlayer.compareItems(this.repairMaterial, inputRepairMaterial, this.ignoreRepairMaterialDamage, this.ignoreRepairMaterialNBT)) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getResult(ItemStack inputItem) {
        if (inputItem == null)
            return null;
        ItemStack result = inputItem.copy();
        if (!result.isItemStackDamageable())
            return result;
        int maxDamage = result.getMaxDamage();
        int repairAmount = (int) (maxDamage * repairPercentage);
        int currentDamage = result.getItemDamage();
        int newDamage = currentDamage - repairAmount;
        if (newDamage < 0) newDamage = 0;
        result.setItemDamage(newDamage);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getID() {
        return this.id;
    }

    public static RecipeAnvil saveRecipe(RecipeAnvil recipe, ItemStack output, ItemStack repairMaterial) {
        if (output != null) {
            recipe.itemToRepair = output.copy();
            if (recipe.itemToRepair.isItemStackDamageable()) {
                recipe.itemToRepair.setItemDamage(0);
            }
        } else {
            recipe.itemToRepair = null;
        }
        recipe.repairMaterial = (repairMaterial == null ? null : repairMaterial.copy());
        return recipe;
    }

    public boolean isValid() {
        return itemToRepair != null && repairMaterial != null;
    }

    /**
     * Creates and returns a deep copy of this RecipeAnvil.
     */
    public void copy(RecipeAnvil recipe) {
        this.id = recipe.id;
        this.name = recipe.name;
        this.availability = recipe.availability;
        this.ignoreRepairMaterialDamage = recipe.ignoreRepairMaterialDamage;
        this.ignoreRepairItemNBT = recipe.ignoreRepairItemNBT;
        this.ignoreRepairMaterialNBT = recipe.ignoreRepairMaterialNBT;
        this.repairPercentage = recipe.repairPercentage;
        ;
        this.xpCost = recipe.xpCost;
    }
}
