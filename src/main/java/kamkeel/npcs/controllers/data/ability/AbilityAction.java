package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.AbilityController;
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
        CHAIN_REFERENCE,
        INLINE_CHAIN
    }

    private SlotType slotType;
    private Ability inlineAbility;
    private ChainedAbility inlineChain;
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

    public static AbilityAction inlineChain(ChainedAbility chain) {
        AbilityAction slot = new AbilityAction();
        slot.slotType = SlotType.INLINE_CHAIN;
        slot.inlineChain = chain;
        return slot;
    }

    // ═══════════════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════════════

    public SlotType getSlotType() {
        return slotType;
    }

    public boolean isReference() {
        return slotType == SlotType.ABILITY_REFERENCE || slotType == SlotType.CHAIN_REFERENCE;
    }

    public boolean isChainReference() {
        return slotType == SlotType.CHAIN_REFERENCE;
    }

    public boolean isInlineChain() {
        return slotType == SlotType.INLINE_CHAIN;
    }

    public boolean isChain() {
        return slotType == SlotType.CHAIN_REFERENCE || slotType == SlotType.INLINE_CHAIN;
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

            case INLINE_CHAIN:
                return inlineChain;

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
                AbilityController ctrl = AbilityController.Instance;
                if (ctrl == null) return null;
                int rev = ctrl.getChainedAbilityRevision();
                if (cachedAction != null && cachedRevision == rev) {
                    return cachedAction;
                }
                ChainedAbility resolved = ctrl.resolveChainedAbility(referenceId);
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
        } else if (slotType == SlotType.INLINE_CHAIN) {
            if (inlineChain != null) {
                inlineChain.setEnabled(enabled);
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

    public ChainedAbility getInlineChain() {
        return inlineChain;
    }

    /**
     * Convert this reference slot to an inline slot by copying the resolved data.
     * Works for ability references (→ INLINE_ABILITY) and chain references (→ INLINE_CHAIN).
     * Chain entries are kept as-is (not auto-converted to inline).
     */
    public boolean convertToInline() {
        if (slotType == SlotType.ABILITY_REFERENCE) {
            Ability resolved = getAbility();
            if (resolved == null) return false;

            inlineAbility = AbilityController.Instance.fromNBT(resolved.writeNBT());
            slotType = SlotType.INLINE_ABILITY;
            referenceId = null;
            cachedAction = null;
            cachedRevision = -1;
            return true;
        }

        if (slotType == SlotType.CHAIN_REFERENCE) {
            ChainedAbility resolved = getChainedAbility();
            if (resolved == null) return false;

            inlineChain = resolved.deepCopy();
            slotType = SlotType.INLINE_CHAIN;
            referenceId = null;
            cachedAction = null;
            cachedRevision = -1;
            return true;
        }

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        switch (slotType) {
            case INLINE_CHAIN: {
                NBTTagCompound nbt = new NBTTagCompound();
                if (inlineChain != null) {
                    nbt.setTag("InlineChain", inlineChain.writeNBT());
                }
                return nbt;
            }

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

        // Inline chain (new format)
        if (nbt.hasKey("InlineChain")) {
            ChainedAbility chain = new ChainedAbility();
            chain.readNBT(nbt.getCompoundTag("InlineChain"));
            return inlineChain(chain);
        }

        // Chain reference
        if (nbt.hasKey("ChainReference")) {
            AbilityAction slot = chainReference(nbt.getString("ChainReference"));
            if (nbt.hasKey("RefEnabled")) {
                slot.enabledOverride = nbt.getBoolean("RefEnabled");
            }
            return slot;
        }

        // Ability reference
        if (nbt.hasKey("Reference")) {
            AbilityAction slot = abilityReference(nbt.getString("Reference"));
            if (nbt.hasKey("RefEnabled")) {
                slot.enabledOverride = nbt.getBoolean("RefEnabled");
            }
            return slot;
        }

        // Inline ability
        Ability ability = AbilityController.Instance.fromNBT(nbt);
        if (ability == null) return null;
        return inline(ability);
    }
}
