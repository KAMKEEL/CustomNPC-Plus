package kamkeel.npcs.controllers.data.ability.data.entry;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.data.effect.IEffectAction;
/**
 * A configured instance of an {@link IEffectAction} stored on an ability.
 * Holds the action ID and its IConfiguration NBT.
 */
public class AbilityEffectActionEntry {

    private String actionId = "";
    private INbt config = new INbt();

    public AbilityEffectActionEntry() {
    }

    public AbilityEffectActionEntry(String actionId) {
        this.actionId = actionId != null ? actionId : "";
        IEffectAction action = AbilityController.Instance.getEffectAction(this.actionId);
        if (action != null) {
            this.config = action.createDefaultConfig();
        }
    }

    public AbilityEffectActionEntry copy() {
        AbilityEffectActionEntry copy = new AbilityEffectActionEntry();
        copy.actionId = this.actionId;
        copy.config = (INbt) this.config.copy();
        return copy;
    }

    /**
     * Apply this action to a target IEntity.
     */
    public void apply(IEntityLivingBase caster, IEntityLivingBase target) {
        if (actionId.isEmpty()) return;
        IEffectAction action = AbilityController.Instance.getEffectAction(actionId);
        if (action != null) {
            action.apply(caster, target, config);
        }
    }

    public boolean isValid() {
        return !actionId.isEmpty() && AbilityController.Instance.getEffectAction(actionId) != null;
    }

    // ── NBT ──

    public INbt writeNBT() {
        INbt nbt = new INbt();
        nbt.setString("actionId", actionId);
        nbt.setTag("config", config);
        return nbt;
    }

    public void readNBT(INbt nbt) {
        this.actionId = nbt.getString("actionId");
        this.config = nbt.hasKey("config") ? nbt.getCompoundTag("config") : new INbt();
    }

    public static AbilityEffectActionEntry fromNBT(INbt nbt) {
        AbilityEffectActionEntry entry = new AbilityEffectActionEntry();
        entry.readNBT(nbt);
        return entry;
    }

    // ── Getters/Setters ──

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId != null ? actionId : "";
    }

    public INbt getConfig() {
        return config;
    }

    public void setConfig(INbt config) {
        this.config = config != null ? config : new INbt();
    }
}
