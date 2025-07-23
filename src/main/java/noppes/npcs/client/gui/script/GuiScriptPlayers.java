package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.PlayerScriptPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.EffectScript;
import noppes.npcs.controllers.data.PlayerDataScript;

import java.util.List;

public class GuiScriptPlayers extends GuiScriptInterface {
    private PlayerDataScript script = new PlayerDataScript((EntityPlayer) null);

    public GuiScriptPlayers() {
        hookList.add(EnumScriptType.INIT.function);
        hookList.add(EnumScriptType.TICK.function);
        hookList.add(EnumScriptType.INTERACT.function);
        hookList.add(EnumScriptType.RIGHT_CLICK.function);
        hookList.add(EnumScriptType.ATTACK.function);
        hookList.add(EnumScriptType.ATTACKED.function);
        hookList.add(EnumScriptType.DAMAGED_ENTITY.function);
        hookList.add(EnumScriptType.DAMAGED.function);
        hookList.add(EnumScriptType.KILLS.function);
        hookList.add(EnumScriptType.KILLED.function);
        hookList.add(EnumScriptType.DROP.function);
        hookList.add(EnumScriptType.RESPAWN.function);
        hookList.add(EnumScriptType.BREAK_BLOCK.function);
        hookList.add(EnumScriptType.CHAT.function);
        hookList.add(EnumScriptType.LOGIN.function);
        hookList.add(EnumScriptType.LOGOUT.function);
        hookList.add(EnumScriptType.KEY_PRESSED.function);
        hookList.add(EnumScriptType.MOUSE_CLICKED.function);
        hookList.add(EnumScriptType.TOSS.function);
        hookList.add(EnumScriptType.PICKUP.function);
        hookList.add(EnumScriptType.PICKUP_XP.function);
        hookList.add(EnumScriptType.RANGED_CHARGE.function);
        hookList.add(EnumScriptType.RANGED_LAUNCHED.function);
        hookList.add(EnumScriptType.TIMER.function);
        hookList.add(EnumScriptType.START_USING_ITEM.function);
        hookList.add(EnumScriptType.USING_ITEM.function);
        hookList.add(EnumScriptType.STOP_USING_ITEM.function);
        hookList.add(EnumScriptType.FINISH_USING_ITEM.function);
        hookList.add(EnumScriptType.CONTAINER_OPEN.function);
        hookList.add(EnumScriptType.USE_HOE.function);
        hookList.add(EnumScriptType.BONEMEAL.function);
        hookList.add(EnumScriptType.FILL_BUCKET.function);
        hookList.add(EnumScriptType.JUMP.function);
        hookList.add(EnumScriptType.FALL.function);
        hookList.add(EnumScriptType.WAKE_UP.function);
        hookList.add(EnumScriptType.SLEEP.function);
        hookList.add(EnumScriptType.PLAYSOUND.function);
        hookList.add(EnumScriptType.LIGHTNING.function);
        hookList.add(EnumScriptType.CHANGED_DIM.function);
        hookList.add(EnumScriptType.QUEST_START.function);
        hookList.add(EnumScriptType.QUEST_COMPLETED.function);
        hookList.add(EnumScriptType.QUEST_TURNIN.function);
        hookList.add(EnumScriptType.FACTION_POINTS.function);
        hookList.add(EnumScriptType.DIALOG_OPEN.function);
        hookList.add(EnumScriptType.DIALOG_OPTION.function);
        hookList.add(EnumScriptType.DIALOG_CLOSE.function);
        hookList.add(EnumScriptType.SCRIPT_COMMAND.function);
        hookList.add(EnumScriptType.CUSTOM_GUI_CLOSED.function);
        hookList.add(EnumScriptType.CUSTOM_GUI_BUTTON.function);
        hookList.add(EnumScriptType.CUSTOM_GUI_SLOT.function);
        hookList.add(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function);
        hookList.add(EnumScriptType.CUSTOM_GUI_SCROLL.function);
        hookList.add(EnumScriptType.CUSTOM_GUI_TEXTFIELD.function);
        hookList.add(EnumScriptType.PARTY_QUEST_COMPLETED.function);
        hookList.add(EnumScriptType.PARTY_QUEST_SET.function);
        hookList.add(EnumScriptType.PARTY_QUEST_TURNED_IN.function);
        hookList.add(EnumScriptType.PARTY_INVITE.function);
        hookList.add(EnumScriptType.PARTY_KICK.function);
        hookList.add(EnumScriptType.PARTY_LEAVE.function);
        hookList.add(EnumScriptType.PARTY_DISBAND.function);
        hookList.add(EnumScriptType.ANIMATION_START.function);
        hookList.add(EnumScriptType.ANIMATION_END.function);
        hookList.add(EnumScriptType.ANIMATION_FRAME_ENTER.function);
        hookList.add(EnumScriptType.ANIMATION_FRAME_EXIT.function);
        hookList.add(EnumScriptType.PROFILE_CHANGE.function);
        hookList.add(EnumScriptType.PROFILE_REMOVE.function);
        hookList.add(EnumScriptType.PROFILE_CREATE.function);
        hookList.add(EffectScript.ScriptType.OnEffectAdd.function);
        hookList.add(EffectScript.ScriptType.OnEffectTick.function);
        hookList.add(EffectScript.ScriptType.OnEffectRemove.function);

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
            ScriptContainer container = new ScriptContainer(script);
            container.readFromNBT(compound.getCompoundTag("Script"));
            if (script.getScripts().isEmpty()) {
                for (int i = 0; i < compound.getInteger("TotalScripts"); i++) {
                    script.getScripts().add(new ScriptContainer(script));
                }
            }
            script.getScripts().set(tab, container);
            initGui();
        }
    }

    public void save() {
        if (loaded) {
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
}
