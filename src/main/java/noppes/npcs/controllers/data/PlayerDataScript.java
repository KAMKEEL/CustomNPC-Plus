//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers.data;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.world.WorldServer;
import noppes.npcs.EventHooks;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.NBTTags;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.event.PlayerEvent;
import noppes.npcs.scripted.interfaces.IPlayer;
import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;

import javax.annotation.CheckForNull;
import javax.script.ScriptEngine;

public class PlayerDataScript implements IScriptHandler {
    public List<ScriptContainer> scripts = new ArrayList();
    public String scriptLanguage = "ECMAScript";
    private EntityPlayer player;
    private IPlayer playerAPI;
    private long lastPlayerUpdate = 0L;
    public long lastInited = -1L;
    public boolean hadInteract = true;
    public boolean enabled = false;
    private Map<Long, String> console = new TreeMap();
    public List<Integer> errored = new ArrayList();

    public IPlayer dummyPlayer;
    public IWorld dummyWorld;

    public PlayerDataScript(EntityPlayer player) {
        if(player != null) {
            this.player = player;
            if (player instanceof EntityPlayer)
                dummyPlayer = (IPlayer) ScriptController.Instance.getScriptForEntity(this.player);
            if (player.worldObj instanceof WorldServer)
                dummyWorld = (IWorld) NpcAPI.Instance().getIWorld((WorldServer) this.player.worldObj);
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

    public void callScript(EnumScriptType type, Event event, Object... obs) {
        if(this.isEnabled()) {
            ScriptContainer script;
            if(ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastPlayerUpdate > this.lastPlayerUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                //ScriptController.Instance.playerScripts.errored.clear();
                if(this.player != null) {
                    this.scripts.clear();
                    Iterator i = ScriptController.Instance.playerScripts.scripts.iterator();

                    while(i.hasNext()) {
                        script = (ScriptContainer)i.next();
                        ScriptContainer s = new ScriptContainer(this);
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
                script = (ScriptContainer)this.scripts.get(var7);

                if(!ScriptController.Instance.playerScripts.errored.contains(Integer.valueOf(var7))) {
                    if(script == null || script.errored || !script.hasCode())
                        return;
                    script.setEngine(scriptLanguage);
                    if(script.engine == null)
                        return;
                    for(int i = 0; i + 1 < obs.length; i += 2){
                        Object ob = obs[i + 1];
                        if(ob instanceof Entity)
                            ob = ScriptController.Instance.getScriptForEntity((Entity)ob);
                        script.engine.put(obs[i].toString(), ob);
                    }

                    ScriptEngine engine = script.engine;
                    engine.put("world", dummyWorld);
                    engine.put("player", dummyPlayer);
                    PlayerEvent result = (PlayerEvent) engine.get("event");
                    if(result == null)
                        engine.put("event", result = new PlayerEvent(this.getPlayer()));
                    script.engine.put("API", new WrapperNpcAPI());
                    script.run(type, event);

                    if (script.errored) {
                        ScriptController.Instance.playerScripts.errored.add(var7);
                    }

                    Iterator var8 = script.console.entrySet().iterator();

                    while(var8.hasNext()) {
                        Entry<Long, String> entry = (Entry)var8.next();
                        if (!ScriptController.Instance.playerScripts.console.containsKey(entry.getKey())) {
                            ScriptController.Instance.playerScripts.console.put(entry.getKey(), " tab " + (var7 + 1) + ":\n" + (String)entry.getValue());
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
        ScriptController.Instance.playerScripts.enabled = bo;
        this.enabled = bo;
    }

    public String getLanguage() {
        return ScriptController.Instance.playerScripts.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
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
        return this.console;
    }

    public void clearConsole() {
        this.console.clear();
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
