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
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class RemoteMainMenuPacket extends AbstractPacket {
    public static String packetName = "Request|RemoteMainMenu";

    private int entityID;

    public RemoteMainMenuPacket() {}

    public RemoteMainMenuPacket(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RemoteMainMenu;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_GUI;
    }



    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        Entity entity = player.worldObj.getEntityByID(in.readInt());
        if(!(entity instanceof EntityNPCInterface))
            return;

        NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) entity);
        if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
            LogWriter.script(String.format("[%s] (Player) %s OPEN NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), ((EntityNPCInterface)entity).display.getName(), entity.posX, entity.posY, entity.posZ,  entity.worldObj.getWorldInfo().getWorldName()));
        }
    }
}
