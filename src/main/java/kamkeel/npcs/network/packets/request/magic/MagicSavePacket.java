package kamkeel.npcs.network.packets.request.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import java.io.IOException;

public class MagicSavePacket extends AbstractPacket {

    public static String packetName = "Request|MagicSave";

    private NBTTagCompound compound;

    public MagicSavePacket() { }

    public MagicSavePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MagicSave;
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
        Magic magic = new Magic();
        magic.readNBT(comp);
        MagicController.getInstance().saveMagic(magic);

        NoppesUtilServer.sendMagicInfo((EntityPlayerMP) player, true);
        NoppesUtilServer.sendMagicInfo((EntityPlayerMP) player, false);
    }
}
