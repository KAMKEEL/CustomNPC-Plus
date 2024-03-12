package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.GuiNpcSoundSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;

public class GuiNpcBard extends GuiNPCInterface2 implements ISubGuiListener
{
	private JobBard job;
	private GuiNpcSoundSelection gui;

    public GuiNpcBard(EntityNPCInterface npc)
    {
    	super(npc);
    	job = (JobBard) npc.jobInterface;
    }

    public void initGui()
    {
    	super.initGui();

    	this.addButton(new GuiNpcButton(1, guiLeft + 55, guiTop + 15,20,20, "X"));
        addLabel(new GuiNpcLabel(0,job.song, guiLeft + 80, guiTop + 20));
    	this.addButton(new GuiNpcButton(0, guiLeft + 75, guiTop + 50, "gui.selectSound"));

    	this.addButton(new GuiNpcButton(2, guiLeft + 75, guiTop + 71, new String[]{"gui.none","item.npcBanjo.name","item.npcViolin.name","item.npcGuitar.name","item.npcHarp.name","item.npcFrenchHorn.name"}, job.getInstrument().ordinal()));
    	this.addButton(new GuiNpcButton(3, guiLeft + 75, guiTop + 92, new String[]{"bard.jukebox","bard.background"}, job.isStreamer?0:1));


        addLabel(new GuiNpcLabel(2,"bard.ondistance", guiLeft + 60, guiTop + 143));
        addTextField(new GuiNpcTextField(2,this, this.fontRendererObj, guiLeft+160, guiTop + 138, 40, 20, job.minRange + ""));
        getTextField(2).integersOnly = true;
        getTextField(2).setMinMaxDefault(2, Integer.MAX_VALUE, 5);

        addLabel(new GuiNpcLabel(4,"bard.hasoff", guiLeft + 60, guiTop + 166));
        addButton(new GuiNpcButton(4, guiLeft + 160, guiTop + 161, 60, 20, new String[]{"gui.no","gui.yes"}, job.hasOffRange?1:0));

        addLabel(new GuiNpcLabel(3,"bard.offdistance", guiLeft + 60, guiTop + 189));
        addTextField(new GuiNpcTextField(3,this, this.fontRendererObj, guiLeft+160, guiTop + 184, 40, 20, job.maxRange + ""));
        getTextField(3).integersOnly = true;
        getTextField(3).setMinMaxDefault(2, Integer.MAX_VALUE, 10);

    	getLabel(3).enabled = job.hasOffRange;
    	getTextField(3).enabled = job.hasOffRange;

    }
    @Override
    public void elementClicked(){
    	job.song = gui.getSelected();
    }

    protected void actionPerformed(GuiButton guibutton)
    {
    	GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0)
        {
            setSubGui(new GuiSoundSelection(job.song));
            MusicController.Instance.stopMusic();
        }
        if(button.id == 1)
        {
        	job.song = "";
        	getLabel(0).label = "";
        	MusicController.Instance.stopMusic();
        }
        if(button.id == 2)
        {
        	job.setInstrument(button.getValue());
        }
        if(button.id == 3)
        {
        	job.isStreamer = button.getValue() == 0;
        	initGui();
        }
        if(button.id == 4)
        {
        	job.hasOffRange = button.getValue() == 1;
        	initGui();
        }

    }

    @Override
	public void save() {
    	job.minRange = getTextField(2).getInteger();
    	job.maxRange = getTextField(3).getInteger();

    	if(job.minRange > job.maxRange)
    		job.maxRange = job.minRange;

    	MusicController.Instance.stopMusic();
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}


    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        GuiSoundSelection gss = (GuiSoundSelection) subgui;
        if(gss.selectedResource != null) {
            job.song = gss.selectedResource.toString();
            getLabel(0).label = job.song;
        }
    }
}
