package kamkeel.npcs.network.packets.request.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCycle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import java.io.IOException;

public class MagicGetAllPacket extends AbstractPacket {

    public static String packetName = "Request|MagicGetAll";

    public MagicGetAllPacket() { }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MagicGetAll;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {}

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        NBTTagCompound compound = new NBTTagCompound();
        StringBuilder cycleNames = new StringBuilder();
        for (MagicCycle cycle : MagicController.getInstance().cycles.values()) {
            cycleNames.append(cycle.name).append(",");
        }
        compound.setString("CycleNames", cycleNames.toString());
        // Build comma-separated list of magic names.
        StringBuilder magicNames = new StringBuilder();
        for (Magic magic : MagicController.getInstance().magics.values()) {
            magicNames.append(magic.name).append(",");
        }
        compound.setString("MagicNames", magicNames.toString());
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
