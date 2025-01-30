package kamkeel.npcs.network.packets.client.sync;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.RecipeController;

import java.io.IOException;

public final class SyncRecipesWorkbenchPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncRecipesWorkbench";

    public SyncRecipesWorkbenchPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNCRECIPES_WORKBENCH;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        RecipeController.reloadGlobalRecipes(RecipeController.syncRecipes);
        RecipeController.syncRecipes.clear();
    }
}
