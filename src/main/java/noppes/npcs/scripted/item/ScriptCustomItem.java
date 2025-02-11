package noppes.npcs.scripted.item;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.item.IItemCustom;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.*;

public class ScriptCustomItem extends ScriptItemStack implements IItemCustom, INpcScriptHandler {
    public List<ScriptContainer> scripts = new ArrayList();
    public List<Integer> errored = new ArrayList();
    public String scriptLanguage = "ECMAScript";
    public boolean enabled = false;
    public boolean loaded = false;

    public double durabilityValue = 1.0D;
    public int stackSize = 64;

    public int maxItemUseDuration = 20;
    public int itemUseAction = 0; //0: none, 1: block, 2: bow, 3: eat, 4: drink

    public boolean isNormalItem = false;
    public boolean isTool = false;
    public int digSpeed = 1;
    public int armorType = -2; //-2: Fits in no armor slot,  -1: Fits in all slots, 0 - 4: Fits in Head -> Boots slot respectively
    public int enchantability;


    // TODO: Move to ItemDisplay
    public Display itemDisplay = new Display();
    // END_TODO

    public long lastInited = -1;

    public ScriptCustomItem(ItemStack item) {
        super(item);
        loadItemData();
    }

    public NBTTagCompound getScriptNBT(NBTTagCompound compound) {
        compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        return compound;
    }

