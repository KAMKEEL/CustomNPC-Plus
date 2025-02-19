package kamkeel.npcs.network.packets.request.profile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.IOException;

public final class ProfileGetPacket extends AbstractPacket {
    public static String packetName = "Request|ProfileGet";

    public ProfileGetPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.ProfileGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {}

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        Profile profile = ProfileController.getProfile(player);
        NBTTagCompound compound = profile.writeToNBT();
        compound.setBoolean("PROFILE", true);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
