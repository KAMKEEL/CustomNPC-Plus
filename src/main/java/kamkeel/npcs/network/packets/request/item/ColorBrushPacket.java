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
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

import static noppes.npcs.items.ItemNpcTool.BRUSH_COLOR_TAG;

public final class ColorBrushPacket extends AbstractPacket {
    public static String packetName = "Request|ColorBrush";

    private int color;

    public ColorBrushPacket(int color) {
        this.color = color;
    }

    public ColorBrushPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ColorBrush;
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
        out.writeInt(this.color);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.BRUSH, player))
            return;

        int color = in.readInt();
        ItemStack stack = player.getHeldItem();
        setBrushColor(stack, color);
    }

    public static void setBrushColor(ItemStack brush, int color) {
        NBTTagCompound brushCompound = brush.getTagCompound();
        if (brushCompound == null)
            brushCompound = new NBTTagCompound();

        brushCompound.setInteger(BRUSH_COLOR_TAG, color);
        brush.setTagCompound(brushCompound);
    }
}
