package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ITextField;

public class ScriptGuiTextField extends ScriptGuiComponent implements ITextField {
    int width;
    int height;
    String defaultText;

    public ScriptGuiTextField() {
    }

    public ScriptGuiTextField(int id, int x, int y, int width, int height) {
        this.setID(id);
        this.setPos(x, y);
        this.setSize(width, height);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ITextField setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public String getText() {
        return this.defaultText;
    }

    public ITextField setText(String defaultText) {
        this.defaultText = defaultText;
        return this;
    }

    public int getType() {
        return 3;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        if (this.defaultText != null && !this.defaultText.isEmpty()) {
            nbt.setString("default", this.defaultText);
        }

        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        if (nbt.hasKey("default")) {
            this.setText(nbt.getString("default"));
        }

        return this;
    }
}
