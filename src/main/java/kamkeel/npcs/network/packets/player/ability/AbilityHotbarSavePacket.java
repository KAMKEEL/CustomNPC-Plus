package kamkeel.npcs.network.packets.player.ability;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.ability.AbilityHotbarSyncPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.AbilityHotbarData;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class AbilityHotbarSavePacket extends AbstractPacket {
    public static final String packetName = "Player|AbilityHotbarSave";

    private int slotIndex;
    private NBTTagCompound slotData;

    public AbilityHotbarSavePacket() {
    }

    public AbilityHotbarSavePacket(int slotIndex, NBTTagCompound slotData) {
        this.slotIndex = slotIndex;
        this.slotData = slotData;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.AbilityHotbarSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(slotIndex);
        ByteBufUtils.writeNBT(out, slotData);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int slot = in.readInt();
        NBTTagCompound compound = ByteBufUtils.readNBT(in);

        if (slot < 0 || slot >= AbilityHotbarData.TOTAL_SLOTS) return;

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data == null) return;

        AbilityHotbarData slotData = data.hotbarData.getSlot(slot);
        if (slotData == null) return;

        slotData.readFromNBT(compound.getCompoundTag("AbilityHotbar" + slot));

        // Validate the ability key
        boolean valid = true;
        if (!slotData.isEmpty() && AbilityController.Instance != null) {
            if (slotData.isChainKey()) {
                valid = AbilityController.Instance.canResolveChainedAbility(slotData.getResolveKey());
            } else {
                valid = AbilityController.Instance.canResolveAbility(slotData.abilityKey);
            }

            if (valid && data.abilityData != null) {
                valid = data.abilityData.hasUnlockedAbility(slotData.abilityKey);
            }
        }

        if (!valid) {
            slotData.reset();
        }

        data.save();
        AbilityHotbarSyncPacket.sendToPlayer((EntityPlayerMP) player);
    }
}
