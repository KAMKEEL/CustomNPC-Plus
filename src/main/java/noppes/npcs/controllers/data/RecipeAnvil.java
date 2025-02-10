package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;

public class RecipeAnvil implements IAnvilRecipe {
    public int id = -1;
    public String name = "";
    public Availability availability = new Availability();
    public boolean ignoreNBT = false;

    public ItemStack itemToRepair;
    public ItemStack repairMaterial;

    private int xpCost;
    private float repairPercentage;

    private boolean isAnvil = true;

    public RecipeAnvil() { }

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
        compound.setTag("ItemToRepair", NoppesUtilServer.writeItem(itemToRepair, new NBTTagCompound()));
        compound.setTag("RepairMaterial", NoppesUtilServer.writeItem(repairMaterial, new NBTTagCompound()));
        compound.setBoolean("IsAnvil", true);
        return compound;
    }

    public int getXpCost() {
        return xpCost;
    }

    @Override
    public float getRepairPercentage() {
        return repairPercentage;
    }

    /**
     * Checks that both input ItemStacks are non-null and match the recipe’s defined items.
     */
    @Override
    public boolean matches(ItemStack inputItem, ItemStack inputRepairMaterial) {
        if (inputItem == null || inputRepairMaterial == null) return false;
        if (!NoppesUtilPlayer.compareItems(this.itemToRepair, inputItem, true, false)) {
            return false;
        }
        if (!NoppesUtilPlayer.compareItems(this.repairMaterial, inputRepairMaterial, false, false)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a repaired copy of the input item by reducing its damage.
     */
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
}
