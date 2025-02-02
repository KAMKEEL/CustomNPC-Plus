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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.io.IOException;

public final class TagSavePacket extends AbstractPacket {
    public static String packetName = "Request|TagSave";

    private NBTTagCompound tagNBT;

    public TagSavePacket(NBTTagCompound tagNBT) {
        this.tagNBT = tagNBT;
    }

    public TagSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TagSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_TAG;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, tagNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player)) return;
        Tag tag = new Tag();
        tag.readNBT(ByteBufUtils.readNBT(in));
        TagController.getInstance().saveTag(tag);
        NoppesUtilServer.sendTagDataAll((EntityPlayerMP) player);
        NBTTagCompound comp = new NBTTagCompound();
        tag.writeNBT(comp);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, comp);
    }
}
