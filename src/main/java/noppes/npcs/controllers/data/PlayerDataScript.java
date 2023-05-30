//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package noppes.npcs.controllers.data;

import com.google.common.base.Preconditions;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.NpcAPI;

import javax.annotation.CheckForNull;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

public class PlayerDataScript implements IScriptHandler {
    public List<ScriptContainer> scripts = new ArrayList();
    public String scriptLanguage = "ECMAScript";
    private EntityPlayer player;
    private IPlayer playerAPI;
    private long lastPlayerUpdate = 0L;
    public long lastInited = -1L;
    public boolean enabled = false;

    public PlayerDataScript(EntityPlayer player) {
        if(player != null) {
            this.player = player;
        }
    }
    public void clear() {
        this.scripts = new ArrayList();
    }
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this);
        } else {
            this.scripts = NBTTags.GetScript(compound,this);
        }
        this.scriptLanguage = compound.getString("ScriptLanguage");
        if (!ScriptController.Instance.languages.containsKey(scriptLanguage)) {
            if (!ScriptController.Instance.languages.isEmpty()) {
                this.scriptLanguage = (String) ScriptController.Instance.languages.keySet().toArray()[0];
            } else {
                this.scriptLanguage = "ECMAScript";
            }
        }
        this.enabled = compound.getBoolean("ScriptEnabled");

        for (ScriptContainer container : this.scripts) {
            container.setEngine(this.getLanguage());
        }
    }
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts",this.scripts.size());
        for (int i = 0; i < this.scripts.size(); i++) {
            compound.setTag("Tab"+i,this.scripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        return compound;
    }

    public void callScript(EnumScriptType type, Event event) {
        if (this.isEnabled()) {
            if (ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastPlayerUpdate > this.lastPlayerUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                this.lastPlayerUpdate = ScriptController.Instance.lastPlayerUpdate;

                for (ScriptContainer scriptContainer : this.scripts) {
                    scriptContainer.errored = false;
                }

                if (type != EnumScriptType.INIT) {
                    noppes.npcs.scripted.event.PlayerEvent playerEvent = (noppes.npcs.scripted.event.PlayerEvent)event;
                    EventHooks.onPlayerInit(this, playerEvent.player);
                }
            }

            for (ScriptContainer script : this.scripts) {
                if(script == null || script.errored || !script.hasCode() )
                    continue;

                script.run(type, event);
            }
        }
    }

    public boolean isEnabled() {
        return ConfigScript.GlobalPlayerScripts && ScriptController.Instance.playerScripts.enabled && ScriptController.HasStart && (this.player == null || !this.player.worldObj.isRemote);
    }
    public boolean isClient() {
        return this.player.isClientWorld();
    }
    public boolean getEnabled() {
        return ScriptController.Instance.playerScripts.enabled;
    }
    public void setEnabled(boolean bo) {
        ScriptController.Instance.playerScripts.enabled = bo;
        this.enabled = bo;
    }
    public String getLanguage() {
        return ScriptController.Instance.playerScripts.scriptLanguage;
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
        if(this.player == null) {
            return "Global script";
        } else {
            BlockPos pos = new BlockPos(this.player);
            return PlayerDataScript.toStringHelper(this.player).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
        }
    }
    public IPlayer getPlayer() {
        if (this.playerAPI == null) {
            this.playerAPI = (IPlayer) NpcAPI.Instance().getIEntity(this.player);
        }
        return this.playerAPI;
    }

    public Map<Long, String> getConsoleText() {
        TreeMap<Long, String> map = new TreeMap<>();
        int tab = 0;
        for (ScriptContainer script : this.getScripts()) {
            ++tab;

            for (Entry<Long, String> longStringEntry : script.console.entrySet()) {
                map.put(longStringEntry.getKey(), " tab " + tab + ":\n" + longStringEntry.getValue());
            }
        }
        return map;
    }

    public void clearConsole() {
        for (ScriptContainer script : this.getScripts()) {
            script.console.clear();
        }
    }

    public static final class ToStringHelper {
        private final String className;
        private final PlayerDataScript.ToStringHelper.ValueHolder holderHead;
        private PlayerDataScript.ToStringHelper.ValueHolder holderTail;
        private boolean omitNullValues;
        private boolean omitEmptyValues;
        private ToStringHelper(String className) {
            this.holderHead = new PlayerDataScript.ToStringHelper.ValueHolder();
            this.holderTail = this.holderHead;
            this.omitNullValues = false;
            this.omitEmptyValues = false;
            this.className = (String) Preconditions.checkNotNull(className);
        }
        public PlayerDataScript.ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }
        public PlayerDataScript.ToStringHelper add(String name, @CheckForNull Object value) {
            return this.addHolder(name, value);
        }
        public PlayerDataScript.ToStringHelper add(String name, boolean value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper add(String name, char value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper add(String name, double value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper add(String name, float value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper add(String name, int value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper add(String name, long value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper addValue(@CheckForNull Object value) {
            return this.addHolder(value);
        }
        public PlayerDataScript.ToStringHelper addValue(boolean value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper addValue(char value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper addValue(double value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper addValue(float value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper addValue(int value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public PlayerDataScript.ToStringHelper addValue(long value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        private static boolean isEmpty(Object value) {
            if (value instanceof CharSequence) {
                return ((CharSequence)value).length() == 0;
            } else if (value instanceof Collection) {
                return ((Collection)value).isEmpty();
            } else if (value instanceof Map) {
                return ((Map)value).isEmpty();
            } else if (value instanceof Optional) {
                return !((Optional)value).isPresent();
            } else if (value instanceof OptionalInt) {
                return !((OptionalInt)value).isPresent();
            } else if (value instanceof OptionalLong) {
                return !((OptionalLong)value).isPresent();
            } else if (value instanceof OptionalDouble) {
                return !((OptionalDouble)value).isPresent();
            } else if (value instanceof com.google.common.base.Optional) {
                return !((com.google.common.base.Optional)value).isPresent();
            } else if (value.getClass().isArray()) {
                return Array.getLength(value) == 0;
            } else {
                return false;
            }
        }
        public String toString() {
            boolean omitNullValuesSnapshot = this.omitNullValues;
            boolean omitEmptyValuesSnapshot = this.omitEmptyValues;
            String nextSeparator = "";
            StringBuilder builder = (new StringBuilder(32)).append(this.className).append('{');
            for(PlayerDataScript.ToStringHelper.ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
                Object value = valueHolder.value;
                if (!(valueHolder instanceof PlayerDataScript.ToStringHelper.UnconditionalValueHolder)) {
                    if (value == null) {
                        if (omitNullValuesSnapshot) {
                            continue;
                        }
                    } else if (omitEmptyValuesSnapshot && isEmpty(value)) {
                        continue;
                    }
                }
                builder.append(nextSeparator);
                nextSeparator = ", ";
                if (valueHolder.name != null) {
                    builder.append(valueHolder.name).append('=');
                }
                if (value != null && value.getClass().isArray()) {
                    Object[] objectArray = new Object[]{value};
                    String arrayString = Arrays.deepToString(objectArray);
                    builder.append(arrayString, 1, arrayString.length() - 1);
                } else {
                    builder.append(value);
                }
            }
            return builder.append('}').toString();
        }
        private PlayerDataScript.ToStringHelper.ValueHolder addHolder() {
            PlayerDataScript.ToStringHelper.ValueHolder valueHolder = new PlayerDataScript.ToStringHelper.ValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }
        private PlayerDataScript.ToStringHelper addHolder(@CheckForNull Object value) {
            PlayerDataScript.ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }
        private PlayerDataScript.ToStringHelper addHolder(String name, @CheckForNull Object value) {
            PlayerDataScript.ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = (String)Preconditions.checkNotNull(name);
            return this;
        }
        private PlayerDataScript.ToStringHelper.UnconditionalValueHolder addUnconditionalHolder() {
            PlayerDataScript.ToStringHelper.UnconditionalValueHolder valueHolder = new PlayerDataScript.ToStringHelper.UnconditionalValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }
        private PlayerDataScript.ToStringHelper addUnconditionalHolder(Object value) {
            PlayerDataScript.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            return this;
        }
        private PlayerDataScript.ToStringHelper addUnconditionalHolder(String name, Object value) {
            PlayerDataScript.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            valueHolder.name = (String)Preconditions.checkNotNull(name);
            return this;
        }
        private static final class UnconditionalValueHolder extends PlayerDataScript.ToStringHelper.ValueHolder {
            private UnconditionalValueHolder() {
                super();
            }
        }
        private static class ValueHolder {
            @CheckForNull
            String name;
            @CheckForNull
            Object value;
            @CheckForNull
            PlayerDataScript.ToStringHelper.ValueHolder next;
            private ValueHolder() {
            }
        }
    }
    public static PlayerDataScript.ToStringHelper toStringHelper(Object self) {
        return new PlayerDataScript.ToStringHelper(self.getClass().getSimpleName());
    }
}