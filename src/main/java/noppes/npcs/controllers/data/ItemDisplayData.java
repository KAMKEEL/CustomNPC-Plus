package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class ItemDisplayData {
    public String texture = "minecraft:textures/items/iron_pickaxe.png";
    public Float translateX = 0F, translateY = 0F, translateZ = 0F;
    public Integer itemColor = 0x8B4513;
    public Float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;
    public Float rotationX = 0F, rotationY = 0F, rotationZ = 0F;
    public Float rotationXRate = 0F, rotationYRate = 0F, rotationZRate = 0F;
    public Boolean durabilityShow = false;
    public Integer durabilityColor = -1;

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return compound;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        this.setBoolean(compound, "DurabilityShow", this.durabilityShow);
        this.setInteger(compound, "DurabilityColor", this.durabilityColor);
        this.setInteger(compound, "ItemColor", this.itemColor);
        if (this.texture != null) {
            compound.setString("ItemTexture", this.texture);
        }

        this.setFloat(compound, "RotationX", this.rotationX);
        this.setFloat(compound, "RotationY", this.rotationY);
        this.setFloat(compound, "RotationZ", this.rotationZ);

        this.setFloat(compound, "RotationXRate", this.rotationXRate);
        this.setFloat(compound, "RotationYRate", this.rotationYRate);
        this.setFloat(compound, "RotationZRate", this.rotationZRate);

        this.setFloat(compound, "ScaleX", this.scaleX);
        this.setFloat(compound, "ScaleY", this.scaleY);
        this.setFloat(compound, "ScaleZ", this.scaleZ);

        this.setFloat(compound, "TranslateX", this.translateX);
        this.setFloat(compound, "TranslateY", this.translateY);
        this.setFloat(compound, "TranslateZ", this.translateZ);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("DurabilityShow")) {
            this.durabilityShow = compound.getBoolean("DurabilityShow");
        }
        if (compound.hasKey("DurabilityColor")) {
            this.durabilityColor = compound.getInteger("DurabilityColor");
        }
        if (compound.hasKey("ItemColor")) {
            this.itemColor = compound.getInteger("ItemColor");
        }
        if (compound.hasKey("ItemTexture")) {
            this.texture = compound.getString("ItemTexture");
        }
        if (compound.hasKey("RotationX")) {
            this.rotationX = compound.getFloat("RotationX");
        }
        if (compound.hasKey("RotationY")) {
            this.rotationY = compound.getFloat("RotationY");
        }
        if (compound.hasKey("RotationZ")) {
            this.rotationZ = compound.getFloat("RotationZ");
        }

        if (compound.hasKey("RotationXRate")) {
            this.rotationXRate = compound.getFloat("RotationXRate");
        }
        if (compound.hasKey("RotationYRate")) {
            this.rotationYRate = compound.getFloat("RotationYRate");
        }
        if (compound.hasKey("RotationZRate")) {
            this.rotationZRate = compound.getFloat("RotationZRate");
        }

        if (compound.hasKey("ScaleX")) {
            this.scaleX = compound.getFloat("ScaleX");
        }
        if (compound.hasKey("ScaleY")) {
            this.scaleY = compound.getFloat("ScaleY");
        }
        if (compound.hasKey("ScaleZ")) {
            this.scaleZ = compound.getFloat("ScaleZ");
        }

        if (compound.hasKey("TranslateX")) {
            this.translateX = compound.getFloat("TranslateX");
        }
        if (compound.hasKey("TranslateY")) {
            this.translateY = compound.getFloat("TranslateY");
        }
        if (compound.hasKey("TranslateZ")) {
            this.translateZ = compound.getFloat("TranslateZ");
        }
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
}
