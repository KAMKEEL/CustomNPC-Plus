package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.GuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.GuiNpcSoundSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;


public class GuiNpcSpawner extends GuiNPCInterface2 implements ITextfieldListener, IGuiData
{	
	private JobSpawner job;
	
	private int slot = -1;

	public String title1 = "gui.selectnpc";
	public String title2 = "gui.selectnpc";
	public String title3 = "gui.selectnpc";
	public String title4 = "gui.selectnpc";
	public String title5 = "gui.selectnpc";
	public String title6 = "gui.selectnpc";
	
    public GuiNpcSpawner(EntityNPCInterface npc)
    {
    	super(npc);    	
    	job = (JobSpawner) npc.jobInterface;
    }

    public void initGui()
    {
    	super.initGui();

    	int y = guiTop + 6;

    	this.addButton(new GuiNpcButton(20, guiLeft + 25, y,20,20, "X"));
        addLabel(new GuiNpcLabel(0, "1:", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(0, guiLeft + 50, y, title1));

    	y += 23; 
    	this.addButton(new GuiNpcButton(21, guiLeft + 25, y,20,20, "X"));
        addLabel(new GuiNpcLabel(1, "2:", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(1, guiLeft + 50, y, title2));

    	y += 23; 
    	this.addButton(new GuiNpcButton(22, guiLeft + 25, y,20,20, "X"));
        addLabel(new GuiNpcLabel(2, "3:", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(2, guiLeft + 50, y, title3));

    	y += 23; 
    	this.addButton(new GuiNpcButton(23, guiLeft + 25, y,20,20, "X"));
        addLabel(new GuiNpcLabel(3, "4:", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(3, guiLeft + 50, y, title4));

    	y += 23; 
    	this.addButton(new GuiNpcButton(24, guiLeft + 25, y,20,20, "X"));
        addLabel(new GuiNpcLabel(4, "5:", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(4, guiLeft + 50, y, title5));

    	y += 23; 
    	this.addButton(new GuiNpcButton(25, guiLeft + 25, y,20,20, "X"));
        addLabel(new GuiNpcLabel(5, "6:", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(5, guiLeft + 50, y, title6));

    	y += 23; 
        addLabel(new GuiNpcLabel(6, "spawner.diesafter", guiLeft + 4, y + 5));
    	this.addButton(new GuiNpcButton(26, guiLeft + 115, y,40,20, new String[]{"gui.yes","gui.no"}, job.doesntDie?1:0));

        addLabel(new GuiNpcLabel(11, "spawner.despawn", guiLeft + 170, y + 5));
    	this.addButton(new GuiNpcButton(11, guiLeft + 335, y,40,20, new String[]{"gui.no","gui.yes"}, job.despawnOnTargetLost?1:0));

    	y += 23; 
        addLabel(new GuiNpcLabel(7,StatCollector.translateToLocal("spawner.posoffset")+" X:", guiLeft + 4, y + 5));
    	addTextField(new GuiNpcTextField(7,this, fontRendererObj, guiLeft + 99, y, 24, 20, job.xOffset + ""));
    	getTextField(7).numbersOnly = true;
        getTextField(7).setMinMaxDefault(-9, 9, 0);
        addLabel(new GuiNpcLabel(8,"Y:", guiLeft + 125, y + 5));
    	addTextField(new GuiNpcTextField(8,this, fontRendererObj, guiLeft + 135, y, 24, 20, job.yOffset + ""));
    	getTextField(8).numbersOnly = true;
        getTextField(8).setMinMaxDefault(-9, 9, 0);
        addLabel(new GuiNpcLabel(9,"Z:", guiLeft + 161, y + 5));
    	addTextField(new GuiNpcTextField(9,this, fontRendererObj, guiLeft + 171, y, 24, 20, job.zOffset + ""));
    	getTextField(9).numbersOnly = true;
        getTextField(9).setMinMaxDefault(-9, 9, 0);  
        
    	y += 23; 
        addLabel(new GuiNpcLabel(10, "spawner.type", guiLeft + 4, y + 5));
        addButton(new GuiNpcButton(10, guiLeft + 80, y, 100, 20, new String[]{"spawner.one", "spawner.all", "spawner.random"}, job.spawnType));
    }

	@Override
    public void elementClicked(){
    }

    protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;     
    	if(button.id >= 0 && button.id < 6){
    		slot = button.id + 1;
    		setSubGui(new GuiNpcMobSpawnerSelector());
    	}    
    	if(button.id >= 20 && button.id < 26){
			job.setJobCompound(button.id - 19, null); 
			Client.sendData(EnumPacketServer.JobSpawnerRemove, button.id - 19);
    	}
    	if(button.id == 26){
    		job.doesntDie = button.getValue() == 1;
    	}
    	if(button.id == 10){
    		job.spawnType = button.getValue();
    	}
    	if(button.id == 11){
    		job.despawnOnTargetLost = button.getValue() == 1;
    	}
    }

	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		GuiNpcMobSpawnerSelector selector = (GuiNpcMobSpawnerSelector) gui;
		if(selector.isServer){
			String selected = selector.getSelected();
			if(selected != null)
				Client.sendData(EnumPacketServer.JobSpawnerAdd, selector.isServer, selected, selector.activeTab, slot);
		}
		else{
			NBTTagCompound compound = selector.getCompound();
			if(compound != null){
				job.setJobCompound(slot, compound); 
				Client.sendData(EnumPacketServer.JobSpawnerAdd, selector.isServer, slot, compound);
			}
		}
		initGui();
	}

    @Override
	public void save() {
    	NBTTagCompound compound = job.writeToNBT(new NBTTagCompound());
    	job.cleanCompound(compound);
		Client.sendData(EnumPacketServer.JobSave, compound);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 7){
			job.xOffset = textfield.getInteger();
		}
		if(textfield.id == 8){
			job.yOffset = textfield.getInteger();
		}
		if(textfield.id == 9){
			job.zOffset = textfield.getInteger();
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		title1 = compound.getString("Title1");
		title2 = compound.getString("Title2");
		title3 = compound.getString("Title3");
		title4 = compound.getString("Title4");
		title5 = compound.getString("Title5");
		title6 = compound.getString("Title6");
		initGui();
	}


}
