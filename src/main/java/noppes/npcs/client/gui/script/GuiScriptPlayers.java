//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerDataScript;

public class GuiScriptPlayers extends GuiScriptInterface {
    private PlayerDataScript script = new PlayerDataScript((EntityPlayer)null);

    public GuiScriptPlayers() {
        this.handler = this.script;
        Client.sendData(EnumPacketServer.ScriptPlayerGet, new Object[0]);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.script.readFromNBT(compound);
        super.setGuiData(compound);
    }

    public void save() {
        super.save();
        Client.sendData(EnumPacketServer.ScriptPlayerSave, new Object[]{this.script.writeToNBT(new NBTTagCompound())});
    }

    public void close() {
        super.close();
        ScriptController.Instance.playerScripts.errored.clear();
    }
}
