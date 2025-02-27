package kamkeel.npcs.network.packets.request.profile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.config.ConfigMain;

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

        if(!ConfigMain.ProfilesEnabled)
            return;

        sendProfileNBT(player);
    }

    public static void sendProfileNBT(EntityPlayer player){
        Profile profile = ProfileController.Instance.getProfile(player);
        NBTTagCompound compound = profile.writeToNBT();
        compound.setBoolean("PROFILE", true);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
