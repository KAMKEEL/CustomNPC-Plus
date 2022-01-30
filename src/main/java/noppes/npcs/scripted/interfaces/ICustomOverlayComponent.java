package noppes.npcs.scripted.interfaces;

public interface ICustomOverlayComponent {
    int getID();

    ICustomOverlayComponent setID(int id);

    int getPosX();

    int getPosY();

    ICustomOverlayComponent setPos(int x, int y);

    int getAlignment();

    void setAlignment(int alignment);
}
