package kamkeel.npcs.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class GuiRedstonePacket extends AbstractPacket {
    public static final String packetName = "Client|GuiRedstone";

    private NBTTagCompound compound;

    public GuiRedstonePacket() {}

    public GuiRedstonePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI_REDSTONE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        NoppesUtil.saveRedstoneBlock(player, nbt);
    }
}
