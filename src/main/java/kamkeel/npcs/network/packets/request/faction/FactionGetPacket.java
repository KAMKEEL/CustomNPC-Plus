package kamkeel.npcs.network.packets.request.faction;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;

import java.io.IOException;

public final class FactionGetPacket extends AbstractPacket {
    public static String packetName = "Request|FactionGet";

    private int factionID;

    public FactionGetPacket() {}

    public FactionGetPacket(int factionID) {
        this.factionID = factionID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.FactionGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.factionID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        NBTTagCompound compound = new NBTTagCompound();
        Faction faction = FactionController.getInstance().get(in.readInt());
        faction.writeNBT(compound);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }

    public static void getFaction(int id){
        PacketClient.sendClient(new FactionGetPacket(id));
    }
}
