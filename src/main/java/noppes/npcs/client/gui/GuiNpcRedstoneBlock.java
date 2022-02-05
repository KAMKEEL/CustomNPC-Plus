package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.blocks.tiles.TileRedstoneBlock;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcRedstoneBlock extends GuiNPCInterface{

	private TileRedstoneBlock tile;
	
    public GuiNpcRedstoneBlock(int x, int y, int z) {
		super();
		tile = (TileRedstoneBlock) player.worldObj.getTileEntity(x, y, z);
	}
    
    @Override
	public void initGui(){
    	super.initGui();

		this.addButton(new GuiNpcButton(4, guiLeft + 40, guiTop + 20, 120, 20, "availability.options"));

    	addLabel(new GuiNpcLabel(11,"gui.detailed", guiLeft+ 40, guiTop + 47, 0xffffff)); 
		this.addButton(new GuiNpcButton(1, guiLeft + 110, guiTop + 42, 50, 20, new String[]{"gui.no", "gui.yes"},tile.isDetailed?1:0));
    	
		if(tile.isDetailed){
	    	addLabel(new GuiNpcLabel(0,StatCollector.translateToLocal("bard.ondistance") + " X:", guiLeft+ 1, guiTop + 76, 0xffffff));    	
	    	addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft+ 80, guiTop + 71,30,20, tile.onRangeX + ""));
	    	getTextField(0).numbersOnly = true;
	    	getTextField(0).setMinMaxDefault(0, 50, 6); 	
	    	addLabel(new GuiNpcLabel(1,"Y:", guiLeft+ 113, guiTop + 76, 0xffffff)); 
	    	addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft+ 122, guiTop + 71,30,20, tile.onRangeY + ""));
	    	getTextField(1).numbersOnly = true;
	    	getTextField(1).setMinMaxDefault(0, 50, 6); 
	    	addLabel(new GuiNpcLabel(2,"Z:", guiLeft+ 155, guiTop + 76, 0xffffff)); 
	    	addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft+ 164, guiTop + 71,30,20, tile.onRangeZ + ""));
	    	getTextField(2).numbersOnly = true;
	    	getTextField(2).setMinMaxDefault(0, 50, 6);
	    	
	    	addLabel(new GuiNpcLabel(3,StatCollector.translateToLocal("bard.offdistance") + " X:", guiLeft - 3, guiTop + 99, 0xffffff));    	
	    	addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft+ 80, guiTop + 94,30,20, tile.offRangeX + ""));
	    	getTextField(3).numbersOnly = true;
	    	getTextField(3).setMinMaxDefault(0, 50, 10);  	
	    	addLabel(new GuiNpcLabel(4,"Y:", guiLeft+ 113, guiTop + 99, 0xffffff)); 
	    	addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft+ 122, guiTop + 94,30,20, tile.offRangeY + ""));
	    	getTextField(4).numbersOnly = true;
	    	getTextField(4).setMinMaxDefault(0, 50, 10);  	
	    	addLabel(new GuiNpcLabel(5,"Z:", guiLeft+ 155, guiTop + 99, 0xffffff)); 
	    	addTextField(new GuiNpcTextField(5, this, fontRendererObj, guiLeft+ 164, guiTop + 94,30,20, tile.offRangeZ + ""));
	    	getTextField(5).numbersOnly = true;
	    	getTextField(5).setMinMaxDefault(0, 50, 10);
		}
		else{
	    	addLabel(new GuiNpcLabel(0,"bard.ondistance", guiLeft+ 1, guiTop + 76, 0xffffff));    	
	    	addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft+ 80, guiTop + 71,30,20, tile.onRange + ""));
	    	getTextField(0).numbersOnly = true;
	    	getTextField(0).setMinMaxDefault(0, 50, 6); 	
	    	
	    	addLabel(new GuiNpcLabel(3,"bard.offdistance", guiLeft - 3, guiTop + 99, 0xffffff));    	
	    	addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft+ 80, guiTop + 94,30,20, tile.offRange + ""));
	    	getTextField(3).numbersOnly = true;
	    	getTextField(3).setMinMaxDefault(0, 50, 10);  
		}
        addButton(new GuiNpcButton(0, guiLeft + 40, guiTop + 190,120,20, "Done"));
    }

    @Override
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if(id == 0)
			close();
		if(id == 1){
			tile.isDetailed = ((GuiNpcButton)guibutton).getValue() == 1;
			initGui();
		}
		if (id == 4) {
			save();
			setSubGui(new SubGuiNpcAvailability(tile.availability));
		}
	}
	@Override
	public void save() {
		if(tile == null)
			return;
		if(tile.isDetailed){
			tile.onRangeX = getTextField(0).getInteger();
			tile.onRangeY = getTextField(1).getInteger();
			tile.onRangeZ = getTextField(2).getInteger();

			tile.offRangeX = getTextField(3).getInteger();
			tile.offRangeY = getTextField(4).getInteger();
			tile.offRangeZ = getTextField(5).getInteger();
			
			if(tile.onRangeX > tile.offRangeX)
				tile.offRangeX = tile.onRangeX;
			if(tile.onRangeY > tile.offRangeY)
				tile.offRangeY = tile.onRangeY;
			if(tile.onRangeZ > tile.offRangeZ)
				tile.offRangeZ = tile.onRangeZ;
		}
		else{
			tile.onRange = getTextField(0).getInteger();
			tile.offRange = getTextField(3).getInteger();
			if(tile.onRange > tile.offRange)
				tile.offRange = tile.onRange;
		}
		tile.isActivated = false;
		
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		Client.sendData(EnumPacketServer.SaveTileEntity, compound);
	}

}
