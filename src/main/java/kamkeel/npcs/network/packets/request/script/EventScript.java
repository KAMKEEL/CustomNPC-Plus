package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.network.packets.player.BankActionPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.controllers.data.IScriptHandler;

import java.io.IOException;

public final class EventScript extends AbstractPacket {
    public static String packetName = "Request|EventScript";

    private EventScript.Action type;
    private int page;
    private int maxSize;
    private NBTTagCompound compound;

    public EventScript() {}

    public EventScript(Action type, int page, int maxSize, NBTTagCompound compound) {
        this.type = type;
        this.page = page;
        this.maxSize = maxSize;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.EventScript;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public boolean needsNPC(){
        return true;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission(){
        return CustomNpcsPermissions.SCRIPT_NPC;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());

        if(type == Action.SAVE){

        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.SCRIPTER))
            return;

        Action requestedAction = Action.values()[in.readInt()];
        DataScript data = npc.script;
        if(requestedAction == Action.GET){
            PacketUtil.getScripts(data, (EntityPlayerMP) player);
        } else {

        }
    }

    public static void Save(int id, int maxSize, NBTTagCompound compound) {
        PacketClient.sendClient(new EventScript(Action.SAVE, id, maxSize, compound));
    }
    public static void Get() {
        PacketClient.sendClient(new EventScript(Action.GET, -1, -1, new NBTTagCompound()));
    }

    private enum Action {
        GET,
        SAVE
    }
}
