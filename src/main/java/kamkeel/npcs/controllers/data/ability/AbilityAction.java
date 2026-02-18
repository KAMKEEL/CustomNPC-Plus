package kamkeel.npcs.controllers.data.ability;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Unified slot for storing any combat action: inline ability, ability reference, or chain reference.
 * Replaces {@link AbilitySlot} with support for all {@link IAbilityAction} types.
 * <p>
 * References are lazy-resolved via their respective controllers and cached until
 * the controller revision changes.
 */
public class AbilityAction {

    public enum SlotType {
        INLINE_ABILITY,
        ABILITY_REFERENCE,
        CHAIN_REFERENCE
    }

    private SlotType slotType;
    private Ability inlineAbility;
    private String referenceId;

    /** Per-slot enabled override for reference slots (null = use master's default). */
    private Boolean enabledOverride;

    // Cache
    private transient IAbilityAction cachedAction;
    private transient int cachedRevision = -1;

    private AbilityAction() {}

    // ═══════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════

    public static AbilityAction inline(Ability ability) {
        AbilityAction slot = new AbilityAction();
        slot.slotType = SlotType.INLINE_ABILITY;
        slot.inlineAbility = ability;
        return slot;
    }

    public static AbilityAction abilityReference(String referenceId) {
        AbilityAction slot = new AbilityAction();
        slot.slotType = SlotType.ABILITY_REFERENCE;
        slot.referenceId = referenceId;
        return slot;
    }

    public static AbilityAction chainReference(String referenceId) {
        AbilityAction slot = new AbilityAction();
        slot.slotType = SlotType.CHAIN_REFERENCE;
        slot.referenceId = referenceId;
        return slot;
    }

    // ═══════════════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════════════

    public SlotType getSlotType() {
        return slotType;
    }

    public boolean isReference() {
        return slotType != SlotType.INLINE_ABILITY;
    }

    public boolean isChainReference() {
        return slotType == SlotType.CHAIN_REFERENCE;
    }

    public boolean isAbilityReference() {
        return slotType == SlotType.ABILITY_REFERENCE;
    }

    public String getReferenceId() {
        return referenceId;
    }

    // ═══════════════════════════════════════════════════════════════════
    // RESOLUTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Resolve the action. Inline returns directly; references resolve
     * from the appropriate controller with version-based caching.
     */
    public IAbilityAction getAction() {
        switch (slotType) {
            case INLINE_ABILITY:
                return inlineAbility;

            case ABILITY_REFERENCE: {
                AbilityController ctrl = AbilityController.Instance;
                if (ctrl == null) return null;
                int rev = ctrl.getCustomAbilityRevision();
                if (cachedAction != null && cachedRevision == rev) {
                    return cachedAction;
                }
                Ability resolved = ctrl.resolveAbility(referenceId);
                if (resolved != null && enabledOverride != null) {
                    resolved.setEnabled(enabledOverride);
                }
                cachedAction = resolved;
                cachedRevision = rev;
                return cachedAction;
            }

            case CHAIN_REFERENCE: {
                ChainedAbilityController ctrl = ChainedAbilityController.Instance;
                if (ctrl == null) return null;
                int rev = ctrl.getRevision();
                if (cachedAction != null && cachedRevision == rev) {
                    return cachedAction;
                }
                ChainedAbility resolved = ctrl.resolve(referenceId);
                if (resolved != null && enabledOverride != null) {
                    resolved.setEnabled(enabledOverride);
                }
                cachedAction = resolved;
                cachedRevision = rev;
                return cachedAction;
            }

            default:
                return null;
        }
    }

    /**
     * Get as Ability (returns null if this is a chain slot or resolution fails).
     */
    public Ability getAbility() {
        IAbilityAction action = getAction();
        return (action instanceof Ability) ? (Ability) action : null;
    }

    /**
     * Get as ChainedAbility (returns null if this is an ability slot or resolution fails).
     */
    public ChainedAbility getChainedAbility() {
        IAbilityAction action = getAction();
        return (action instanceof ChainedAbility) ? (ChainedAbility) action : null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ENABLED OVERRIDE
    // ═══════════════════════════════════════════════════════════════════

    public void setEnabled(boolean enabled) {
        if (slotType == SlotType.INLINE_ABILITY) {
            if (inlineAbility != null) {
                inlineAbility.setEnabled(enabled);
            }
        } else {
            this.enabledOverride = enabled;
            if (cachedAction != null) {
                cachedAction.setEnabled(enabled);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INLINE CONVERSION (ability references only)
    // ═══════════════════════════════════════════════════════════════════

    public Ability getInlineAbility() {
        return inlineAbility;
    }

    /**
     * Convert this reference slot to an inline slot by copying the resolved ability.
     * Only works for ability references (chains cannot be inlined).
     */
    public boolean convertToInline() {
        if (slotType != SlotType.ABILITY_REFERENCE) return false;

        Ability resolved = getAbility();
        if (resolved == null) return false;

        inlineAbility = AbilityController.Instance.fromNBT(resolved.writeNBT());
        slotType = SlotType.INLINE_ABILITY;
        referenceId = null;
        cachedAction = null;
        cachedRevision = -1;
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        switch (slotType) {
            case CHAIN_REFERENCE: {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("ChainReference", referenceId);
                if (enabledOverride != null) {
                    nbt.setBoolean("RefEnabled", enabledOverride);
                }
                return nbt;
            }

            case ABILITY_REFERENCE: {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("Reference", referenceId);
                if (enabledOverride != null) {
                    nbt.setBoolean("RefEnabled", enabledOverride);
                }
                return nbt;
            }

            case INLINE_ABILITY:
            default:
                return inlineAbility != null ? inlineAbility.writeNBT() : new NBTTagCompound();
        }
    }

    public static AbilityAction fromNBT(NBTTagCompound nbt) {
        if (nbt == null) return null;

        // Chain reference (new format)
        if (nbt.hasKey("ChainReference")) {
            AbilityAction slot = chainReference(nbt.getString("ChainReference"));
            if (nbt.hasKey("RefEnabled")) {
                slot.enabledOverride = nbt.getBoolean("RefEnabled");
            }
            return slot;
        }

        // Ability reference (existing format)
        if (nbt.hasKey("Reference")) {
            AbilityAction slot = abilityReference(nbt.getString("Reference"));
            if (nbt.hasKey("RefEnabled")) {
                slot.enabledOverride = nbt.getBoolean("RefEnabled");
            }
            return slot;
        }

        // Inline ability (existing format)
        Ability ability = AbilityController.Instance.fromNBT(nbt);
        if (ability == null) return null;
        return inline(ability);
    }
}
