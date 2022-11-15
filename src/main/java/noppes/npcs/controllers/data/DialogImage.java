package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IDialogImage;

public class DialogImage implements IDialogImage {
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

    public DialogImage() {
    }

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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setTexture(String texture) {
        this.texture = texture;
    }

    @Override
    public String getTexture() {
        return texture;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setWidthHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setTextureOffset(int offsetX, int offsetY) {
        this.textureX = offsetX;
        this.textureY = offsetY;
    }

    @Override
    public int getTextureX() {
        return textureX;
    }

    @Override
    public int getTextureY() {
        return textureY;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setSelectedColor(int color) {
        this.selectedColor = color;
    }

    @Override
    public int getSelectedColor() {
        return selectedColor;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    @Override
    public int getImageType() {
        return imageType;
    }

    @Override
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    @Override
    public int getAlignment() {
        return alignment;
    }
}
