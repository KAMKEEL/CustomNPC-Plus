package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;

public class GuiNpcHealer extends GuiNPCInterface2
{	
	private JobHealer job;
    public GuiNpcHealer(EntityNPCInterface npc)
    {
    	super(npc);    	
    	job = (JobHealer) npc.jobInterface;
    }

    public void initGui()
    {
    	super.initGui();

        addLabel(new GuiNpcLabel(1,"Healing Speed:", guiLeft + 60, guiTop + 110));
        addTextField(new GuiNpcTextField(1,this, this.fontRendererObj, guiLeft+130, guiTop + 105, 40, 20, job.speed + ""));
        getTextField(1).numbersOnly = true;
        getTextField(1).setMinMaxDefault(1, 10, 8);
        
        addLabel(new GuiNpcLabel(2,"Range:", guiLeft + 60, guiTop + 133));
        addTextField(new GuiNpcTextField(2,this, this.fontRendererObj, guiLeft+130, guiTop + 128, 40, 20, job.range + ""));
        getTextField(2).numbersOnly = true;
        getTextField(2).setMinMaxDefault(2, 20, 5);
        
    }
    @Override
    public void elementClicked(){
    	
    }


    @Override
	public void save() {
    	job.speed = getTextField(1).getInteger();
    	job.range = getTextField(2).getInteger();

		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}


}
