package noppes.npcs.scripted.item;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.item.IItemCustom;

import java.util.*;

public class ScriptCustomItem extends ScriptItemStack implements IItemCustom, IScriptHandler {
    public List<ScriptContainer> scripts = new ArrayList();
    public List<Integer> errored = new ArrayList();
    private Map<Long, String> console = new TreeMap();
    public String scriptLanguage = "ECMAScript";
    public boolean enabled = false;
    public long lastInited = -1L;
    public boolean loaded = false;

    public boolean durabilityShow = false;
    public double durabilityValue = 1.0D;
    public int durabilityColor = -1;
    public int itemColor = 0x8B4513;
    public int stackSize = 64;

    public int maxItemUseDuration = 20;
    public int itemUseAction = 0; //0: none, 1: block, 2: bow, 3: eat, 4: drink

    public boolean isTool = false;
    public int digSpeed = 1;
    public int armorType = -2; //-2: Fits in no armor slot,  -1: Fits in all slots, 0 - 4: Fits in Head -> Boots slot respectively
    public int enchantability;

    public String texture = "minecraft:textures/items/iron_pickaxe.png";
    public int width = -1, height = -1;

    public float translateX, translateY, translateZ;
    public float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;
    public float rotationX, rotationY, rotationZ;
    public float rotationXRate, rotationYRate, rotationZRate;

    public ScriptCustomItem(ItemStack item) {
        super(item);
        loadScriptData();
        loadItemData();
    }

