package kamkeel.npcs.network.packets.request.script.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.io.IOException;

public final class ItemScriptPacket extends AbstractPacket {
    public static String packetName = "Request|ItemScript";

    private ItemScriptPacket.Action type;
    private NBTTagCompound compound;

    public ItemScriptPacket() {
    }

    public ItemScriptPacket(Action type, NBTTagCompound compound) {
        this.type = type;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ItemScript;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SCRIPT_ITEM;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());

        if (type == Action.SAVE) {
            ByteBufUtils.writeNBT(out, this.compound);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT))
            return;

        Action requestedAction = Action.values()[in.readInt()];
        if (requestedAction == Action.GET) {
            ScriptCustomItem iw = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
            iw.loadScriptData();
            NBTTagCompound compound = iw.getMCNbt();
            compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        } else {
            if (!player.capabilities.isCreativeMode) {
                return;
            }

            NBTTagCompound compound = ByteBufUtils.readNBT(in);
            ScriptCustomItem wrapper = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
            wrapper.setMCNbt(compound);
            wrapper.saveScriptData();
            wrapper.loaded = false;
            wrapper.errored.clear();
            wrapper.lastInited = -1;
            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
        }
    }

    public static void Save(NBTTagCompound compound) {
        PacketClient.sendClient(new ItemScriptPacket(Action.SAVE, compound));
    }

    public static void Get() {
        PacketClient.sendClient(new ItemScriptPacket(Action.GET, new NBTTagCompound()));
    }

    private enum Action {
        GET,
        SAVE
    }
}
