package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.PlayerScriptPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.controllers.data.PlayerDataScript;

import java.util.ArrayList;
import java.util.List;

public class GuiScriptPlayers extends GuiScriptInterface {
    private PlayerDataScript script = new PlayerDataScript((EntityPlayer) null);

    public GuiScriptPlayers() {
        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_PLAYER));

        this.handler = this.script;
        PlayerScriptPacket.Get();
    }

    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            loaded = true;
            return;
        }

        if (!compound.hasKey("Tab")) {
            script.setLanguage(compound.getString("ScriptLanguage"));
            script.setEnabled(compound.getBoolean("ScriptEnabled"));
            super.setGuiData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            NBTTagCompound scriptCompound = compound.getCompoundTag("Script");
            IScriptUnit container = IScriptUnit.createFromNBT(scriptCompound, script);
            if (script.getScripts().isEmpty()) {
                for (int i = 0; i < compound.getInteger("TotalScripts"); i++) {
                    script.getScripts().add(new ScriptContainer(script));
                }
            }
            script.getScripts().set(tab, container);
            initGui();
        }
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.PLAYER;
    }

    public void save() {
        if (loaded) {
            super.save();
            List<IScriptUnit> containers = this.script.getScripts();
            for (int i = 0; i < containers.size(); i++) {
                IScriptUnit container = containers.get(i);
                PlayerScriptPacket.Save(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
            }
            NBTTagCompound scriptData = new NBTTagCompound();
            scriptData.setString("ScriptLanguage", this.script.getLanguage());
            scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
            scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
            PlayerScriptPacket.Save(-1, containers.size(), scriptData);
        }
    }
}
