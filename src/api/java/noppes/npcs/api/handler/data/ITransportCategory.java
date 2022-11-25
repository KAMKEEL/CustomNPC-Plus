package noppes.npcs.api.handler.data;

public interface ITransportCategory {

    int getId();

    void setTitle(String title);

    String getTitle();

    void addLocation(String name);

    ITransportLocation getLocation(String name);

    void removeLocation(String name);
}
