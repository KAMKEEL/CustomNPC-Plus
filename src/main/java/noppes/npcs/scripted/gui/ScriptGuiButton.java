package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGuiComponent;

public class ScriptGuiButton extends ScriptGuiComponent implements IButton {
    int width;
    int height;
    String label;
    String texture;
    int textureX;
    int textureY;

    public ScriptGuiButton() {
        this.height = -1;
        this.textureY = -1;
    }

    public ScriptGuiButton(int id, String label, int x, int y) {
        this.height = -1;
        this.textureY = -1;
        this.setID(id);
        this.setLabel(label);
        this.setPos(x, y);
    }

    public ScriptGuiButton(int id, String label, int x, int y, int width, int height) {
        this(id, label, x, y);
        this.setSize(width, height);
    }

    public ScriptGuiButton(int id, String label, int x, int y, int width, int height, String texture) {
        this(id, label, x, y, width, height);
        this.setTexture(texture);
    }

    public ScriptGuiButton(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        this(id, label, x, y, width, height, texture);
        this.setTextureOffset(textureX, textureY);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public IButton setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public IButton setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getTexture() {
        return this.texture;
    }

    public boolean hasTexture() {
        return this.texture != null;
    }

    public IButton setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public int getTextureX() {
        return this.textureX;
    }

    public int getTextureY() {
        return this.textureY;
    }

    public IButton setTextureOffset(int textureX, int textureY) {
        this.textureX = textureX;
        this.textureY = textureY;
        return this;
    }

    public int getID() {
        return this.id;
    }

    public ICustomGuiComponent setID(int id) {
        this.id = id;
        return this;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public int getType() {
        return 0;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        if (this.width > 0 && this.height > 0) {
            nbt.setIntArray("size", new int[]{this.width, this.height});
        }

        nbt.setString("label", this.label);
        if (this.hasTexture()) {
            nbt.setString("texture", this.texture);
        }

        if (this.textureX >= 0 && this.textureY >= 0) {
            nbt.setIntArray("texPos", new int[]{this.textureX, this.textureY});
        }

        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        if (nbt.hasKey("size")) {
            this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        }

        this.setLabel(nbt.getString("label"));
        if (nbt.hasKey("texture")) {
            this.setTexture(nbt.getString("texture"));
        }

        if (nbt.hasKey("texPos")) {
            this.setTextureOffset(nbt.getIntArray("texPos")[0], nbt.getIntArray("texPos")[1]);
        }

        return this;
    }
}
