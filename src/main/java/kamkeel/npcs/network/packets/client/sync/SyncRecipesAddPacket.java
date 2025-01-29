package kamkeel.npcs.network.packets.client.sync;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;

import java.io.IOException;

public final class SyncRecipesAddPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncRecipesAdd";

    public SyncRecipesAddPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNCRECIPES_ADD;
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
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        NBTTagList list = Server.readNBT(in).getTagList("recipes", 10);
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                RecipeCarpentry recipe = RecipeCarpentry.read(list.getCompoundTagAt(i));
                RecipeController.syncRecipes.put(recipe.id, recipe);
            }
        }
    }
}
