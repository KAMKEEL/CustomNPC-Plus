package kamkeel.npcs.network.packets.request;

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
import kamkeel.npcs.network.packets.player.BankPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.ServerEventsHandler;

import java.io.IOException;

public final class MountPacket extends AbstractPacket {
    public static final String packetName = "Request|MountPacket";

    private Action action;
    private NBTTagCompound compound;

    public MountPacket() {}

    public MountPacket(Action action, NBTTagCompound nbtTagCompound) {
        this.action = action;
        this.compound = nbtTagCompound;
    }


    @Override
    public Enum getType() {
        return EnumRequestPacket.MountPacket;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.TOOL_MOUNTER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action.ordinal());
        if(action == Action.Spawn)
            ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.MOUNTER, player))
            return;

        MountPacket.Action requestedAction = MountPacket.Action.values()[in.readInt()];
        switch (requestedAction){
            case Player:
                player.mountEntity(ServerEventsHandler.mounted);
                break;
            case Spawn:
                Entity entity = EntityList.createEntityFromNBT(ByteBufUtils.readNBT(in), player.worldObj);
                player.worldObj.spawnEntityInWorld(entity);
                entity.mountEntity(ServerEventsHandler.mounted);
                break;
        }
    }

    public static MountPacket Player() {
        return new MountPacket(Action.Player, new NBTTagCompound());
    }
    public static MountPacket Spawn(NBTTagCompound compound) {
        return new MountPacket(Action.Spawn, compound);
    }

    private enum Action {
        Player,
        Spawn,
    }
}
