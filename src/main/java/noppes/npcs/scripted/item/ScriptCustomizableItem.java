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
        this.setBoolean(compound, "DurabilityShow",this.itemDisplay.durabilityShow);
        this.setInteger(compound, "DurabilityColor",this.itemDisplay.durabilityColor);
        this.setInteger(compound, "ItemColor",this.itemDisplay.itemColor);
        if (this.itemDisplay.texture != null) {
            compound.setString("ItemTexture", this.itemDisplay.texture);
        }

        this.setFloat(compound, "RotationX",this.itemDisplay.rotationX);
        this.setFloat(compound, "RotationY",this.itemDisplay.rotationY);
        this.setFloat(compound, "RotationZ",this.itemDisplay.rotationZ);

        this.setFloat(compound, "RotationXRate",this.itemDisplay.rotationXRate);
        this.setFloat(compound, "RotationYRate",this.itemDisplay.rotationYRate);
        this.setFloat(compound, "RotationZRate",this.itemDisplay.rotationZRate);

        this.setFloat(compound, "ScaleX",this.itemDisplay.scaleX);
        this.setFloat(compound, "ScaleY",this.itemDisplay.scaleY);
        this.setFloat(compound, "ScaleZ",this.itemDisplay.scaleZ);

        this.setFloat(compound, "TranslateX",this.itemDisplay.translateX);
        this.setFloat(compound, "TranslateY",this.itemDisplay.translateY);
        this.setFloat(compound, "TranslateZ",this.itemDisplay.translateZ);
        return compound;
    }

    private void setBoolean(NBTTagCompound compound, String key, Boolean value) {
        if (value != null) {
            compound.setBoolean(key, value);
        }
    }

    private void setInteger(NBTTagCompound compound, String key, Integer value) {
        if (value != null) {
            compound.setInteger(key, value);
        }
    }

    private void setFloat(NBTTagCompound compound, String key, Float value) {
        if (value != null) {
            compound.setFloat(key, value);
        }
    }

    public void setItemNBT(NBTTagCompound compound) {
        if (compound.hasKey("DurabilityShow")) {
            this.itemDisplay.durabilityShow = compound.getBoolean("DurabilityShow");
        }
        if (compound.hasKey("DurabilityColor")) {
            this.itemDisplay.durabilityColor = compound.getInteger("DurabilityColor");
        }
        if (compound.hasKey("ItemColor")) {
            this.itemDisplay.itemColor = compound.getInteger("ItemColor");
        }
        if (compound.hasKey("ItemTexture")) {
            this.itemDisplay.texture = compound.getString("ItemTexture");
        }

        if (compound.hasKey("RotationX")) {
            this.itemDisplay.rotationX = compound.getFloat("RotationX");
        }
        if (compound.hasKey("RotationY")) {
            this.itemDisplay.rotationY = compound.getFloat("RotationY");
        }
        if (compound.hasKey("RotationZ")) {
            this.itemDisplay.rotationZ = compound.getFloat("RotationZ");
        }

        if (compound.hasKey("RotationXRate")) {
            this.itemDisplay.rotationXRate = compound.getFloat("RotationXRate");
        }
        if (compound.hasKey("RotationYRate")) {
            this.itemDisplay.rotationYRate = compound.getFloat("RotationYRate");
        }
        if (compound.hasKey("RotationZRate")) {
            this.itemDisplay.rotationZRate = compound.getFloat("RotationZRate");
        }

        if (compound.hasKey("ScaleX")) {
            this.itemDisplay.scaleX = compound.getFloat("ScaleX");
        }
        if (compound.hasKey("ScaleY")) {
            this.itemDisplay.scaleY = compound.getFloat("ScaleY");
        }
        if (compound.hasKey("ScaleZ")) {
            this.itemDisplay.scaleZ = compound.getFloat("ScaleZ");
        }

        if (compound.hasKey("TranslateX")) {
            this.itemDisplay.translateX = compound.getFloat("TranslateX");
        }
        if (compound.hasKey("TranslateY")) {
            this.itemDisplay.translateY = compound.getFloat("TranslateY");
        }
        if (compound.hasKey("TranslateZ")) {
            this.itemDisplay.translateZ = compound.getFloat("TranslateZ");
        }
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
