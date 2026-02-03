package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

/**
 * Request packet to remove a custom ability by UUID.
 */
public final class CustomAbilityRemovePacket extends AbstractPacket {
    public static String packetName = "Request|CustomAbilityRemove";

    private String uuid;

    public CustomAbilityRemovePacket() {
    }

    public CustomAbilityRemovePacket(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CustomAbilityRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_ABILITY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, uuid);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String id = ByteBufUtils.readString(in);
        AbilityController.Instance.deleteCustomAbility(id);
        NoppesUtilServer.sendCustomAbilitiesData((EntityPlayerMP) player);
    }
}
