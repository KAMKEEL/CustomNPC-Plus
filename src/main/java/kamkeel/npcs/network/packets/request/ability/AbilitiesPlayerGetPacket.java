package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public class AbilitiesPlayerGetPacket extends AbstractPacket {
    public static final String packetName = "Request|PlayerDataAbilitiesGet";

    public AbilitiesPlayerGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataAbilitiesGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND))
            return;

        NBTTagCompound compound = new NBTTagCompound();
        PlayerData.get(player).abilities.writeToNBT(compound);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
