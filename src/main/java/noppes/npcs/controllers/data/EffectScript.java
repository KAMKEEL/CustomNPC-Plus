package noppes.npcs.controllers.data;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.event.player.PlayerEvent;

import java.io.IOException;

public class EffectScript extends ScriptHandler {


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
            container = IScriptUnit.createFromNBT(compound.getCompoundTag("ScriptContent"), this);
        }
        return this;
    }

    public void callScript(ScriptType type, PlayerEvent.EffectEvent event) {
        callScript(type.function, event);
    }


    public void saveScript(ByteBuf buffer) throws IOException {
        int tab = buffer.readInt();
        int totalScripts = buffer.readInt();
        if (totalScripts == 0) {
            this.container = null;
        }

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
        OnEffectAdd("onEffectAdd"),
        OnEffectTick("onEffectTick"),
        OnEffectRemove("onEffectRemove");

        public final String function;

        ScriptType(String functionName) {
            this.function = functionName;
        }
    }
}