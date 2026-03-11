package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
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
import noppes.npcs.controllers.data.AbilityScript;
import noppes.npcs.controllers.data.ChainedAbilityScript;

import java.io.IOException;

/**
 * Copies script data from a source ability/chain to a target UUID.
 * Used when cloning into an NPC as inline - the clone gets a new UUID
 * but needs the source's script handler copied server-side (scripts are never sent to client).
 *
 * Mode 0 = ability scripts, Mode 1 = chained ability scripts.
 */
public final class CopyAbilityScriptsPacket extends AbstractPacket {
    public static String packetName = "Request|CopyAbilityScripts";

    public static final int MODE_ABILITY = 0;
    public static final int MODE_CHAINED = 1;

    private int mode;
    private String sourceName;
    private String targetId;

    public CopyAbilityScriptsPacket() {
    }

    public CopyAbilityScriptsPacket(String sourceName, String targetId) {
        this(MODE_ABILITY, sourceName, targetId);
    }

    public CopyAbilityScriptsPacket(int mode, String sourceName, String targetId) {
        this.mode = mode;
        this.sourceName = sourceName;
        this.targetId = targetId;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CopyAbilityScripts;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_ABILITY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeByte(mode);
        ByteBufUtils.writeString(out, sourceName);
        ByteBufUtils.writeString(out, targetId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int mode = in.readByte();
        String sourceName = ByteBufUtils.readString(in);
        String targetId = ByteBufUtils.readString(in);

        if (targetId == null || targetId.isEmpty()) return;

        if (mode == MODE_CHAINED) {
            copyChainedAbilityScripts(sourceName, targetId);
        } else {
            copyAbilityScripts(sourceName, targetId);
        }
    }

    private void copyAbilityScripts(String sourceKey, String targetId) {
        // Try name-based lookup first
        Ability source = AbilityController.Instance.getCustomAbility(sourceKey);

        AbilityScript sourceHandler;
        if (source != null) {
            sourceHandler = source.getScriptHandler();
        } else {
            // sourceKey might be a UUID - try direct handler lookup
            sourceHandler = AbilityController.Instance.abilityScriptHandlers.get(sourceKey);
        }
        if (sourceHandler == null) return;

        NBTTagCompound scriptNbt = sourceHandler.writeToNBT(new NBTTagCompound());
        AbilityScript targetHandler = new AbilityScript(targetId);
        targetHandler.readFromNBT(scriptNbt);
        AbilityController.Instance.abilityScriptHandlers.put(targetId, targetHandler);
    }

    private void copyChainedAbilityScripts(String sourceName, String targetId) {
        ChainedAbility source = AbilityController.Instance.getChainedAbility(sourceName);
        if (source == null) return;

        ChainedAbilityScript sourceHandler = source.getScriptHandler();
        if (sourceHandler == null) return;

        NBTTagCompound scriptNbt = sourceHandler.writeToNBT(new NBTTagCompound());
        ChainedAbilityScript targetHandler = new ChainedAbilityScript(targetId);
        targetHandler.readFromNBT(scriptNbt);
        AbilityController.Instance.chainedAbilityScriptHandlers.put(targetId, targetHandler);
    }
}
