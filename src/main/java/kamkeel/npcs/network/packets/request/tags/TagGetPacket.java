package kamkeel.npcs.network.packets.request.tags;

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
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.io.IOException;

public final class TagGetPacket extends AbstractPacket {
    public static final String packetName = "Request|TagGet";

    private int tagID;

    public TagGetPacket() {}

    public TagGetPacket(int tagID) {
        this.tagID = tagID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TagGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(tagID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND, EnumItemPacketType.CLONER))
            return;

        NBTTagCompound compound = new NBTTagCompound();
        Tag tag = TagController.getInstance().get(in.readInt());
        tag.writeNBT(compound);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }


}
