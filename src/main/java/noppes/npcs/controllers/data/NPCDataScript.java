package noppes.npcs.controllers.data;

import com.google.common.base.Preconditions;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;

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
    public List<Integer> errored = new ArrayList();

    public NPCDataScript(EntityNPCInterface npc) {
        if(npc != null) {
            this.npc = npc;
        }
    }
    public void clear() {
        this.console = new TreeMap();
        this.errored = new ArrayList();
        this.scripts = new ArrayList();
    }
    public void readFromNBT(NBTTagCompound compound) {
        this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
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
        compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.console));
        return compound;
    }

    public void callScript(EnumScriptType type, Event event) {
        if (this.isEnabled()) {
            ScriptContainer script;
            if (ScriptController.Instance.lastLoaded > this.lastInited) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                errored.clear();
            }

            for(int i = 0; i < ScriptController.Instance.npcScripts.scripts.size(); ++i) {
                script = (ScriptContainer)ScriptController.Instance.npcScripts.scripts.get(i);
                if (!errored.contains(i)) {
                    if(script == null || script.errored || !script.hasCode() || ScriptController.Instance.npcScripts.errored.contains(i))
                        return;

                    script.run(type, event);

                    if (script.errored) {
                        ScriptController.Instance.npcScripts.errored.add(i);
                    }

                    Iterator var8 = script.console.entrySet().iterator();

                    while(var8.hasNext()) {
                        Map.Entry<Long, String> entry = (Map.Entry)var8.next();
                        if (!ScriptController.Instance.npcScripts.console.containsKey(entry.getKey())) {
                            ScriptController.Instance.npcScripts.console.put(entry.getKey(), " tab " + (i + 1) + ":\n" + (String)entry.getValue());
                        }
                    }

                    script.console.clear();
                }
            }

        }
    }

    public boolean isEnabled() {
        return CustomNpcs.GlobalNPCScripts && ScriptController.Instance.npcScripts.enabled && ScriptController.HasStart && (this.npc == null || !this.npc.worldObj.isRemote);
    }
    public boolean isClient() {
        return this.npc.isClientWorld();
    }
    public boolean getEnabled() {
        return ScriptController.Instance.npcScripts.enabled;
    }
    public void setEnabled(boolean bo) {
        ScriptController.Instance.npcScripts.enabled = bo;
        this.enabled = bo;
    }
    public String getLanguage() {
        return ScriptController.Instance.npcScripts.scriptLanguage;
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
