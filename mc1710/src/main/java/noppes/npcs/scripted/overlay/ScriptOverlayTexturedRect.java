package noppes.npcs.scripted.overlay;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.overlay.IOverlayTexturedRect;

public class ScriptOverlayTexturedRect extends ScriptOverlayComponent implements IOverlayTexturedRect {
    int width;
    int height;
    int textureX;
    int textureY;
    float scale;
    String texture;

    public ScriptOverlayTexturedRect() {
        this.textureY = -1;
        this.scale = 1.0F;
    }

    public ScriptOverlayTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this.textureY = -1;
        this.scale = 1.0F;
        this.setID(id);
        this.setTexture(texture);
        this.setPos(x, y);
        this.setSize(width, height);
    }

    public ScriptOverlayTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        this(id, texture, x, y, width, height);
        this.setTextureOffset(textureX, textureY);
    }

    public String getTexture() {
        return this.texture;
    }

    public IOverlayTexturedRect setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public IOverlayTexturedRect setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public float getScale() {
        return this.scale;
    }

    public IOverlayTexturedRect setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public int getTextureX() {
        return this.textureX;
    }

    public int getTextureY() {
        return this.textureY;
    }

    public IOverlayTexturedRect setTextureOffset(int offsetX, int offsetY) {
        this.textureX = offsetX;
        this.textureY = offsetY;
        return this;
    }

    @Override
    public int getType() {
        return 0;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        nbt.setFloat("scale", this.scale);
        nbt.setString("texture", this.texture);
        if (this.textureX >= 0 && this.textureY >= 0) {
            nbt.setIntArray("texPos", new int[]{this.textureX, this.textureY});
        }

        return nbt;
    }

    public ScriptOverlayComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        this.setScale(nbt.getFloat("scale"));
        this.setTexture(nbt.getString("texture"));
        if (nbt.hasKey("texPos")) {
            this.setTextureOffset(nbt.getIntArray("texPos")[0], nbt.getIntArray("texPos")[1]);
        }

        return this;
    }
}
