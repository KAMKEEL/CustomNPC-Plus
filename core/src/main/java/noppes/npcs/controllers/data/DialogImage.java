package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.IDialogImage;
import noppes.npcs.platform.nbt.INBTCompound;

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

    public INBTCompound writeToNBT(INBTCompound compound) {
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

    public void readNBT(INBTCompound compound) {
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

    public int getId() {
        return id;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getTexture() {
        return texture;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setWidthHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setTextureOffset(int offsetX, int offsetY) {
        this.textureX = offsetX;
        this.textureY = offsetY;
    }

    public int getTextureX() {
        return textureX;
    }

    public int getTextureY() {
        return textureY;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public int getImageType() {
        return imageType;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getAlignment() {
        return alignment;
    }
}
