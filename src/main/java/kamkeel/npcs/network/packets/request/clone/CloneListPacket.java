package kamkeel.npcs.network.packets.request.clone;

import cpw.mods.fml.common.FMLCommonHandler;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class CloneListPacket extends AbstractPacket {
    public static String packetName = "Request|CloneList";

    private int tab;

    public CloneListPacket(int tab) {
        this.tab = tab;
    }

    public CloneListPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.CloneList;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(tab);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.MOUNTER, EnumItemPacketType.CLONER))
            return;

        int id = in.readInt();
        Entity entity = player.worldObj.getEntityByID(id);
        if (!(entity instanceof EntityNPCInterface))
            return;

        NBTTagList list = new NBTTagList();
        int tab = in.readInt();
        for(String name : ServerCloneController.Instance.getClones(tab))
            list.appendTag(new NBTTagString(name));

        NBTTagList listDate = new NBTTagList();
        for(String name : ServerCloneController.Instance.getClonesDate(tab))
            listDate.appendTag(new NBTTagString(name));

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("List", list);
        compound.setTag("ListDate", listDate);

        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
