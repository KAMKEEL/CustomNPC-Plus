package noppes.npcs.controllers.data;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.event.player.PlayerEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EffectScript implements INpcScriptHandler {
    public ScriptContainer container;
    public String scriptLanguage = "ECMAScript";
    public boolean enabled = false;


    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("ScriptLanguage", scriptLanguage);
        compound.setBoolean("ScriptEnabled", enabled);

        if (container != null)
            compound.setTag("ScriptContent", container.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    public EffectScript readFromNBT(NBTTagCompound compound) {
        scriptLanguage = compound.getString("ScriptLanguage");
        enabled = compound.getBoolean("ScriptEnabled");

        if (compound.hasKey("ScriptContent", Constants.NBT.TAG_COMPOUND)) {
            container = new ScriptContainer(this);
            container.readFromNBT(compound.getCompoundTag("ScriptContent"));
        }
        return this;
    }

    public void callScript(ScriptType type, PlayerEvent.EffectEvent event) {
        callScript(type.function, event);
    }

    public boolean isEnabled() {
        return this.enabled && ScriptController.HasStart && container != null && ConfigScript.ScriptingEnabled;
    }

    @Override
    public void callScript(EnumScriptType type, Event event) {
        this.callScript(type.function, event);
    }

    @Override
    public void callScript(String s, Event event) {
        if (!this.isEnabled()) {
            return;
        }
        container.run(s, event);
    }

    @Override
    public boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    @Override
    public boolean getEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    @Override
    public String getLanguage() {
        return this.scriptLanguage;
    }

    @Override
    public void setLanguage(String s) {
        this.scriptLanguage = s;
    }

    @Override
    public void setScripts(List<ScriptContainer> list) {
        if (list == null || list.isEmpty()) {
            container = null;
            return;
        }
        container = list.get(0);
    }

    @Override
    public List<ScriptContainer> getScripts() {
        if (container == null)
            return new ArrayList<>();
        return Collections.singletonList(container);
    }

    @Override
    public String noticeString() {
        return "";
    }

    @Override
    public Map<Long, String> getConsoleText() {
        TreeMap<Long, String> map = new TreeMap();
        int tab = 0;
        Iterator var3 = this.getScripts().iterator();

        while (var3.hasNext()) {
            ScriptContainer script = (ScriptContainer) var3.next();
            ++tab;
            Iterator var5 = script.console.entrySet().iterator();

            while (var5.hasNext()) {
                Map.Entry<Long, String> longStringEntry = (Map.Entry) var5.next();
                map.put(longStringEntry.getKey(), " tab " + tab + ":\n" + (String) longStringEntry.getValue());
            }
        }

        return map;
    }

    @Override
    public void clearConsole() {
        Iterator var1 = this.getScripts().iterator();

        while (var1.hasNext()) {
            ScriptContainer script = (ScriptContainer) var1.next();
            script.console.clear();
        }
    }

    public void saveScript(ByteBuf buffer) throws IOException {
        int tab = buffer.readInt();
        int totalScripts = buffer.readInt();
        if (totalScripts == 0) {
            this.container = null;
        }

        if (tab == 0) {
            NBTTagCompound tabCompound = ByteBufUtils.readNBT(buffer);
            ScriptContainer script = new ScriptContainer(this);
            script.readFromNBT(tabCompound);
            this.container = script;
        } else {
            NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
            this.setLanguage(compound.getString("ScriptLanguage"));
            if (!ScriptController.Instance.languages.containsKey(this.getLanguage())) {
                if (!ScriptController.Instance.languages.isEmpty()) {
                    this.setLanguage((String) ScriptController.Instance.languages.keySet().toArray()[0]);
                } else {
                    this.setLanguage("ECMAScript");
                }
            }
            this.setEnabled(compound.getBoolean("ScriptEnabled"));
        }
    }

    public enum ScriptType {
        OnEffectAdd("onEffectAdd"),
        OnEffectTick("onEffectTick"),
        OnEffectRemove("onEffectRemove");

        public final String function;

        ScriptType(String functionName) {
            this.function = functionName;
        }
    }
}
