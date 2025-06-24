package kamkeel.npcs.network.packets.request.party;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class PartySavePacket extends AbstractPacket {
    public static final String packetName = "Request|PartySave";

    private NBTTagCompound compound;

    public PartySavePacket() {
    }

    public PartySavePacket(NBTTagCompound partyCompound) {
        this.compound = partyCompound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartySave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID != null) {
            Party party = PartyController.Instance().getParty(playerData.partyUUID);
            party.readClientNBT(ByteBufUtils.readNBT(in));
            PartyController.Instance().pingPartyUpdate(party);
        }
    }
}
