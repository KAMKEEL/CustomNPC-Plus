//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.gui;

import noppes.npcs.scripted.gui.ICustomGuiComponent;

public interface ITexturedRect extends ICustomGuiComponent {
    String getTexture();

    ITexturedRect setTexture(String var1);

    int getWidth();

    int getHeight();

    ITexturedRect setSize(int var1, int var2);

    float getScale();

    ITexturedRect setScale(float var1);

    int getTextureX();

    int getTextureY();

    ITexturedRect setTextureOffset(int var1, int var2);
}
