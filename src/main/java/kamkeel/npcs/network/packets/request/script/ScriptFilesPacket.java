package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NBTTags;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ScriptFilesPacket extends AbstractPacket {
    public static String packetName = "Request|ScriptFiles";

    private String lang;

    public ScriptFilesPacket() {
    }

    public ScriptFilesPacket(String lang) {
        this.lang = lang;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ScriptFiles;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SCRIPT_GLOBAL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, getScriptsNbt(lang));
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound compound = ByteBufUtils.readNBT(in);

        String lang = compound.getString("Lang");
        String ext = compound.getString("Ext");
        Map<String, String> scripts = NBTTags.getStringStringMap(compound.getTagList("Scripts", 10));

        ScriptController cont = ScriptController.Instance;
        if (!cont.languages.containsKey(lang) && ext != null)
            cont.languages.put(lang, ext);

        for (Map.Entry<String, String> script : scripts.entrySet()) {
            cont.scripts.put(script.getKey(), script.getValue());
        }

        //Set client side configs
        ConfigScript.ScriptingEnabled = compound.getBoolean("ScriptingEnabled");
        ConfigScript.RunLoadedScriptsFirst = compound.getBoolean("LoadedFirst");
        ScriptController.Instance.globalRevision = compound.getInteger("GlobalRevision");
    }

    public NBTTagCompound getScriptsNbt(String lang) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("Lang", lang);

        Map<String, String> scriptss = new HashMap<>();
        String ext = ScriptController.Instance.languages.get(lang);
        if (ext != null) {
            for (Map.Entry<String, String> script : ScriptController.Instance.scripts.entrySet()) {
                if (script.getKey().endsWith(ext))
                    scriptss.put(script.getKey(), script.getValue());
            }
            compound.setString("Ext", ext);
        }

        //Send server configs to client
        compound.setBoolean("ScriptingEnabled", ConfigScript.ScriptingEnabled);
        compound.setBoolean("LoadedFirst", ConfigScript.RunLoadedScriptsFirst);
        compound.setInteger("GlobalRevision", ScriptController.Instance.globalRevision);
        compound.setTag("Scripts", NBTTags.nbtStringStringMap(scriptss));
        return compound;
    }

    public static void sendToAll(String lang) {
        PacketHandler.Instance.sendToAll(new ScriptFilesPacket(lang));
    }

    public static void sendToPlayer(EntityPlayerMP player, String lang) {
        PacketHandler.Instance.sendToPlayer(new ScriptFilesPacket(lang), player);
    }
}
