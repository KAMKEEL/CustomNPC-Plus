package kamkeel.npcs.network.packets.player;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.roles.RoleCompanion;

import java.io.IOException;

public class CompanionActionPacket extends AbstractPacket {
    public static final String packetName = "Player|CompanionAction";

    private Type type;
    private int talentOrdinal;
    private int exp;

    public CompanionActionPacket() {

    }

    public static void OpenInventory() {
        CompanionActionPacket packet = new CompanionActionPacket();
        packet.type = Type.OpenInventory;
        PacketClient.sendClient(packet);
    }

    public static void TalentExp(EnumCompanionTalent talent, int exp) {
        CompanionActionPacket packet = new CompanionActionPacket();
        packet.type = Type.TalentExp;
        packet.talentOrdinal = talent.ordinal();
        packet.exp = exp;
        PacketClient.sendClient(packet);
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.CompanionAction;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());

        if (type == Type.TalentExp) {
            out.writeInt(talentOrdinal);
            out.writeInt(exp);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Type type = Type.values()[in.readInt()];

        if(npc.advanced.role != EnumRoleType.Companion || player != npc.getOwner())
            return;

        if (type == Type.OpenInventory) {
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionInv, npc);
            return;
        }
        int id = in.readInt();
        int exp = in.readInt();
        RoleCompanion role = (RoleCompanion) npc.roleInterface;
        if(exp <= 0 || !role.canAddExp(-exp) || id < 0 || id >= EnumCompanionTalent.values().length) //should never happen unless hacking
            return;
        EnumCompanionTalent talent = EnumCompanionTalent.values()[id];
        role.addExp(-exp);
        role.addTalentExp(talent, exp);
    }

    private enum Type {
        OpenInventory,
        TalentExp
    }
}
