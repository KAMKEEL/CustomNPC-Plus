package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.CustomItems;

public class LinkedItem {
    public static final String LINKED_NBT_TAG = "LinkedName";

    private final String name;
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

    public LinkedItem(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public ItemStack createStack() {
        ItemStack linkedStack = new ItemStack(CustomItems.linked_item);
        linkedStack.setTagInfo(LinkedItem.LINKED_NBT_TAG, new NBTTagString(this.getName()));
        return linkedStack;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
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

    public void readFromNBT(NBTTagCompound compound) {
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
}
