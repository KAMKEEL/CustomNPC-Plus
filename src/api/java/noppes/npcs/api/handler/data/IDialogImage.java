package noppes.npcs.api.handler.data;

public interface IDialogImage {
    int getId();

    void setTexture(String texture);
    String getTexture();

    void setPosition(int x, int y);
    int getX();
    int getY();

    void setWidthHeight(int width, int height);
    int getWidth();
    int getHeight();

    void setTextureOffset(int offsetX, int offsetY);
    int getTextureX();
    int getTextureY();

    void setColor(int color);
    int getColor();

    void setSelectedColor(int color);
    int getSelectedColor();

    void setScale(float scale);
    float getScale();

    void setAlpha(float alpha);
    float getAlpha();

    void setRotation(float rotation);
    float getRotation();

    void setImageType(int imageType);
    int getImageType();

    void setAlignment(int alignment);
    int getAlignment();
}
