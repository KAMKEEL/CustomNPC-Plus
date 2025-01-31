package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.io.IOException;
import java.util.UUID;

public final class TagsNpcGetPacket extends AbstractPacket {
    public static final String packetName = "Request|NpcTagsGet";

    public TagsNpcGetPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.NpcTagsGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public boolean needsNPC(){
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {}

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        for (UUID uuid : npc.advanced.tagUUIDs) {
            Tag tag = TagController.getInstance().getTagFromUUID(uuid);
            if (tag != null) {
                tagList.appendTag(new NBTTagString(tag.name));
            }
        }
        compound.setTag("TagNames",tagList);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
