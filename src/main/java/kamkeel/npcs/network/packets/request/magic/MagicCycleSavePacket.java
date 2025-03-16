package kamkeel.npcs.network.packets.request.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.MagicCycle;

import java.io.IOException;

public class MagicCycleSavePacket extends AbstractPacket {

    public static String packetName = "Request|MagicCycleSave";

    private NBTTagCompound compound;

    public MagicCycleSavePacket() {
    }

    public MagicCycleSavePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MagicCycleSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_MAGIC;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        NBTTagCompound comp = ByteBufUtils.readNBT(in);
        MagicCycle cycle = new MagicCycle();
        cycle.readNBT(comp);
        MagicController.getInstance().saveCycle(cycle);

        NoppesUtilServer.sendMagicInfo((EntityPlayerMP) player, true);
        NoppesUtilServer.sendMagicInfo((EntityPlayerMP) player, false);
    }
}
