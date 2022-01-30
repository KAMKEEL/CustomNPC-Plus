package noppes.npcs.client.gui.customoverlay.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.customoverlay.interfaces.IOverlayComponent;
import noppes.npcs.scripted.interfaces.ICustomOverlayComponent;
import noppes.npcs.scripted.overlay.ScriptOverlayLine;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class CustomOverlayLine extends Gui implements IOverlayComponent {
    OverlayCustom parent;
    int alignment;
    int id;

    int x1;
    int y1;
    int x2;
    int y2;
    int color;
    int thickness;
    float alpha;

    public CustomOverlayLine(int id, int x1, int y1, int x2, int y2, int color, int thickness){
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.thickness = thickness;
    }

    public void setParent(OverlayCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, float partialTicks) {
        double distance = Math.hypot(x1-x2, y1-y2);

        mc.getTextureManager().bindTexture(new ResourceLocation("customnpcs:textures/gui/misc.png"));
        GL11.glPushMatrix();
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8  & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red,green,blue,this.alpha);

            GL11.glTranslatef(this.alignment%3*((float)(OverlayCustom.scaledWidth)/2), (float) (Math.floor((float)(alignment/3))*((float)(OverlayCustom.scaledHeight)/2)),0.0F);//alignment%3 * width/2  Math.floor(alignment/3) * height/2
            GL11.glTranslatef(this.x1,this.y1,0.0F);

            GL11.glRotated(-Math.toDegrees(Math.atan2(x2-x1,y2-y1)),0.0F,0.0F,1.0F);
            GL11.glScaled(thickness, distance,0.0D);
            this.drawTexturedModalRect(0,0,0,0, 1, 1);
        GL11.glPopMatrix();
    }

    public ICustomOverlayComponent toComponent() {
        ScriptOverlayLine line = new ScriptOverlayLine(this.id, this.x1,this.y1,this.x2,this.y2,this.color,this.thickness);
        line.setAlignment(alignment);
        line.setAlpha(alpha);
        line.setColor(color);
        return line;
    }

    public static CustomOverlayLine fromComponent(ScriptOverlayLine component) {
        CustomOverlayLine line = new CustomOverlayLine(component.getID(), component.getX1(), component.getY1(), component.getX2(), component.getY2(), component.getColor(), component.getThickness());
        line.alignment = component.getAlignment();
        line.alpha = component.getAlpha();
        line.color = component.getColor();

        return line;
    }
}
