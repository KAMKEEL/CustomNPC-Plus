package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiNpcMobSpawnerMounter extends GuiNPCInterface implements IGuiData{
    
    private GuiCustomScroll scroll;
    
    private int posX,posY,posZ;
    
    private List<String> list;
    
    private static int showingClones = 0;

	private static String search = "";
	
	private int activeTab =  1;
    
	public GuiNpcMobSpawnerMounter(int i, int j, int k) {
		super();
        xSize = 256;

        posX = i;
        posY = j;
        posZ = k;
        
        this.closeOnEsc = true;
        
        setBackground("menubg.png");
	}
    public void initGui()
    {
        super.initGui();
        guiTop += 10;
        
        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0);
	        scroll.setSize(165, 188);
        }
        else
        	scroll.clear();
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 26;
        addScroll(scroll);
        
    	addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 4, guiTop + 4, 165, 20, search));

        GuiMenuTopButton button;
        addTopButton(button = new GuiMenuTopButton(3,guiLeft + 4, guiTop - 17, "spawner.clones"));
        button.active = showingClones == 0;
        addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
        button.active = showingClones == 1;
        addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
        button.active = showingClones == 2;

        addButton(new GuiNpcButton(1, guiLeft + 170, guiTop + 6, 82, 20, "spawner.mount"));

        addButton(new GuiNpcButton(2, guiLeft + 170, guiTop + 50, 82, 20, "spawner.mountplayer"));

        if(showingClones == 0 || showingClones == 2){
			addSideButton(new GuiMenuSideButton(21,guiLeft - 90, this.guiTop + 2, 90,22, "1"));
			addSideButton(new GuiMenuSideButton(22,guiLeft - 90, this.guiTop + 23, 90,22, "2"));
			addSideButton(new GuiMenuSideButton(23,guiLeft - 90, this.guiTop + 44, 90,22, "3"));
			addSideButton(new GuiMenuSideButton(24,guiLeft - 90, this.guiTop + 65, 90,22, "4"));
			addSideButton(new GuiMenuSideButton(25,guiLeft - 90, this.guiTop + 86, 90,22, "5"));
			addSideButton(new GuiMenuSideButton(26,guiLeft - 45, this.guiTop + 107, 45,22, "6"));
			addSideButton(new GuiMenuSideButton(27,guiLeft - 90, this.guiTop + 107, 45,22, "7"));
			addSideButton(new GuiMenuSideButton(28,guiLeft - 45, this.guiTop + 128, 45,22, "8"));
			addSideButton(new GuiMenuSideButton(29,guiLeft - 90, this.guiTop + 128, 45,22, "9"));
			addSideButton(new GuiMenuSideButton(30,guiLeft - 45, this.guiTop + 149, 45,22, "10"));
			addSideButton(new GuiMenuSideButton(31,guiLeft - 90, this.guiTop + 149, 45,22, "11"));
			addSideButton(new GuiMenuSideButton(32,guiLeft - 45, this.guiTop + 170, 45,22, "12"));
			addSideButton(new GuiMenuSideButton(33,guiLeft - 90, this.guiTop + 170, 45,22, "13"));
			addSideButton(new GuiMenuSideButton(34,guiLeft - 45, this.guiTop + 191, 45,22, "14"));
			addSideButton(new GuiMenuSideButton(35,guiLeft - 90, this.guiTop + 191, 45,22, "15"));
        	
        	getSideButton(20 + activeTab).active = true;
        	showClones();
        }
        else
        	showEntities();
    }
	private void showEntities() {
        Map<?,?> data = EntityList.stringToClassMapping;
        ArrayList<String> list = new ArrayList<String>();
        for(Object name : data.keySet()){
        	Class<?> c = (Class<?>) data.get(name);
        	try {
        		if(EntityLiving.class.isAssignableFrom(c) && c.getConstructor(new Class[] {World.class}) != null && !Modifier.isAbstract(c.getModifiers()))
        				list.add(name.toString());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			}
        }  
        this.list = list;
        scroll.setList(getSearchList());
	}
	private void showClones() {

		if(showingClones == 2){
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
        
        ArrayList<String> list = new ArrayList<String>();
        this.list = ClientCloneController.Instance.getClones(activeTab);
        scroll.setList(getSearchList());
	}
    public void keyTyped(char c, int i)
    {
    	super.keyTyped(c, i);
    	
    	if(search.equals(getTextField(1).getText()))
    		return;
    	search = getTextField(1).getText().toLowerCase();
    	scroll.setList(getSearchList());
    }
    private List<String> getSearchList(){
    	if(search.isEmpty())
    		return new ArrayList<String>(list);
    	List<String> list = new ArrayList<String>();
    	for(String name : this.list){
    		if(name.toLowerCase().contains(search))
    			list.add(name);
    	}
    	return list;
    }
	private NBTTagCompound getCompound(){
    	String sel = scroll.getSelected();
    	if(sel == null)
    		return null;
    	
    	if(showingClones == 0){
    		return ClientCloneController.Instance.getCloneData(player, sel, activeTab);
    	}
    	else{
    		Entity entity = EntityList.createEntityByName(sel, Minecraft.getMinecraft().theWorld);
    		if(entity == null)
    			return null;
    		NBTTagCompound compound = new NBTTagCompound();
    		entity.writeToNBTOptional(compound);
    		return compound;
    	}
		
	}
	
	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
    	if(id == 0){
    		close();
    	}
    	if(id == 1){
    		NBTTagCompound compound = getCompound();
    		if(compound != null){
    			compound.setTag("Pos", this.newDoubleNBTList(new double[] {this.posX + 0.5, this.posY + 1, this.posZ + 0.5}));
    			Client.sendData(EnumPacketServer.SpawnRider, compound);
	    		close();
    		}
    	}
    	if(id == 2){
			Client.sendData(EnumPacketServer.PlayerRider);
    		close();
    	}
    	if(id == 3){
    		showingClones = 0;
    		initGui();
    	}
    	if(id == 4){
    		showingClones = 1;
    		initGui();
    	}
    	if(id == 5){
    		showingClones = 2;
    		initGui();
    	}
    	if(id > 20){
    		activeTab = id - 20;
    		initGui();
    	}
    }
    protected NBTTagList newDoubleNBTList(double ... par1ArrayOfDouble)
    {
        NBTTagList nbttaglist = new NBTTagList();
        double[] adouble = par1ArrayOfDouble;
        int i = par1ArrayOfDouble.length;

        for (int j = 0; j < i; ++j)
        {
            double d1 = adouble[j];
            nbttaglist.appendTag(new NBTTagDouble(d1));
        }

        return nbttaglist;
    }
	
	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtlist = compound.getTagList("List", 8);
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < nbtlist.tagCount(); i++){
			list.add(nbtlist.getStringTagAt(i));
		}
		this.list = list;
        scroll.setList(getSearchList());
	}
}
