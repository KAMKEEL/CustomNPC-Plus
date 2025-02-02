package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public class BankPacket extends AbstractPacket {
    public static final String packetName = "Player|Follower";

    private Action type;
    private int slotID;
    private int bankID;

    public BankPacket() {

    }

    private BankPacket(Action action, int bankID, int slotID) {
        this.type = action;
        this.bankID = bankID;
        this.slotID = slotID;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    public static BankPacket Open(int bankID, int slotID) {
        return new BankPacket(Action.OpenSlot, bankID, slotID);
    }
    public static BankPacket Upgrade() {
        return new BankPacket(Action.Upgrade, -1, -1);
    }
    public static BankPacket Unlock() {
        return new BankPacket(Action.Unlock, -1, -1);
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.BankAction;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());

        if (type == Action.OpenSlot) {
            out.writeInt(bankID);
            out.writeInt(slotID);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Action requestedAction = Action.values()[in.readInt()];

        if (!(player instanceof EntityPlayerMP))
            return;

        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        if(npc.advanced.role != EnumRoleType.Bank)
            return;

        switch (requestedAction) {
            case Unlock:
                NoppesUtilPlayer.bankUnlock(playerMP, npc);
                break;
            case Upgrade:
                NoppesUtilPlayer.bankUpgrade(playerMP, npc);
                break;
            case OpenSlot:
                int bankID = in.readInt();
                int slotID = in.readInt();
                BankData data = PlayerDataController.Instance.getBankData(playerMP,bankID).getBankOrDefault(bankID);
                data.openBankGui(playerMP, npc, bankID, slotID);
                break;
        }

    }

    private enum Action {
        Unlock,
        Upgrade,
        OpenSlot
    }
}
