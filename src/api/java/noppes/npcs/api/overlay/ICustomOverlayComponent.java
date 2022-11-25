package noppes.npcs.api.overlay;

import net.minecraft.nbt.NBTTagCompound;

public interface ICustomOverlayComponent {
    int getID();

    ICustomOverlayComponent setID(int id);

    int getPosX();

    int getPosY();

    ICustomOverlayComponent setPos(int x, int y);

    int getAlignment();

    void setAlignment(int alignment);

    int getColor();

    ICustomOverlayComponent setColor(int color);

    float getAlpha();

    void setAlpha(float alpha);

    float getRotation();

    void setRotation(float rotation);

    NBTTagCompound toNBT(NBTTagCompound nbt);

    ICustomOverlayComponent fromNBT(NBTTagCompound nbt);
}
