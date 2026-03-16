package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
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

    public IItemStack itemToRepair;
    public IItemStack repairMaterial;

    public int xpCost;
    public float repairPercentage;

    public RecipeAnvil() {
    }

    public RecipeAnvil(String name, IItemStack itemToRepair, IItemStack repairMaterial, int xpCost, float repairPercentage) {
        this.name = name;
        this.itemToRepair = itemToRepair;
        this.repairMaterial = repairMaterial;
        this.xpCost = xpCost;
        this.repairPercentage = repairPercentage;
    }

    public void readNBT(INbt compound) {
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

        if (compound.hasKey("ScriptData", NbtConstants.TAG_COMPOUND)) {
            RecipeScript handler = new RecipeScript();
            handler.readFromNBT(compound.getCompoundTag("ScriptData"));
            setScriptHandler(handler);
        }
    }

    public INbt writeNBT() {
        return writeNBT(true);
    }


    public INbt writeNBT(boolean saveScripts) {
        INbt compound = new INbt();
        compound.setInteger("ID", id);
        compound.setString("Name", name);
        compound.setTag("Availability", availability.writeToNBT(new INbt()));
        compound.setInteger("XPCost", xpCost);
        compound.setFloat("RepairPercentage", repairPercentage);
        if (itemToRepair != null) {
            compound.setTag("ItemToRepair", NoppesUtilServer.writeItem(itemToRepair, new INbt()));
        }
        if (repairMaterial != null) {
            compound.setTag("RepairMaterial", NoppesUtilServer.writeItem(repairMaterial, new INbt()));
        }
        compound.setBoolean("IgnoreRepairMatNBT", ignoreRepairMaterialNBT);
        compound.setBoolean("IgnoreRepairItemNBT", ignoreRepairItemNBT);
        compound.setBoolean("IgnoreRepairMatDamage", ignoreRepairMaterialDamage);
        compound.setBoolean("IsAnvil", true);

        if (saveScripts) {
            INbt scriptData = new INbt();
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
    public boolean matches(IItemStack inputItem, IItemStack inputRepairMaterial) {
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
    public IItemStack getResult(IItemStack inputItem) {
        if (inputItem == null)
            return null;
        IItemStack result = inputItem.copy();
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

    public static RecipeAnvil saveRecipe(RecipeAnvil recipe, IItemStack output, IItemStack repairMaterial) {
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
