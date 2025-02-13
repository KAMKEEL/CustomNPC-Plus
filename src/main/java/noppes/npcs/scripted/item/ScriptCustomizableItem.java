package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class ScriptCustomizableItem extends ScriptItemStack {
    public final Display itemDisplay = new Display();

    public ScriptCustomizableItem(ItemStack item) {
        super(item);
    }

    public abstract int getMaxStackSize();

//    public abstract void setArmorType(int armorType);

    public abstract int getArmorType();

//    public abstract void setIsTool(boolean isTool);

    public abstract boolean isTool();

//    public abstract void setIsNormalItem(boolean normalItem);

    public abstract boolean isNormalItem();

//    public abstract void setDigSpeed(int digSpeed);

    public abstract int getDigSpeed();

//    public abstract void setMaxStackSize(int size);

    public abstract double getDurabilityValue();

//    public abstract void setDurabilityValue(float value);

    public abstract int getMaxItemUseDuration();

//    public abstract void setMaxItemUseDuration(int duration);

    public abstract int getItemUseAction();

//    public abstract void setItemUseAction(int action);

    public abstract int getEnchantability();

//    public abstract void setEnchantability(int enchantability);

    public String getTexture() {
        return this.itemDisplay.texture == null ? "" : this.itemDisplay.texture;
    }

    public void setTexture(String texture){
        if(texture == null)
            texture = "";
        this.itemDisplay.texture = texture;
        saveItemData();
    }

    public boolean getDurabilityShow() {
        return this.itemDisplay.durabilityShow;
    }

    public void setDurabilityShow(boolean bo) {
        this.itemDisplay.durabilityShow = bo;
        saveItemData();
    }

    public int getDurabilityColor() {
        return this.itemDisplay.durabilityColor;
    }

    public void setDurabilityColor(int color) {
        this.itemDisplay.durabilityColor = color;
        saveItemData();
    }

    public int getColor() {
        return this.itemDisplay.itemColor;
    }

    public void setColor(int color) {
        this.itemDisplay.itemColor = color;
        saveItemData();
    }


    public void setRotation(float rotationX, float rotationY, float rotationZ){
        this.itemDisplay.rotationX = rotationX;
        this.itemDisplay.rotationY = rotationY;
        this.itemDisplay.rotationZ = rotationZ;
        saveItemData();
    }

    public void setRotationRate(float rotationXRate, float rotationYRate, float rotationZRate){
        this.itemDisplay.rotationXRate = rotationXRate;
        this.itemDisplay.rotationYRate = rotationYRate;
        this.itemDisplay.rotationZRate = rotationZRate;
        saveItemData();
    }

    public void setScale(float scaleX, float scaleY, float scaleZ){
        this.itemDisplay.scaleX = scaleX;
        this.itemDisplay.scaleY = scaleY;
        this.itemDisplay.scaleZ = scaleZ;
        saveItemData();
    }

    public void setTranslate(float translateX, float translateY, float translateZ){
        this.itemDisplay.translateX = translateX;
        this.itemDisplay.translateY = translateY;
        this.itemDisplay.translateZ = translateZ;
        saveItemData();
    }

    public float getRotationX() {
        return this.itemDisplay.rotationX;
    }

    public float getRotationY() {
        return this.itemDisplay.rotationY;
    }

    public float getRotationZ() {
        return this.itemDisplay.rotationZ;
    }

    public float getRotationXRate() {
        return this.itemDisplay.rotationXRate;
    }

    public float getRotationYRate() {
        return this.itemDisplay.rotationYRate;
    }

    public float getRotationZRate() {
        return this.itemDisplay.rotationZRate;
    }

    public float getScaleX() {
        return this.itemDisplay.scaleX;
    }

    public float getScaleY() {
        return this.itemDisplay.scaleY;
    }

    public float getScaleZ() {
        return this.itemDisplay.scaleZ;
    }

    public float getTranslateX() {
        return this.itemDisplay.translateX;
    }

    public float getTranslateY() {
        return this.itemDisplay.translateY;
    }

    public float getTranslateZ() {
        return this.itemDisplay.translateZ;
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
        compound.setBoolean("DurabilityShow",this.itemDisplay.durabilityShow);
        compound.setInteger("DurabilityColor",this.itemDisplay.durabilityColor );
        compound.setInteger("ItemColor",this.itemDisplay.itemColor);
        compound.setString("ItemTexture",this.itemDisplay.texture);

        compound.setFloat("RotationX",this.itemDisplay.rotationX);
        compound.setFloat("RotationY",this.itemDisplay.rotationY);
        compound.setFloat("RotationZ",this.itemDisplay.rotationZ);

        compound.setFloat("RotationXRate",this.itemDisplay.rotationXRate);
        compound.setFloat("RotationYRate",this.itemDisplay.rotationYRate);
        compound.setFloat("RotationZRate",this.itemDisplay.rotationZRate);

        compound.setFloat("ScaleX",this.itemDisplay.scaleX);
        compound.setFloat("ScaleY",this.itemDisplay.scaleY);
        compound.setFloat("ScaleZ",this.itemDisplay.scaleZ);

        compound.setFloat("TranslateX",this.itemDisplay.translateX);
        compound.setFloat("TranslateY",this.itemDisplay.translateY);
        compound.setFloat("TranslateZ",this.itemDisplay.translateZ);
        return compound;
    }

    public void setItemNBT(NBTTagCompound compound) {
        this.itemDisplay.durabilityShow = compound.getBoolean("DurabilityShow");
        if (compound.hasKey("DurabilityColor")) {
            this.itemDisplay.durabilityColor = compound.getInteger("DurabilityColor");
        }
        this.itemDisplay.itemColor = compound.getInteger("ItemColor");
        this.itemDisplay.texture = compound.getString("ItemTexture");

        this.itemDisplay.rotationX = compound.getFloat("RotationX");
        this.itemDisplay.rotationY = compound.getFloat("RotationY");
        this.itemDisplay.rotationZ = compound.getFloat("RotationZ");

        this.itemDisplay.rotationXRate = compound.getFloat("RotationXRate");
        this.itemDisplay.rotationYRate = compound.getFloat("RotationYRate");
        this.itemDisplay.rotationZRate = compound.getFloat("RotationZRate");

        this.itemDisplay.scaleX = compound.getFloat("ScaleX");
        this.itemDisplay.scaleY = compound.getFloat("ScaleY");
        this.itemDisplay.scaleZ = compound.getFloat("ScaleZ");

        this.itemDisplay.translateX = compound.getFloat("TranslateX");
        this.itemDisplay.translateY = compound.getFloat("TranslateY");
        this.itemDisplay.translateZ = compound.getFloat("TranslateZ");
    }

    public static class Display {
        public String texture = "minecraft:textures/items/iron_pickaxe.png";
        public Float translateX = 0F, translateY = 0F, translateZ = 0F;
        public Integer itemColor = 0x8B4513;
        public Float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;
        public Float rotationX = 0F, rotationY = 0F, rotationZ = 0F;
        public Float rotationXRate = 0F, rotationYRate = 0F, rotationZRate = 0F;
        public Boolean durabilityShow = false;
        public Integer durabilityColor = -1;
    }
}
