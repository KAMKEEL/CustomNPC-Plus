package kamkeel.npcs.network.packets.player;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.roles.RoleTrader;

import java.io.IOException;

/**
 * Client requests trader-specific GUI data from server.
 * Server responds with player balance, stock info, currency costs via GuiDataPacket.
 */
public class GetTraderData extends AbstractPacket {
    public static final String packetName = "Player|GetTraderData";

    @Override
    public Enum getType() {
        return EnumPlayerPacket.GetTraderData;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // No data needed for request
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!(npc.roleInterface instanceof RoleTrader))
            return;

        RoleTrader role = (RoleTrader) npc.roleInterface;
        NBTTagCompound compound = new NBTTagCompound();

        // Player balance
        PlayerData data = PlayerData.get(player);
        compound.setLong("Balance", data.currencyData.getBalance());

        // Stock info
        compound.setBoolean("StockEnabled", role.stock.enableStock);
        compound.setLong("ResetTimeMillis", role.getResetTimeRemainingMillis());

        // Available stock per slot (player-specific if perPlayer mode)
        String playerName = player.getCommandSenderName();
        int[] stock = new int[18];
        for (int i = 0; i < 18; i++) {
            stock[i] = role.getAvailableStock(i, playerName);
        }
        compound.setIntArray("Stock", stock);

        // Currency cost per slot
        for (int i = 0; i < 18; i++) {
            compound.setLong("Cost" + i, role.getCurrencyCost(i));
        }

        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
