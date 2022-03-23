//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import org.lwjgl.opengl.GL11;

public class GuiScriptGlobal extends GuiNPCInterface {
    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/smallbg.png");

    public GuiScriptGlobal() {
        this.xSize = 176;
        this.ySize = 222;
        this.drawDefaultBackground = false;
        this.title = "";
    }

    public void initGui() {
        super.initGui();
        this.addButton(new GuiNpcButton(0, this.guiLeft + 38, this.guiTop + 20, 100, 20, "Players"));
        this.addButton(new GuiNpcButton(1, this.guiLeft + 38, this.guiTop + 50, 100, 20, "Forge"));
        this.addButton(new GuiNpcButton(2, this.guiLeft + 38, this.guiTop + 80, 100, 20, "All NPCs"));
    }

    public void drawScreen(int i, int j, float f) {
        this.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(this.resource);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        super.drawScreen(i, j, f);
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            this.displayGuiScreen(new GuiScriptPlayers());
        }
        if (guibutton.id == 1) {
            this.displayGuiScreen(new GuiScriptForge());
        }
        if (guibutton.id == 2) {
            this.displayGuiScreen(new GuiScriptAllNPCs());
        }
    }

    public void keyTyped(char c, int i) {
        if (i == 1 || this.isInventoryKey(i)) {
            this.close();
        }
    }

    public void save() {
    }
}
