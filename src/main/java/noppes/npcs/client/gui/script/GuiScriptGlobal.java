package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.ScriptInfoPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

public class GuiScriptGlobal extends GuiNPCInterface implements IGuiData {
    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/smallbg.png");
    private HashMap<Integer, Class<?>> scriptGuiClasses = new HashMap<>();

    public GuiScriptGlobal() {
        this.xSize = 176;
        this.ySize = 222;
        this.drawDefaultBackground = false;
        this.title = "";
    }

    public void initGui() {
        super.initGui();
        GuiNpcButton playerButton = new GuiNpcButton(0, this.guiLeft + 38, this.guiTop + 20, 100, 20, "Players");
        playerButton.setEnabled(false);
        this.addButton(playerButton);
        GuiNpcButton forgeButton = new GuiNpcButton(1, this.guiLeft + 38, this.guiTop + 50, 100, 20, "Forge");
        forgeButton.setEnabled(false);
        this.addButton(forgeButton);
        GuiNpcButton npcButton = new GuiNpcButton(2, this.guiLeft + 38, this.guiTop + 80, 100, 20, "All NPCs");
        npcButton.setEnabled(false);
        this.addButton(npcButton);
        ScriptInfoPacket.Get();
    }

    public void setGuiData(NBTTagCompound compound) {
        getButton(0).setEnabled(compound.getBoolean("PlayerScriptsEnabled") && compound.getBoolean("ScriptsEnabled"));
        getButton(1).setEnabled(compound.getBoolean("ForgeScriptsEnabled") && compound.getBoolean("ScriptsEnabled"));
        getButton(2).setEnabled(compound.getBoolean("GlobalNPCScriptsEnabled") && compound.getBoolean("ScriptsEnabled"));
    }

    //For use with mods wanting to add their own scripts/APIs. Add the class of your GUI that extends from GuiScriptInterface as
    //a parameter, the text the new button that appears when the scripter is right clicked should show, and whether it's enabled.
    //For examples of what kind of GUI class to create, look at GuiScriptForge.
    public void addScriptGui(Class<?> guiClass, String buttonText, boolean enabled) {
        int buttonId = this.buttons.size();
        this.scriptGuiClasses.put(buttonId, guiClass);

        GuiNpcButton npcButton = new GuiNpcButton(buttonId, this.guiLeft + 38, this.guiTop + 80 + (buttonId - 2) * 30, 100, 20, buttonText);
        npcButton.setEnabled(enabled);
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
        if (guibutton.id > 2) {
            try {
                Class<?> guiClass = this.scriptGuiClasses.get(guibutton.id);
                this.displayGuiScreen((GuiScreen) guiClass.newInstance());
            } catch (Exception ignored) {
            }
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
