package kamkeel.npcs.network.packets.request.magic;

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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.controllers.data.Tag;

import java.io.IOException;
import java.util.UUID;

public final class MagicNpcGetPacket extends AbstractPacket {
    public static final String packetName = "Request|NpcMagicGet";

    public MagicNpcGetPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.NpcMagicGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND))
            return;

        NBTTagCompound compound = new NBTTagCompound();
        npc.stats.magicData.writeToNBT(compound);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
