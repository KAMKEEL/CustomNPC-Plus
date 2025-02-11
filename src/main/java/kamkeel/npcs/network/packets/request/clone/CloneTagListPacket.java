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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.ServerTagMapController;
import noppes.npcs.controllers.data.TagMap;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class CloneTagListPacket extends AbstractPacket {
    public static String packetName = "Request|CloneTagList";

    private int tab;

    public CloneTagListPacket(int tab) {
        this.tab = tab;
    }

    public CloneTagListPacket() {}

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
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.CLONER))
            return;

        int tab = in.readInt();
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("CloneTags", tagMap.writeNBT());
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
