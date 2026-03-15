package kamkeel.npcs.network.packets.request.dialog;

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
import noppes.npcs.controllers.data.DialogOption;

import java.io.IOException;

public final class DialogNpcSetPacket extends AbstractPacket {
    public static String packetName = "Request|DialogNpcSet";

    private int slot;
    private int dialog;

    public DialogNpcSetPacket(int slot, int dialog) {
        this.slot = slot;
        this.dialog = dialog;
    }

    public DialogNpcSetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogNpcSet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_ADVANCED_DIALOG;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(slot);
        out.writeInt(dialog);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player)) return;
        int s = in.readInt();
        int d = in.readInt();
        DialogOption option = NoppesUtilServer.setNpcDialog(s, d, player);
        if (option != null && option.hasDialog()) {
            NBTTagCompound compound = option.writeNBT();
            compound.setInteger("Position", s);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }
}
