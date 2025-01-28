package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IPartyData;

import java.io.IOException;

public final class PartyDataPacket extends AbstractPacket {
    public static final String packetName = "Client|PartyData";

    public PartyDataPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.PARTY_DATA;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiNPCInterface) gui).getSubGui();
        } else if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiContainerNPCInterface) gui).getSubGui();
        }
        if (gui instanceof IPartyData) {
            NBTTagCompound nbt = Server.readNBT(in);
            ((IPartyData) gui).setPartyData(nbt);
        }
    }
}
