package noppes.npcs.api;

public interface IPos {

    int getX();

    int getY();

    int getZ();

    double getXD();

    double getYD();

    double getZD();

    IPos up();

    IPos up(double n);

    IPos down();

    IPos down(double n);

    IPos north();

    IPos north(double n);

    IPos east();

    IPos east(double n);

    IPos south();

    IPos south(double n);

    IPos west();

    IPos west(double n);

    IPos add(double x, double y, double z);

    IPos add(IPos pos);

    IPos subtract(double x, double y, double z);

    IPos subtract(IPos pos);

    IPos normalize();

    double[] normalizeDouble();

    IPos offset(int direction);

    IPos offset(int direction, double n);

    IPos crossProduct(double x, double y, double z);

    IPos crossProduct(IPos pos);

    IPos divide(double scalar);

    long toLong();

    IPos fromLong(long serialized);

    double distanceTo(IPos pos);

    double distanceTo(double x, double y, double z);
}
