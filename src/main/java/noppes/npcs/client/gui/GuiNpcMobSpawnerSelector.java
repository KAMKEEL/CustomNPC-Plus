package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcMobSpawnerSelector extends SubGuiInterface implements IGuiData{
    
    private GuiCustomScroll scroll;
    private List<String> list;
    
	private static String search = "";
	public int activeTab =  1;
	public boolean isServer = false;
    
	public GuiNpcMobSpawnerSelector() {
		super();
        xSize = 256;
        this.closeOnEsc = true;
        setBackground("menubg.png");
	}
	
	
    public void initGui(){
        super.initGui();        
        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0);
	        scroll.setSize(165, 188);
        }
        else
        	scroll.clear();
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 26;
        addScroll(scroll);

//        GuiMenuTopButton button;
//        addTopButton(button = new GuiMenuTopButton(3,guiLeft + 4, guiTop - 17, "spawner.clones"));
//        button.active = !isServer;
//        addTopButton(button = new GuiMenuTopButton(4, button, "gui.server"));
//        button.active = isServer;
        
    	addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 4, guiTop + 4, 165, 20, search));

    	addButton(new GuiNpcButton(0, guiLeft + 171, guiTop + 80, 80, 20, "gui.done"));
    	addButton(new GuiNpcButton(1, guiLeft + 171, guiTop + 103, 80, 20, "gui.cancel"));

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
    
    public String getSelected(){
    	return scroll.getSelected();
    }
	private void showClones() {

		if(isServer){
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
        
        ArrayList<String> list = new ArrayList<String>();
        
        this.list = new ArrayList<String>(ClientCloneController.Instance.getClones(activeTab));
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
	public NBTTagCompound getCompound(){
    	String sel = scroll.getSelected();
    	if(sel == null)
    		return null;

		return ClientCloneController.Instance.getCloneData(player, sel, activeTab);
	}
	
	public void buttonEvent(GuiButton guibutton)
    {
		int id = guibutton.id;
    	if(id == 0){
    		close();
    	}
    	if(id == 1){
    		scroll.clear();
    		close();
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
