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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class CloneSavePacket extends AbstractPacket {
    public static String packetName = "Request|CloneSave";

    private String name;
    private int tab;
    private String folderName;
    private NBTTagCompound tagExtra;
    private NBTTagCompound tagCompound;

    public CloneSavePacket() {
    }

    public CloneSavePacket(String name, int tab, NBTTagCompound tagExtra, NBTTagCompound tagCompound) {
        this.name = name;
        this.tab = tab;
        this.folderName = null;
        this.tagExtra = tagExtra;
        this.tagCompound = tagCompound;
    }

    public CloneSavePacket(String name, String folderName, NBTTagCompound tagExtra, NBTTagCompound tagCompound) {
        this.name = name;
        this.tab = -1;
        this.folderName = folderName;
        this.tagExtra = tagExtra;
        this.tagCompound = tagCompound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneSave;
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
        ByteBufUtils.writeNBT(out, this.tagExtra);
        ByteBufUtils.writeNBT(out, this.tagCompound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        PlayerData data = PlayerData.get(player);
        if (data.cloned == null)
            return;

        String name = ByteBufUtils.readString(in);
        int tab = in.readInt();
        String folder = null;
        if (tab == -1) {
            folder = ByteBufUtils.readString(in);
        }
        NBTTagCompound tagExtra = ByteBufUtils.readNBT(in);
        NBTTagCompound tagCompound = ByteBufUtils.readNBT(in);

        NBTTagList tagList = tagCompound.getTagList("TagUUIDs", 8);
        data.cloned.setTag("TagUUIDs", tagList);

        if (folder != null) {
            ServerCloneController.Instance.addClone(data.cloned, name, folder, tagExtra);
        } else {
            ServerCloneController.Instance.addClone(data.cloned, name, tab, tagExtra);
        }
    }
}
