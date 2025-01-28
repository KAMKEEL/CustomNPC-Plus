package kamkeel.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;

import java.io.IOException;

public final class GUIClonerPacket extends AbstractPacket {
    public static final String packetName = "Client|Clone";

    public GUIClonerPacket() {}

    public GUIClonerPacket(boolean ok) {}

    @Override
    public Enum getType() {
        return EnumClientPacket.CLONE;
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
        if(CustomNpcs.side() != Side.CLIENT)
            return;
        NBTTagCompound nbt = Server.readNBT(in);
        NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(nbt));
    }
}