    public void setScriptNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this);
            this.scriptLanguage = compound.getString("ScriptLanguage");
            this.enabled = compound.getBoolean("ScriptEnabled");
        }
    }

    public int getType() {
        return 6;
    }

    private boolean isEnabled() {
        return this.enabled && ScriptController.HasStart;
    }

    public void callScript(EnumScriptType type, Event event) {
        this.callScript(type.function, event);
    }

    @Override
    public void callScript(String hookName, Event event) {
        if (!this.loaded) {
            this.loadScriptData();
            this.loaded = true;
        }

        if(!this.isEnabled())
            return;

        if(ScriptController.Instance.lastLoaded > lastInited){
            lastInited = ScriptController.Instance.lastLoaded;
            if (!Objects.equals(hookName, EnumScriptType.INIT.function)) {
                EventHooks.onScriptItemInit(this);
            }
        }

        for (int i = 0; i < this.scripts.size(); i++) {
            ScriptContainer script = this.scripts.get(i);
            if (!this.errored.contains(i)) {
                if(script == null || script.errored || !script.hasCode())
                    continue;

                script.run(hookName, event);

                if (script.errored) {
                    this.errored.add(i);
                }
            }
        }
    }

    public boolean isClient() {
        return false;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    public String getLanguage() {
        return this.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
    }

    public void setScripts(List<ScriptContainer> list) {
        this.scripts = list;
    }

    public List<ScriptContainer> getScripts() {
        return this.scripts;
    }

    public String noticeString() {
        return "ScriptedItem";
    }

    public Map<Long, String> getConsoleText() {
        return new TreeMap<>();
    }

    public void clearConsole() {
    }

    public String getTexture() {
        return this.itemDisplay.texture == null ? "" : this.itemDisplay.texture;
    }

    public void setTexture(String texture){
        if(texture == null)
            texture = "";
        this.itemDisplay.texture = texture;
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

    public void setIsNormalItem(boolean normalItem){
        this.isNormalItem = normalItem;
        saveItemData();
    }

    public boolean isNormalItem(){
        return this.isNormalItem;
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
        return this.itemDisplay.durabilityShow;
    }

    public void setDurabilityShow(boolean bo) {
        this.itemDisplay.durabilityShow = bo;
        saveItemData();
    }

    public int getDurabilityColor() {
        return this.itemDisplay.durabilityColor;
    }

    public void setDurabilityColor(int color) {
        this.itemDisplay.durabilityColor = color;
        saveItemData();
    }

    public int getColor() {
        return this.itemDisplay.itemColor;
    }

    public void setColor(int color) {
        this.itemDisplay.itemColor = color;
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
        this.itemDisplay.rotationX = rotationX;
        this.itemDisplay.rotationY = rotationY;
        this.itemDisplay.rotationZ = rotationZ;
        saveItemData();
    }

    public void setRotationRate(float rotationXRate, float rotationYRate, float rotationZRate){
        this.itemDisplay.rotationXRate = rotationXRate;
        this.itemDisplay.rotationYRate = rotationYRate;
        this.itemDisplay.rotationZRate = rotationZRate;
        saveItemData();
    }

    public void setScale(float scaleX, float scaleY, float scaleZ){
        this.itemDisplay.scaleX = scaleX;
        this.itemDisplay.scaleY = scaleY;
        this.itemDisplay.scaleZ = scaleZ;
        saveItemData();
    }

    public void setTranslate(float translateX, float translateY, float translateZ){
        this.itemDisplay.translateX = translateX;
        this.itemDisplay.translateY = translateY;
        this.itemDisplay.translateZ = translateZ;
        saveItemData();
    }

    public float getRotationX() {
        return this.itemDisplay.rotationX;
    }

    public float getRotationY() {
        return this.itemDisplay.rotationY;
    }

    public float getRotationZ() {
        return this.itemDisplay.rotationZ;
    }

    public float getRotationXRate() {
        return this.itemDisplay.rotationXRate;
    }

    public float getRotationYRate() {
        return this.itemDisplay.rotationYRate;
    }

    public float getRotationZRate() {
        return this.itemDisplay.rotationZRate;
    }

    public float getScaleX() {
        return this.itemDisplay.scaleX;
    }

    public float getScaleY() {
        return this.itemDisplay.scaleY;
    }

    public float getScaleZ() {
        return this.itemDisplay.scaleZ;
    }

    public float getTranslateX() {
        return this.itemDisplay.translateX;
    }

    public float getTranslateY() {
        return this.itemDisplay.translateY;
    }

    public float getTranslateZ() {
        return this.itemDisplay.translateZ;
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
        compound.setBoolean("DurabilityShow",this.itemDisplay.durabilityShow);
        compound.setDouble("DurabilityValue",this.durabilityValue);
        compound.setInteger("DurabilityColor",this.itemDisplay.durabilityColor );
        compound.setInteger("ItemColor",this.itemDisplay.itemColor);
        compound.setInteger("MaxStackSize",this.stackSize);
        compound.setString("ItemTexture",this.itemDisplay.texture);

        compound.setFloat("RotationX",this.itemDisplay.rotationX);
        compound.setFloat("RotationY",this.itemDisplay.rotationY);
        compound.setFloat("RotationZ",this.itemDisplay.rotationZ);

        compound.setFloat("RotationXRate",this.itemDisplay.rotationXRate);
        compound.setFloat("RotationYRate",this.itemDisplay.rotationYRate);
        compound.setFloat("RotationZRate",this.itemDisplay.rotationZRate);

        compound.setFloat("ScaleX",this.itemDisplay.scaleX);
        compound.setFloat("ScaleY",this.itemDisplay.scaleY);
        compound.setFloat("ScaleZ",this.itemDisplay.scaleZ);

        compound.setFloat("TranslateX",this.itemDisplay.translateX);
        compound.setFloat("TranslateY",this.itemDisplay.translateY);
        compound.setFloat("TranslateZ",this.itemDisplay.translateZ);

        compound.setBoolean("IsTool", this.isTool);
        compound.setBoolean("IsNormalItem", this.isNormalItem);
        compound.setInteger("DigSpeed", this.digSpeed);
        compound.setInteger("ArmorType", this.armorType);
        compound.setInteger("Enchantability", this.enchantability);

        compound.setInteger("MaxItemUseDuration", this.maxItemUseDuration);
        compound.setInteger("ItemUseAction", this.itemUseAction);
        return compound;
    }

    public void setItemNBT(NBTTagCompound compound) {
        this.itemDisplay.durabilityShow = compound.getBoolean("DurabilityShow");
        this.durabilityValue = compound.getDouble("DurabilityValue");
        if (compound.hasKey("DurabilityColor")) {
            this.itemDisplay.durabilityColor = compound.getInteger("DurabilityColor");
        }
        this.itemDisplay.itemColor = compound.getInteger("ItemColor");
        this.stackSize = compound.getInteger("MaxStackSize");
        this.itemDisplay.texture = compound.getString("ItemTexture");

        this.itemDisplay.rotationX = compound.getFloat("RotationX");
        this.itemDisplay.rotationY = compound.getFloat("RotationY");
        this.itemDisplay.rotationZ = compound.getFloat("RotationZ");

        this.itemDisplay.rotationXRate = compound.getFloat("RotationXRate");
        this.itemDisplay.rotationYRate = compound.getFloat("RotationYRate");
        this.itemDisplay.rotationZRate = compound.getFloat("RotationZRate");

        this.itemDisplay.scaleX = compound.getFloat("ScaleX");
        this.itemDisplay.scaleY = compound.getFloat("ScaleY");
        this.itemDisplay.scaleZ = compound.getFloat("ScaleZ");

        this.itemDisplay.translateX = compound.getFloat("TranslateX");
        this.itemDisplay.translateY = compound.getFloat("TranslateY");
        this.itemDisplay.translateZ = compound.getFloat("TranslateZ");

        this.isTool = compound.getBoolean("IsTool");
        this.isNormalItem = compound.getBoolean("IsNormalItem");
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
        if (c != null && !c.getCompoundTag("ItemData").hasNoTags()){
            this.setItemNBT(c.getCompoundTag("ItemData"));
        }
    }


    public static class Display {
        public String texture = "minecraft:textures/items/iron_pickaxe.png";
        public float translateX, translateY, translateZ;
        public int itemColor = 0x8B4513;
        public float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;
        public float rotationX, rotationY, rotationZ;
        public float rotationXRate, rotationYRate, rotationZRate;
        public boolean durabilityShow = false;
        public int durabilityColor = -1;
    }
}
