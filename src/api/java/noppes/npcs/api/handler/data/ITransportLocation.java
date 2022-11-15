package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;

public interface ITransportLocation {

    int getId();

    void setName(String name);

    String getName();

    void setDimension(int dimension);

    int getDimension();

    void setType(int type);

    int getType();

    void setPosition(int x, int y, int z);

    void setPosition(IPos pos);

    double getX();

    double getY();

    double getZ();

    void save();
}
