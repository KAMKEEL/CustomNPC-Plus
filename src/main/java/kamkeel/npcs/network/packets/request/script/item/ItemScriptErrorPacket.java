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
import noppes.npcs.NBTTags;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.gui.script.GuiScriptItem;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public final class ItemScriptErrorPacket extends AbstractPacket {
    public static String packetName = "Request|ItemScriptError";

    private Action action;

    public ItemScriptErrorPacket() {}

    public ItemScriptErrorPacket(Action action) {
        this.action = action;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ItemScriptError;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission(){
        return CustomNpcsPermissions.SCRIPT_ITEM;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action.ordinal());
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT))
            return;


        Action requestedAction = Action.values()[in.readInt()];
        IItemStack iw = NpcAPI.Instance().getIItemStack(player.getHeldItem());
        if(iw instanceof ScriptCustomItem){
            if(requestedAction == Action.GET){
                TreeMap<Long, String> map = new TreeMap<>();
                int tab = 0;
                for (ScriptContainer script : ((ScriptCustomItem) iw).scripts) {
                    ++tab;

                    for (Map.Entry<Long, String> longStringEntry : script.console.entrySet()) {
                        map.put(longStringEntry.getKey(), " tab " + tab + ":\n" + longStringEntry.getValue());
                    }
                }

                NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("ItemScriptConsole", NBTTags.NBTLongStringMap(map));
                GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
            } else {
                for (ScriptContainer script : ((ScriptCustomItem) iw).scripts) {
                    script.console.clear();
                }
            }
        }
    }

    public static void Clear() {
        PacketClient.sendClient(new ItemScriptErrorPacket(Action.CLEAR));
    }

    public static void Get() {
        PacketClient.sendClient(new ItemScriptErrorPacket(Action.GET));
    }

    private enum Action {
        GET,
        CLEAR
    }
}
