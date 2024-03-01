package noppes.npcs.client.gui.util;

import kamkeel.util.TextSplitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiNpcButton extends GuiButton{

	protected String[] display;
	private int displayValue = 0;
	public int id;
    public String hoverableText = "";
    public int oldHover;
    public int hoverCount = 0;

	public GuiNpcButton(int i, int j, int k,  String s) {
		super(i, j, k,  StatCollector.translateToLocal(s));
		id = i;
	}
	public GuiNpcButton(int i, int j, int k,  String[] display, int val) {
		this(i, j, k, display[val]);
		this.display = display;
		this.displayValue = val;
	}
	public GuiNpcButton(int i, int j, int k, int l, int m, String string) {
		super(i, j, k, l, m, StatCollector.translateToLocal(string));
		id = i;
	}
	public GuiNpcButton(int i, int j, int k, int l, int m, String[] display,
			int val) {
		this(i, j, k, l, m, display.length == 0?"":display[val % display.length]);
		this.display = display;
		this.displayValue = display.length == 0?0:val % display.length;
	}

    public GuiNpcButton(int i, int j, int k, int l, int m, Enum<?>[] displayEnums,
                        int val) {
        this(i, j, k, l, m, displayEnums.length == 0 ? "" : displayEnums[val % displayEnums.length].toString());

        ArrayList<String> strings = new ArrayList<>();
        for (Enum<?> e : displayEnums) {
            strings.add(e.toString());
        }

        this.display = strings.toArray(new String[0]);
        this.displayValue = display.length == 0?0:val % display.length;
    }

	public void setDisplayText(String text){
		this.displayString = StatCollector.translateToLocal(text);
	}
    public void setHoverText(String text){
        this.hoverableText = StatCollector.translateToLocal(text);
    }
	public int getValue(){
		return displayValue;
	}

	public void setEnabled(boolean bo){
		this.enabled = bo;
	}
	public void setVisible(boolean b) {
		this.visible = b;
	}
    public boolean getVisible() {
		return visible;
	}

    public void setDisplay(int value){
    	this.displayValue = value;
    	this.setDisplayText(display[value]);
    }

    public void setTextColor(int color){
    	this.packedFGColour = color;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
    	boolean bo = super.mousePressed(minecraft, i, j);
    	if(bo && display != null && display.length != 0){
    		displayValue = (displayValue+1) % display.length;
    		this.setDisplayText(display[displayValue]);
    	}
    	return bo;
    }

	public int getWidth() {
		return width;
	}

    public void drawHover(int i, int j, boolean hasSubGui) {
        if (hasSubGui || !visible || hoverableText.isEmpty())
            return;

        int hoverState = this.getHoverState(this.field_146123_n);
        if (oldHover != hoverState) {
            oldHover = hoverState;
            hoverCount = 0;
        } else {
            if (hoverCount < 110)
                hoverCount++;
        }

        if (hoverState == 2 && hoverCount > 100) {
            GL11.glPushMatrix();
            Minecraft mc = Minecraft.getMinecraft();
            String displayString = StatCollector.translateToLocal(hoverableText);
            GL11.glColor4f(1, 1, 1, 1);
            List<String> lines = TextSplitter.splitText(displayString, 30);
            drawHoveringText(lines, i, j, mc);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    protected void drawHoveringText(List textLines, int x, int y, Minecraft mc) {
        if (mc.fontRenderer == null || textLines.isEmpty())
            return;

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int maxWidth = 0;
        Iterator iterator = textLines.iterator();
        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            int lineWidth = mc.fontRenderer.getStringWidth(s);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int j2 = x + 12;
        int k2 = y - 12;
        int maxHeight = 8;

        if (textLines.size() > 1) {
            maxHeight += 2 + (textLines.size() - 1) * 10;
        }

        // Calculate the maximum x-coordinate to ensure the tooltip stays within the game window
        int gameWidth = mc.displayWidth;
        int maxTooltipX = gameWidth - maxWidth - 4; // Subtract the tooltip width

        // Adjust tooltip position if it goes off the screen to the right
        if (j2 > maxTooltipX) {
            int diff = j2 - maxTooltipX;
            j2 -= diff; // Shift tooltip to the left
            GL11.glTranslatef(-300, 0, 0); // Apply translation
        }

        if (k2 + maxHeight + 6 > mc.displayHeight) {
            k2 = mc.displayHeight - maxHeight - 6;
        }

        // Draw the tooltip
        this.zLevel = 300.0F;
        int j1 = -267386864;
        // Draw the gradient background
        // (Ensure to adjust the coordinates according to the translation)
        this.drawGradientRect(j2 - 3, k2 - 4, j2 + maxWidth + 3, k2 - 3, j1, j1);
        this.drawGradientRect(j2 - 3, k2 + maxHeight + 3, j2 + maxWidth + 3, k2 + maxHeight + 4, j1, j1);
        this.drawGradientRect(j2 - 3, k2 - 3, j2 + maxWidth + 3, k2 + maxHeight + 3, j1, j1);
        this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + maxHeight + 3, j1, j1);
        this.drawGradientRect(j2 + maxWidth + 3, k2 - 3, j2 + maxWidth + 4, k2 + maxHeight + 3, j1, j1);
        int k1 = 1347420415;
        int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
        this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + maxHeight + 3 - 1, k1, l1);
        this.drawGradientRect(j2 + maxWidth + 2, k2 - 3 + 1, j2 + maxWidth + 3, k2 + maxHeight + 3 - 1, k1, l1);
        this.drawGradientRect(j2 - 3, k2 - 3, j2 + maxWidth + 3, k2 - 3 + 1, k1, k1);
        this.drawGradientRect(j2 - 3, k2 + maxHeight + 2, j2 + maxWidth + 3, k2 + maxHeight + 3, l1, l1);

        // Draw the text lines
        for (int i2 = 0; i2 < textLines.size(); ++i2) {
            String s1 = (String) textLines.get(i2);
            mc.fontRenderer.drawStringWithShadow(s1, j2, k2, -1);
            if (i2 == 0) {
                k2 += 2;
            }
            k2 += 10;
        }

        this.zLevel = 0.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

}
