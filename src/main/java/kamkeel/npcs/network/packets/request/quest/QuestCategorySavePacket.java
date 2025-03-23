package kamkeel.npcs.network.packets.request.quest;

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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.QuestCategory;

import java.io.IOException;

public final class QuestCategorySavePacket extends AbstractPacket {
    public static String packetName = "Request|QuestCategorySave";

    private NBTTagCompound categoryNBT;

    public QuestCategorySavePacket(NBTTagCompound categoryNBT) {
        this.categoryNBT = categoryNBT;
    }

    public QuestCategorySavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestCategorySave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_QUEST;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, categoryNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        QuestCategory category = new QuestCategory();
        category.readNBT(compound);
        QuestController.Instance.saveCategory(category);
        NoppesUtilServer.sendQuestCategoryData((EntityPlayerMP) player);
    }
}
