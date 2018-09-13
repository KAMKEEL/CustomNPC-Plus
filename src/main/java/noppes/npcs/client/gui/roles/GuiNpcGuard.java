package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import javax.management.relation.RoleStatus;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.biome.BiomeGenBase;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;

public class GuiNpcGuard extends GuiNPCInterface2{	
	private JobGuard role;
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;
	
    public GuiNpcGuard(EntityNPCInterface npc){
    	super(npc);    	
    	role = (JobGuard) npc.jobInterface;
    }

    public void initGui(){
    	super.initGui();
    	this.addLabel(new GuiNpcLabel(0, "guard.animals", guiLeft + 10, guiTop + 9));
    	this.addButton(new GuiNpcButton(0, guiLeft + 85, guiTop + 4, 50, 20, new String[]{"gui.no", "gui.yes"},role.attacksAnimals?1:0));

    	this.addLabel(new GuiNpcLabel(1, "guard.mobs", guiLeft + 140, guiTop + 9));
    	this.addButton(new GuiNpcButton(1, guiLeft + 222, guiTop + 4, 50, 20, new String[]{"gui.no", "gui.yes"},role.attackHostileMobs?1:0));

    	this.addLabel(new GuiNpcLabel(2, "guard.creepers", guiLeft + 275, guiTop + 9));
    	this.addButton(new GuiNpcButton(2, guiLeft + 360, guiTop + 4, 50, 20, new String[]{"gui.no", "gui.yes"},role.attackCreepers?1:0));
    	getButton(2).enabled = role.attackHostileMobs;
    	
    	this.addLabel(new GuiNpcLabel(3, "guard.specifictargets", guiLeft + 10, guiTop + 31));
    	this.addButton(new GuiNpcButton(3, guiLeft + 85, guiTop + 26, 50, 20, new String[]{"gui.no", "gui.yes"},role.specific?1:0));   
    	
    	if(role.specific){
	        if(scroll1 == null){
	        	scroll1 = new GuiCustomScroll(this,0);
	        	scroll1.setSize(175, 154);
	        }
	        scroll1.guiLeft = guiLeft + 4;
	        scroll1.guiTop = guiTop + 58;
	        this.addScroll(scroll1);
	        addLabel(new GuiNpcLabel(11, "guard.availableTargets", guiLeft + 4, guiTop + 48));
	        
	        if(scroll2 == null){
	        	scroll2 = new GuiCustomScroll(this,1);
	        	scroll2.setSize(175, 154);
	        }
	        scroll2.guiLeft = guiLeft + 235;
	        scroll2.guiTop = guiTop + 58;
	        this.addScroll(scroll2);
	        addLabel(new GuiNpcLabel(12, "guard.currentTargets", guiLeft + 235, guiTop + 48));
	        
	        List<String> all = new ArrayList<String>();
	        for(Object entity : EntityList.stringToClassMapping.keySet()){
	        	String name = "entity." + entity + ".name";
	        	Class cl = (Class) EntityList.stringToClassMapping.get(entity);
	        	if(role.targets.contains(name) || EntityNPCInterface.class.isAssignableFrom(cl))
	        		continue;
	        	if(EntityLivingBase.class.isAssignableFrom(cl))
	        		all.add(name);
	        }
	        scroll1.setList(all);
	        scroll2.setList(role.targets);
	        
	
	    	addButton(new GuiNpcButton(11, guiLeft + 180, guiTop + 80, 55, 20, ">"));
	    	addButton(new GuiNpcButton(12, guiLeft + 180, guiTop + 102, 55, 20, "<"));
	
	    	addButton(new GuiNpcButton(13, guiLeft + 180, guiTop + 130, 55, 20, ">>"));
	    	addButton(new GuiNpcButton(14, guiLeft + 180, guiTop + 152, 55, 20, "<<"));
    	}
    }

    protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0){
        	role.attacksAnimals = button.getValue()==1;
        }
        if(button.id == 1){
        	role.attackHostileMobs = button.getValue()==1;
        	initGui();
        }
        if(button.id == 2){
        	role.attackCreepers = button.getValue()==1;
        }     
        if(button.id == 3){
        	role.specific = button.getValue()==1;
        	initGui();
        }   

		if(button.id == 11){
			if(scroll1.hasSelected()){
				role.targets.add(scroll1.getSelected());
				scroll1.selected = -1;
				scroll1.selected = -1;
				initGui();
			}				
		}
		if(button.id == 12){
			if(scroll2.hasSelected()){
				role.targets.remove(scroll2.getSelected());
				scroll2.selected = -1;
				initGui();
			}				
		}
		if(button.id == 13){
			role.targets.clear();
	        List<String> all = new ArrayList<String>();
	        for(Object entity : EntityList.stringToClassMapping.keySet()){
	        	String name = "entity." + entity + ".name";
	        	Class cl = (Class) EntityList.stringToClassMapping.get(entity);
	        	if(EntityLivingBase.class.isAssignableFrom(cl))
	        		all.add(name);
	        }
	        role.targets = all;
			scroll1.selected = -1;
			scroll1.selected = -1;
			initGui();
		}
		if(button.id == 14){
			role.targets.clear();
			scroll1.selected = -1;
			scroll1.selected = -1;
			initGui();
		}
    }

	public void save() {
		Client.sendData(EnumPacketServer.JobSave, role.writeToNBT(new NBTTagCompound()));
	}

}
