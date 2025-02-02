package kamkeel.npcs.network.packets.request.bank;

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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank;

import java.io.IOException;

public final class BankRemovePacket extends AbstractPacket {
    public static String packetName = "Request|BankRemove";

    private int bankId;

    public BankRemovePacket(int bankId) {
        this.bankId = bankId;
    }

    public BankRemovePacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.BankRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_BANK;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(bankId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        BankController.getInstance().removeBank(id);
        NoppesUtilServer.sendBankDataAll((EntityPlayerMP) player);
        NoppesUtilServer.sendBank((EntityPlayerMP) player, new Bank());
    }
}
