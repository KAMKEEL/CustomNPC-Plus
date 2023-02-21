//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerDataScript;

import java.util.List;

public class GuiScriptPlayers extends GuiScriptInterface {
    private PlayerDataScript script = new PlayerDataScript((EntityPlayer)null);

    public GuiScriptPlayers() {
        this.handler = this.script;
        Client.sendData(EnumPacketServer.ScriptPlayerGet);
    }

    public void setGuiData(NBTTagCompound compound) {
        if (!compound.hasKey("Tab")) {
            script.setLanguage(compound.getString("ScriptLanguage"));
            script.setEnabled(compound.getBoolean("ScriptEnabled"));
            super.setGuiData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            ScriptContainer container = new ScriptContainer(script);
            container.readFromNBT(compound.getCompoundTag("Script"));
            if (script.getScripts().isEmpty()) {
                for (int i = 0; i < compound.getInteger("TotalScripts"); i++) {
                    script.getScripts().add(new ScriptContainer(script));
                }
            }
            script.getScripts().set(tab,container);
            initGui();
        }
    }

    public void save() {
        super.save();
        List<ScriptContainer> containers = this.script.getScripts();
        for (int i = 0; i < containers.size(); i++) {
            ScriptContainer container = containers.get(i);
            Client.sendData(EnumPacketServer.ScriptPlayerSave, i, containers.size(), container.writeToNBT(new NBTTagCompound()));
        }
        NBTTagCompound scriptData = new NBTTagCompound();
        scriptData.setString("ScriptLanguage", this.script.getLanguage());
        scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
        scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
        Client.sendData(EnumPacketServer.ScriptPlayerSave, -1, scriptData);
    }
}
