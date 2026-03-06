package kamkeel.npcs.network.packets.player.ability;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class AbilityTogglePacket extends AbstractPacket {
    public static final String packetName = "Player|AbilityToggle";
    private String abilityKey;

    public AbilityTogglePacket() {
    }

    public AbilityTogglePacket(String abilityKey) {
        this.abilityKey = abilityKey;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.AbilityToggle;
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
        if (key == null || key.isEmpty()) return;
        if (AbilityController.Instance == null) return;

        Ability ability = AbilityController.Instance.resolveAbility(key);
        if (ability == null || !ability.isToggleable() || !ability.getAllowedBy().allowsPlayer()) return;

        PlayerData playerData = PlayerData.get(player);
        if (playerData == null || playerData.abilityData == null) return;
        if (!playerData.abilityData.hasUnlockedAbility(key)) return;

        int newState = playerData.abilityData.toggleAbility(key);

        // Sync toggle states to client (toggle may have side effects like mutual exclusivity)
        playerData.abilityData.syncToClient();

        String displayName = ability.getDisplayName();
        if (newState > 0) {
            String stateLabel = ability.getToggleStateLabel(newState);
            if (stateLabel != null) {
                player.addChatMessage(new ChatComponentText("\u00A7a" + displayName + " " + stateLabel));
            } else {
                String enabled = StatCollector.translateToLocal("gui.enabled");
                player.addChatMessage(new ChatComponentText("\u00A7a" + displayName + " " + enabled));
            }
        } else {
            String disabled = StatCollector.translateToLocal("gui.disabled");
            player.addChatMessage(new ChatComponentText("\u00A7c" + displayName + " " + disabled));
        }
    }
}
