package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.ILinkedItem;
import noppes.npcs.controllers.LinkedItemController;

public class LinkedItem implements ILinkedItem {
    public static final String LINKED_VERSION_VERSION_TAG = "LinkedVersion";
    public static final String LINKED_DATA_NBT_TAG = "LinkedData";

    public int id = -1;
    public int version = 1;
    public String name;
    public final ItemDisplayData display = new ItemDisplayData();

    public double durabilityValue = 1.0D;
    public int stackSize = 64;

    public int maxItemUseDuration = 20;
    public int itemUseAction = 0; //0: none, 1: block, 2: bow, 3: eat, 4: drink

    public boolean isNormalItem = false;
    public boolean isTool = false;
    public int digSpeed = 1;
    public int armorType = -2; //-2: Fits in no armor slot,  -1: Fits in all slots, 0 - 4: Fits in Head -> Boots slot respectively
    public int enchantability;

    public LinkedItem() {
        this("New");
    }

    public LinkedItem(String name) {
        this.name = name;
    }

    public INpcScriptHandler getScriptHandler() {
        return LinkedItemController.Instance().getScriptHandler(this.getId());
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("Id", this.id);
        compound.setInteger("Version", this.version);
        compound.setString("Name", this.name);

        compound.setTag("Display", this.display.writeToNBT());

        compound.setDouble("DurabilityValue",this.durabilityValue);
        compound.setInteger("MaxStackSize",this.stackSize);

        compound.setBoolean("IsTool", this.isTool);
        compound.setBoolean("IsNormalItem", this.isNormalItem);
        compound.setInteger("DigSpeed", this.digSpeed);
        compound.setInteger("ArmorType", this.armorType);
        compound.setInteger("Enchantability", this.enchantability);

        compound.setInteger("MaxItemUseDuration", this.maxItemUseDuration);
        compound.setInteger("ItemUseAction", this.itemUseAction);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound, boolean checkID) {
        if(checkID)
            this.id = compound.hasKey("Id") ? compound.getInteger("Id") : LinkedItemController.Instance().getUnusedId();
        else
            this.id = compound.getInteger("Id");

        this.name = compound.getString("Name");
        this.version = compound.getInteger("Version");

        this.display.readFromNBT(compound.getCompoundTag("Display"));

        this.durabilityValue = compound.getDouble("DurabilityValue");
        this.stackSize = compound.getInteger("MaxStackSize");

        this.isTool = compound.getBoolean("IsTool");
        this.isNormalItem = compound.getBoolean("IsNormalItem");
        this.digSpeed = compound.getInteger("DigSpeed");
        this.armorType = compound.getInteger("ArmorType");
        this.enchantability = compound.getInteger("Enchantability");

        this.maxItemUseDuration = compound.getInteger("MaxItemUseDuration");
        this.itemUseAction = compound.getInteger("ItemUseAction");
    }

    public ILinkedItem save() {
        return LinkedItemController.Instance().saveLinkedItem(this);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getDurabilityValue() {
        return durabilityValue;
    }

    @Override
    public void setDurabilityValue(double durabilityValue) {
        this.durabilityValue = durabilityValue;
    }

    @Override
    public int getStackSize() {
        return stackSize;
    }

    @Override
    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    @Override
    public int getMaxItemUseDuration() {
        return maxItemUseDuration;
    }

    @Override
    public void setMaxItemUseDuration(int maxItemUseDuration) {
        this.maxItemUseDuration = maxItemUseDuration;
    }

    @Override
    public int getItemUseAction() {
        return itemUseAction;
    }

    @Override
    public void setItemUseAction(int itemUseAction) {
        this.itemUseAction = itemUseAction;
    }

    @Override
    public boolean isNormalItem() {
        return isNormalItem;
    }

    @Override
    public void setNormalItem(boolean normalItem) {
        isNormalItem = normalItem;
    }

    @Override
    public boolean isTool() {
        return isTool;
    }

    @Override
    public void setTool(boolean tool) {
        isTool = tool;
    }

    @Override
    public int getDigSpeed() {
        return digSpeed;
    }

    @Override
    public void setDigSpeed(int digSpeed) {
        this.digSpeed = digSpeed;
    }

    @Override
    public int getArmorType() {
        return armorType;
    }

    @Override
    public void setArmorType(int armorType) {
        this.armorType = armorType;
    }

    @Override
    public int getEnchantability() {
        return enchantability;
    }

    @Override
    public void setEnchantability(int enchantability) {
        this.enchantability = enchantability;
    }

    // ADD / GET SETS FOR LINKED ITEM DISPLAY THINGS

}
