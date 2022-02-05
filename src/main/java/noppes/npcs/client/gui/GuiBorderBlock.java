package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;

public class GuiBorderBlock extends GuiNPCInterface implements IGuiData{

	private TileBorder tile;
	
    public GuiBorderBlock(int x, int y, int z) {
		super();
		tile = (TileBorder) player.worldObj.getTileEntity(x, y, z);
		
		Client.sendData(EnumPacketServer.GetTileEntity, x, y, z);
	}
	public void initGui()
    {
    	super.initGui();
    	
		this.addButton(new GuiNpcButton(4, guiLeft + 40, guiTop + 40, 120, 20, "Availability Options"));
    	
    	addLabel(new GuiNpcLabel(0,"Height", guiLeft+ 1, guiTop + 76, 0xffffff));    	
    	addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft+ 60, guiTop + 71,40,20, tile.height + ""));
    	getTextField(0).numbersOnly = true;
    	getTextField(0).setMinMaxDefault(0, 500, 6); 	

    	addLabel(new GuiNpcLabel(1,"Message", guiLeft+ 1, guiTop + 100, 0xffffff));    	
    	addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft+ 60, guiTop + 95,200,20, tile.message));
    	
        addButton(new GuiNpcButton(0, guiLeft + 40, guiTop + 190,120,20, "Done"));
    }
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if(id == 0)
			close();
		if (id == 4) {
			save();
			setSubGui(new SubGuiNpcAvailability(tile.availability));
		}
	}
	@Override
	public void save() {
		if(tile == null)
			return;
		tile.height = getTextField(0).getInteger();
		tile.message = getTextField(1).getText();
		
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}
	@Override
	public void setGuiData(NBTTagCompound compound) {
		tile.readFromNBT(compound);
	}

}
