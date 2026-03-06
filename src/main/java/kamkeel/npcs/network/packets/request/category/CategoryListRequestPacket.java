package kamkeel.npcs.network.packets.request.category;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.CategoryManager;

import java.io.IOException;

public final class CategoryListRequestPacket extends AbstractPacket {
    public static String packetName = "NPC|CatList";

    private int catType;

    public CategoryListRequestPacket(int catType) {
        this.catType = catType;
    }

    public CategoryListRequestPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CategoryListRequest;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(catType);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int type = in.readInt();

        CategoryManager cm = CategorySavePacket.getManager(type);
        if (cm == null)
            return;

        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, cm.getCategoryScrollData(), EnumScrollData.CATEGORY_LIST);
    }
}
