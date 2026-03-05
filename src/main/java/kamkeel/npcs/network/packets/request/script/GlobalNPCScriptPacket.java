package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.GlobalNPCDataScript;
import noppes.npcs.controllers.data.IScriptHandler;

import java.io.IOException;

public final class GlobalNPCScriptPacket extends AbstractPacket {
    public static String packetName = "Request|GlobalNPCScript";

    private GlobalNPCScriptPacket.Action type;
    private int page;
    private int maxSize;
    private NBTTagCompound compound;

    public GlobalNPCScriptPacket() {
    }

    public GlobalNPCScriptPacket(Action type, int page, int maxSize, NBTTagCompound compound) {
        this.type = type;
        this.page = page;
        this.maxSize = maxSize;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.GlobalNPCScript;
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
        out.writeInt(type.ordinal());

        if (type == Action.SAVE) {
            out.writeInt(this.page);
            out.writeInt(this.maxSize);
            ByteBufUtils.writeNBT(out, this.compound);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.SCRIPTER))
            return;

        Action requestedAction = Action.values()[in.readInt()];
        GlobalNPCDataScript data = ScriptController.Instance.globalNpcScripts;
        if (requestedAction == Action.GET) {
            PacketUtil.getScripts((IScriptHandler) data, (EntityPlayerMP) player);
        } else {
            int tab = in.getInt(in.readerIndex());
            PacketUtil.saveScripts(data, in);
            ScriptController.Instance.lastGlobalNpcUpdate = System.currentTimeMillis();
            if (tab == -1)
                ScriptController.Instance.saveGlobalScriptsSync();
        }
    }

    public static void Save(int id, int maxSize, NBTTagCompound compound) {
        PacketClient.sendClient(new GlobalNPCScriptPacket(Action.SAVE, id, maxSize, compound));
    }

    public static void Get() {
        PacketClient.sendClient(new GlobalNPCScriptPacket(Action.GET, -1, -1, new NBTTagCompound()));
    }

    private enum Action {
        GET,
        SAVE
    }
}
