package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.AbilityScript;

import java.io.IOException;

/**
 * Copies script data from a source ability to a target ability UUID.
 * Used when cloning an ability into an NPC as inline - the clone gets a new UUID
 * but needs the source's script handler copied server-side (scripts are never sent to client).
 */
public final class CopyAbilityScriptsPacket extends AbstractPacket {
    public static String packetName = "Request|CopyAbilityScripts";

    private String sourceAbilityName;
    private String targetAbilityId;

    public CopyAbilityScriptsPacket() {
    }

    public CopyAbilityScriptsPacket(String sourceAbilityName, String targetAbilityId) {
        this.sourceAbilityName = sourceAbilityName;
        this.targetAbilityId = targetAbilityId;
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
        ByteBufUtils.writeString(out, sourceAbilityName);
        ByteBufUtils.writeString(out, targetAbilityId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String sourceName = ByteBufUtils.readString(in);
        String targetId = ByteBufUtils.readString(in);

        if (targetId == null || targetId.isEmpty()) return;

        // Look up source ability's script handler
        Ability source = AbilityController.Instance.getCustomAbility(sourceName);
        if (source == null) return;

        AbilityScript sourceHandler = source.getScriptHandler();
        if (sourceHandler == null) return;

        // Clone the script handler for the target UUID via NBT round-trip
        NBTTagCompound scriptNbt = sourceHandler.writeToNBT(new NBTTagCompound());
        AbilityScript targetHandler = new AbilityScript(targetId);
        targetHandler.readFromNBT(scriptNbt);
        AbilityController.Instance.abilityScriptHandlers.put(targetId, targetHandler);
    }
}
