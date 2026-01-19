package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.janino.JaninoScript;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Reusable IScriptHandler implementation for objects that own an optional single JaninoScript unit.
 *
 * This is meant for "function provider" scripts (not EnumScriptType driven). Execution is typically
 * initiated by the owning system calling script.call(...) directly.
 *
 * NBT storage is kept under the parent compound key "Script" (same convention as JaninoScriptable).
 */
public class JaninoScriptHandler<S extends JaninoScript<?>> extends ScriptHandler    {

    protected S script;

    private final Supplier<S> factory;
    private final Class<S> scriptClass;

    public JaninoScriptHandler(Supplier<S> factory, Class<S> scriptClass) {
        this.factory = factory;
        this.scriptClass = scriptClass;
        scriptLanguage = "Java";
    }

    // -------------------- Script ownership (JaninoScriptable) --------------------

    public boolean hasScript() {
        return script != null;
    }

    public S getScript() {
        return script;
    }

    public JaninoScript createScript() {
        if (script == null) 
            script = factory.get();
        
        return script;
    }

    public void deleteScript() {
        unloadScript();
        script = null;
    }

    public void unloadScript() {
        if (script != null) {
            script.unload();
        }
    }

    // -------------------- NBT helpers --------------------

    public void readFromNBT(NBTTagCompound compound) {
        // JaninoScriptable's helper generics assume JaninoScript<T>, but most of our scripts are JaninoScript<?>.
        // Keep it simple and safe: treat it as raw.
        script = (S) JaninoScript.readFromNBT(compound, (JaninoScript) script, (Supplier) factory);
    }

    public void writeToNBT(NBTTagCompound compound) {
        JaninoScript.writeToNBT(compound, (JaninoScript) script);
    }

    // -------------------- IScriptHandler --------------------

    @Override
    public void callScript(String hookName, Event event) {
        // No-op: these scripts are not executed through the global hook dispatcher.
    }

    @Override
    public void callScript(EnumScriptType type, Event event) {
        // No-op
    }

    @Override
    public void setScripts(List<IScriptUnit> list) {
        if (list == null || list.isEmpty() || list.get(0) == null) {
            deleteScript();
            return;
        }

        IScriptUnit unit = list.get(0);
        if (scriptClass.isInstance(unit)) {
            this.script = scriptClass.cast(unit);
            return;
        }

        // Fallback: copy code into a real instance of this script type.
        S target = scriptClass.cast(createScript());
        target.setScript(unit.getScript());
        target.setExternalScripts(unit.getExternalScripts());
    }

    @Override
    public List<IScriptUnit> getScripts() {
        if (script == null) 
            return Collections.emptyList();
        
        return Collections.<IScriptUnit>singletonList(script);
    }

    @Override
    public void addScriptUnit(IScriptUnit unit) {
        setScripts(Collections.singletonList(unit));
    }

    @Override
    public void replaceScriptUnit(int index, IScriptUnit unit) {
        setScripts(Collections.singletonList(unit));
    }

    @Override
    public void removeScriptUnit(int index) {
        deleteScript();
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return factory.get();
    }

    @Override
    public boolean supportsJanino() {
        return true;
    }
}
