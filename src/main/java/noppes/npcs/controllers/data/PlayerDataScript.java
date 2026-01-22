package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import kamkeel.npcs.network.packets.request.script.PlayerScriptPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.janino.impl.JaninoPlayerScript;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.player.PlayerEvent;
import noppes.npcs.util.ScriptToStringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

public class PlayerDataScript extends MultiScriptHandler {
    private EntityPlayer player;
    private IPlayer playerAPI;
    private long lastPlayerUpdate = 0L;

    // Static console/errored for IndividualPlayerScripts mode
    private static Map<Long, String> staticConsole = new TreeMap<>();
    private static List<Integer> errored = new ArrayList<>();

    public PlayerDataScript(EntityPlayer player) {
        if (player != null) {
            this.player = player;
        }
    }

    public void clear() {
        super.clear();
        staticConsole = new TreeMap<>();
        errored = new ArrayList<>();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        staticConsole = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(staticConsole));
        return compound;
    }

    public boolean isEnabled() {
        return ConfigScript.GlobalPlayerScripts && ScriptController.Instance.playerScripts.enabled && ScriptController.HasStart && (this.player == null || !this.player.worldObj.isRemote);
    }

    @Override
    protected boolean canRunScripts() {
        return isEnabled();
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.PLAYER;
    }

    @Override
    public void requestData() {
        PlayerScriptPacket.Get();
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        PlayerScriptPacket.Save(index, totalCount, nbt);
    }

    @Override
    public void callScript(String hookName, Event event) {
        if (!isEnabled())
            return;

        if (ConfigScript.IndividualPlayerScripts) {
            if (ScriptController.Instance.lastLoaded > lastInited || ScriptController.Instance.lastPlayerUpdate > lastPlayerUpdate) {
                lastInited = ScriptController.Instance.lastLoaded;
                errored.clear();
                if (player != null) {
                    scripts.clear();
                    for (IScriptUnit script : ScriptController.Instance.playerScripts.scripts) {
                        // Clone the script unit by writing to NBT and reading back with factory
                        NBTTagCompound nbt = script.writeToNBT(new NBTTagCompound());
                        IScriptUnit cloned = IScriptUnit.createFromNBT(nbt, this);
                        scripts.add(cloned);
                    }
                }
                lastPlayerUpdate = ScriptController.Instance.lastPlayerUpdate;
                if (!Objects.equals(hookName, EnumScriptType.INIT.function) && event instanceof PlayerEvent) {
                    PlayerEvent playerEvent = (PlayerEvent) event;
                    EventHooks.onPlayerInit(this, playerEvent.player);
                }
            }
            for (int i = 0; i < scripts.size(); i++) {
                IScriptUnit script = scripts.get(i);
                if (errored.contains(i))
                    continue;
                script.run(hookName, event);
                if (script.hasErrored()) {
                    errored.add(i);
                }
                for (Entry<Long, String> entry : script.getConsole().entrySet()) {
                    if (!staticConsole.containsKey(entry.getKey()))
                        staticConsole.put(entry.getKey(), " tab " + (i + 1) + ":\n" + entry.getValue());
                }
                script.clearConsole();
            }
        } else {
            if (ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastPlayerUpdate > this.lastPlayerUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                this.lastPlayerUpdate = ScriptController.Instance.lastPlayerUpdate;

                for (IScriptUnit script : this.scripts) {
                    if (script instanceof ScriptContainer)
                        ((ScriptContainer) script).errored = false;
                }

                if (!Objects.equals(hookName, EnumScriptType.INIT.function) && event instanceof PlayerEvent) {
                    PlayerEvent playerEvent = (PlayerEvent) event;
                    EventHooks.onPlayerInit(this, playerEvent.player);
                }
            }

            for (IScriptUnit script : this.scripts) {
                if (script == null || script.hasErrored() || !script.hasCode())
                    continue;
                script.run(hookName, event);
            }
        }
    }

    @Override
    public boolean isClient() {
        return this.player != null && this.player.isClientWorld();
    }

    @Override
    public boolean getEnabled() {
        return ScriptController.Instance.playerScripts.enabled;
    }

    @Override
    public void setEnabled(boolean bo) {
        ScriptController.Instance.playerScripts.enabled = bo;
        this.enabled = bo;
    }

    @Override
    public String getLanguage() {
        return ScriptController.Instance.playerScripts.scriptLanguage;
    }

    @Override
    public String noticeString() {
        if (this.player == null) {
            return "Global script";
        }
        BlockPos pos = new BlockPos(this.player);
        return ScriptToStringHelper.toStringHelper(this.player).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
    }

    public IPlayer getPlayer() {
        if (this.playerAPI == null) {
            this.playerAPI = (IPlayer) NpcAPI.Instance().getIEntity(this.player);
        }
        return this.playerAPI;
    }

    @Override
    public Map<Long, String> getConsoleText() {
        if (ConfigScript.IndividualPlayerScripts) {
            return staticConsole;
        }
        return super.getConsoleText();
    }

    @Override
    public void clearConsole() {
        if (ConfigScript.IndividualPlayerScripts) {
            staticConsole.clear();
            return;
        }
        super.clearConsole();
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return new JaninoPlayerScript();
    }
}
