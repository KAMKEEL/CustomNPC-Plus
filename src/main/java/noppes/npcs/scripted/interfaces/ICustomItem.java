package noppes.npcs.scripted.interfaces;

public interface ICustomItem extends IItemStack {
    String getTexture();

    void setTexture(String var2);

    void setMaxStackSize(int var1);

    double getDurabilityValue();

    void setDurabilityValue(float var1);

    boolean getDurabilityShow();

    void setDurabilityShow(boolean var1);

    int getDurabilityColor();

    void setDurabilityColor(int var1);

    int getColor();

    void setColor(int var1);
}
