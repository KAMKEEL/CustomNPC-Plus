package noppes.npcs.client.gui.roles;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Bank;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleBank;


public class GuiNpcBankSetup extends GuiNPCInterface2 implements IScrollData,ICustomScrollListener
{
	private GuiCustomScroll scroll;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private RoleBank role;

    public GuiNpcBankSetup(EntityNPCInterface npc)
    {
    	super(npc);
    	Client.sendData(EnumPacketServer.BanksGet);
    	role = (RoleBank) npc.roleInterface;
    }

    public void initGui()
    {
        super.initGui();
        if(scroll == null)
        	scroll = new GuiCustomScroll(this,0);
        scroll.setSize(200, 152);
        scroll.guiLeft = guiLeft + 85;
        scroll.guiTop = guiTop + 20;
        addScroll(scroll);
    }

	protected void actionPerformed(GuiButton guibutton)
    {
    }
	
	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) 
	{
		String name = null;
		Bank bank = role.getBank();
		if(bank != null)
			name = bank.name;
		this.data = data;
		scroll.setList(list);
		
		if(name != null)
			setSelected(name);
	}
	
    public void mouseClicked(int i, int j, int k)
    {
    	super.mouseClicked(i, j, k);
    	if(k == 0 && scroll != null)
    		scroll.mouseClicked(i, j, k);
    }
    
	@Override
	public void setSelected(String selected) {
		scroll.setSelected(selected);
	}
	
	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			role.bankId = data.get(scroll.getSelected());
			save();
		}
	}
	
	public void save() {
		Client.sendData(EnumPacketServer.RoleSave, role.writeToNBT(new NBTTagCompound()));
	}
}
