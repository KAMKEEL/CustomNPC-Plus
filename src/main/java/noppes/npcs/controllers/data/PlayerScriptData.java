//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers.data;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.EventScriptContainer;
import noppes.npcs.NBTTags;
import noppes.npcs.scripted.ScriptPlayer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;

import javax.annotation.CheckForNull;

public class PlayerScriptData implements IScriptHandler {
    public List<EventScriptContainer> scripts = new ArrayList();
    private String scriptLanguage = "ECMAScript";
    private EntityPlayer player;
    private ScriptPlayer playerAPI;
    private long lastPlayerUpdate = 0L;
    public long lastInited = -1L;
    public boolean hadInteract = true;
    private boolean enabled = false;
    private static Map<Long, String> console = new TreeMap();
    private static List<Integer> errored = new ArrayList();

    public PlayerScriptData(EntityPlayer player) {
        this.player = player;
    }

    public void clear() {
        console = new TreeMap();
        errored = new ArrayList();
        this.scripts = new ArrayList();
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
        this.scriptLanguage = compound.getString("ScriptLanguage");
        this.enabled = compound.getBoolean("ScriptEnabled");
        console = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(console));
        return compound;
    }

    public void runScript(EnumScriptType type, Event event) {
        if(this.isEnabled()) {
            EventScriptContainer script;
            if(ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastPlayerUpdate > this.lastPlayerUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                errored.clear();
                if(this.player != null) {
                    this.scripts.clear();
                    Iterator i = ScriptController.Instance.playerScripts.scripts.iterator();

                    while(i.hasNext()) {
                        script = ( EventScriptContainer)i.next();
                         EventScriptContainer s = new  EventScriptContainer(this);
                        s.readFromNBT(script.writeToNBT(new NBTTagCompound()));
                        this.scripts.add(s);
                    }
                }

                this.lastPlayerUpdate = ScriptController.Instance.lastPlayerUpdate;
                if(type != EnumScriptType.INIT) {
                    EventHooks.onPlayerInit(this);
                }
            }

            for(int var7 = 0; var7 < this.scripts.size(); ++var7) {
                script = (EventScriptContainer)this.scripts.get(var7);
                if(!errored.contains(Integer.valueOf(var7))) {
                    script.run(type, event);
                    if(script.errored) {
                        errored.add(Integer.valueOf(var7));
                    }

                    Iterator var8 = script.console.entrySet().iterator();

                    while(var8.hasNext()) {
                        Entry entry = (Entry)var8.next();
                        if(!console.containsKey(entry.getKey())) {
                            console.put((Long)entry.getKey(), " tab " + (var7 + 1) + ":\n" + (String)entry.getValue());
                        }
                    }

                    script.console.clear();
                }
            }

        }
    }

    public boolean isEnabled() {
        return ScriptController.Instance.playerScripts.enabled && ScriptController.HasStart && (this.player == null || !this.player.worldObj.isRemote);
    }

    public boolean isClient() {
        return this.player.isClientWorld();
    }

    public boolean getEnabled() {
        return ScriptController.Instance.playerScripts.enabled;
    }

    public void setEnabled(boolean bo) {
        this.enabled = bo;
    }

    public String getLanguage() {
        return ScriptController.Instance.playerScripts.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
    }

    public List<EventScriptContainer> getScripts() {
        return this.scripts;
    }

    public static final class ToStringHelper {
        private final String className;
        private final PlayerScriptData.ToStringHelper.ValueHolder holderHead;
        private PlayerScriptData.ToStringHelper.ValueHolder holderTail;
        private boolean omitNullValues;
        private boolean omitEmptyValues;

        private ToStringHelper(String className) {
            this.holderHead = new PlayerScriptData.ToStringHelper.ValueHolder();
            this.holderTail = this.holderHead;
            this.omitNullValues = false;
            this.omitEmptyValues = false;
            this.className = (String) Preconditions.checkNotNull(className);
        }

        public PlayerScriptData.ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }

        public PlayerScriptData.ToStringHelper add(String name, @CheckForNull Object value) {
            return this.addHolder(name, value);
        }

        public PlayerScriptData.ToStringHelper add(String name, boolean value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper add(String name, char value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper add(String name, double value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper add(String name, float value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper add(String name, int value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper add(String name, long value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper addValue(@CheckForNull Object value) {
            return this.addHolder(value);
        }

        public PlayerScriptData.ToStringHelper addValue(boolean value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper addValue(char value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper addValue(double value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper addValue(float value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper addValue(int value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }

        public PlayerScriptData.ToStringHelper addValue(long value) {
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

            for(PlayerScriptData.ToStringHelper.ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
                Object value = valueHolder.value;
                if (!(valueHolder instanceof PlayerScriptData.ToStringHelper.UnconditionalValueHolder)) {
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

        private PlayerScriptData.ToStringHelper.ValueHolder addHolder() {
            PlayerScriptData.ToStringHelper.ValueHolder valueHolder = new PlayerScriptData.ToStringHelper.ValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }

        private PlayerScriptData.ToStringHelper addHolder(@CheckForNull Object value) {
            PlayerScriptData.ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }

        private PlayerScriptData.ToStringHelper addHolder(String name, @CheckForNull Object value) {
            PlayerScriptData.ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = (String)Preconditions.checkNotNull(name);
            return this;
        }

        private PlayerScriptData.ToStringHelper.UnconditionalValueHolder addUnconditionalHolder() {
            PlayerScriptData.ToStringHelper.UnconditionalValueHolder valueHolder = new PlayerScriptData.ToStringHelper.UnconditionalValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }

        private PlayerScriptData.ToStringHelper addUnconditionalHolder(Object value) {
            PlayerScriptData.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            return this;
        }

        private PlayerScriptData.ToStringHelper addUnconditionalHolder(String name, Object value) {
            PlayerScriptData.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            valueHolder.name = (String)Preconditions.checkNotNull(name);
            return this;
        }

        private static final class UnconditionalValueHolder extends PlayerScriptData.ToStringHelper.ValueHolder {
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
            PlayerScriptData.ToStringHelper.ValueHolder next;

            private ValueHolder() {
            }
        }
    }

    public static PlayerScriptData.ToStringHelper toStringHelper(Object self) {
        return new PlayerScriptData.ToStringHelper(self.getClass().getSimpleName());
    }

    public String noticeString() {
        if(this.player == null) {
            return "Global script";
        } else {
            BlockPos pos = new BlockPos(this.player);
            return PlayerScriptData.toStringHelper(this.player).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
        }
    }

    public ScriptPlayer getPlayer() {
        if(this.playerAPI == null) {
            this.playerAPI = (ScriptPlayer) ScriptController.Instance.getScriptForEntity(this.player);
        }
        return this.playerAPI;
    }

    public Map<Long, String> getConsoleText() {
        return console;
    }

    public void clearConsole() {
        console.clear();
    }
}
