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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;

import java.io.IOException;

public final class RecipeRemovePacket extends AbstractPacket {
    public static String packetName = "Request|RecipeRemove";

    private int recipeId;
    private boolean isAnvil;

    public RecipeRemovePacket(int recipeId, boolean isAnvil) {
        this.recipeId = recipeId;
        this.isAnvil = isAnvil;
    }

    public RecipeRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RecipeRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_RECIPE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(recipeId);
        out.writeBoolean(isAnvil);
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
            RecipeAnvil recipe = RecipeController.Instance.deleteAnvil(id);
            NoppesUtilServer.sendRecipeData((EntityPlayerMP) player, 1);
            NoppesUtilServer.setRecipeAnvilGui((EntityPlayerMP) player, new RecipeAnvil());
        } else {
            RecipeCarpentry recipe = RecipeController.Instance.delete(id);
            NoppesUtilServer.sendRecipeData((EntityPlayerMP) player, recipe.isGlobal ? 3 : 4);
            NoppesUtilServer.setRecipeGui((EntityPlayerMP) player, new RecipeCarpentry(""));
        }
    }
}
