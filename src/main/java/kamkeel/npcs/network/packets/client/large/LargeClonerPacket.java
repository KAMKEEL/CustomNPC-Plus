package kamkeel.npcs.network.packets.client.large;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;

import java.io.IOException;

public final class LargeClonerPacket extends LargeAbstractPacket {
    public static final String packetName = "Client|Clone";

    private NBTTagCompound compound;

    public LargeClonerPacket() {}

    public LargeClonerPacket(NBTTagCompound comp){
        this.compound = comp;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.CLONE_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(nbt));
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
        NBTTagCompound nbt = ByteBufUtils.readBigNBT(data);
        NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(nbt));
    }
}
