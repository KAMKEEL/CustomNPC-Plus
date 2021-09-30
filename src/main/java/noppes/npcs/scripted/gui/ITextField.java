//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.gui;

import noppes.npcs.scripted.gui.ICustomGuiComponent;

public interface ITextField extends ICustomGuiComponent {
    int getWidth();

    int getHeight();

    ITextField setSize(int var1, int var2);

    String getText();

    ITextField setText(String var1);
}
