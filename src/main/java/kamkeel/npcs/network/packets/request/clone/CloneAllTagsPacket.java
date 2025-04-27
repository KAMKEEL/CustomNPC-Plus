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
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.io.IOException;
import java.util.HashSet;

public final class CloneAllTagsPacket extends AbstractPacket {
    public static String packetName = "Request|CloneAllTags";

    public CloneAllTagsPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneAllTags;
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
        HashSet<Tag> validTags = TagController.getInstance().getAllTags();
        NBTTagList validTagList = new NBTTagList();
        for (Tag tag : validTags) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tag.writeNBT(tagCompound);
            validTagList.appendTag(tagCompound);
        }
        compound.setTag("AllTags", validTagList);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
