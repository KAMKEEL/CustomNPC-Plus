package kamkeel.npcs.network.packets.player.ability;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class AbilityHotbarSelectPacket extends AbstractPacket {
    public static final String packetName = "Player|AbilityHotbarSelect";

    private String abilityKey;

    public AbilityHotbarSelectPacket() {
    }

    public AbilityHotbarSelectPacket(String abilityKey) {
        this.abilityKey = abilityKey;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.AbilityHotbarSelect;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, abilityKey != null ? abilityKey : "");
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String key = ByteBufUtils.readString(in);

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data == null || data.abilityData == null) return;

        if (key == null || key.isEmpty()) {
            // Deselect
            data.abilityData.setSelectedIndex(-1);
        } else {
            int index = data.abilityData.getUnlockedAbilityList().indexOf(key);
            if (index >= 0) {
                data.abilityData.setSelectedIndex(index);
            }
        }
        data.save();
    }
}
