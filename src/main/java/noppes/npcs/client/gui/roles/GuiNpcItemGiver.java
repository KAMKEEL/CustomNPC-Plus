package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobItemGiver;

public class GuiNpcItemGiver extends GuiContainerNPCInterface2
{	
	private JobItemGiver role;
    public GuiNpcItemGiver(EntityNPCInterface npc, ContainerNpcItemGiver container)
    {
    	super(npc,container);    	
    	ySize = 200;
    	role = (JobItemGiver) npc.jobInterface;
    	setBackground("npcitemgiver.png");
    }

    public void initGui()
    {
    	super.initGui();
    	this.addButton(new GuiNpcButton(0, guiLeft + 6, guiTop + 6, 140,20, new String[]{"Random Item","All Items","Give Not Owned Items","Give When Doesnt Own Any","Chained"},role.givingMethod));
    	this.addButton(new GuiNpcButton(1, guiLeft + 6, guiTop + 29, 140,20, new String[]{"Timer","Give Only Once","Daily"},role.cooldownType));

    	addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 55, guiTop + 54, 90, 20, role.cooldown + ""));
    	getTextField(0).numbersOnly = true;
        addLabel(new GuiNpcLabel(0,"Cooldown:", guiLeft + 6, guiTop + 59));
        addLabel(new GuiNpcLabel(1,"Items to give", guiLeft + 46, guiTop + 79));
        
        getTextField(0).numbersOnly = true;

        int i = 0;
        for(String line : role.lines){
        	addTextField(new GuiNpcTextField(i+1, this, fontRendererObj, guiLeft + 150, guiTop + 6 + i * 24, 236, 20,line));
        	i++;
        }
        for(;i <3; i++){
        	addTextField(new GuiNpcTextField(i+1, this, fontRendererObj, guiLeft + 150, guiTop + 6 + i * 24, 236, 20,""));
        }
    	getTextField(0).enabled = role.isOnTimer();
    	getLabel(0).enabled = role.isOnTimer();

		addLabel(new GuiNpcLabel(4, "availability.options", guiLeft + 180, guiTop + 101));
		addButton(new GuiNpcButton(4, guiLeft + 280, guiTop + 96, 50, 20, "selectServer.edit"));
    }

    public void actionPerformed(GuiButton guibutton)
    {
    	GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0)
        {
        	role.givingMethod = button.getValue();
        }
        if(button.id == 1)
        {
        	role.cooldownType = button.getValue();
        	getTextField(0).enabled = role.isOnTimer();
        	getLabel(0).enabled = role.isOnTimer();
        }
        if(button.id == 4){
        	setSubGui(new SubGuiNpcAvailability(role.availability));
        }
    	        
    }

	public void save() {

		List<String> lines = new ArrayList<String>();
    	for(int i = 1; i < 4; i++){
    		GuiNpcTextField tf = getTextField(i);
    		if(!tf.isEmpty())
    			lines.add(tf.getText());
    	}
    	role.lines = lines;
		int cc = 10;
		if(!getTextField(0).isEmpty() && getTextField(0).isInteger())
			cc = getTextField(0).getInteger();

		role.cooldown = cc;

		Client.sendData(EnumPacketServer.JobSave, role.writeToNBT(new NBTTagCompound()));
	}

}
