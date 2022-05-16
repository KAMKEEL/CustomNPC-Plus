//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import org.lwjgl.opengl.GL11;

public class GuiScriptGlobal extends GuiNPCInterface implements IGuiData {
    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/smallbg.png");

    public GuiScriptGlobal() {
        this.xSize = 176;
        this.ySize = 222;
        this.drawDefaultBackground = false;
        this.title = "";
    }

    public void initGui() {
        super.initGui();
        Client.sendData(EnumPacketServer.ScriptGlobalGuiDataGet, new Object[0]);
    }

    public void setGuiData(NBTTagCompound compound) {
        GuiNpcButton playerButton = new GuiNpcButton(0, this.guiLeft + 38, this.guiTop + 20, 100, 20, "Players");
        playerButton.setEnabled(compound.getBoolean("PlayerScriptsEnabled") && compound.getBoolean("ScriptsEnabled"));
        this.addButton(playerButton);

        GuiNpcButton forgeButton = new GuiNpcButton(1, this.guiLeft + 38, this.guiTop + 50, 100, 20, "Forge");
        forgeButton.setEnabled(compound.getBoolean("ForgeScriptsEnabled") && compound.getBoolean("ScriptsEnabled"));
        this.addButton(forgeButton);

        GuiNpcButton npcButton = new GuiNpcButton(2, this.guiLeft + 38, this.guiTop + 80, 100, 20, "All NPCs");
        npcButton.setEnabled(compound.getBoolean("GlobalNPCScriptsEnabled") && compound.getBoolean("ScriptsEnabled"));
        this.addButton(npcButton);
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
