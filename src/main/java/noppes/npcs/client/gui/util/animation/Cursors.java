package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public enum Cursors {

    MOVE(0, 0, 0, 0, 19, 19, 0.5f), SLIDE(0, 0, 0, 20, 16, 8, 0.66f), EMPTY(0, 0, 0, 0, 0, 0, 0);


    public int offsetX, offsetY, textureX, textureY, width, height;
    public float scale;

    Cursors(int offsetX, int offsetY, int textureX, int textureY, int width, int height, float scale) {

        this.textureX = textureX;
        this.textureY = textureY;
        this.width = width;
        this.height = height;
        this.offsetX = -width / 2;
        this.offsetY = offsetY;
        this.scale = scale;
    }

    public void draw(double mouseX, double mouseY) {
        offsetX = width / 2;
        offsetY = height / 2;
        double screenX = (mouseX / scale - offsetX);
        double screenY = (mouseY / scale - offsetY);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GuiUtil.drawTexturedModalRect(screenX, screenY, width, height, textureX, textureY);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static Cursors currentCursor;
    public static Supplier<Boolean> condition;

    public static void setCursor(Cursors cursor, Supplier<Boolean> con) {
        try {
            Cursor c = null;
            if (cursor != null)
                c = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
            Mouse.setNativeCursor(c);
        } catch (LWJGLException e) {
        }
        currentCursor = cursor;
        condition = con;
    }

    public static void reset() {
        setCursor(null, null);
        condition = null;
    }

    public static void tick() {
        if (condition != null && !condition.get())
            reset();
    }

    public static Cursors currentCursor() {
        return currentCursor;
    }

    private static ResourceLocation TEXTURE = new ResourceLocation("customnpcs:textures/gui/cursors.png");
}

