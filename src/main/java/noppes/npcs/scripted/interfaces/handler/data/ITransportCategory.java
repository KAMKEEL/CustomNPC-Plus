package noppes.npcs.scripted.interfaces.handler.data;

public interface ITransportCategory {

    int getId();

    void setTitle(String title);

    String getTitle();

    void addLocation(String name);

    ITransportLocation getLocation(String name);

    void removeLocation(String name);
}
