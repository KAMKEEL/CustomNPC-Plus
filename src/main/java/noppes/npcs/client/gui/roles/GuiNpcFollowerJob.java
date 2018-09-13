package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFollower;

public class GuiNpcFollowerJob extends GuiNPCInterface2 implements ICustomScrollListener
{	
	private JobFollower job;
	private GuiCustomScroll scroll;
    public GuiNpcFollowerJob(EntityNPCInterface npc)
    {
    	super(npc);    	
    	job = (JobFollower) npc.jobInterface;
    }

    public void initGui()
    {
    	super.initGui();

        addLabel(new GuiNpcLabel(1,"gui.name", guiLeft + 6, guiTop + 110));
        addTextField(new GuiNpcTextField(1,this, this.fontRendererObj, guiLeft + 50, guiTop + 105, 200, 20, job.name));

    	scroll = new GuiCustomScroll(this,0);
        scroll.setSize(143, 208);
        scroll.guiLeft = guiLeft + 268;
        scroll.guiTop = guiTop + 4;
        this.addScroll(scroll);
        
        List<String> names = new ArrayList<String>();
        List<EntityNPCInterface> list = npc.worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, npc.boundingBox.expand(40, 40, 40));
        for(EntityNPCInterface npc : list){
        	if(npc == this.npc || names.contains(npc.display.name))
        		continue;
        	names.add(npc.display.name);
        }
        scroll.setList(names);
    }

    @Override
	public void save() {
    	job.name = getTextField(1).getText();
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void customScrollClicked(int i, int j, int k,
			GuiCustomScroll guiCustomScroll) {
		getTextField(1).setText(guiCustomScroll.getSelected());
	}


}
