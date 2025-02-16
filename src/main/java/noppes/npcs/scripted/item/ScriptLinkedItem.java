package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.item.IItemLinked;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.controllers.data.LinkedItem;

public class ScriptLinkedItem extends ScriptCustomizableItem implements IItemLinked {
    public LinkedItem linkedItem;
    public double durabilityValue = 1.0D;
    public int linkedVersion = 1;
    public ScriptLinkedItem(ItemStack item) {
        super(item);
        loadItemData();
    }

    public LinkedItem getLinkedItem() {
        return LinkedItemController.getInstance().get(this.linkedItem.getId());
    }

    @Override
    public INpcScriptHandler getScriptHandler() {
        return this.getLinkedItem().getScriptHandler();
    }

    @Override
    public int getMaxStackSize() {
        return this.linkedItem.stackSize;
    }

    @Override
    public int getArmorType() {
        return this.linkedItem.armorType;
    }

    @Override
    public boolean isTool() {
        return this.linkedItem.isTool;
    }

    @Override
    public boolean isNormalItem() {
        return this.linkedItem.isNormalItem;
    }

    @Override
    public int getDigSpeed() {
        return this.linkedItem.digSpeed;
    }

    @Override
    public double getDurabilityValue() {
        return this.durabilityValue;
    }

    @Override
    public int getMaxItemUseDuration() {
        return this.linkedItem.maxItemUseDuration;
    }

    @Override
    public int getItemUseAction() {
        return this.linkedItem.itemUseAction;
    }

    @Override
    public int getEnchantability() {
        return this.linkedItem.enchantability;
    }

    @Override
    public String getTexture() {
        return this.itemDisplay.texture == null ? this.linkedItem.display.texture : this.itemDisplay.texture;
    }

    @Override
    public Boolean getDurabilityShow() {
        return this.itemDisplay.durabilityShow != null ? this.itemDisplay.durabilityShow : this.linkedItem.display.durabilityShow;
    }

    @Override
    public Integer getDurabilityColor() {
        return this.itemDisplay.durabilityColor != null ? this.itemDisplay.durabilityColor : this.linkedItem.display.durabilityColor;
    }

    @Override
    public Integer getColor() {
        return this.itemDisplay.itemColor != null ? this.itemDisplay.itemColor : this.linkedItem.display.itemColor;
    }

    @Override
    public Float getRotationX() {
        return this.itemDisplay.rotationX != null ? this.itemDisplay.rotationX : this.linkedItem.display.rotationX;
    }

    @Override
    public Float getRotationY() {
        return this.itemDisplay.rotationY != null ? this.itemDisplay.rotationY : this.linkedItem.display.rotationY;
    }

    @Override
    public Float getRotationZ() {
        return this.itemDisplay.rotationZ != null ? this.itemDisplay.rotationZ : this.linkedItem.display.rotationZ;
    }

    @Override
    public Float getRotationXRate() {
        return this.itemDisplay.rotationXRate != null ? this.itemDisplay.rotationXRate : this.linkedItem.display.rotationXRate;
    }

    @Override
    public Float getRotationYRate() {
        return this.itemDisplay.rotationYRate != null ? this.itemDisplay.rotationYRate : this.linkedItem.display.rotationYRate;
    }

    @Override
    public Float getRotationZRate() {
        return this.itemDisplay.rotationZRate != null ? this.itemDisplay.rotationZRate : this.linkedItem.display.rotationZRate;
    }

    @Override
    public Float getScaleX() {
        return this.itemDisplay.scaleX != null ? this.itemDisplay.scaleX : this.linkedItem.display.scaleX;
    }

    @Override
    public Float getScaleY() {
        return this.itemDisplay.scaleY != null ? this.itemDisplay.scaleY : this.linkedItem.display.scaleY;
    }

    @Override
    public Float getScaleZ() {
        return this.itemDisplay.scaleZ != null ? this.itemDisplay.scaleZ : this.linkedItem.display.scaleZ;
    }

    @Override
    public Float getTranslateX() {
        return this.itemDisplay.translateX != null ? this.itemDisplay.translateX : this.linkedItem.display.translateX;
    }

    public NBTTagCompound getMCNbt() {
        NBTTagCompound compound = super.getMCNbt();
        compound.setTag("ItemData", this.getItemNBT(new NBTTagCompound()));
        return compound;
    }

    public void setMCNbt(NBTTagCompound compound) {
        super.setMCNbt(compound);
        setItemNBT(compound.getCompoundTag("ItemData"));
    }

    public void saveItemData() {
        NBTTagCompound c = this.item.getTagCompound();
        if (c == null) {
            this.item.setTagCompound(c = new NBTTagCompound());
        }
        c.setTag("ItemData", this.getItemNBT(new NBTTagCompound()));
    }

    public void loadItemData() {
        NBTTagCompound c = this.item.getTagCompound();
        if (c != null && !c.getCompoundTag("ItemData").hasNoTags()){
            this.setItemNBT(c.getCompoundTag("ItemData"));
        }
    }

    public NBTTagCompound getItemNBT(NBTTagCompound compound) {
        this.itemDisplay.writeToNBT(compound);
        compound.setTag(LinkedItem.LINKED_DATA_NBT_TAG, this.linkedItem.writeToNBT());
        compound.setInteger(LinkedItem.LINKED_VERSION_VERSION_TAG, this.linkedVersion);
        return compound;
    }

    public void setItemNBT(NBTTagCompound compound) {
        this.itemDisplay.readFromNBT(compound);
        this.linkedItem.readFromNBT(compound, false);
        this.linkedVersion = compound.getInteger(LinkedItem.LINKED_VERSION_VERSION_TAG);
    }
}
