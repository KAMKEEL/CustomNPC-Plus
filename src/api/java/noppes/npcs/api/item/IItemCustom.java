package noppes.npcs.api.item;

public interface IItemCustom extends IItemStack {

    boolean getEnabled();

    void setEnabled(boolean bo);

    String getTexture();

    void setTexture(String var2);

    void setArmorType(int armorType);

    int getArmorType();

    void setIsTool(boolean isTool);

    boolean isTool();

    void setDigSpeed(int digSpeed);

    int getDigSpeed();

    void setMaxStackSize(int var1);

    double getDurabilityValue();

    void setDurabilityValue(float var1);

    boolean getDurabilityShow();

    void setDurabilityShow(boolean var1);

    int getDurabilityColor();

    void setDurabilityColor(int var1);

    int getColor();

    void setColor(int var1);

    int getMaxItemUseDuration();

    void setMaxItemUseDuration(int duration);

    void setItemUseAction(int action);

    int getItemUseAction();

    int getEnchantability();

    void setEnchantability(int enchantability);

    void setRotation(float rotationX, float rotationY, float rotationZ);

    void setRotationRate(float rotationXRate, float rotationYRate, float rotationZRate);

    void setScale(float scaleX, float scaleY, float scaleZ);

    void setTranslate(float translateX, float translateY, float translateZ);

    float getRotationX();

    float getRotationY();

    float getRotationZ();

    float getRotationXRate();

    float getRotationYRate();

    float getRotationZRate();

    float getScaleX();

    float getScaleY();

    float getScaleZ();

    float getTranslateX();

    float getTranslateY();

    float getTranslateZ();
}
