package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.AbilityController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A configured instance of an {@link IEffectAction} stored on an ability.
 * Holds the action ID and its configuration NBT.
 */
public class AbilityEffectActionEntry {

    private String actionId = "";
    private NBTTagCompound config = new NBTTagCompound();

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
        copy.config = (NBTTagCompound) this.config.copy();
        return copy;
    }

    /**
     * Apply this action to a target entity.
     */
    public void apply(EntityLivingBase caster, EntityLivingBase target) {
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

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("actionId", actionId);
        nbt.setTag("config", config);
        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        this.actionId = nbt.getString("actionId");
        this.config = nbt.hasKey("config") ? nbt.getCompoundTag("config") : new NBTTagCompound();
    }

    public static AbilityEffectActionEntry fromNBT(NBTTagCompound nbt) {
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

    public NBTTagCompound getConfig() {
        return config;
    }

    public void setConfig(NBTTagCompound config) {
        this.config = config != null ? config : new NBTTagCompound();
    }
}
