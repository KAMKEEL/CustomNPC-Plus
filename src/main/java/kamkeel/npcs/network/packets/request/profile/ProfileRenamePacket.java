package kamkeel.npcs.network.packets.request.profile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public final class ProfileRenamePacket extends AbstractPacket {
    public static String packetName = "Request|ProfileRename";

    private int slotID;
    private String name;

    public ProfileRenamePacket() {}

    public ProfileRenamePacket(int slotID, String name) {
        this.slotID = slotID;
        this.name = name;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ProfileRename;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.PROFILE_RENAME;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.slotID);
        ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        int slot = in.readInt();
        String newName = ByteBufUtils.readString(in);

        Profile profile = ProfileController.getProfile(player);
        if(!profile.getSlots().containsKey(slot)) {
            // TODO: Check Operation and Send Message
            return;
        }

        ProfileController.getProfile(player).getSlots().get(slot).setName(newName);
        ProfileController.save(player, ProfileController.getProfile(player));

        ProfileGetPacket.sendProfileNBT(player);
        ProfileGetInfoPacket.sendProfileInfo(player);
    }
}
