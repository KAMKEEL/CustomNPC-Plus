package noppes.npcs.client.gui.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import java.io.IOException;
import java.util.Map;

public final class PlayerDataSendPacketNew extends LargeAbstractPacket {
    public static final String packetName = "Large|PlayerDataSendNew";

    private String playerName;
    private Map<String, Integer> questCategories;
    private Map<String, Integer> questActive;
    private Map<String, Integer> questFinished;

    private Map<String, Integer> dialogCategories;
    private Map<String, Integer> dialogRead;

    private Map<String, Integer> transportCategories;
    private Map<String, Integer> transportLocations;

    private Map<String, Integer> bankData;
    private Map<String, Integer> factionData;

    public PlayerDataSendPacketNew() {}

    public PlayerDataSendPacketNew(String playerName,
                                   Map<String, Integer> questCategories, Map<String, Integer> questActive, Map<String, Integer> questFinished,
                                   Map<String, Integer> dialogCategories, Map<String, Integer> dialogRead,
                                   Map<String, Integer> transportCategories, Map<String, Integer> transportLocations,
                                   Map<String, Integer> bankData, Map<String, Integer> factionData) {
        this.playerName = playerName;
        this.questCategories = questCategories;
        this.questActive = questActive;
        this.questFinished = questFinished;
        this.dialogCategories = dialogCategories;
        this.dialogRead = dialogRead;
        this.transportCategories = transportCategories;
        this.transportLocations = transportLocations;
        this.bankData = bankData;
        this.factionData = factionData;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.PLAYER_DATA;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        // Write player name:
        ByteBufUtils.writeString(buffer, playerName);
        // Write quest data maps:
        writeMap(buffer, questCategories);
        writeMap(buffer, questActive);
        writeMap(buffer, questFinished);
        // Write dialog data maps:
        writeMap(buffer, dialogCategories);
        writeMap(buffer, dialogRead);
        // Write transport data maps:
        writeMap(buffer, transportCategories);
        writeMap(buffer, transportLocations);
        // Write bank data map:
        writeMap(buffer, bankData);
        // Write faction data map:
        writeMap(buffer, factionData);

        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    private void writeMap(ByteBuf buffer, Map<String, Integer> map) {
        if (map != null) {
            buffer.writeInt(map.size());
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                ByteBufUtils.writeString(buffer, entry.getKey());
                buffer.writeInt(entry.getValue());
            }
        } else {
            buffer.writeInt(0);
        }
    }

    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        NoppesUtilClientNew.handlePlayerData(data, player);
    }
}
