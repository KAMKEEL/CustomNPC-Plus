package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.controllers.data.RecipeScript;

import java.io.IOException;

public final class RecipeScriptPacket extends AbstractPacket {
    public static String packetName = "Request|RecipeScript";

    private Action type;
    private boolean anvil;
    private int id;
    private int tab;
    private int maxSize;
    private NBTTagCompound compound;

    public RecipeScriptPacket() {
    }

    public RecipeScriptPacket(Action type, boolean anvil, int id, int tab, int maxSize, NBTTagCompound compound) {
        this.type = type;
        this.anvil = anvil;
        this.id = id;
        this.tab = tab;
        this.maxSize = maxSize;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RecipeScript;
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
        out.writeInt(type.ordinal());
        out.writeBoolean(anvil);
        out.writeInt(id);
        if (type == Action.SAVE) {
            out.writeInt(tab);
            out.writeInt(maxSize);
            ByteBufUtils.writeNBT(out, compound);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT))
            return;

        Action action = Action.values()[in.readInt()];
        boolean isAnvil = in.readBoolean();
        int recipeId = in.readInt();

        if (isAnvil) {
            RecipeAnvil recipe = RecipeController.Instance.getAnvilRecipe(recipeId);
            if (recipe == null)
                return;
            handle(action, recipe.getOrCreateScriptHandler(), in, (EntityPlayerMP) player);
        } else {
            RecipeCarpentry recipe = RecipeController.Instance.getRecipe(recipeId);
            if (recipe == null)
                return;
            handle(action, recipe.getOrCreateScriptHandler(), in, (EntityPlayerMP) player);
        }
    }

    private void handle(Action action, RecipeScript script, ByteBuf in, EntityPlayerMP player) throws IOException {
        if (action == Action.GET) {
            PacketUtil.getScripts(script, player);
        } else {
            script.saveScript(in);
            if (ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                // TODO: Log Recipe Script Saving
            }
        }
    }

    public static void Save(boolean anvil, int id, int tab, int maxSize, NBTTagCompound compound) {
        PacketClient.sendClient(new RecipeScriptPacket(Action.SAVE, anvil, id, tab, maxSize, compound));
    }

    public static void Get(boolean anvil, int id) {
        PacketClient.sendClient(new RecipeScriptPacket(Action.GET, anvil, id, -1, -1, new NBTTagCompound()));
    }

    private enum Action {
        GET,
        SAVE
    }
}
