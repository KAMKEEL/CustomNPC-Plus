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

public final class ClonePreSavePacket extends AbstractPacket {
    public static String packetName = "Request|ClonePreSave";

    private String name;
    private int tab;
    private String folderName;

    public ClonePreSavePacket() {
    }

    public ClonePreSavePacket(String name, int tab) {
        this.name = name;
        this.tab = tab;
        this.folderName = null;
    }

    public ClonePreSavePacket(String name, String folderName) {
        this.name = name;
        this.tab = -1;
        this.folderName = folderName;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ClonePreSave;
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
        ByteBufUtils.writeString(out, this.name);
        out.writeInt(tab);
        if (tab == -1) {
            ByteBufUtils.writeString(out, this.folderName);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        String name = ByteBufUtils.readString(in);
        int tab = in.readInt();
        boolean bo;
        if (tab == -1) {
            String folder = ByteBufUtils.readString(in);
            bo = ServerCloneController.Instance.getCloneData(null, name, folder) != null;
        } else {
            bo = ServerCloneController.Instance.getCloneData(null, name, tab) != null;
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("NameExists", bo);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
