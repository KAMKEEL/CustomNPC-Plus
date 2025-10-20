package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.network.enums.EnumSyncType;
import kamkeel.npcs.network.packets.player.SyncRevisionInfoPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.config.ConfigMain;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public final class LoginPacket extends AbstractPacket {
    public static final String packetName = "CNPC+|Login";

    private String serverCacheKey = "";
    private final EnumMap<EnumSyncType, Integer> revisionSnapshot = new EnumMap<>(EnumSyncType.class);

    public LoginPacket() {
    }

    public LoginPacket(String serverCacheKey, EnumMap<EnumSyncType, Integer> revisionSnapshot) {
        if (serverCacheKey != null) {
            this.serverCacheKey = serverCacheKey;
        }
        if (revisionSnapshot != null) {
            this.revisionSnapshot.putAll(revisionSnapshot);
        }
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.LOGIN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeBoolean(ConfigMain.PartiesEnabled);
        out.writeBoolean(ConfigMain.ProfilesEnabled);
        ByteBufUtils.writeString(out, serverCacheKey);
        out.writeShort(revisionSnapshot.size());
        for (Map.Entry<EnumSyncType, Integer> entry : revisionSnapshot.entrySet()) {
            out.writeInt(entry.getKey().ordinal());
            out.writeInt(entry.getValue());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        ClientCacheHandler.allowParties = in.readBoolean();
        ClientCacheHandler.allowProfiles = in.readBoolean();

        String serverKey = ByteBufUtils.readString(in);
        int revisionCount = in.readUnsignedShort();
        EnumMap<EnumSyncType, Integer> serverRevisions = new EnumMap<>(EnumSyncType.class);
        for (int i = 0; i < revisionCount; i++) {
            EnumSyncType type = EnumSyncType.values()[in.readInt()];
            int revision = in.readInt();
            serverRevisions.put(type, revision);
        }

        ClientCacheHandler.setActiveServer(serverKey, serverRevisions);

        PacketClient.sendClient(new SyncRevisionInfoPacket(
            serverKey,
            ClientCacheHandler.getLastServerKey(),
            ClientCacheHandler.getCachedRevisionsForServer(serverKey)
        ));
    }
}
