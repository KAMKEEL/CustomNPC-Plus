package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.BlockScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.blocks.tiles.TileScripted;


public class GuiScriptBlock extends GuiScriptInterface {
    private final TileScripted tileScripted;

    public GuiScriptBlock(int x, int y, int z) {
        hookList.add("init");
        hookList.add("tick");
        hookList.add("interact");
        hookList.add("fallenUpon");
        hookList.add("redstone");
        hookList.add("broken");
        hookList.add("exploded");
        hookList.add("rainFilled");
        hookList.add("neighborChanged");
        hookList.add("clicked");
        hookList.add("harvested");
        hookList.add("collide");
        hookList.add("timer");

        this.handler = this.tileScripted = (TileScripted) player.worldObj.getTileEntity(x, y, z);
        BlockScriptPacket.Get(tileScripted.xCoord, tileScripted.yCoord, tileScripted.zCoord);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.tileScripted.setNBT(compound);
        super.setGuiData(compound);
        loaded = true;
    }

    public void save() {
        if (loaded) {
            super.save();
            BlockScriptPacket.Save(tileScripted.xCoord, tileScripted.yCoord, tileScripted.zCoord, this.tileScripted.getNBT(new NBTTagCompound()));
        }
    }
}
