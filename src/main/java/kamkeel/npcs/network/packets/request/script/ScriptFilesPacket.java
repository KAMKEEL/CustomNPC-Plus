package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;

import java.io.IOException;
import java.util.List;

public final class ScriptFilesPacket extends AbstractPacket {
    public static String packetName = "Request|ScriptLanguages";

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
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SCRIPT_GLOBAL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, this.lang);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT))
            return;

        lang = ByteBufUtils.readString(in);

        NBTTagCompound compound = new NBTTagCompound();
        List<String> files = ScriptController.Instance.getScripts(lang);

        if (!files.isEmpty()) {
            NBTTagList scripts = new NBTTagList();
            for (String script : files)
                scripts.appendTag(new NBTTagString(script));
            compound.setTag("Scripts", scripts);
        }

        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }

    public static void Get(String lang) {
        PacketClient.sendClient(new ScriptFilesPacket(lang));
    }
}
