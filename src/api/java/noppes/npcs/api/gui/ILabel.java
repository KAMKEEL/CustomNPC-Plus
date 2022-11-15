//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.gui;

public interface ILabel extends ICustomGuiComponent {
    String getText();

    ILabel setText(String var1);

    int getWidth();

    int getHeight();

    ILabel setSize(int var1, int var2);

    float getScale();

    ILabel setScale(float var1);

    boolean getShadow();

    void setShadow(boolean shadow);
}
