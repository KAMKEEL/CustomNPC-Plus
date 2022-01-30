package noppes.npcs.scripted.overlay;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.scripted.gui.ScriptGuiComponent;
import noppes.npcs.scripted.interfaces.ILabel;
import noppes.npcs.scripted.interfaces.IOverlayLabel;

public class ScriptOverlayLabel extends ScriptOverlayComponent implements IOverlayLabel {
    String label;
    int width;
    int height;
    float scale;
    boolean shadow;

    boolean randomStyle = false;
    boolean boldStyle = false;
    boolean italicStyle = false;
    boolean underlineStyle = false;
    boolean strikethroughStyle = false;

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
        this.setShadow(false);
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

    public float getScale() {
        return this.scale;
    }

    public IOverlayLabel setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public boolean getShadow(){
        return this.shadow;
    }

    public void setShadow(boolean shadow){
        this.shadow = shadow;
    }

    public void setRandomStyle(boolean randomStyle){
        this.randomStyle = randomStyle;
    }
    public void setUnderlineStyle(boolean underlineStyle){
        this.underlineStyle = underlineStyle;
    }
    public void setBoldStyle(boolean boldStyle){
        this.boldStyle = boldStyle;
    }
    public void setItalicStyle(boolean italicStyle){
        this.italicStyle = italicStyle;
    }
    public void setStrikethroughStyle(boolean strikethroughStyle){
        this.strikethroughStyle = strikethroughStyle;
    }

    public boolean getRandomStyle(){
        return this.randomStyle;
    }
    public boolean getUnderlineStyle(){
        return this.underlineStyle;
    }
    public boolean getBoldStyle(){
        return this.boldStyle;
    }
    public boolean getItalicStyle(){
        return this.italicStyle;
    }
    public boolean getStrikethroughStyle(){
        return this.strikethroughStyle;
    }

    @Override
    public int getType() {
        return 1;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setString("label", this.label);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        nbt.setFloat("scale", this.scale);
        nbt.setBoolean("shadow",this.shadow);

        nbt.setBoolean("strikethrough",this.strikethroughStyle);
        nbt.setBoolean("bold",this.boldStyle);
        nbt.setBoolean("italic",this.italicStyle);
        nbt.setBoolean("random",this.randomStyle);
        nbt.setBoolean("underline",this.underlineStyle);
        return nbt;
    }

    public ScriptOverlayLabel fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setText(nbt.getString("label"));
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        this.setScale(nbt.getFloat("scale"));
        this.setShadow(nbt.getBoolean("shadow"));

        this.setStrikethroughStyle(nbt.getBoolean("strikethrough"));
        this.setBoldStyle(nbt.getBoolean("bold"));
        this.setItalicStyle(nbt.getBoolean("italic"));
        this.setRandomStyle(nbt.getBoolean("random"));
        this.setUnderlineStyle(nbt.getBoolean("underline"));
        return this;
    }
}
