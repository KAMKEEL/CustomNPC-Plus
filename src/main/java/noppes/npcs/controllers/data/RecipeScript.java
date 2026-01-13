package noppes.npcs.controllers.data;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RecipeScript implements IScriptHandler {
    public IScriptUnit container;
    public String scriptLanguage = "ECMAScript";
    public boolean enabled = false;

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("ScriptLanguage", scriptLanguage);
        compound.setBoolean("ScriptEnabled", enabled);
        if (container != null)
            compound.setTag("ScriptContent", container.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    public RecipeScript readFromNBT(NBTTagCompound compound) {
        scriptLanguage = compound.getString("ScriptLanguage");
        enabled = compound.getBoolean("ScriptEnabled");
        if (compound.hasKey("ScriptContent", Constants.NBT.TAG_COMPOUND)) {
            container = IScriptUnit.createFromNBT(compound.getCompoundTag("ScriptContent"), this);
        }
        return this;
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
        if (!this.isEnabled())
            return;
        if (container != null) {
            container.run(s, event);
        }
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
    public void setScripts(List<IScriptUnit> list) {
        if (list == null || list.isEmpty()) {
            container = null;
            return;
        }
        container = list.get(0);
    }

    @Override
    public List<IScriptUnit> getScripts() {
        if (container == null)
            return new ArrayList<>();
        return Collections.singletonList(container);
    }

    @Override
    public String noticeString() {
        return "RecipeScript";
    }

    @Override
    public Map<Long, String> getConsoleText() {
        TreeMap<Long, String> map = new TreeMap<>();
        int tab = 0;
        for (IScriptUnit script : this.getScripts()) {
            ++tab;
            for (Map.Entry<Long, String> entry : script.getConsole().entrySet()) {
                map.put(entry.getKey(), " tab " + tab + ":\n" + entry.getValue());
            }
        }
        return map;
    }

    @Override
    public void clearConsole() {
        for (IScriptUnit script : this.getScripts()) {
            script.clearConsole();
        }
    }

    public void saveScript(ByteBuf buffer) throws IOException {
        int tab = buffer.readInt();
        int totalScripts = buffer.readInt();
        if (totalScripts == 0)
            this.container = null;
        if (tab == 0) {
            NBTTagCompound tabCompound = ByteBufUtils.readNBT(buffer);
            this.container = IScriptUnit.createFromNBT(tabCompound, this);
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
        PRE("pre"),
        POST("post");
        public final String function;

        ScriptType(String functionName) {
            this.function = functionName;
        }
    }
}