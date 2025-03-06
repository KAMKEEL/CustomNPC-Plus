package kamkeel.npcs.network.packets.request.role;

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
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.roles.RoleCompanion;

import java.io.IOException;

public final class RoleCompanionUpdatePacket extends AbstractPacket {
    public static String packetName = "Request|RoleCompanionUpdate";

    private EnumCompanionStage companionStage;

    public RoleCompanionUpdatePacket() { }

    public RoleCompanionUpdatePacket(EnumCompanionStage companionStage) {
        this.companionStage = companionStage;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RoleCompanionUpdate;
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
        out.writeInt(this.companionStage.ordinal());
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        if(npc.advanced.role != EnumRoleType.Companion)
            return;

        ((RoleCompanion)npc.roleInterface).matureTo(EnumCompanionStage.values()[in.readInt()]);
        npc.updateClient = true;
    }
}
