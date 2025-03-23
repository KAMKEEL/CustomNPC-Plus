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
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.ServerCloneController;

import java.io.IOException;

public final class CloneRemovePacket extends AbstractPacket {
    public static String packetName = "Request|CloneRemove";

    private int tab;
    private String name;

    public CloneRemovePacket() {
    }

    public CloneRemovePacket(int tab, String name) {
        this.tab = tab;
        this.name = name;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneRemove;
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
        out.writeInt(tab);
        ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        int tab = in.readInt();
        ServerCloneController.Instance.removeClone(ByteBufUtils.readString(in), tab);

        NBTTagList list = new NBTTagList();

        for (String name : ServerCloneController.Instance.getClones(tab))
            list.appendTag(new NBTTagString(name));

        NBTTagList listDate = new NBTTagList();
        for (String name : ServerCloneController.Instance.getClonesDate(tab))
            listDate.appendTag(new NBTTagString(name));

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("List", list);
        compound.setTag("ListDate", listDate);

        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
