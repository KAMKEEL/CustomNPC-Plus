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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.TagController;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class TagSetPacket extends AbstractPacket {
    public static String packetName = "Request|TagSet";

    private NBTTagCompound compound;

    public TagSetPacket() {
    }

    public TagSetPacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TagSet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_ADVANCED_TAGS;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND, EnumItemPacketType.CLONER))
            return;

        this.setTags(npc, in);
    }

    private void setTags(EntityNPCInterface npc, ByteBuf buffer) throws IOException {
        npc.advanced.tagUUIDs.removeIf(uuid -> TagController.getInstance().getTagFromUUID(uuid) != null);
        NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
        NBTTagList list = compound.getTagList("TagNames", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            String tagName = list.getStringTagAt(i);
            npc.advanced.tagUUIDs.add(TagController.getInstance().getTagFromName(tagName).uuid);
        }
    }
}
