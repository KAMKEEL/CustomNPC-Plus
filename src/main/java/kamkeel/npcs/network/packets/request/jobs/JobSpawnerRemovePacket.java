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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.roles.JobSpawner;

import java.io.IOException;

public final class JobSpawnerRemovePacket extends AbstractPacket {
    public static String packetName = "Request|JobSpawnerRemove";

    private int slot;

    public JobSpawnerRemovePacket() {
    }

    public JobSpawnerRemovePacket(int slot) {
        this.slot = slot;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.JobSpawnerRemove;
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
        out.writeInt(this.slot);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        if (npc.advanced.job != EnumJobType.Spawner)
            return;

        JobSpawner job = (JobSpawner) npc.jobInterface;
        job.setJobCompound(in.readInt(), null);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, job.getTitles());
    }
}
