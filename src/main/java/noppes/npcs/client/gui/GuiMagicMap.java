package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import noppes.npcs.client.gui.util.GuiDiagram;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiMagicMap extends GuiDiagram {

    private List<Magic> magics;

    // Single RenderItem instance for efficiency.
    private RenderItem renderItem = new RenderItem();

    public GuiMagicMap(GuiNPCInterface parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        magics = new ArrayList<>();

        // Cache all magics from the MagicController.
        for (Magic magic : MagicController.getInstance().magics.values()) {
            magics.add(magic);
        }
        setLayout(DiagramLayout.CIRCULAR_MANUAL);
        this.setCurvedArrows(true);
        this.setCurveAngle(-20);
    }

    @Override
    protected List<DiagramIcon> createIcons() {
        List<DiagramIcon> icons = new ArrayList<>();
        for (Magic magic : magics) {
            MagicIcon icon = new MagicIcon(magic);
            // Set your desired flags (for example, enabled and pressable).
            icon.enabled = true;
            icon.pressable = true;
            icons.add(icon);
        }
        return icons;
    }

    @Override
    protected List<DiagramConnection> createConnections() {
        List<DiagramConnection> conns = new ArrayList<>();
        // For each Magic, generate connections from each weakness (attacker) to this magic.
        for (Magic magic : magics) {
            if (magic.weaknesses == null)
                continue;
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
    protected void renderIcon(DiagramIcon icon, int posX, int posY, IconRenderState state) {
        Magic magic = ((MagicIcon) icon).magic;
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        int size = this.iconSize;
        int iconX = posX - size / 2;
        int iconY = posY - size / 2;

        RenderHelper.enableGUIStandardItemLighting();

        if (magic.iconItem != null) {
            renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), magic.iconItem, iconX, iconY);
        } else if (magic.iconTexture != null && !magic.iconTexture.isEmpty()) {
            mc.getTextureManager().bindTexture(new ResourceLocation(magic.iconTexture));
            this.drawTexturedModalRect(iconX, iconY, 0, 0, size, size);
        } else {
            ItemStack sword = new ItemStack(Items.iron_sword);
            renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), sword, iconX, iconY);
        }
        RenderHelper.disableStandardItemLighting();

        // If the icon is not highlighted, draw a translucent overlay.
        if (state == IconRenderState.NOT_HIGHLIGHTED) {
            drawRect(iconX, iconY, iconX + size, iconY + size, 0x80202020);
        }

        // Reset color.
        GL11.glColor4f(1f, 1f, 1f, 1f);
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
        parent.renderHoveringText(text, mouseX, mouseY, fontRenderer);
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
            super(magic.id, magic.index, magic.priority);
            this.magic = magic;
        }
    }
}
