package kamkeel.npcs.network.packets.request.jobs;

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
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.roles.JobSpawner;

import java.io.IOException;

public final class JobGetPacket extends AbstractPacket {
    public static String packetName = "Request|JobGet";

    public JobGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.JobGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_ADVANCED_JOB;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        if (npc.jobInterface == null)
            return;

        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("JobData", true);
        npc.jobInterface.writeToNBT(compound);
        if (npc.advanced.job == EnumJobType.Spawner)
            ((JobSpawner) npc.jobInterface).cleanCompound(compound);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        if (npc.advanced.job == EnumJobType.Spawner)
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, ((JobSpawner) npc.jobInterface).getTitles());
    }
}
