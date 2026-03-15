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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.blocks.tiles.TileColorable;

import java.io.IOException;

import static noppes.npcs.items.ItemNpcTool.BRUSH_COLOR_TAG;
import static noppes.npcs.items.ItemNpcTool.getColor;

public final class ColorSetPacket extends AbstractPacket {
    public static String packetName = "Request|ColorSet";

    private int x;
    private int y;
    private int z;

    public ColorSetPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ColorSetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ColorSet;
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
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.BRUSH, player))
            return;

        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();

        TileEntity tile = player.worldObj.getTileEntity(x, y, z);
        ItemStack stack = player.getHeldItem();
        if (tile instanceof TileColorable) {
            int color = getColor(stack.getTagCompound());
            TileColorable colorable = (TileColorable) tile;
            colorable.setColor(color);
        }
    }

    public static void setBrushColor(ItemStack brush, int color) {
        NBTTagCompound brushCompound = brush.getTagCompound();
        if (brushCompound == null)
            brushCompound = new NBTTagCompound();

        brushCompound.setInteger(BRUSH_COLOR_TAG, color);
        brush.setTagCompound(brushCompound);
    }
}
