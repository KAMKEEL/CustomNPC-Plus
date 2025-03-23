package kamkeel.npcs.network.packets.request.npc;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class RemoteDeletePacket extends AbstractPacket {
    public static String packetName = "Request|RemoteDelete";

    private int entityId;

    public RemoteDeletePacket(int entityId) {
        this.entityId = entityId;
    }

    public RemoteDeletePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RemoteDelete;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_DELETE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(entityId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof net.minecraft.entity.player.EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;
        int id = in.readInt();
        Entity entity = player.worldObj.getEntityByID(id);
        if (!(entity instanceof EntityNPCInterface))
            return;

        npc = (EntityNPCInterface) entity;
        if (ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            LogWriter.script(String.format("[%s] (Player) %s DELETE NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), npc.display.getName(), (int) npc.posX, (int) npc.posY, (int) npc.posZ, npc.worldObj.getWorldInfo().getWorldName()));
        }
        npc.delete();
        NoppesUtilServer.deleteNpc(npc, player);
        NoppesUtilServer.sendNearbyNpcs((EntityPlayerMP) player);
    }
}
