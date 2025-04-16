package kamkeel.npcs.network.packets.request.faction;

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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;

import java.io.IOException;

public final class FactionRemovePacket extends AbstractPacket {
    public static String packetName = "Request|FactionRemove";

    private int factionId;

    public FactionRemovePacket(int factionId) {
        this.factionId = factionId;
    }

    public FactionRemovePacket() {

    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.FactionRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_FACTION;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(factionId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        FactionController.getInstance().delete(id);
        NoppesUtilServer.sendFactionDataAll((EntityPlayerMP) player);
        NBTTagCompound comp = new NBTTagCompound();
        new Faction().writeNBT(comp);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, comp);
    }
}
