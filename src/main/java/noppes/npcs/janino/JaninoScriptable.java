package noppes.npcs.janino;

import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Supplier;

/**

 */
public interface JaninoScriptable<T extends JaninoScript<T>> {

    boolean hasScript();

    JaninoScript<T> getScript();

    JaninoScript<T> createScript();

    void deleteScript();
    
    void unloadScript();

    static <T, S extends JaninoScript<T>> S readFromNBT(NBTTagCompound compound, S script, Supplier<S> factory) {
        if (compound.hasKey("Script")) {
            if (script == null)
                script = factory.get();
            
            script.readFromNBT(compound.getCompoundTag("Script"));
        }
        return script;
    }
    
    static <T> NBTTagCompound writeToNBT(NBTTagCompound compound, JaninoScript<T> script) {
        if (script != null)
            compound.setTag("Script", script.writeToNBT(new NBTTagCompound()));

        return compound;
    }
}
