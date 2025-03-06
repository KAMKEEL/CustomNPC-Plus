package kamkeel.npcs.network;

public class PacketClient extends PacketHandler {

    public static void sendClient(AbstractPacket packet){
        PacketHandler.Instance.sendToServer(packet);
    }
}
