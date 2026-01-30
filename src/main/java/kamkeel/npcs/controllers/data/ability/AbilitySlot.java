package kamkeel.npcs.controllers.data.ability;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Wrapper that stores either an inline ability (full NBT) or a reference
 * to a built-in/custom ability (by name or UUID).
 * <p>
 * References are lazy-resolved via {@link AbilityController#resolveAbility(String)}
 * and cached until the controller version changes.
 */
public class AbilitySlot {

    private Ability inlineAbility;
    private String referenceId;

    private transient Ability cachedAbility;
    private transient int cachedVersion = -1;

    private AbilitySlot() {}

    /**
     * Create a slot with an inline (embedded) ability.
     */
    public static AbilitySlot inline(Ability ability) {
        AbilitySlot slot = new AbilitySlot();
        slot.inlineAbility = ability;
        slot.referenceId = null;
        return slot;
    }

    /**
     * Create a slot that references a built-in ability (by name) or custom ability (by UUID).
     */
    public static AbilitySlot reference(String referenceId) {
        AbilitySlot slot = new AbilitySlot();
        slot.inlineAbility = null;
        slot.referenceId = referenceId;
        return slot;
    }

    public boolean isReference() {
        return referenceId != null;
    }

    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Get the resolved ability. For inline slots, returns the ability directly.
     * For reference slots, lazily resolves from AbilityController and caches.
     * Returns null if the reference cannot be resolved (broken ref).
     */
    public Ability getAbility() {
        if (!isReference()) {
            return inlineAbility;
        }

        // Lazy resolve with version-based cache invalidation
        AbilityController controller = AbilityController.Instance;
        if (controller == null) return null;

        int currentVersion = controller.getVersion();
        if (cachedAbility != null && cachedVersion == currentVersion) {
            return cachedAbility;
        }

        cachedAbility = controller.resolveAbility(referenceId);
        cachedVersion = currentVersion;
        return cachedAbility;
    }

    /**
     * Get the inline ability directly (null for reference slots).
     */
    public Ability getInlineAbility() {
        return inlineAbility;
    }

    /**
     * Convert this reference slot to an inline slot by copying the resolved ability.
     * No-op if already inline. Returns false if resolution fails.
     */
    public boolean convertToInline() {
        if (!isReference()) return true;

        Ability resolved = getAbility();
        if (resolved == null) return false;

        // Deep copy via NBT round-trip
        inlineAbility = AbilityController.Instance.fromNBT(resolved.writeNBT());
        referenceId = null;
        cachedAbility = null;
        cachedVersion = -1;
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        if (isReference()) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Reference", referenceId);
            return nbt;
        }
        return inlineAbility != null ? inlineAbility.writeNBT() : new NBTTagCompound();
    }

    public static AbilitySlot fromNBT(NBTTagCompound nbt) {
        if (nbt == null) return null;

        if (nbt.hasKey("Reference")) {
            return reference(nbt.getString("Reference"));
        }

        // Inline ability
        Ability ability = AbilityController.Instance.fromNBT(nbt);
        if (ability == null) return null;
        return inline(ability);
    }
}
