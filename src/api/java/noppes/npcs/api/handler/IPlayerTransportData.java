package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.ITransportLocation;

public interface IPlayerTransportData {

    boolean hasTransport(int id);

    void addTransport(int id);

    void addTransport(ITransportLocation location);

    ITransportLocation getTransport(int id);

    ITransportLocation[] getTransports();

    void removeTransport(int id);
}
