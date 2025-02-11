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
import noppes.npcs.scripted.NpcAPI;

import java.io.IOException;

public final class CustomGuiButtonPacket extends AbstractPacket {
    public static final String packetName = "Request|CustomGuiButton";

    private NBTTagCompound compound;
    private int id;

    public CustomGuiButtonPacket() {
    }

    public CustomGuiButtonPacket(int id, NBTTagCompound comp) {
        this.compound = comp;
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CustomGuiButton;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(id);
        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player.openContainer instanceof ContainerCustomGui))
            return;

        int id = in.readInt();
        NBTTagCompound comp = ByteBufUtils.readNBT(in);

        ((ContainerCustomGui) player.openContainer).customGui.fromNBT(comp);
        EventHooks.onCustomGuiButton((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui) player.openContainer).customGui, id);
    }
}
