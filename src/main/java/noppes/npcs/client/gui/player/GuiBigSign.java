package noppes.npcs.client.gui.player;

import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.blocks.tiles.TileBigSign;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.constants.EnumPlayerPacket;

public class GuiBigSign extends SubGuiNpcTextArea{
	public TileBigSign tile;
	public GuiBigSign(int x, int y, int z) {
		super("");
		tile = (TileBigSign) player.worldObj.getTileEntity(x, y, z);
		text = tile.getText();
	}

	@Override
    public void close(){
		super.close();
		NoppesUtilPlayer.sendData(EnumPlayerPacket.SignSave, tile.xCoord, tile.yCoord, tile.zCoord, text);
	}
}
