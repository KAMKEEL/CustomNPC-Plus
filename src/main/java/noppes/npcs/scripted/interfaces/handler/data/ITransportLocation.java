package noppes.npcs.scripted.interfaces.handler.data;

public interface ITransportLocation {

    int getId();

    void setName(String name);

    String getName();

    void setDimension(int dimension);

    int getDimension();

    void setType(int type);

    int getType();

    void setPosition(int x, int y, int z);

    double getX();

    double getY();

    double getZ();

    void save();
}
