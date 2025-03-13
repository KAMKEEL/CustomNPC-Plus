package kamkeel.npcs.network.packets.request.naturalspawns;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.SpawnController;

import java.io.IOException;

public final class NaturalSpawnRemovePacket extends AbstractPacket {
    public static String packetName = "Request|NaturalSpawnRemove";

    private int spawnId;

    public NaturalSpawnRemovePacket(int spawnId) {
        this.spawnId = spawnId;
    }

    public NaturalSpawnRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.NaturalSpawnRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_NATURALSPAWN;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(spawnId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;
        int id = in.readInt();
        SpawnController.Instance.removeSpawnData(id);
        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, SpawnController.Instance.getScroll(), EnumScrollData.OPTIONAL);
    }
}
