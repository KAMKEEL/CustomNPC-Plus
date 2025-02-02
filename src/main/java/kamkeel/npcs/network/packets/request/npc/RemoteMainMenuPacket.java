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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.constants.EnumGuiType;

import java.io.IOException;

public final class RemoteMainMenuPacket extends AbstractPacket {
    public static String packetName = "Request|RemoteMainMenu";

    public RemoteMainMenuPacket() {
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

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        // No extra data since npc is provided externally.
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, npc);
        if (ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            LogWriter.script(String.format("[%s] (Player) %s OPEN NPC %s (%s, %s, %s) [%s]",
                "WAND", player.getCommandSenderName(), npc.display.getName(),
                (int) npc.posX, (int) npc.posY, (int) npc.posZ,
                npc.worldObj.getWorldInfo().getWorldName()));
        }
    }
}
