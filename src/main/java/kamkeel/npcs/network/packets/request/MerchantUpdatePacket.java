package kamkeel.npcs.network.packets.request;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public final class MerchantUpdatePacket extends AbstractPacket {
    public static String packetName = "Request|MerchantUpdate";

    private int entityID;
    private MerchantRecipeList list;

    public MerchantUpdatePacket() {
    }

    public MerchantUpdatePacket(int entityID, MerchantRecipeList list) {
        this.entityID = entityID;
        this.list = list;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MerchantUpdate;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.EDIT_VILLAGER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.fillBuffer(out, this.entityID, this.list);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int entityId = in.readInt();
        Entity entity = player.worldObj.getEntityByID(entityId);
        if (!(entity instanceof EntityVillager))
            return;

        MerchantRecipeList list = MerchantRecipeList.func_151390_b(new PacketBuffer(in));
        ((EntityVillager) entity).setRecipes(list);
    }
}
