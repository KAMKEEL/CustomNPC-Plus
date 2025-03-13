package kamkeel.npcs.network.packets.request.recipe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;

import java.io.IOException;

public final class RecipeGetPacket extends AbstractPacket {
    public static String packetName = "Request|RecipeGet";

    private int recipeId;
    private boolean isAnvil = false;

    public RecipeGetPacket(int recipeId, boolean anvil) {
        this.recipeId = recipeId;
        this.isAnvil = anvil;
    }

    public RecipeGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RecipeGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(recipeId);
        out.writeBoolean(this.isAnvil);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;
        int id = in.readInt();
        boolean anvil = in.readBoolean();
        if (anvil) {
            RecipeAnvil recipe = RecipeController.Instance.getAnvilRecipe(id);
            NoppesUtilServer.setRecipeAnvilGui((EntityPlayerMP) player, recipe);
        } else {
            RecipeCarpentry recipe = RecipeController.Instance.getRecipe(id);
            NoppesUtilServer.setRecipeGui((EntityPlayerMP) player, recipe);
        }
    }
}
