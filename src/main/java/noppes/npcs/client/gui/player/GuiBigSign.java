package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.SaveSignPacket;
import noppes.npcs.blocks.tiles.TileBigSign;
import noppes.npcs.client.gui.SubGuiNpcTextArea;

public class GuiBigSign extends SubGuiNpcTextArea {
    public TileBigSign tile;

    public GuiBigSign(int x, int y, int z) {
        super("");
        tile = (TileBigSign) player.worldObj.getTileEntity(x, y, z);
        text = tile.getText();
    }

    @Override
    public void close() {
        super.close();
        PacketClient.sendClient(new SaveSignPacket(tile.xCoord, tile.yCoord, tile.zCoord, text));
    }
}
