//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.scripted.gui.ScriptGuiComponent;

public interface ICustomGuiComponent {
    int getID();

    ICustomGuiComponent setID(int var1);

    int getPosX();

    int getPosY();

    ICustomGuiComponent setPos(int var1, int var2);

    boolean hasHoverText();

    String[] getHoverText();

    ICustomGuiComponent setHoverText(String var1);

    ICustomGuiComponent setHoverText(String[] var1);

    int getColor();

    ICustomGuiComponent setColor(int color);

     float getAlpha();

    void setAlpha(float alpha);

    float getRotation();

    void setRotation(float rotation);

    NBTTagCompound toNBT(NBTTagCompound nbt);

    ScriptGuiComponent fromNBT(NBTTagCompound nbt);
}
