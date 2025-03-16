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
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;

import java.io.IOException;

public final class NaturalSpawnSavePacket extends AbstractPacket {
    public static String packetName = "Request|NaturalSpawnSave";

    private NBTTagCompound spawnNBT;

    public NaturalSpawnSavePacket(NBTTagCompound spawnNBT) {
        this.spawnNBT = spawnNBT;
    }

    public NaturalSpawnSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.NaturalSpawnSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_NATURALSPAWN;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, spawnNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        SpawnData data = new SpawnData();
        data.readNBT(compound);
        SpawnController.Instance.saveSpawnData(data);
        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, SpawnController.Instance.getScroll(), EnumScrollData.OPTIONAL);
    }
}
