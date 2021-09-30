//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.gui;

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
}
