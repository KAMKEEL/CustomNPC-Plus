package noppes.npcs.scripted.interfaces.item;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IItemCustom extends IItemStack {

    void callScript(EnumScriptType type, Event event);

    boolean isClient();

    boolean getEnabled();

    void setEnabled(boolean bo);

    String getLanguage();

    void setLanguage(String lang);

    List<ScriptContainer> getScripts();

    String noticeString();

    Map<Long, String> getConsoleText();

    void clearConsole();

    String getTexture();

    void setTexture(String var2);

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

    void setRotationX(float rotationX);

    void setRotationY(float rotationY);

    void setRotationZ(float rotationZ);

    void setRotationXRate(float rotationXRate);

    void setRotationYRate(float rotationYRate);

    void setRotationZRate(float rotationZRate);

    void setScaleX(float scaleX);

    void setScaleY(float scaleY);

    void setScaleZ(float scaleZ);

    void setTranslateX(float translateX);

    void setTranslateY(float translateY);

    void setTranslateZ(float translateZ);

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