    public NBTTagCompound getScriptNBT(NBTTagCompound compound) {
        compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.console));
        return compound;
    }

    public void setScriptNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
            this.scriptLanguage = compound.getString("ScriptLanguage");
            this.enabled = compound.getBoolean("ScriptEnabled");
            this.console = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
        }
    }

    public int getType() {
        return 6;
    }

    public void callScript(EnumScriptType type, Event event) {
        if (!this.loaded) {
            this.loadScriptData();
            this.loaded = true;
        }

        if (this.isEnabled()) {
            if (ScriptController.Instance.lastLoaded > this.lastInited) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                if (type != EnumScriptType.INIT) {
                    EventHooks.onScriptItemInit(this);
                }
            }

            for (int i = 0; i < this.scripts.size(); i++) {
                ScriptContainer script = this.scripts.get(i);
                if (!this.errored.contains(i)) {
                    if(script == null || script.errored || !script.hasCode())
                        return;

                    script.run(type, event);

                    if (script.errored) {
                        this.errored.add(i);
                    }

                    Iterator var8 = script.console.entrySet().iterator();

                    boolean saveConsoleData = false;
                    while(var8.hasNext()) {
                        Map.Entry<Long, String> entry = (Map.Entry)var8.next();
                        if (!this.console.containsKey(entry.getKey())) {
                            this.console.put(entry.getKey(), " tab " + (i + 1) + ":\n" + (String)entry.getValue());
                            saveConsoleData = true;
                        }
                    }

                    script.console.clear();

                    if (saveConsoleData) {
                        saveScriptData();
                    }
                }
            }
        }
    }

    private boolean isEnabled() {
        return this.enabled && ScriptController.HasStart;
    }

    public boolean isClient() {
        return false;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean bo) {
        this.enabled = bo;
    }

    public String getLanguage() {
        return this.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
    }

    public List<ScriptContainer> getScripts() {
        return this.scripts;
    }

    public String noticeString() {
        return "ScriptedItem";
    }

    public Map<Long, String> getConsoleText() {
        return this.console;
    }

    public void clearConsole() {
        this.console.clear();
    }

    public String getTexture() {
        return this.texture == null ? "" : this.texture;
    }

    public void setTexture(String texture){
        if(texture == null)
            texture = "";
        this.texture = texture;
        saveItemData();
    }

    public int getMaxStackSize() {
        return this.stackSize;
    }

    public void setArmorType(int armorType) {
        this.armorType = armorType;
        saveItemData();
    }

    public int getArmorType(){
        return this.armorType;
    }

    public void setIsTool(boolean isTool){
        this.isTool = isTool;
        saveItemData();
    }

    public boolean isTool(){
        return this.isTool;
    }

    public void setDigSpeed(int digSpeed){
        this.digSpeed = digSpeed;
        saveItemData();
    }

    public int getDigSpeed(){
        return this.digSpeed;
    }

    public void setMaxStackSize(int size) {
        if (size >= 1 && size <= 127) {
            this.stackSize = size;
            saveItemData();
        } else {
            throw new CustomNPCsException("Stacksize has to be between 1 and 127", new Object[0]);
        }
    }

    public double getDurabilityValue() {
        return this.durabilityValue;
    }

    public void setDurabilityValue(float value) {
        this.durabilityValue = (double)value;
        saveItemData();
    }

    public boolean getDurabilityShow() {
        return this.durabilityShow;
    }

    public void setDurabilityShow(boolean bo) {
        this.durabilityShow = bo;
        saveItemData();
    }

    public int getDurabilityColor() {
        return this.durabilityColor;
    }

    public void setDurabilityColor(int color) {
        this.durabilityColor = color;
        saveItemData();
    }

    public int getColor() {
        return this.itemColor;
    }

    public void setColor(int color) {
        this.itemColor = color;
        saveItemData();
    }

    public int getMaxItemUseDuration(){
        return this.maxItemUseDuration;
    }

    public void setMaxItemUseDuration(int duration) {
        this.maxItemUseDuration = duration;
        saveItemData();
    }

    public void setItemUseAction(int action) {
        this.itemUseAction = action;
        saveItemData();
    }

    public int getItemUseAction() {
        return this.itemUseAction;
    }

    public int getEnchantability(){
        return this.enchantability;
    }

    public void setEnchantability(int enchantability){
        this.enchantability = enchantability;
        saveItemData();
    }

    public void setRotation(float rotationX, float rotationY, float rotationZ){
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        saveItemData();
    }

    public void setRotationRate(float rotationXRate, float rotationYRate, float rotationZRate){
        this.rotationXRate = rotationXRate;
        this.rotationYRate = rotationYRate;
        this.rotationZRate = rotationZRate;
        saveItemData();
    }

    public void setScale(float scaleX, float scaleY, float scaleZ){
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        saveItemData();
    }

    public void setTranslate(float translateX, float translateY, float translateZ){
        this.translateX = translateX;
        this.translateY = translateY;
        this.translateZ = translateZ;
        saveItemData();
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public float getRotationXRate() {
        return rotationXRate;
    }

    public float getRotationYRate() {
        return rotationYRate;
    }

    public float getRotationZRate() {
        return rotationZRate;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public float getTranslateX() {
        return translateX;
    }

    public float getTranslateY() {
        return translateY;
    }

    public float getTranslateZ() {
        return translateZ;
    }

    public NBTTagCompound getMCNbt() {
        NBTTagCompound compound = super.getMCNbt();
        compound.setTag("ItemData", this.getItemNBT(new NBTTagCompound()));
        compound.setTag("ScriptedData", this.getScriptNBT(new NBTTagCompound()));
        return compound;
    }

    public void setMCNbt(NBTTagCompound compound) {
        setScriptNBT(compound.getCompoundTag("ScriptedData"));
        setItemNBT(compound.getCompoundTag("ItemData"));
    }

    public NBTTagCompound getItemNBT(NBTTagCompound compound) {
        compound.setBoolean("DurabilityShow",this.durabilityShow);
        compound.setDouble("DurabilityValue",this.durabilityValue);
        compound.setInteger("DurabilityColor",this.durabilityColor );
        compound.setInteger("ItemColor",this.itemColor);
        compound.setInteger("MaxStackSize",this.stackSize);
        compound.setString("ItemTexture",this.texture);

        compound.setFloat("RotationX",this.rotationX);
        compound.setFloat("RotationY",this.rotationY);
        compound.setFloat("RotationZ",this.rotationZ);

        compound.setFloat("RotationXRate",this.rotationXRate);
        compound.setFloat("RotationYRate",this.rotationYRate);
        compound.setFloat("RotationZRate",this.rotationZRate);

        compound.setFloat("ScaleX",this.scaleX);
        compound.setFloat("ScaleY",this.scaleY);
        compound.setFloat("ScaleZ",this.scaleZ);

        compound.setFloat("TranslateX",this.translateX);
        compound.setFloat("TranslateY",this.translateY);
        compound.setFloat("TranslateZ",this.translateZ);

        compound.setInteger("Width", this.width);
        compound.setInteger("Height", this.height);

        compound.setBoolean("IsTool", this.isTool);
        compound.setInteger("DigSpeed", this.digSpeed);
        compound.setInteger("ArmorType", this.armorType);
        compound.setInteger("Enchantability", this.enchantability);

        compound.setInteger("MaxItemUseDuration", this.maxItemUseDuration);
        compound.setInteger("ItemUseAction", this.itemUseAction);
        return compound;
    }

    public void setItemNBT(NBTTagCompound compound) {
        this.durabilityShow = compound.getBoolean("DurabilityShow");
        this.durabilityValue = compound.getDouble("DurabilityValue");
        if (compound.hasKey("DurabilityColor")) {
            this.durabilityColor = compound.getInteger("DurabilityColor");
        }
        this.itemColor = compound.getInteger("ItemColor");
        this.stackSize = compound.getInteger("MaxStackSize");
        this.texture = compound.getString("ItemTexture");

        this.rotationX = compound.getFloat("RotationX");
        this.rotationY = compound.getFloat("RotationY");
        this.rotationZ = compound.getFloat("RotationZ");

        this.rotationXRate = compound.getFloat("RotationXRate");
        this.rotationYRate = compound.getFloat("RotationYRate");
        this.rotationZRate = compound.getFloat("RotationZRate");

        this.scaleX = compound.getFloat("ScaleX");
        this.scaleY = compound.getFloat("ScaleY");
        this.scaleZ = compound.getFloat("ScaleZ");

        this.translateX = compound.getFloat("TranslateX");
        this.translateY = compound.getFloat("TranslateY");
        this.translateZ = compound.getFloat("TranslateZ");

        this.width = compound.getInteger("Width");
        this.height = compound.getInteger("Height");

        this.isTool = compound.getBoolean("IsTool");
        this.digSpeed = compound.getInteger("DigSpeed");
        this.armorType = compound.getInteger("ArmorType");
        this.enchantability = compound.getInteger("Enchantability");

        this.maxItemUseDuration = compound.getInteger("MaxItemUseDuration");
        this.itemUseAction = compound.getInteger("ItemUseAction");
    }

    public void saveScriptData() {
        NBTTagCompound c = this.item.getTagCompound();
        if (c == null) {
            this.item.setTagCompound(c = new NBTTagCompound());
        }

        c.setTag("ScriptedData", this.getScriptNBT(new NBTTagCompound()));
    }

    public void loadScriptData() {
        NBTTagCompound c = this.item.getTagCompound();
        if (c != null) {
            this.setScriptNBT(c.getCompoundTag("ScriptedData"));
        }
    }

    public void saveItemData() {
        NBTTagCompound c = this.item.getTagCompound();
        if (c == null) {
            this.item.setTagCompound(c = new NBTTagCompound());
        }

        c.setTag("ItemData", this.getItemNBT(new NBTTagCompound()));
    }

    public void loadItemData() {
        NBTTagCompound c = this.item.getTagCompound();
        if (c != null){
            this.setItemNBT(c.getCompoundTag("ItemData"));
        }
    }
}
