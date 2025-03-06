package kamkeel.npcs.network.packets.request.mainmenu;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public final class MainmenuAISavePacket extends AbstractPacket {
    public static String packetName = "Request|MainmenuInvSave";

    private NBTTagCompound compound;

    public MainmenuAISavePacket() {}

    public MainmenuAISavePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MainmenuAISave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_AI;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        npc.ais.readToNBT(ByteBufUtils.readNBT(in));
        npc.setHealth(npc.getMaxHealth());
        npc.updateAI = true;
        npc.updateClient = true;
    }
}
