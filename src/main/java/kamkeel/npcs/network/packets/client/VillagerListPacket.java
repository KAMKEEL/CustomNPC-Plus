package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ServerEventsHandler;
import net.minecraft.village.MerchantRecipeList;

import java.io.IOException;

public final class VillagerListPacket extends AbstractPacket {
    public static final String packetName = "Client|VillagerList";

    private MerchantRecipeList merchantrecipelist;

    public VillagerListPacket() {}

    public VillagerListPacket(MerchantRecipeList merchantrecipelist) {
        this.merchantrecipelist = merchantrecipelist;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.VILLAGER_LIST;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.fillBuffer(out, merchantrecipelist);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        MerchantRecipeList recipes = MerchantRecipeList.func_151390_b(new PacketBuffer(in));
        ServerEventsHandler.Merchant.setRecipes(recipes);
    }
}
