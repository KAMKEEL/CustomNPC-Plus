package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.controllers.data.ItemDisplayData;

public abstract class ScriptCustomizableItem extends ScriptItemStack implements IItemCustomizable {
    public final ItemDisplayData itemDisplay = new ItemDisplayData();

    public ScriptCustomizableItem(ItemStack item) {
        super(item);
    }

    public abstract int getMaxStackSize();

    public abstract int getArmorType();

    public abstract boolean isTool();

    public abstract boolean isNormalItem();

    public abstract int getDigSpeed();

    public abstract double getDurabilityValue();

    public abstract int getMaxItemUseDuration();

    public abstract int getItemUseAction();

    public abstract int getEnchantability();


    public String getTexture() {
        return this.itemDisplay.texture == null ? "" : this.itemDisplay.texture;
    }

    public void setTexture(String texture){
        if(texture == null)
            texture = "";
        this.itemDisplay.texture = texture;
        saveItemData();
    }

    public Boolean getDurabilityShow() {
        return this.itemDisplay.durabilityShow != null ? this.itemDisplay.durabilityShow : false;
    }

    public void setDurabilityShow(Boolean bo) {
        this.itemDisplay.durabilityShow = bo;
        saveItemData();
    }

    public Integer getDurabilityColor() {
        return this.itemDisplay.durabilityColor != null ? this.itemDisplay.durabilityColor : -1;
    }

    public void setDurabilityColor(Integer color) {
        this.itemDisplay.durabilityColor = color;
        saveItemData();
    }

    public Integer getColor() {
        return this.itemDisplay.itemColor != null ? this.itemDisplay.itemColor : 0x8B4513;
    }

    public void setColor(Integer color) {
        this.itemDisplay.itemColor = color;
        saveItemData();
    }


    public void setRotation(Float rotationX, Float rotationY, Float rotationZ){
        this.itemDisplay.rotationX = rotationX;
        this.itemDisplay.rotationY = rotationY;
        this.itemDisplay.rotationZ = rotationZ;
        saveItemData();
    }

    public void setRotationRate(Float rotationXRate, Float rotationYRate, Float rotationZRate){
        this.itemDisplay.rotationXRate = rotationXRate;
        this.itemDisplay.rotationYRate = rotationYRate;
        this.itemDisplay.rotationZRate = rotationZRate;
        saveItemData();
    }

    public void setScale(Float scaleX, Float scaleY, Float scaleZ){
        this.itemDisplay.scaleX = scaleX;
        this.itemDisplay.scaleY = scaleY;
        this.itemDisplay.scaleZ = scaleZ;
        saveItemData();
    }

    public void setTranslate(Float translateX, Float translateY, Float translateZ){
        this.itemDisplay.translateX = translateX;
        this.itemDisplay.translateY = translateY;
        this.itemDisplay.translateZ = translateZ;
        saveItemData();
    }

    public Float getRotationX() {
        return this.itemDisplay.rotationX != null ? this.itemDisplay.rotationX : 0;
    }

    public Float getRotationY() {
        return this.itemDisplay.rotationY != null ? this.itemDisplay.rotationY : 0;
    }

    public Float getRotationZ() {
        return this.itemDisplay.rotationZ != null ? this.itemDisplay.rotationZ : 0;
    }

    public Float getRotationXRate() {
        return this.itemDisplay.rotationXRate != null ? this.itemDisplay.rotationXRate : 0;
    }

    public Float getRotationYRate() {
        return this.itemDisplay.rotationYRate != null ? this.itemDisplay.rotationYRate : 0;
    }

    public Float getRotationZRate() {
        return this.itemDisplay.rotationZRate != null ? this.itemDisplay.rotationZRate : 0;
    }

    public Float getScaleX() {
        return this.itemDisplay.scaleX != null ? this.itemDisplay.scaleX : 0;
    }

    public Float getScaleY() {
        return this.itemDisplay.scaleY != null ? this.itemDisplay.scaleY : 0;
    }

    public Float getScaleZ() {
        return this.itemDisplay.scaleZ != null ? this.itemDisplay.scaleZ : 0;
    }

    public Float getTranslateX() {
        return this.itemDisplay.translateX != null ? this.itemDisplay.translateX : 0;
    }

    public Float getTranslateY() {
        return this.itemDisplay.translateY != null ? this.itemDisplay.translateY : 0;
    }

    public Float getTranslateZ() {
        return this.itemDisplay.translateZ != null ? this.itemDisplay.translateZ : 0;
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
        return compound;
    }

    public void setItemNBT(NBTTagCompound compound) {
        this.itemDisplay.readFromNBT(compound);
    }

}
