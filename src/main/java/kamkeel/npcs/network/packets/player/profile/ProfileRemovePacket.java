package kamkeel.npcs.network.packets.player.profile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.ProfileOperation;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.ChatAlertPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigMain;

import java.io.IOException;

public final class ProfileRemovePacket extends AbstractPacket {
    public static String packetName = "Request|ProfileRemove";

    private int slotID;

    public ProfileRemovePacket() {
    }

    public ProfileRemovePacket(int slotID) {
        this.slotID = slotID;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.ProfileRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.PROFILE_DELETE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.slotID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!ConfigMain.ProfilesEnabled)
            return;

        int slot = in.readInt();
        ProfileOperation operation = ProfileController.Instance.removeSlot(player, slot);
        ProfileGetPacket.sendProfileNBT(player);
        ProfileGetInfoPacket.sendProfileInfo(player);
        ChatAlertPacket.sendChatAlert((EntityPlayerMP) player, operation.getMessage());
    }
}
