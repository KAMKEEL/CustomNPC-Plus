package kamkeel.npcs.network.packets.request.transport;

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
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.roles.RoleTransporter;

import java.io.IOException;

public final class TransportSavePacket extends AbstractPacket {
    public static String packetName = "Request|TransportSave";

    private int categoryId;
    private NBTTagCompound compound;

    public TransportSavePacket(int categoryId, NBTTagCompound compound) {
        this.categoryId = categoryId;
        this.compound = compound;
    }

    public TransportSavePacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.TransportSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_ADVANCED;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(categoryId);
        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int cat = in.readInt();
        TransportLocation location = TransportController.getInstance().saveLocation(cat, ByteBufUtils.readNBT(in), npc);
        if(location != null){
            if(npc.advanced.role != EnumRoleType.Transporter)
                return;
            RoleTransporter role = (RoleTransporter) npc.roleInterface;
            role.setTransport(location);
        }
    }
}
