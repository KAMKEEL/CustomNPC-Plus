package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.BlockScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.constants.ScriptContext;

import java.util.ArrayList;

public class GuiScriptBlock extends GuiScriptInterface {
    private final TileScripted tileScripted;

    public GuiScriptBlock(int x, int y, int z) {
        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_BLOCK));

        this.handler = this.tileScripted = (TileScripted) player.worldObj.getTileEntity(x, y, z);
        BlockScriptPacket.Get(tileScripted.xCoord, tileScripted.yCoord, tileScripted.zCoord);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.tileScripted.setNBT(compound);
        super.setGuiData(compound);
        loaded = true;
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.BLOCK;
    }

    public void save() {
        if (loaded) {
            super.save();
            BlockScriptPacket.Save(tileScripted.xCoord, tileScripted.yCoord, tileScripted.zCoord, this.tileScripted.getNBT(new NBTTagCompound()));
        }
    }
}
