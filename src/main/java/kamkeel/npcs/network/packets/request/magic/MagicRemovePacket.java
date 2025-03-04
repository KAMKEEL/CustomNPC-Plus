package kamkeel.npcs.network.packets.request.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.MagicController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import java.io.IOException;

public class MagicRemovePacket extends AbstractPacket {

    public static String packetName = "Request|MagicRemove";

    private int magicId;

    public MagicRemovePacket() { }

    public MagicRemovePacket(int magicId) {
        this.magicId = magicId;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MagicRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_MAGIC;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(magicId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        int id = in.readInt();
        MagicController.getInstance().removeMagic(id);
    }
}
