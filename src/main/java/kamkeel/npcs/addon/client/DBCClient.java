package kamkeel.npcs.addon.client;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.gui.mainmenu.GuiNpcStats;
import noppes.npcs.client.gui.player.inventory.GuiCNPCInventory;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.entity.EntityNPCInterface;

public class DBCClient {
    public static DBCClient Instance;
    public boolean supportEnabled = true;

    /**
     * This class is a shell class to be changed via mixins
     * The Addon Mod will replace all blank functions within
     * this class to change the ongoing code.
     * The fewer classes modified by mixins the safer.
     * These are for mixins affecting only client-side classes.
     */
    public DBCClient(){
        Instance = this;
    }

    // Stat Buttons
    public void showDBCStatButtons(GuiNpcStats stats, EntityLivingBase entity){}
    public void showDBCStatActionPerformed(GuiNpcStats stats, GuiNpcButton btn){}


    // Render Auras
    public void renderDBCAuras(EntityNPCInterface npcInterface){}

    // Manage Custom Forms
    public GuiNPCInterface2 manageCustomForms(EntityNPCInterface npcInterface){ return null; }
    // Manage Custom Forms
    public GuiNPCInterface2 manageCustomAuras(EntityNPCInterface npcInterface){ return null; }
}
