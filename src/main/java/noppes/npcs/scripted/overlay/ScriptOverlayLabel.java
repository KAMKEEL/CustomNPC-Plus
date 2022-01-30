package noppes.npcs.scripted.overlay;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.scripted.gui.ScriptGuiComponent;
import noppes.npcs.scripted.interfaces.ILabel;
import noppes.npcs.scripted.interfaces.IOverlayLabel;

public class ScriptOverlayLabel extends ScriptOverlayComponent implements IOverlayLabel {
    String label;
    int width;
    int height;
    int color;
    float scale;
    boolean shadow;

    public ScriptOverlayLabel() {
        this.color = 16777215;
        this.scale = 1.0F;
    }

    public ScriptOverlayLabel(int id, String label, int x, int y, int width, int height) {
        this.color = 16777215;
        this.scale = 1.0F;
        this.setID(id);
        this.setText(label);
        this.setPos(x, y);
        this.setSize(width, height);
        this.enableShadow(false);
    }

    public ScriptOverlayLabel(int id, String label, int x, int y, int width, int height, int color) {
        this(id, label, x, y, width, height);
        this.setColor(color);
    }

    public String getText() {
        return this.label;
    }

    public IOverlayLabel setText(String label) {
        this.label = label;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public IOverlayLabel setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public int getColor() {
        return this.color;
    }

    public IOverlayLabel setColor(int color) {
        this.color = color;
        return this;
    }

    public float getScale() {
        return this.scale;
    }

    public IOverlayLabel setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public boolean hasShadow(){
        return this.shadow;
    }

    public void enableShadow(boolean shadow){
        this.shadow = shadow;
    }

    @Override
    public int getType() {
        return 1;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setString("label", this.label);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        nbt.setInteger("color", this.color);
        nbt.setFloat("scale", this.scale);
        nbt.setBoolean("shadow",this.shadow);
        return nbt;
    }

    public ScriptOverlayLabel fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setText(nbt.getString("label"));
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        this.setColor(nbt.getInteger("color"));
        this.setScale(nbt.getFloat("scale"));
        this.enableShadow(this.shadow);
        return this;
    }
}
