package kamkeel.npcs.network.packets.request.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.MagicController;

import java.io.IOException;

public class MagicCycleRemovePacket extends AbstractPacket {

    public static String packetName = "Request|MagicCycleRemove";

    private int cycleId;

    public MagicCycleRemovePacket() {
    }

    public MagicCycleRemovePacket(int cycleId) {
        this.cycleId = cycleId;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MagicCycleRemove;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_MAGIC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(cycleId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        int id = in.readInt();
        MagicController.getInstance().removeCycle(id);

        NoppesUtilServer.sendMagicInfo((EntityPlayerMP) player, true);
        NoppesUtilServer.sendMagicInfo((EntityPlayerMP) player, false);
    }
}
