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
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;

import java.io.IOException;

public final class NaturalSpawnGetPacket extends AbstractPacket {
    public static String packetName = "Request|NaturalSpawnGet";

    private int spawnId;

    public NaturalSpawnGetPacket(int spawnId) {
        this.spawnId = spawnId;
    }

    public NaturalSpawnGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.NaturalSpawnGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
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
        SpawnData spawn = SpawnController.Instance.getSpawnData(id);
        if (spawn != null) {
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, spawn.writeNBT(new NBTTagCompound()));
        }
    }
}
