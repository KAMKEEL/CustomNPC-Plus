package kamkeel.npcs.network.packets.request.npc;

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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.entity.EntityCustomNpc;

import java.io.IOException;

public final class ModelDataSavePacket extends AbstractPacket {
    public static String packetName = "Request|ModelDataSave";

    private NBTTagCompound save;

    public ModelDataSavePacket() {
    }

    public ModelDataSavePacket(NBTTagCompound save) {
        this.save = save;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ModelDataSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_DISPLAY;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.save);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        if (npc instanceof EntityCustomNpc)
            ((EntityCustomNpc) npc).modelData.readFromNBT(ByteBufUtils.readNBT(in));
    }
}
