//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.interfaces;

import net.minecraft.client.Minecraft;
import noppes.npcs.api.gui.ICustomGuiComponent;

public interface IGuiComponent {
    int getID();

    void onRender(Minecraft var1, int var2, int var3, int var4, float var5);

    ICustomGuiComponent toComponent();
}
