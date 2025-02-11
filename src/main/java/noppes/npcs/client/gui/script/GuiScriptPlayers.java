package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.PlayerScriptPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;


import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.PlayerDataScript;

import java.util.List;

public class GuiScriptPlayers extends GuiScriptInterface {
    private PlayerDataScript script = new PlayerDataScript((EntityPlayer)null);

    public GuiScriptPlayers() {
        hookList.add("init");
        hookList.add("tick");
        hookList.add("interact");
        hookList.add("attack");
        hookList.add("attacked");
        hookList.add("damagedEntity");
        hookList.add("damaged");
        hookList.add("kills");
        hookList.add("killed");
        hookList.add("drop");
        hookList.add("respawn");
        hookList.add("breakBlock");
        hookList.add("chat");
        hookList.add("login");
        hookList.add("logout");
        hookList.add("keyPressed");
        hookList.add("mouseClicked");
        hookList.add("toss");
        hookList.add("pickUp");
        hookList.add("pickupXP");
        hookList.add("rangedCharge");
        hookList.add("rangedLaunched");
        hookList.add("timer");
        hookList.add("startItem");
        hookList.add("usingItem");
        hookList.add("stopItem");
        hookList.add("finishItem");
        hookList.add("containerOpen");
        hookList.add("useHoe");
        hookList.add("bonemeal");
        hookList.add("fillBucket");
        hookList.add("jump");
        hookList.add("fall");
        hookList.add("wakeUp");
        hookList.add("sleep");
        hookList.add("playSound");
        hookList.add("lightning");
        hookList.add("changedDim");
        hookList.add("questStart");
        hookList.add("questCompleted");
        hookList.add("questTurnIn");
        hookList.add("factionPoints");
        hookList.add("dialogOpen");
        hookList.add("dialogOption");
        hookList.add("dialogClose");
        hookList.add("scriptCommand");
        hookList.add("customGuiClosed");
        hookList.add("customGuiButton");
        hookList.add("customGuiSlot");
        hookList.add("customGuiSlotClicked");
        hookList.add("customGuiScroll");
        hookList.add("customGuiTextfield");
        hookList.add("partyQuestCompleted");
        hookList.add("partyQuestSet");
        hookList.add("partyQuestTurnedIn");
        hookList.add("partyInvite");
        hookList.add("partyKick");
        hookList.add("partyLeave");
        hookList.add("partyDisband");
        hookList.add("animationStart");
        hookList.add("animationEnd");
        hookList.add("frameEnter");
        hookList.add("frameExit");

        this.handler = this.script;
        PlayerScriptPacket.Get();
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
            PlayerScriptPacket.Save(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
        }
        NBTTagCompound scriptData = new NBTTagCompound();
        scriptData.setString("ScriptLanguage", this.script.getLanguage());
        scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
        scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
        PlayerScriptPacket.Save(-1, containers.size(), scriptData);
    }
}
