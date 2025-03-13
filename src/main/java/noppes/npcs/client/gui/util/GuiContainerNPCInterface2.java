package noppes.npcs.client.gui.util;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.roles.GuiNpcTraderSetup;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

public abstract class GuiContainerNPCInterface2 extends GuiContainerNPCInterface {
    private ResourceLocation background = new ResourceLocation("customnpcs", "textures/gui/menubg.png");
    private final ResourceLocation defaultBackground = new ResourceLocation("customnpcs", "textures/gui/menubg.png");
    private final ResourceLocation defaultBackground2 = new ResourceLocation("customnpcs", "textures/gui/menubg2.png");

    private final GuiNpcMenu menu;
    public int menuYOffset = 0;

    public GuiContainerNPCInterface2(EntityNPCInterface npc, Container cont) {
        this(npc, cont, -1);
    }

    public GuiContainerNPCInterface2(EntityNPCInterface npc, Container cont, int activeMenu) {
        super(npc, cont);
        this.xSize = 420;
        this.menu = new GuiNpcMenu(this, activeMenu, npc);
        title = "";
        drawDefaultBackground = true;
    }

    public void setBackground(String texture) {
        background = new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture) {
        return new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    @Override
    public void initGui() {
        super.initGui();
        menu.initGui(guiLeft, guiTop + menuYOffset, xSize);
    }

    @Override
    protected void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (!hasSubGui())
            menu.mouseClicked(i, j, k);
    }

    public void delete() {
        npc.delete();
        displayGuiScreen(null);
        mc.setIngameFocus();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 256);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(defaultBackground2);
        if (this instanceof GuiNpcTraderSetup)
            drawTexturedModalRect(guiLeft + xSize - 210, guiTop, 46, 0, 256, 220);
        else
            drawTexturedModalRect(guiLeft + xSize - 230, guiTop, 26, 0, 256, 220);

        menu.drawElements(fontRendererObj, i, j, mc, f);

        super.drawGuiContainerBackgroundLayer(f, i, j);
    }

}
