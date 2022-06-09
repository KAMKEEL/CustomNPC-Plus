package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class DialogImage {
    public int id;
    public String texture = "";
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;
    public int textureX = 0;
    public int textureY = 0;
    public float scale = 1.0F;

    public int color = 0xFFFFFF;
    public int selectedColor = 0xFFFFFF;
    public float alpha = 1.0F;
    public float rotation = 0;

    public int imageType = 1; //0 - Default, 1 - Text, 2 - Option
    public int alignment = 0;

    public DialogImage(int id) {
        this.id = id;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("ID", id);
        compound.setString("Texture", texture);
        compound.setInteger("PosX", x);
        compound.setInteger("PosY", y);
        compound.setInteger("Width", width);
        compound.setInteger("Height", height);
        compound.setInteger("TextureX", textureX);
        compound.setInteger("TextureY", textureY);
        compound.setFloat("Scale", scale);
        compound.setInteger("Color", color);
        compound.setInteger("SelectedColor", selectedColor);
        compound.setFloat("Alpha", alpha);
        compound.setFloat("Rotation", rotation);
        compound.setInteger("ImageType", imageType);
        compound.setInteger("Alignment", alignment);

        return compound;
    }

    public void readNBT(NBTTagCompound compound) {
        id = compound.getInteger("ID");
        texture = compound.getString("Texture");
        x = compound.getInteger("PosX");
        y = compound.getInteger("PosY");
        width = compound.getInteger("Width");
        height = compound.getInteger("Height");
        textureX = compound.getInteger("TextureX");
        textureY = compound.getInteger("TextureY");
        scale = compound.getFloat("Scale");
        color = compound.getInteger("Color");
        selectedColor = compound.getInteger("SelectedColor");
        alpha = compound.getFloat("Alpha");
        rotation = compound.getFloat("Rotation");
        imageType = compound.getInteger("ImageType");
        alignment = compound.getInteger("Alignment");
    }
}
