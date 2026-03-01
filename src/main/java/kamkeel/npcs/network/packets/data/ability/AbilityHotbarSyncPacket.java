package kamkeel.npcs.network.packets.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.AbilityHotbarData;
import noppes.npcs.controllers.data.PlayerAbilityHotbarData;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class AbilityHotbarSyncPacket extends AbstractPacket {
    public static final String packetName = "Data|AbilityHotbarSync";

    private NBTTagCompound hotbarNBT;

    public AbilityHotbarSyncPacket() {
    }

    public AbilityHotbarSyncPacket(NBTTagCompound hotbarNBT) {
        this.hotbarNBT = hotbarNBT;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ABILITY_HOTBAR_SYNC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, hotbarNBT);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        if (nbt == null) return;

        PlayerData data = ClientCacheHandler.playerData;
        if (data != null) {
            data.hotbarData.readFromNBT(nbt);
        }
    }

    public static void sendToPlayer(EntityPlayerMP player) {
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data == null) return;

        PlayerAbilityHotbarData hotbarData = data.hotbarData;
        boolean anyCleared = false;

        // Validate all slots before sending
        if (AbilityController.Instance != null) {
            for (int i = 0; i < hotbarData.slots.length; i++) {
                AbilityHotbarData slot = hotbarData.slots[i];
                if (slot.isEmpty()) continue;

                boolean valid;
                if (slot.isChainKey()) {
                    valid = AbilityController.Instance.canResolveChainedAbility(slot.getResolveKey());
                } else {
                    valid = AbilityController.Instance.canResolveAbility(slot.abilityKey);
                }

                if (valid && data.abilityData != null) {
                    valid = data.abilityData.hasUnlockedAbility(slot.abilityKey);
                    // For chains, also check with resolved key in case key format differs
                    if (!valid && slot.isChainKey()) {
                        valid = data.abilityData.hasUnlockedAbility("chain:" + slot.getResolveKey());
                    }
                }

                if (!valid) {
                    slot.reset();
                    anyCleared = true;
                }
            }
        }

        NBTTagCompound nbt = new NBTTagCompound();
        hotbarData.writeToNBT(nbt);
        PacketHandler.Instance.sendToPlayer(new AbilityHotbarSyncPacket(nbt), player);

        if (anyCleared) {
            data.save();
        }
    }
}
