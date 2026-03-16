package noppes.npcs.controllers.data;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptController;
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
        return CustomNpcs.proxy.isGlobalNPCScripts() && this.enabled && ScriptController.HasStart && this.scripts.size() > 0;
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
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.NPC);
    }

    @Override
    public void requestData() {
        GlobalNPCScriptPacket.Get();
    }

    @Override
    public void sendSavePacket(int index, int totalCount, INbt nbt) {
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
