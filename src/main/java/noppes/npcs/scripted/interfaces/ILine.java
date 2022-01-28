package noppes.npcs.scripted.interfaces;

public interface ILine extends ICustomGuiComponent {
    int getX1();
    int getY1();
    int getX2();
    int getY2();
    int getColor();
    int getThickness();

    void setX1(int x1);
    void setY1(int y1);
    void setX2(int x2);
    void setY2(int y2);
    void setColor(int color);
    void setThickness(int thickness);
}
