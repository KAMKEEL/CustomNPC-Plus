package kamkeel.network;

import kamkeel.network.enums.EnumPacketType;

import java.util.Hashtable;
import java.util.Map;

public class PacketChannel {

    private String channelName;
    private EnumPacketType channelType;

    public Map<Integer, AbstractPacket> packets = new Hashtable<>();

    public PacketChannel(String channelName, EnumPacketType channelType) {
        this.channelName = channelName;
        this.channelType = channelType;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public EnumPacketType getChannelType() {
        return channelType;
    }

    public void setChannelType(EnumPacketType channelType) {
        this.channelType = channelType;
    }

    public void registerPacket(AbstractPacket packet) {
        this.packets.put(packet.getType().ordinal(), packet);
    }
}
