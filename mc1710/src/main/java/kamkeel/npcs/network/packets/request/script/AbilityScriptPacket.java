package kamkeel.npcs.network.packets.request.script;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
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
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.data.AbilityScript;
import noppes.npcs.controllers.data.IScriptHandler;

import java.io.IOException;

public final class AbilityScriptPacket extends AbstractPacket {
    public static String packetName = "Request|AbilityScript";

    private Action type;
    private String abilityId;
    private int page;
    private int maxSize;
    private NBTTagCompound compound;

    public AbilityScriptPacket() {
    }

    public AbilityScriptPacket(Action type, String abilityId, int page, int maxSize, NBTTagCompound compound) {
        this.type = type;
        this.abilityId = abilityId;
        this.page = page;
        this.maxSize = maxSize;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.AbilityScript;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SCRIPT_PLAYER;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());
        ByteBufUtils.writeUTF8String(out, abilityId);

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

        int actionOrd = in.readInt();
        if (actionOrd < 0 || actionOrd >= Action.values().length) return;
        Action requestedAction = Action.values()[actionOrd];
        String id = ByteBufUtils.readUTF8String(in);

        Ability ability = AbilityController.Instance.resolveAbility(id);
        if (ability == null) {
            // Ability not yet saved — store handler in global map so scripts persist
            // until the ability is saved via CustomAbilitySavePacket
            AbilityScript data = AbilityController.Instance.abilityScriptHandlers.get(id);
            if (data == null) {
                data = new AbilityScript(id);
                AbilityController.Instance.abilityScriptHandlers.put(id, data);
            }
            if (requestedAction == Action.GET) {
                PacketUtil.getScripts((IScriptHandler) data, (EntityPlayerMP) player);
            } else {
                data.saveScript(in);
            }
            return;
        }

        AbilityScript data = ability.getOrCreateScriptHandler();
        if (requestedAction == Action.GET) {
            PacketUtil.getScripts((IScriptHandler) data, (EntityPlayerMP) player);
        } else {
            data.saveScript(in);
            // Persist to disk
            AbilityController.Instance.saveCustomAbility(ability);
            if (ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                LogWriter.script(String.format("[%s] (Player) %s SAVED ABILITY SCRIPT %s [%s]",
                    "ABILITY SCRIPTS", player.getCommandSenderName(), ability.getName(), ability.getId()));
            }
        }
    }

    public static void Save(String abilityId, int id, int maxSize, NBTTagCompound compound) {
        PacketClient.sendClient(new AbilityScriptPacket(Action.SAVE, abilityId, id, maxSize, compound));
    }

    public static void Get(String abilityId) {
        PacketClient.sendClient(new AbilityScriptPacket(Action.GET, abilityId, -1, -1, new NBTTagCompound()));
    }

    private enum Action {
        GET,
        SAVE
    }
}
