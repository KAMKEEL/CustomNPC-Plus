package noppes.npcs.controllers.data;

import com.google.common.base.Preconditions;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.NBTTags;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;

import javax.annotation.CheckForNull;
import java.lang.reflect.Array;
import java.util.*;

public class NPCDataScript implements IScriptHandler {
    public List<ScriptContainer> scripts = new ArrayList();
    public String scriptLanguage = "ECMAScript";
    private EntityNPCInterface npc;
    private ICustomNpc npcAPI;
    public long lastInited = -1L;
    public boolean enabled = false;
    private Map<Long, String> console = new TreeMap();

    public NPCDataScript(EntityNPCInterface npc) {
        if(npc != null) {
            this.npc = npc;
        }
    }
    public void clear() {
        this.console = new TreeMap();
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
        this.console = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
    }
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts",this.scripts.size());
        for (int i = 0; i < this.scripts.size(); i++) {
            compound.setTag("Tab"+i,this.scripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.console));
        return compound;
    }

    public void callScript(EnumScriptType type, Event event) {
        if (this.isEnabled()) {
            if (ScriptController.Instance.lastLoaded > this.lastInited) {
                this.lastInited = ScriptController.Instance.lastLoaded;
            }

            int i = 0;
            for (ScriptContainer script : this.scripts) {
                script.run(type, event);

                for (Map.Entry<Long, String> longStringEntry : script.console.entrySet()) {
                    if (!ScriptController.Instance.npcScripts.console.containsKey(longStringEntry.getKey())) {
                        ScriptController.Instance.npcScripts.console.put(longStringEntry.getKey(), " tab " + (i + 1) + ":\n" + longStringEntry.getValue());
                    }
                }
                i++;
            }
        }
    }

    public boolean isEnabled() {
        return ConfigScript.GlobalNPCScripts && this.enabled && ScriptController.HasStart && this.scripts.size() > 0;
    }
    public boolean isClient() {
        return this.npc.isClientWorld();
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
        if(this.npc == null) {
            return "Global script";
        } else {
            BlockPos pos = new BlockPos(this.npc);
            return NPCDataScript.toStringHelper(this.npc).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
        }
    }
    public ICustomNpc getNpc() {
        if (this.npcAPI == null) {
            this.npcAPI = (ICustomNpc) NpcAPI.Instance().getIEntity(this.npc);
        }
        return this.npcAPI;
    }

    public void setConsoleText(Map<Long, String> map) {
        this.console = map;
    }

    public Map<Long, String> getConsoleText() {
        return this.console;
    }

    public void clearConsole() {
        this.console.clear();
    }

    public static final class ToStringHelper {
        private final String className;
        private final NPCDataScript.ToStringHelper.ValueHolder holderHead;
        private NPCDataScript.ToStringHelper.ValueHolder holderTail;
        private boolean omitNullValues;
        private boolean omitEmptyValues;
        private ToStringHelper(String className) {
            this.holderHead = new NPCDataScript.ToStringHelper.ValueHolder();
            this.holderTail = this.holderHead;
            this.omitNullValues = false;
            this.omitEmptyValues = false;
            this.className = (String) Preconditions.checkNotNull(className);
        }
        public NPCDataScript.ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }
        public NPCDataScript.ToStringHelper add(String name, @CheckForNull Object value) {
            return this.addHolder(name, value);
        }
        public NPCDataScript.ToStringHelper add(String name, boolean value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper add(String name, char value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper add(String name, double value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper add(String name, float value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper add(String name, int value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper add(String name, long value) {
            return this.addUnconditionalHolder(name, String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper addValue(@CheckForNull Object value) {
            return this.addHolder(value);
        }
        public NPCDataScript.ToStringHelper addValue(boolean value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper addValue(char value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper addValue(double value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper addValue(float value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper addValue(int value) {
            return this.addUnconditionalHolder(String.valueOf(value));
        }
        public NPCDataScript.ToStringHelper addValue(long value) {
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
            for(NPCDataScript.ToStringHelper.ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
                Object value = valueHolder.value;
                if (!(valueHolder instanceof NPCDataScript.ToStringHelper.UnconditionalValueHolder)) {
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
        private NPCDataScript.ToStringHelper.ValueHolder addHolder() {
            NPCDataScript.ToStringHelper.ValueHolder valueHolder = new NPCDataScript.ToStringHelper.ValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }
        private NPCDataScript.ToStringHelper addHolder(@CheckForNull Object value) {
            NPCDataScript.ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }
        private NPCDataScript.ToStringHelper addHolder(String name, @CheckForNull Object value) {
            NPCDataScript.ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = (String)Preconditions.checkNotNull(name);
            return this;
        }
        private NPCDataScript.ToStringHelper.UnconditionalValueHolder addUnconditionalHolder() {
            NPCDataScript.ToStringHelper.UnconditionalValueHolder valueHolder = new NPCDataScript.ToStringHelper.UnconditionalValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }
        private NPCDataScript.ToStringHelper addUnconditionalHolder(Object value) {
            NPCDataScript.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            return this;
        }
        private NPCDataScript.ToStringHelper addUnconditionalHolder(String name, Object value) {
            NPCDataScript.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
            valueHolder.value = value;
            valueHolder.name = (String)Preconditions.checkNotNull(name);
            return this;
        }
        private static final class UnconditionalValueHolder extends NPCDataScript.ToStringHelper.ValueHolder {
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
            NPCDataScript.ToStringHelper.ValueHolder next;
            private ValueHolder() {
            }
        }
    }
    public static NPCDataScript.ToStringHelper toStringHelper(Object self) {
        return new NPCDataScript.ToStringHelper(self.getClass().getSimpleName());
    }
}
