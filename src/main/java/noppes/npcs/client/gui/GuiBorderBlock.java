package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.TileEntityGetPacket;
import kamkeel.npcs.network.packets.request.TileEntitySavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.client.gui.util.*;

public class GuiBorderBlock extends GuiNPCInterface implements IGuiData {

    private final TileBorder tile;

    public GuiBorderBlock(int x, int y, int z) {
        super();
        tile = (TileBorder) player.worldObj.getTileEntity(x, y, z);

        PacketClient.sendClient(new TileEntityGetPacket(x, y, z));
    }

    public void initGui() {
        super.initGui();

        this.addButton(new GuiNpcButton(4, guiLeft + 40, guiTop + 40, 120, 20, "Availability Options"));

        addLabel(new GuiNpcLabel(0, "Height", guiLeft + 1, guiTop + 76, 0xffffff));
        addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 60, guiTop + 71, 40, 20, tile.height + ""));
        getTextField(0).integersOnly = true;
        getTextField(0).setMinMaxDefault(0, 500, 6);

        addLabel(new GuiNpcLabel(1, "Message", guiLeft + 1, guiTop + 100, 0xffffff));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 60, guiTop + 95, 200, 20, tile.message));

        addButton(new GuiNpcButton(0, guiLeft + 40, guiTop + 190, 120, 20, "Done"));
    }

    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 0)
            close();
        if (id == 4) {
            save();
            setSubGui(new SubGuiNpcAvailability(tile.availability));
        }
    }

    @Override
    public void save() {
        if (tile == null)
            return;
        tile.height = getTextField(0).getInteger();
        tile.message = getTextField(1).getText();

        NBTTagCompound compound = new NBTTagCompound();
        tile.writeToNBT(compound);
        PacketClient.sendClient(new TileEntitySavePacket(compound));
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        tile.readFromNBT(compound);
    }

}
