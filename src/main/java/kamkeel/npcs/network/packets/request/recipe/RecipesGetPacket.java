package kamkeel.npcs.network.packets.request.recipe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

public final class RecipesGetPacket extends AbstractPacket {
    public static String packetName = "Request|RecipesGet";

    private int categoryId;

    public RecipesGetPacket(int categoryId) {
        this.categoryId = categoryId;
    }

    public RecipesGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RecipesGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(categoryId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player)) return;
        int id = in.readInt();
        NoppesUtilServer.sendRecipeData((EntityPlayerMP) player, id);
    }
}
