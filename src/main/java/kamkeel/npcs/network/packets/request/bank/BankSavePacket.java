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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank;

import java.io.IOException;

public final class BankSavePacket extends AbstractPacket {
    public static String packetName = "Request|BankSave";

    private NBTTagCompound bankNBT;

    public BankSavePacket(NBTTagCompound bankNBT) {
        this.bankNBT = bankNBT;
    }

    public BankSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.BankSave;
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
        ByteBufUtils.writeNBT(out, bankNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Bank bank = new Bank();
        bank.readEntityFromNBT(compound);
        BankController.getInstance().saveBank(bank);
        NoppesUtilServer.sendBankDataAll((EntityPlayerMP) player);
        NoppesUtilServer.sendBank((EntityPlayerMP) player, bank);
    }
}
