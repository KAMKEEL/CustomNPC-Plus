package kamkeel.npcs.addon.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.gui.model.GuiCreationScreen;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.entity.EntityNPCInterface;

public class GeckoAddonClient {
    public static GeckoAddonClient Instance;
    public boolean supportEnabled = true;

    /**
     * This class is a shell class to be changed via mixins
     * The Addon Mod will replace all blank functions within
     * this class to change the ongoing code.
     * The fewer classes modified by mixins the safer.
     * These are for mixins affecting only client-side classes.
     */
    public GeckoAddonClient(){
        Instance = this;
    }

    // Creation Screen Mixins
    public void showGeckoButtons(GuiCreationScreen creationScreen, EntityLivingBase entity){}
    public void geckoGuiCreationScreenActionPerformed(GuiCreationScreen creationScreen, GuiNpcButton btn){}

    // Npc Display Mixins
    public void geckoNpcDisplayInitGui(GuiNPCInterface2 guiNPCInterface){}
    public void geckoNpcDisplayActionPerformed(GuiNPCInterface2 guiNPCInterface, GuiNpcButton btn){}

    // Render Npc Mixins
    public boolean isGeckoModel(ModelBase mainModel){ return false;}
    public void geckoRenderModel(ModelMPM mainModel, EntityNPCInterface npc, float rotationYaw, float renderPartialTicks) {}
}
