package kamkeel.npcs.network.packets.data.large;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IPartyData;

import java.io.IOException;

public final class PartyDataPacket extends LargeAbstractPacket {
    public static final String packetName = "Client|PartyData";

    private NBTTagCompound compound;

    public PartyDataPacket(){}

    public PartyDataPacket(NBTTagCompound comp){
        this.compound = comp;
    }

    public static void sendPartyData(EntityPlayerMP playerMP, NBTTagCompound compound){
        PartyDataPacket packet = new PartyDataPacket(compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.PARTY_DATA;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeBigNBT(buffer, compound);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiNPCInterface) gui).getSubGui();
        } else if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiContainerNPCInterface) gui).getSubGui();
        }
        if (gui instanceof IPartyData) {
            NBTTagCompound nbt = ByteBufUtils.readBigNBT(data);
            ((IPartyData) gui).setPartyData(nbt);
        }
    }
}
