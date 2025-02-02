package kamkeel.npcs.network.packets.request.customgui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.scripted.NpcAPI;

import java.io.IOException;

public final class CustomScrollClickPacket extends AbstractPacket {
    public static final String packetName = "Request|CustomGuiScrollClick";

    private int scrollId;
    private int scrollIndex;
    private NBTTagCompound selection;
    private boolean doubleClick;
    private NBTTagCompound compound;

    public CustomScrollClickPacket() {}

    public CustomScrollClickPacket(NBTTagCompound compound, int scrollId, int scrollIndex, NBTTagCompound selection, boolean doubleClick) {
        this.compound = compound;
        this.scrollId = scrollId;
        this.scrollIndex = scrollIndex;
        this.selection = selection;
        this.doubleClick = doubleClick;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CustomGuiScrollClick;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, compound);
        out.writeInt(this.scrollId);
        out.writeInt(this.scrollIndex);
        ByteBufUtils.writeNBT(out, selection);
        out.writeBoolean(this.doubleClick);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player.openContainer instanceof ContainerCustomGui))
            return;

        NBTTagCompound comp = ByteBufUtils.readNBT(in);
        int scrollId = in.readInt();
        int scrollIndex = in.readInt();
        String[] list = CustomGuiController.readScrollSelection(in);
        boolean doubleClick = in.readBoolean();

        ((ContainerCustomGui) player.openContainer).customGui.fromNBT(comp);
        EventHooks.onCustomGuiScrollClick((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui) player.openContainer).customGui, scrollId, scrollIndex, list, doubleClick);
    }
}
