package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Implementation of GuiNPCDiagram for displaying Magic nodes and their connections.
 * Magic objects are wrapped as DiagramIcons, and each Magic's weaknesses generate connections.
 */
public class GuiMagicMap extends GuiNPCDiagram {

    private GuiScreen parent;
    private List<Magic> magics;
    // Keep one RenderItem instance.
    private RenderItem renderItem = new RenderItem();

    public GuiMagicMap(GuiScreen parent, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.parent = parent;
        magics = new ArrayList<>();
        for (Magic magic : MagicController.getInstance().magics.values()) {
            magics.add(magic);
        }
    }

    @Override
    protected List<DiagramIcon> getIcons() {
        List<DiagramIcon> icons = new ArrayList<>();
        for (Magic magic : magics) {
            icons.add(new MagicIcon(magic));
        }
        return icons;
    }

    @Override
    protected List<DiagramConnection> getConnections() {
        List<DiagramConnection> conns = new ArrayList<>();
        // For each Magic, generate connections from each weakness (attacker) to this magic.
        for (Magic magic : magics) {
            if (magic.weaknesses == null) continue;
            for (Map.Entry<Integer, Float> entry : magic.weaknesses.entrySet()) {
                int attackerId = entry.getKey();
                float percent = entry.getValue();
                String hover = "+" + (int) (percent * 100) + "%";
                conns.add(new DiagramConnection(attackerId, magic.id, percent, hover));
            }
        }
        return conns;
    }

    @Override
    protected void renderIcon(DiagramIcon icon, int posX, int posY, boolean highlighted) {
        // Cast to our custom wrapper class.
        Magic magic = ((MagicIcon) icon).magic;
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        int iconSize = this.iconSize;
        int iconX = posX - iconSize / 2;
        int iconY = posY - iconSize / 2;

        if (!highlighted) {
            GL11.glColor4f(0.4f, 0.4f, 0.4f, 1f);
        }

        // Use standard item lighting.
        RenderHelper.enableGUIStandardItemLighting();
        if (magic.iconItem != null) {
            renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), magic.iconItem, iconX, iconY);
        } else if (magic.iconTexture != null && !magic.iconTexture.isEmpty()) {
            mc.getTextureManager().bindTexture(new ResourceLocation(magic.iconTexture));
            this.drawTexturedModalRect(iconX, iconY, 0, 0, iconSize, iconSize);
        } else {
            ItemStack sword = new ItemStack(Items.iron_sword);
            renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), sword, iconX, iconY);
        }
        RenderHelper.disableStandardItemLighting();

        if (!highlighted) {
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    @Override
    protected List<String> getIconTooltip(DiagramIcon icon) {
        Magic magic = ((MagicIcon) icon).magic;
        List<String> tooltip = new ArrayList<>();
        tooltip.add(magic.getName());
        return tooltip;
    }

    @Override
    protected void drawHoveringText(List<String> text, int mouseX, int mouseY, FontRenderer fontRenderer) {
        if (parent instanceof GuiNPCInterface2) {
            ((GuiNPCInterface2) parent).renderHoveringText(text, mouseX, mouseY, fontRenderer);
        }
    }

    /**
     * Override getIconName so that connection tooltips can show Magic names.
     */
    @Override
    protected String getIconName(DiagramIcon icon) {
        Magic magic = ((MagicIcon) icon).magic;
        return magic.getName();
    }

    // Wrap a Magic object as a DiagramIcon.
    private class MagicIcon extends DiagramIcon {
        public Magic magic;
        public MagicIcon(Magic magic) {
            super(magic.id);
            this.magic = magic;
        }
    }
}
