package kamkeel.npcs.network.packets.request.item;

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
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.blocks.tiles.TileBanner;
import noppes.npcs.blocks.tiles.TileChair;

import java.io.IOException;

public final class HammerPacket extends AbstractPacket {
    public static String packetName = "Request|Hammer";

    private int x;
    private int y;
    private int z;

    public HammerPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public HammerPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.Hammer;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_BUILD;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.x);
        out.writeInt(this.y);
        out.writeInt(this.z);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.HAMMER, player))
            return;

        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();

        TileEntity tile = player.worldObj.getTileEntity(x, y, z);
        if (tile instanceof TileChair) {
            ((TileChair) tile).push();
        } else if (tile instanceof TileBanner) {
            ((TileBanner) tile).changeVariant();
        }
    }
}
