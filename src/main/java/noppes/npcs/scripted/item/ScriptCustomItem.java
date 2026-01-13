package noppes.npcs.scripted.item;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.item.IItemCustom;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ScriptCustomItem extends ScriptCustomizableItem implements IItemCustom, INpcScriptHandler {
    public List<IScriptUnit> scripts = new ArrayList();
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
    public int attackSpeed = 10;

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

    @Override
    public INpcScriptHandler getScriptHandler() {
        return this;
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

        if (!this.isEnabled())
            return;

        if (ScriptController.Instance.lastLoaded > lastInited) {
            lastInited = ScriptController.Instance.lastLoaded;
            if (!Objects.equals(hookName, EnumScriptType.INIT.function)) {
                EventHooks.onScriptItemInit(this);
            }
        }

        for (int i = 0; i < this.scripts.size(); i++) {
            IScriptUnit script = this.scripts.get(i);
            if (!this.errored.contains(i)) {
                if (script == null || script.hasErrored() || !script.hasCode())
                    continue;

                if (script instanceof ScriptContainer) {
                    ((ScriptContainer) script).run(hookName, event);
                }

                if (script.hasErrored()) {
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

    public void setScripts(List<IScriptUnit> list) {
        this.scripts = list;
    }

    public List<IScriptUnit> getScripts() {
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

    public int getMaxStackSize() {
        return this.stackSize;
    }

    public void setArmorType(int armorType) {
        this.armorType = armorType;
        saveItemData();
    }

    public int getArmorType() {
        return this.armorType;
    }

    public void setIsTool(boolean isTool) {
        this.isTool = isTool;
        saveItemData();
    }

    public boolean isTool() {
        return this.isTool;
    }

    public void setIsNormalItem(boolean normalItem) {
        this.isNormalItem = normalItem;
        saveItemData();
    }

    public boolean isNormalItem() {
        return this.isNormalItem;
    }


    public void setDigSpeed(int digSpeed) {
        this.digSpeed = digSpeed;
        saveItemData();
    }

    public int getDigSpeed() {
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
        this.durabilityValue = (double) value;
        saveItemData();
    }

    public int getMaxItemUseDuration() {
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

    public int getEnchantability() {
        return this.enchantability;
    }

    public int getAttackSpeed() {
        return this.attackSpeed;
    }

    public void setAttackSpeed(int speed) {
        if(speed <= 0) {
            speed = 10;
        }
        this.attackSpeed = speed;
        saveItemData();
    }

    public void setEnchantability(int enchantability) {
        this.enchantability = enchantability;
        saveItemData();
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

    public NBTTagCompound getMCNbt() {
        NBTTagCompound compound = super.getMCNbt();
        compound.setTag("ScriptedData", this.getScriptNBT(new NBTTagCompound()));
        return compound;
    }

    public void setMCNbt(NBTTagCompound compound) {
        super.setMCNbt(compound);
        setScriptNBT(compound.getCompoundTag("ScriptedData"));
    }

    public NBTTagCompound getItemNBT(NBTTagCompound compound) {
        super.getItemNBT(compound);
        compound.setDouble("DurabilityValue", this.durabilityValue);
        compound.setInteger("MaxStackSize", this.stackSize);

        compound.setBoolean("IsTool", this.isTool);
        compound.setBoolean("IsNormalItem", this.isNormalItem);
        compound.setInteger("DigSpeed", this.digSpeed);
        compound.setInteger("ArmorType", this.armorType);
        compound.setInteger("Enchantability", this.enchantability);
        compound.setInteger("AttackSpeed", this.attackSpeed);

        compound.setInteger("MaxItemUseDuration", this.maxItemUseDuration);
        compound.setInteger("ItemUseAction", this.itemUseAction);
        return compound;
    }

    public void setItemNBT(NBTTagCompound compound) {
        super.setItemNBT(compound);
        this.durabilityValue = compound.getDouble("DurabilityValue");
        this.stackSize = compound.getInteger("MaxStackSize");

        this.isTool = compound.getBoolean("IsTool");
        this.isNormalItem = compound.getBoolean("IsNormalItem");
        this.digSpeed = compound.getInteger("DigSpeed");
        this.armorType = compound.getInteger("ArmorType");
        this.enchantability = compound.getInteger("Enchantability");
        setAttackSpeed(compound.getInteger("AttackSpeed"));

        this.maxItemUseDuration = compound.getInteger("MaxItemUseDuration");
        this.itemUseAction = compound.getInteger("ItemUseAction");
    }
}
