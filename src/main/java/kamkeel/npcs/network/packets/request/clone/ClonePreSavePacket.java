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

    public ClonePreSavePacket() {
    }

    public ClonePreSavePacket(String name, int tab) {
        this.name = name;
        this.tab = tab;
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
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.CLONER))
            return;

        boolean bo = ServerCloneController.Instance.getCloneData(null, ByteBufUtils.readString(in), in.readInt()) != null;
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("NameExists", bo);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
