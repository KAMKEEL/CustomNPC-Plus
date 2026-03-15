package kamkeel.npcs.network.packets.request.clone;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.wrapper.nbt.NBTWrapper;

import java.io.IOException;

public final class CloneFolderListPacket extends AbstractPacket {
    public static String packetName = "Request|CloneFolderList";

    public CloneFolderListPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneFolderList;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList folderList = new NBTTagList();
        for (CloneFolder folder : ServerCloneController.Instance.getFolderList()) {
            folderList.appendTag(((NBTWrapper) folder.writeNBT(new NBTWrapper(new NBTTagCompound()))).getMCTag());
        }
        compound.setTag("CloneFolders", folderList);

        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
