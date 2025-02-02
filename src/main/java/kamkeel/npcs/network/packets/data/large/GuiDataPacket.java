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
import noppes.npcs.client.gui.util.IGuiData;

import java.io.IOException;

public final class GuiDataPacket extends LargeAbstractPacket {
    public static final String packetName = "Data|GuiData";

    private NBTTagCompound compound;

    public GuiDataPacket() {
    }

    public GuiDataPacket(NBTTagCompound comp) {
        this.compound = comp;
    }

    public static void sendGuiData(EntityPlayerMP playerMP, NBTTagCompound compound) {
        GuiDataPacket packet = new GuiDataPacket(compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.GUI_DATA;
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
            gui = ((GuiNPCInterface) gui).getSubGui();
        } else if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
            gui = ((GuiContainerNPCInterface) gui).getSubGui();
        }
        if (gui instanceof IGuiData) {
            NBTTagCompound nbt = ByteBufUtils.readBigNBT(data);
            ((IGuiData) gui).setGuiData(nbt);
        }
    }
}
