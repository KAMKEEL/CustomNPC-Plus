package kamkeel.npcs.network.packets.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight packet that syncs cooldown state (global + per-ability) to the client.
 * Sent when cooldowns are rolled, reset, or on login.
 */
public final class AbilityCooldownSyncPacket extends AbstractPacket {
    public static final String packetName = "Data|AbilityCooldownSync";

    private long globalCooldownEndTime;
    private int globalCooldownDuration;
    private HashMap<String, Long> perAbilityCooldownEndTimes;
    private HashMap<String, Integer> perAbilityCooldownDurations;

    public AbilityCooldownSyncPacket() {
    }

    public AbilityCooldownSyncPacket(long globalEndTime, int globalDuration,
                                     HashMap<String, Long> perEndTimes,
                                     HashMap<String, Integer> perDurations) {
        this.globalCooldownEndTime = globalEndTime;
        this.globalCooldownDuration = globalDuration;
        this.perAbilityCooldownEndTimes = perEndTimes;
        this.perAbilityCooldownDurations = perDurations;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ABILITY_COOLDOWN_SYNC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeLong(globalCooldownEndTime);
        out.writeInt(globalCooldownDuration);

        // Write per-ability cooldowns
        int count = perAbilityCooldownEndTimes != null ? perAbilityCooldownEndTimes.size() : 0;
        out.writeInt(count);
        if (count > 0) {
            for (Map.Entry<String, Long> entry : perAbilityCooldownEndTimes.entrySet()) {
                ByteBufUtils.writeUTF8String(out, entry.getKey());
                out.writeLong(entry.getValue());
                Integer dur = perAbilityCooldownDurations.get(entry.getKey());
                out.writeInt(dur != null ? dur : 0);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        long globalEndTime = in.readLong();
        int globalDuration = in.readInt();

        int count = in.readInt();
        HashMap<String, Long> perEndTimes = new HashMap<>();
        HashMap<String, Integer> perDurations = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String key = ByteBufUtils.readUTF8String(in);
            long endTime = in.readLong();
            int duration = in.readInt();
            perEndTimes.put(key, endTime);
            perDurations.put(key, duration);
        }

        PlayerData data = ClientCacheHandler.playerData;
        if (data != null && data.abilityData != null) {
            data.abilityData.applyCooldownSync(globalEndTime, globalDuration, perEndTimes, perDurations);
        }
    }

    /**
     * Send the player's current cooldown state to their client.
     */
    public static void sendToPlayer(EntityPlayerMP player) {
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data == null || data.abilityData == null) return;

        PacketHandler.Instance.sendToPlayer(new AbilityCooldownSyncPacket(
            data.abilityData.getCooldownEndTime(),
            data.abilityData.getGlobalCooldownDurationValue(),
            data.abilityData.getPerAbilityCooldownEndTimes(),
            data.abilityData.getPerAbilityCooldownDurations()
        ), player);
    }
}
