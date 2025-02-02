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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;

import java.io.IOException;

public final class RecipeSavePacket extends AbstractPacket {
    public static String packetName = "Request|RecipeSave";

    private NBTTagCompound recipeNBT;

    public RecipeSavePacket(NBTTagCompound recipeNBT) {
        this.recipeNBT = recipeNBT;
    }

    public RecipeSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RecipeSave;
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
        ByteBufUtils.writeNBT(out, recipeNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        RecipeCarpentry recipe = RecipeController.Instance.saveRecipe(compound);
        NoppesUtilServer.sendRecipeData((EntityPlayerMP) player, recipe.isGlobal ? 3 : 4);
        NoppesUtilServer.setRecipeGui((EntityPlayerMP) player, recipe);
    }
}
