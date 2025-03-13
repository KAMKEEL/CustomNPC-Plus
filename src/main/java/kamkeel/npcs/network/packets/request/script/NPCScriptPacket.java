package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;

import java.io.IOException;

public final class NPCScriptPacket extends AbstractPacket {
    public static String packetName = "Request|NPCScript";

    private NPCScriptPacket.Action type;
    private NBTTagCompound compound;

    public NPCScriptPacket() {
    }

    public NPCScriptPacket(Action type, NBTTagCompound compound) {
        this.type = type;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.NPCScript;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SCRIPT_NPC;
    }

    @Override
    public boolean needsNPC() {
        return true;
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

        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.SCRIPTER))
            return;

        Action requestedAction = Action.values()[in.readInt()];
        if (requestedAction == Action.GET) {
            NBTTagCompound compound = npc.script.writeToNBT(new NBTTagCompound());
            compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        } else {
            npc.script.readFromNBT(ByteBufUtils.readNBT(in));
            npc.updateAI = true;
            npc.script.hasInited = false;
            if (ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                LogWriter.script(String.format("[%s] (Player) %s SAVED NPC %s (%s, %s, %s) [%s]", "SCRIPTER", player.getCommandSenderName(), npc.display.getName(), (int) npc.posX, (int) (npc).posY, (int) npc.posZ, npc.worldObj.getWorldInfo().getWorldName()));
            }
        }
    }

    public static void Save(NBTTagCompound compound) {
        PacketClient.sendClient(new NPCScriptPacket(Action.SAVE, compound));
    }

    public static void Get() {
        PacketClient.sendClient(new NPCScriptPacket(Action.GET, new NBTTagCompound()));
    }

    private enum Action {
        GET,
        SAVE
    }
}
