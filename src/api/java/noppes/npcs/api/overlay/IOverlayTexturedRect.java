package noppes.npcs.api.overlay;

public interface IOverlayTexturedRect extends ICustomOverlayComponent {
    String getTexture();

    IOverlayTexturedRect setTexture(String var1);

    int getWidth();

    int getHeight();

    IOverlayTexturedRect setSize(int var1, int var2);

    float getScale();

    IOverlayTexturedRect setScale(float var1);

    int getTextureX();

    int getTextureY();

    IOverlayTexturedRect setTextureOffset(int var1, int var2);
}
