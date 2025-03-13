package noppes.npcs.client.gui;

import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.util.GuiDiagram;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicAssociation;
import noppes.npcs.controllers.data.MagicCycle;
import noppes.npcs.constants.EnumDiagramLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiMagicCycleMap extends GuiDiagram {

    private List<Magic> cycleMagics;
    private MagicCycle cycle;

    /**
     * Constructor using a MagicCycle object.
     */
    public GuiMagicCycleMap(GuiNPCInterface parent, int x, int y, int width, int height, MagicCycle cycle) {
        super(parent, x, y, width, height);
        this.cycle = cycle;
        cycleMagics = new ArrayList<>();
        // If cycle is null, leave cycleMagics empty.
        if (this.cycle == null) {
            setLayout(EnumDiagramLayout.CIRCULAR_MANUAL);
            setCurvedArrows(false);
            invalidateCache();
            return;
        }
        MagicController mc = MagicController.getInstance();
        // Get magics from the cycle's associations if associations are not null.
        if (cycle.associations != null) {
            for (MagicAssociation assoc : cycle.associations.values()) {
                Magic m = mc.getMagic(assoc.magicId);
                if (m != null) {
                    cycleMagics.add(m);
                }
            }
        }

        // Use the cycle's layout if set; otherwise default to CIRCULAR_MANUAL.
        setLayout(cycle.layout != null ? cycle.layout : EnumDiagramLayout.CIRCULAR_MANUAL);
        setCurvedArrows(true);
        setCurveAngle(30);
        invalidateCache();

        this.iconSize = 20;
        this.slotSize = iconSize + slotPadding;
    }

    /**
     * Constructor using a cycle ID.
     */
    public GuiMagicCycleMap(GuiNPCInterface parent, int x, int y, int width, int height, int cycleId) {
        this(parent, x, y, width, height, MagicController.getInstance().getCycle(cycleId));
    }

    @Override
    protected List<DiagramIcon> createIcons() {
        List<DiagramIcon> icons = new ArrayList<>();
        // If the magic list is empty, simply return an empty list.
        if (cycleMagics == null || cycleMagics.isEmpty()) {
            return icons;
        }
        // For each magic in the cycle, create an icon.
        for (Magic m : cycleMagics) {
            if (m == null) continue;
            MagicIcon icon = new MagicIcon(m);
            icon.enabled = true;
            icon.pressable = true;
            // If available, set manual ordering data from the cycle association.
            MagicAssociation assoc = cycle.associations.get(m.id);
            if (assoc != null) {
                icon.index = assoc.index;
                icon.priority = assoc.priority;
            }
            icons.add(icon);
        }
        return icons;
    }

    @Override
    protected List<DiagramConnection> createConnections() {
        List<DiagramConnection> conns = new ArrayList<>();
        if (cycleMagics == null || cycleMagics.isEmpty())
            return conns;
        // Only add connections for interactions between magics in this cycle.
        Map<Integer, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < cycleMagics.size(); i++) {
            orderMap.put(cycleMagics.get(i).id, i);
        }
        for (Magic m : cycleMagics) {
            if (m == null || m.interactions == null) continue;
            for (Map.Entry<Integer, Float> entry : m.interactions.entrySet()) {
                int targetId = entry.getKey();
                if (!orderMap.containsKey(targetId)) continue;
                float percent = entry.getValue();
                String hover = "+" + (int) (percent * 100) + "%";
                conns.add(new DiagramConnection(m.id, targetId, percent, hover));
            }
        }
        return conns;
    }

    @Override
    protected void renderIcon(DiagramIcon icon, int posX, int posY, IconRenderState state) {
        Magic magic = ((MagicIcon) icon).magic;
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        int size = this.iconSize;
        float scale = (float) size / 16.0f;
        float renderX = posX - 8 * scale;
        float renderY = posY - 8 * scale;

        if (magic.item != null) {
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glPushMatrix();
            GL11.glTranslatef(renderX, renderY, 0);
            GL11.glScalef(scale, scale, 1.0f);
            renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), magic.item, 0, 0);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
        } else if (magic.iconTexture != null && !magic.iconTexture.isEmpty()) {
            ImageData imageData = ClientCacheHandler.getImageData(magic.iconTexture);
            if (imageData != null && imageData.imageLoaded()) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                imageData.bindTexture();
                int texWidth = imageData.getTotalWidth();
                int texX = posX - size / 2;
                int texY = posY - size / 2;
                func_152125_a(texX, texY, 0, 0, texWidth, texWidth, size, size, texWidth, texWidth);
                GL11.glPopAttrib();
            }
        } else {
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glPushMatrix();
            GL11.glTranslatef(renderX, renderY, 0);
            GL11.glScalef(scale, scale, 1.0f);
            ItemStack sword = new ItemStack(Items.iron_sword);
            renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), sword, 0, 0);
            GL11.glPopMatrix();
            RenderHelper.disableStandardItemLighting();
        }

        if (state == IconRenderState.NOT_HIGHLIGHTED) {
            int rectX = posX - size / 2;
            int rectY = posY - size / 2;
            drawRect(rectX, rectY, rectX + size, rectY + size, 0x80202020);
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    protected List<String> getIconTooltip(DiagramIcon icon) {
        Magic magic = ((MagicIcon) icon).magic;
        List<String> tooltip = new ArrayList<>();
        tooltip.add(magic.getDisplayName().replace("&", "\u00A7"));
        return tooltip;
    }

    @Override
    protected String getIconName(DiagramIcon icon) {
        Magic magic = ((MagicIcon) icon).magic;
        return magic.getName();
    }

    // Inner class wrapping a Magic as a DiagramIcon.
    private class MagicIcon extends DiagramIcon {
        public Magic magic;
        public MagicIcon(Magic magic) {
            super(magic.id, 0, 0);
            this.magic = magic;
        }
    }

    @Override
    protected void drawHoveringText(List<String> text, int mouseX, int mouseY, FontRenderer fontRenderer) {
        parent.renderHoveringText(text, mouseX, mouseY, fontRenderer);
    }
}
