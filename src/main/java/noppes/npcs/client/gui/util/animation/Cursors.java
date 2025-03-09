package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public enum Cursors {

    MOVE(0, 0, 0, 0, 19, 19);


    public int offsetX, offsetY, textureX, textureY, width, height;
    Cursors(int offsetX, int offsetY, int textureX, int textureY, int width, int height) {

        this.textureX = textureX;
        this.textureY = textureY;
        this.width = width;
        this.height = height;
        this.offsetX = -width / 2;
        this.offsetY = offsetY;
    }

    public void draw(double mouseX, double mouseY) {
        offsetX = width / 2;
        offsetY = height / 2;
        float scale = 0.5f;
        double screenX = (mouseX / scale - offsetX);
        double screenY = (mouseY / scale - offsetY);
        GL11.glColor4f(1,1,1,1);
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GuiUtil.drawTexturedModalRect(screenX, screenY, width, height, textureX, textureY);
        GL11.glPopMatrix();
    }

    public static Cursors currentCursor;

    public static void setCursor(Cursors cursor) {
        try {
            Cursor c = null;
            if (cursor != null)
                c = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
            Mouse.setNativeCursor(c);
        } catch (LWJGLException e) {
        }
        currentCursor = cursor;
    }

    private static ResourceLocation TEXTURE = new ResourceLocation("customnpcs:textures/gui/cursors.png");
}

