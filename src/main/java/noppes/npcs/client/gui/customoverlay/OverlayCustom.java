package noppes.npcs.client.gui.customoverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.customoverlay.components.CustomOverlayLabel;
import noppes.npcs.client.gui.customoverlay.components.CustomOverlayLine;
import noppes.npcs.client.gui.customoverlay.components.CustomOverlayTexturedRect;
import noppes.npcs.client.gui.customoverlay.interfaces.IOverlayComponent;
import noppes.npcs.scripted.interfaces.ICustomOverlayComponent;
import noppes.npcs.scripted.overlay.*;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OverlayCustom extends Gui {
    protected final Minecraft mc;

    private ScaledResolution res = null;
    public static int scaledWidth = 0;
    public static int scaledHeight = 0;

    public ScriptOverlay overlay;
    Map<Integer, IOverlayComponent> components = new HashMap();

    float scale = 1.0F;
    int x = 0;
    int y = 0;
    int textureX = 0;
    int textureY = 0;
    int width = 200;
    int height = 200;

    public OverlayCustom(Minecraft mc) {
        this.mc = mc;
    }

    public void initOverlay(){
        res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        if(this.overlay != null){
            scaledWidth = res.getScaledWidth();
            scaledHeight = res.getScaledHeight();

            this.components.clear();
            Iterator var1 = this.overlay.getComponents().iterator();

            while(var1.hasNext()) {
                ICustomOverlayComponent c = (ICustomOverlayComponent)var1.next();
                this.addComponent(c);
            }
        }
    }

    private void addComponent(ICustomOverlayComponent component) {
        ScriptOverlayComponent c = (ScriptOverlayComponent)component;
        switch(c.getType()) {
            case 0:
                CustomOverlayTexturedRect rect = CustomOverlayTexturedRect.fromComponent((ScriptOverlayTexturedRect) component);
                rect.setParent(this);
                this.components.put(rect.getID(), rect);
                break;
            case 1:
                CustomOverlayLabel lbl = CustomOverlayLabel.fromComponent((ScriptOverlayLabel)component);
                lbl.setParent(this);
                this.components.put(lbl.getID(), lbl);
                break;
            case 2:
                CustomOverlayLine line = CustomOverlayLine.fromComponent((ScriptOverlayLine) component);
                line.setParent(this);
                this.components.put(line.getID(), line);
                break;
        }
    }

    public void renderGameOverlay(float partialTicks){
        res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        scaledWidth = res.getScaledWidth();
        scaledHeight = res.getScaledHeight();

        GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_BLEND);

            Iterator var4 = this.components.values().iterator();

            while(var4.hasNext()) {
                IOverlayComponent component = (IOverlayComponent)var4.next();
                component.onRender(this.mc, partialTicks);
            }

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public void setOverlayData(NBTTagCompound compound) {
        this.overlay = (ScriptOverlay)(new ScriptOverlay()).fromNBT(compound);
        this.initOverlay();
    }
}
