package kamkeel.npcs.network.packets.request.clone;

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
import noppes.npcs.controllers.ServerCloneController;

import java.io.IOException;

public final class CloneMovePacket extends AbstractPacket {
    public static String packetName = "Request|CloneMove";

    private String cloneName;
    private int fromTab;
    private String fromFolder;
    private int toTab;
    private String toFolder;

    public CloneMovePacket() {
    }

    public CloneMovePacket(String cloneName, int fromTab, String fromFolder, int toTab, String toFolder) {
        this.cloneName = cloneName;
        this.fromTab = fromTab;
        this.fromFolder = fromFolder;
        this.toTab = toTab;
        this.toFolder = toFolder;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneMove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_CLONE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, cloneName);
        out.writeInt(fromTab);
        ByteBufUtils.writeString(out, fromFolder != null ? fromFolder : "");
        out.writeInt(toTab);
        ByteBufUtils.writeString(out, toFolder != null ? toFolder : "");
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        String cloneName = ByteBufUtils.readString(in);
        int fromTab = in.readInt();
        String fromFolder = ByteBufUtils.readString(in);
        if (fromFolder.isEmpty()) fromFolder = null;
        int toTab = in.readInt();
        String toFolder = ByteBufUtils.readString(in);
        if (toFolder.isEmpty()) toFolder = null;

        boolean success = false;
        if (fromFolder != null && toFolder != null) {
            success = ServerCloneController.Instance.moveClone(cloneName, fromFolder, toFolder);
        } else if (fromFolder != null) {
            success = ServerCloneController.Instance.moveClone(cloneName, fromFolder, toTab);
        } else if (toFolder != null) {
            success = ServerCloneController.Instance.moveClone(cloneName, fromTab, toFolder);
        } else {
            success = ServerCloneController.Instance.moveClone(cloneName, fromTab, toTab);
        }

        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("MoveSuccess", success);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
