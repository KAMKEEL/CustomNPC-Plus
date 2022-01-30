package noppes.npcs.scripted.interfaces;

public interface IOverlayLabel extends ICustomOverlayComponent {
    String getText();

    IOverlayLabel setText(String var1);

    int getWidth();

    int getHeight();

    IOverlayLabel setSize(int var1, int var2);

    int getColor();

    IOverlayLabel setColor(int var1);

    float getScale();

    IOverlayLabel setScale(float var1);
}
