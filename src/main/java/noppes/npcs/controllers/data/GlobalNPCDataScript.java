package noppes.npcs.controllers.data;

import kamkeel.npcs.network.packets.request.script.GlobalNPCScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.util.ScriptToStringHelper;

public class GlobalNPCDataScript extends MultiScriptHandler {
    private EntityNPCInterface npc;
    private ICustomNpc npcAPI;
    private long lastNpcUpdate = -1L;

    public GlobalNPCDataScript(EntityNPCInterface npc) {
        if (npc != null) {
            this.npc = npc;
        }
    }

    @Override
    protected boolean canRunScripts() {
        return isEnabled();
    }

    public boolean isEnabled() {
        return ConfigScript.GlobalNPCScripts && this.enabled && ScriptController.HasStart && this.scripts.size() > 0;
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.NPC;
    }

    @Override
    protected boolean needsReInit() {
        return ScriptController.Instance.lastLoaded > lastInited || ScriptController.Instance.lastGlobalNpcUpdate > lastNpcUpdate;
    }

    @Override
    protected void reInitScripts() {
        super.reInitScripts();
        lastNpcUpdate = ScriptController.Instance.lastGlobalNpcUpdate;
    }

    @Override
    public void requestData() {
        GlobalNPCScriptPacket.Get();
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        GlobalNPCScriptPacket.Save(index, totalCount, nbt);
    }

    @Override
    public boolean isClient() {
        return this.npc != null && this.npc.isClientWorld();
    }

    @Override
    public String noticeString() {
        if (this.npc == null) {
            return "Global script";
        } else {
            BlockPos pos = new BlockPos(this.npc);
            return ScriptToStringHelper.toStringHelper(this.npc).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
        }
    }

    public ICustomNpc getNpc() {
        if (this.npcAPI == null) {
            this.npcAPI = (ICustomNpc) NpcAPI.Instance().getIEntity(this.npc);
        }
        return this.npcAPI;
    }
}
