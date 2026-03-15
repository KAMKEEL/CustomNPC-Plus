package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ITexturedRect;

public class ScriptGuiTexturedRect extends ScriptGuiComponent implements ITexturedRect {
    int width;
    int height;
    int textureX;
    int textureY;
    float scale;
    String texture;

    // Animation
    boolean animated = false;
    int frameCount = 1;
    int frametime = 2;

    public ScriptGuiTexturedRect() {
        this.textureY = -1;
        this.scale = 1.0F;
    }

    public ScriptGuiTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this.textureY = -1;
        this.scale = 1.0F;
        this.setID(id);
        this.setTexture(texture);
        this.setPos(x, y);
        this.setSize(width, height);
    }

    public ScriptGuiTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        this(id, texture, x, y, width, height);
        this.setTextureOffset(textureX, textureY);
    }

    public String getTexture() {
        return this.texture;
    }

    public ITexturedRect setTexture(String texture) {
        this.texture = texture;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ITexturedRect setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public float getScale() {
        return this.scale;
    }

    public ITexturedRect setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public int getTextureX() {
        return this.textureX;
    }

    public int getTextureY() {
        return this.textureY;
    }

    public ITexturedRect setTextureOffset(int offsetX, int offsetY) {
        this.textureX = offsetX;
        this.textureY = offsetY;
        return this;
    }

    public boolean isAnimated() {
        return this.animated && this.frameCount > 1;
    }

    public int getFrameCount() {
        return this.frameCount;
    }

    public int getFrameTime() {
        return this.frametime;
    }

    public ITexturedRect setAnimation(int frameCount, int frametime) {
        this.animated = frameCount > 1;
        this.frameCount = Math.max(1, frameCount);
        this.frametime = Math.max(1, frametime);
        return this;
    }

    public int getType() {
        return 2;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        nbt.setFloat("scale", this.scale);
        nbt.setString("texture", this.texture);
        if (this.textureX >= 0 && this.textureY >= 0) {
            nbt.setIntArray("texPos", new int[]{this.textureX, this.textureY});
        }

        if (this.animated) {
            nbt.setBoolean("animated", true);
            nbt.setInteger("frameCount", this.frameCount);
            nbt.setInteger("frameTime", this.frametime);
        }

        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        this.setScale(nbt.getFloat("scale"));
        this.setTexture(nbt.getString("texture"));
        if (nbt.hasKey("texPos")) {
            this.setTextureOffset(nbt.getIntArray("texPos")[0], nbt.getIntArray("texPos")[1]);
        }

        if (nbt.hasKey("animated") && nbt.getBoolean("animated")) {
            this.setAnimation(
                nbt.hasKey("frameCount") ? Math.max(1, nbt.getInteger("frameCount")) : 1,
                nbt.hasKey("frameTime") ? Math.max(1, nbt.getInteger("frameTime")) : 2
            );
        }

        return this;
    }
}
