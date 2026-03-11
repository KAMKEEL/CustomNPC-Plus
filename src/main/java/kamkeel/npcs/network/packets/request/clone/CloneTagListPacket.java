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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.ServerTagMapController;
import noppes.npcs.controllers.data.TagMap;

import java.io.IOException;

public final class CloneTagListPacket extends AbstractPacket {
    public static String packetName = "Request|CloneTagList";

    private int tab;
    private String folderName;

    public CloneTagListPacket(int tab) {
        this.tab = tab;
        this.folderName = null;
    }

    public CloneTagListPacket(String folderName) {
        this.tab = -1;
        this.folderName = folderName;
    }

    public CloneTagListPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneTagList;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(tab);
        if (tab == -1) {
            ByteBufUtils.writeString(out, folderName);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        int tab = in.readInt();
        TagMap tagMap;
        if (tab == -1) {
            String folder = ByteBufUtils.readString(in);
            tagMap = ServerTagMapController.Instance.getTagMap(folder);
        } else {
            tagMap = ServerTagMapController.Instance.getTagMap(tab);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("CloneTags", tagMap.writeNBT());
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
