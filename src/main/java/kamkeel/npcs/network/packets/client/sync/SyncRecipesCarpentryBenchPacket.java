package kamkeel.npcs.network.packets.client.sync;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.RecipeController;

import java.io.IOException;

public final class SyncRecipesCarpentryBenchPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncRecipesCarpentryBench";

    public SyncRecipesCarpentryBenchPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNCRECIPES_CARPENTRYBENCH;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        RecipeController.Instance.anvilRecipes = RecipeController.syncRecipes;
        RecipeController.syncRecipes.clear();
    }
}
