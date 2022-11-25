package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ILabel;

public class ScriptGuiLabel extends ScriptGuiComponent implements ILabel {
    String label;
    int width;
    int height;
    float scale;
    boolean shadow;

    public ScriptGuiLabel() {
        this.scale = 1.0F;
    }

    public ScriptGuiLabel(int id, String label, int x, int y, int width, int height) {
        this.scale = 1.0F;
        this.setID(id);
        this.setText(label);
        this.setPos(x, y);
        this.setSize(width, height);
        this.setShadow(false);
    }

    public ScriptGuiLabel(int id, String label, int x, int y, int width, int height, int color) {
        this(id, label, x, y, width, height);
        this.setColor(color);
    }

    public String getText() {
        return this.label;
    }

    public ILabel setText(String label) {
        this.label = label;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ILabel setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public float getScale() {
        return this.scale;
    }

    public ILabel setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public boolean getShadow(){
        return this.shadow;
    }

    public void setShadow(boolean shadow){
        this.shadow = shadow;
    }

    public int getType() {
        return 1;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setString("label", this.label);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        nbt.setFloat("scale", this.scale);
        nbt.setBoolean("shadow",this.shadow);
        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setText(nbt.getString("label"));
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        this.setScale(nbt.getFloat("scale"));
        this.setShadow(nbt.getBoolean("shadow"));
        return this;
    }
}
