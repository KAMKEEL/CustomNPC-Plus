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
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.wrapper.nbt.NBTWrapper;

import java.io.IOException;

public final class CloneFolderCrudPacket extends AbstractPacket {
    public static String packetName = "Request|CloneFolderCrud";

    public static final byte ACTION_CREATE = 0;
    public static final byte ACTION_RENAME = 1;
    public static final byte ACTION_DELETE = 2;

    private byte action;
    private String name;
    private String newName;

    public CloneFolderCrudPacket() {
    }

    public CloneFolderCrudPacket(byte action, String name) {
        this.action = action;
        this.name = name;
        this.newName = "";
    }

    public CloneFolderCrudPacket(byte action, String name, String newName) {
        this.action = action;
        this.name = name;
        this.newName = newName;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneFolderCrud;
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
        out.writeByte(action);
        ByteBufUtils.writeString(out, name);
        if (action == ACTION_RENAME) {
            ByteBufUtils.writeString(out, newName);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        byte action = in.readByte();
        String name = ByteBufUtils.readString(in);

        boolean success = false;
        switch (action) {
            case ACTION_CREATE:
                if (CloneFolder.isValidName(name))
                    success = ServerCloneController.Instance.createFolder(name) != null;
                break;
            case ACTION_RENAME:
                String newName = ByteBufUtils.readString(in);
                if (CloneFolder.isValidName(newName))
                    success = ServerCloneController.Instance.renameFolder(name, newName);
                break;
            case ACTION_DELETE:
                success = ServerCloneController.Instance.deleteFolder(name);
                break;
        }

        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Success", success);
        NBTTagList folderList = new NBTTagList();
        for (CloneFolder folder : ServerCloneController.Instance.getFolderList()) {
            folderList.appendTag(((NBTWrapper) folder.writeNBT(new NBTWrapper(new NBTTagCompound()))).getMCTag());
        }
        compound.setTag("CloneFolders", folderList);

        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
