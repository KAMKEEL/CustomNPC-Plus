package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCTransportCategoryEdit extends GuiNPCInterface
{
	private GuiScreen parent;
	private String name;
	private int id;
    public GuiNPCTransportCategoryEdit(EntityNPCInterface npc,GuiScreen parent, String name, int id)
    {
    	super(npc);
    	this.parent = parent;
    	this.name = name;
    	this.id = id;
    	title = "Npc Transport Category";
    }

    public void initGui()
    {
        super.initGui();
    	this.addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, width / 2 - 40, 100, 140, 20, name));
        addLabel(new GuiNpcLabel(1,"Title:", width / 2 - 100+4, 105, 0xffffff));
        
    	this.addButton(new GuiNpcButton(2, width / 2 - 100, 210,98, 20, "gui.back"));
    	this.addButton(new GuiNpcButton(3, width / 2 +2, 210,98, 20, "Save"));
    }


    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
    }

	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 2)
        {
        	NoppesUtil.openGUI(player, parent);
    		Client.sendData(EnumPacketServer.TransportCategoriesGet);
        }
        if(id == 3)
        {
        	save();
        	NoppesUtil.openGUI(player, parent);
        	Client.sendData(EnumPacketServer.TransportCategoriesGet);
        }
    }
	public void save() {
		String name = getTextField(1).getText();
		if(name.trim().isEmpty())
			return;
		Client.sendData(EnumPacketServer.TransportCategorySave,name,id);
	}
//	@Override
//    public void drawDefaultBackground()
//    {
//		drawBackground(0);
//    }

}
