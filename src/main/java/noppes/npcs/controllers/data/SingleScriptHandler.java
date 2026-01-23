package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for script handlers that manage a single script container.
 * Provides shared NBT/ByteBuf serialization and packet handling.
 * <p>
 * Subclasses should:
 * - Store their parent object (e.g., CustomEffect, LinkedItem)
 * - Override getHookContext() to return the appropriate context string
 * - Override getContext() if needed (default is GLOBAL)
 * - Override requestData() and sendSavePacket() for packet communication
 */
public abstract class SingleScriptHandler extends ScriptHandler {
    public IScriptUnit container;

    @Override
    public boolean isSingleContainer() {
        return true;
    }

    protected boolean canRunScripts() {
        return enabled && ScriptController.HasStart && ConfigScript.ScriptingEnabled && container != null;
    }

    @Override
    public void callScript(String hookName, Event event) {
        if (!canRunScripts()) {
            return;
        }
        container.run(hookName, event);
    }

    @Override
    public void setScripts(List<IScriptUnit> list) {
        if (list == null || list.isEmpty()) {
            container = null;
            this.scripts = new ArrayList<>();
        } else {
            container = list.get(0);
            this.scripts = new ArrayList<>(1);
            this.scripts.add(container);
        }
    }

    @Override
    public List<IScriptUnit> getScripts() {
        if (container == null)
            return new ArrayList<>();

        return Collections.singletonList(container);
    }

    public void addScriptUnit(IScriptUnit unit) {
        this.container = unit;
        this.scripts.clear();
        if (unit != null)
            this.scripts.add(unit);
    }

    public void replaceScriptUnit(int index, IScriptUnit unit) {
        this.container = unit;
        this.scripts.clear();
        if (unit != null)
            this.scripts.add(unit);
    }

    public void removeScriptUnit(int index) {
        this.container = null;
        this.scripts.clear();
    }

    /**
     * Write script data to NBT.
     * Uses standard keys: ScriptLanguage, ScriptEnabled, ScriptContent
     */
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("ScriptLanguage", scriptLanguage);
        compound.setBoolean("ScriptEnabled", enabled);

        if (container != null) {
            compound.setTag("ScriptContent", container.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    /**
     * Read script data from NBT.
     * Uses standard keys: ScriptLanguage, ScriptEnabled, ScriptContent
     */
    public void readFromNBT(NBTTagCompound compound) {
        scriptLanguage = compound.getString("ScriptLanguage");
        enabled = compound.getBoolean("ScriptEnabled");

        if (compound.hasKey("ScriptContent", Constants.NBT.TAG_COMPOUND)) {
            container = IScriptUnit.createFromNBT(compound.getCompoundTag("ScriptContent"), this);
        }
    }

    /**
     * Save script data from a ByteBuf packet.
     * Handles the tab/totalScripts protocol used by script packets.
     * <p>
     * Protocol:
     * - tab == 0: Script content NBT follows
     * - tab != 0 (typically -1): Script metadata (language, enabled) follows
     */
    public void saveScript(ByteBuf buffer) throws IOException {
        int tab = buffer.readInt();
        int totalScripts = buffer.readInt();

        if (totalScripts == 0) {
            this.container = null;
        }

        if (tab == 0) {
            // Script content
            NBTTagCompound tabCompound = ByteBufUtils.readNBT(buffer);
            this.container = IScriptUnit.createFromNBT(tabCompound, this);
        } else {
            // Metadata (language, enabled)
            NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
            this.setLanguage(compound.getString("ScriptLanguage"));
            normalizeLanguage();
            this.setEnabled(compound.getBoolean("ScriptEnabled"));
        }
    }

    /**
     * Normalize the language to a valid one if the current language is not available.
     */
    protected void normalizeLanguage() {
        if (!ScriptController.Instance.languages.containsKey(this.getLanguage())) {
            if (!ScriptController.Instance.languages.isEmpty()) {
                this.setLanguage((String) ScriptController.Instance.languages.keySet().toArray()[0]);
            } else {
                this.setLanguage("ECMAScript");
            }
        }
    }
}
