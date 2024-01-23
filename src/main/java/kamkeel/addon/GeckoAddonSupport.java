package kamkeel.addon;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.DataDisplay;
import noppes.npcs.DataGecko;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.model.custom.GuiCustomAnimFileSelection;
import noppes.npcs.client.gui.model.custom.GuiCustomAnimationSelection;
import noppes.npcs.client.gui.model.custom.GuiCustomModelSelection;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.entity.EntityCustomModel;
import noppes.npcs.entity.EntityNPCInterface;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.HashMap;

public class GeckoAddonSupport {
    public static boolean supportEnabled = false;

    public static boolean geckoModelRender(ModelBase modelBase) {
        if(!supportEnabled)
            return false;

        return modelBase instanceof ModelMPM && ((ModelMPM) modelBase).entity instanceof IAnimatable;
    }

    public static void openGeckoGui(GuiModelInterface parent, GuiNpcButton button, EntityPlayer player, EntityNPCInterface npc){
        if(!supportEnabled)
            return;

        if(button.id == 202){
            NoppesUtil.openGUI(player, new GuiCustomModelSelection(npc, parent));
        }

        if(button.id == 203){
            NoppesUtil.openGUI(player, new GuiCustomAnimFileSelection(npc, parent));
        }

        if(button.id == 204){
            DataGecko geckoData = (DataGecko) getGeckoData(npc);
            if(geckoData != null){
                NoppesUtil.openGUI(player, new GuiCustomAnimationSelection(npc, parent, (name)-> geckoData.idleAnim=name));
            }
        }
    }

    public static void geckoModelCopy(EntityLivingBase copied, EntityLivingBase entity) {
        if(!supportEnabled)
            return;

        if(entity instanceof EntityCustomModel && copied instanceof EntityNPCInterface){
            DataGecko geckoData = (DataGecko)  getGeckoData((EntityNPCInterface) copied);
            if(geckoData != null){
                ((EntityCustomModel) entity).textureResLoc=new ResourceLocation(((EntityNPCInterface) copied).display.texture);
                ((EntityCustomModel) entity).modelResLoc=new ResourceLocation(geckoData.model);
                ((EntityCustomModel) entity).animResLoc=new ResourceLocation(geckoData.animFile);
                ((EntityCustomModel) entity).idleAnim=geckoData.idleAnim;
            }
        }
    }

    public static void initGeckoData(HashMap<String, DataDisplaySupport> dataDisplaySupportHashMap){
        if(!supportEnabled)
            return;

        dataDisplaySupportHashMap.put("GECKO", new DataGecko());
    }

    public static void putGeckoData(EntityNPCInterface npc, DataDisplaySupport geckoData){
        if(!supportEnabled)
            return;

        npc.display.dataDisplaySupport.put("GECKO", geckoData);
    }

    public static DataDisplaySupport getGeckoData(EntityNPCInterface npc){
        if(!supportEnabled)
            return null;

        DataDisplaySupport dataDisplaySupport = npc.display.dataDisplaySupport.get("GECKO");
        if(dataDisplaySupport instanceof DataGecko){
            return dataDisplaySupport;
        }

        dataDisplaySupport = new DataGecko();
        putGeckoData(npc, dataDisplaySupport);
        return dataDisplaySupport;
    }
}