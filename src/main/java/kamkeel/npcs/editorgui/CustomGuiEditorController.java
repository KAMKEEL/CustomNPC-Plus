package kamkeel.npcs.editorgui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.util.NBTJsonUtil;
import java.io.File;

/**
 * CustomGuiEditorController handles saving and loading of ScriptGui objects.
 */
public class CustomGuiEditorController {
    private ScriptGui currentGui;

    public CustomGuiEditorController() {
        currentGui = new ScriptGui();
    }

    public void saveGui(File file, ScriptGui gui) {
        NBTTagCompound compound = gui.toNBT();
        try {
            NBTJsonUtil.SaveFile(file, compound);
        } catch(Exception e) {
            e.printStackTrace();
        }
        currentGui = gui;
    }

    public ScriptGui loadGui(File file) {
        try {
            NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
            currentGui = (ScriptGui) (new ScriptGui()).fromNBT(compound);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return currentGui;
    }

    public void applyGuiToPlayer(EntityPlayer player) {
        // player.showCustomGui(currentGui);
    }
}
