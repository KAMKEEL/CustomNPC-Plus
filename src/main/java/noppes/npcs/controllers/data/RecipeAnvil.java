package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.controllers.RecipeController;

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

    public RecipeAnvil() {
    }

    public RecipeAnvil(String name, ItemStack itemToRepair, ItemStack repairMaterial, int xpCost, float repairPercentage) {
        this.name = name;
        this.itemToRepair = itemToRepair;
        this.repairMaterial = repairMaterial;
        this.xpCost = xpCost;
        this.repairPercentage = repairPercentage;
    }

    public void readNBT(NBTTagCompound compound) {
        this.id = compound.getInteger("ID");
        this.name = compound.getString("Name");
        this.availability.readFromNBT(compound.getCompoundTag("Availability"));
        this.xpCost = compound.getInteger("XPCost");
        this.repairPercentage = compound.getFloat("RepairPercentage");
        this.itemToRepair = NoppesUtilServer.readItem(compound.getCompoundTag("ItemToRepair"));
        this.repairMaterial = NoppesUtilServer.readItem(compound.getCompoundTag("RepairMaterial"));
        this.ignoreRepairMaterialNBT = compound.getBoolean("IgnoreRepairMatNBT");
        this.ignoreRepairItemNBT = compound.getBoolean("IgnoreRepairItemNBT");
        this.ignoreRepairMaterialDamage = compound.getBoolean("IgnoreRepairMatDamage");

        if (compound.hasKey("ScriptData", Constants.NBT.TAG_COMPOUND)) {
            RecipeScript handler = new RecipeScript();
            handler.readFromNBT(compound.getCompoundTag("ScriptData"));
            setScriptHandler(handler);
        }
    }

    public NBTTagCompound writeNBT() {
        return writeNBT(true);
    }


    public NBTTagCompound writeNBT(boolean saveScripts) {
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

        if (saveScripts) {
            NBTTagCompound scriptData = new NBTTagCompound();
            RecipeScript handler = getScriptHandler();
            if (handler != null)
                handler.writeToNBT(scriptData);
            compound.setTag("ScriptData", scriptData);
        }
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
        this.xpCost = recipe.xpCost;
    }
    public RecipeScript getScriptHandler() {
        return RecipeController.Instance.anvilScripts.get(this.id);
    }

    public void setScriptHandler(RecipeScript handler) {
        RecipeController.Instance.anvilScripts.put(this.id, handler);
    }

    public RecipeScript getOrCreateScriptHandler() {
        RecipeScript data = getScriptHandler();
        if (data == null)
            setScriptHandler(data = new RecipeScript());
        return data;
    }
}
