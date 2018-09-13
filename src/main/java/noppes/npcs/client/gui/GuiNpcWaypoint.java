package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.blocks.tiles.TileWaypoint;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcWaypoint extends GuiNPCInterface{

	private TileWaypoint tile;
	
    public GuiNpcWaypoint(int x, int y, int z) {
		super();
		tile = (TileWaypoint) player.worldObj.getTileEntity(x, y, z);
    	xSize = 265;
	}
	public void initGui()
    {
    	super.initGui();

    	
    	addLabel(new GuiNpcLabel(0,"gui.name", guiLeft+ 1, guiTop + 76, 0xffffff));    	
    	addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft+ 60, guiTop + 71,200,20, tile.name));
    	
    	addLabel(new GuiNpcLabel(1,"gui.range", guiLeft+ 1, guiTop + 97, 0xffffff));    	
    	addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft+ 60, guiTop + 92,200,20, tile.range + ""));
    	getTextField(1).numbersOnly = true;
    	getTextField(1).setMinMaxDefault(2, 60, 10);

        addButton(new GuiNpcButton(0, guiLeft + 40, guiTop + 190,120,20, "Done"));
    }
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if(id == 0)
			close();
	}
	@Override
	public void save() {
		tile.name = getTextField(0).getText();
		tile.range = getTextField(1).getInteger();
		
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}

}
