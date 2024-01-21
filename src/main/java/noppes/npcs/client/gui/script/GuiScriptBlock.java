package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;

public class GuiScriptBlock extends GuiScriptInterface {
    private TileScripted tileScripted;

    public GuiScriptBlock(int x, int y, int z) {
        hookList.add("init");
        hookList.add("tick");
        hookList.add("tossed");
        hookList.add("pickedUp");
        hookList.add("spawn");
        hookList.add("interact");
        hookList.add("attack");
        hookList.add("startItem");
        hookList.add("usingItem");
        hookList.add("stopItem");
        hookList.add("finishItem");

        this.handler = this.tileScripted = (TileScripted) player.worldObj.getTileEntity(x, y, z);
        Client.sendData(EnumPacketServer.ScriptBlockDataGet);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.tileScripted.setNBT(compound);
        super.setGuiData(compound);
    }

    public void save() {
        super.save();
        Client.sendData(EnumPacketServer.ScriptBlockDataSave, tileScripted.xCoord, tileScripted.yCoord, tileScripted.zCoord, this.tileScripted.getNBT(new NBTTagCompound()));
    }
}