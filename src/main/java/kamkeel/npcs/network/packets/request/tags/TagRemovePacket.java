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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.io.IOException;

public final class TagRemovePacket extends AbstractPacket {
    public static String packetName = "Request|TagRemove";

    private int tagId;

    public TagRemovePacket(int tagId) {
        this.tagId = tagId;
    }

    public TagRemovePacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.TagRemove;
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
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(tagId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        TagController.getInstance().delete(id);
        NoppesUtilServer.sendTagDataAll((EntityPlayerMP) player);
        NBTTagCompound comp = new NBTTagCompound();
        (new Tag()).writeNBT(comp);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, comp);
    }
}
