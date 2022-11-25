//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.gui;

public interface IButton extends ICustomGuiComponent {
    int getWidth();

    int getHeight();

    IButton setSize(int var1, int var2);

    String getLabel();

    IButton setLabel(String var1);

    String getTexture();

    boolean hasTexture();

    IButton setTexture(String var1);

    int getTextureX();

    int getTextureY();

    IButton setTextureOffset(int var1, int var2);
}
