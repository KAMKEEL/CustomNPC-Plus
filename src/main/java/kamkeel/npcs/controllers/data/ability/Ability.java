package kamkeel.npcs.controllers.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.DataAbilities;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.AbilityEvent;
import noppes.npcs.scripted.event.player.PlayerAbilityEvent;

import noppes.npcs.client.gui.builder.FieldDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;

public abstract class Ability implements IAbility {

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION (saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    protected String id;
    protected String name;
    protected String typeId;                // e.g., "ability.cnpc.slam" (also used as lang key)

    // Selection
    protected int weight = 10;
    protected boolean enabled = true;
    protected List<Condition> conditions = new ArrayList<>();

    // Targeting
    protected TargetingMode targetingMode = TargetingMode.AGGRO_TARGET;
    protected float minRange = 0;
    protected float maxRange = 20;

    // Timing (ticks)
    protected int cooldownTicks = 0;      // Added to global cooldown after ability completes
    protected int windUpTicks = 20;
    protected boolean syncWindupWithAnimation = true;
    protected int dazedTicks = 80;        // Only used when interrupted during WINDUP (if interruptible)

    // Interruption
    protected boolean interruptible = true;

    // Feedback
    protected LockMovementType lockMovement = LockMovementType.WINDUP;
    protected RotationMode rotationMode = RotationMode.FREE;
    protected LockMovementType rotationPhase = LockMovementType.WINDUP_AND_ACTIVE;
    protected int windUpColor = 0x80FF4400;   // Telegraph color during wind up
    protected int activeColor = 0xC0FF0000;   // Telegraph warning/active color

    // Sounds
    protected String windUpSound = "";        // Sound to play when wind up starts
    protected String activeSound = "";        // Sound to play when active phase starts

    // Animations (global animation IDs, -1 = none)
    protected int windUpAnimationId = -1;     // Animation to play during wind up (user animations)
    protected int activeAnimationId = -1;     // Animation to play during active phase (user animations)

    // Built-in animation names (empty = none, takes priority over IDs if set)
    protected String windUpAnimationName = "";   // Built-in animation name for wind up
    protected String activeAnimationName = "";   // Built-in animation name for active phase

    // Telegraph configuration
    protected boolean showTelegraph = true;
    protected TelegraphType telegraphType = TelegraphType.CIRCLE;
    protected float telegraphHeightOffset = 0.1f;

    // Custom data for external mods
    protected NBTTagCompound customData = new NBTTagCompound();

    // User type restriction
    protected UserType allowedBy = UserType.BOTH;

    // Cooldown override
    protected boolean ignoreCooldown = false;

    // Configurable potion effects
    protected List<AbilityEffect> effects = new ArrayList<>();

    // Burst
    protected boolean burstEnabled = false;
    protected int burstAmount = 0;             // number of EXTRA repetitions
    protected int burstDelay = 0;              // ticks between bursts
    protected boolean burstReplayAnimations = true;
    protected boolean burstOverlap = false;    // if true, don't wait for entity death between bursts

    // ═══════════════════════════════════════════════════════════════════
    // EXECUTION STATE (not saved, reset each combat)
    // ═══════════════════════════════════════════════════════════════════

    protected transient AbilityPhase phase = AbilityPhase.IDLE;
    protected transient int currentTick = 0;
    protected transient EntityLivingBase currentTarget;
    protected transient long executionStartTime;
    protected transient List<TelegraphInstance> telegraphInstances = new ArrayList<>();

    // Burst execution state
    protected transient int burstIndex = 0;
    protected transient List<Entity> burstEntities = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════
    // PREVIEW STATE (client-side only, not saved)
    // ═══════════════════════════════════════════════════════════════════

    protected transient boolean previewMode = false;
    protected transient PreviewEntityHandler previewEntityHandler;

    // ═══════════════════════════════════════════════════════════════════
    // GUI CONTEXT (client-side only, not saved)
    // ═══════════════════════════════════════════════════════════════════

    protected transient boolean npcInlineEdit = false;

    // ═══════════════════════════════════════════════════════════════════
    // ABSTRACT METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Called first tick of ACTIVE phase
     */
    public abstract void onExecute(EntityLivingBase caster, EntityLivingBase target, World world);

    /**
     * Called every tick of ACTIVE phase
     */
    public abstract void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick);

    /**
     * Write type-specific config to NBT
     */
    public abstract void writeTypeNBT(NBTTagCompound nbt);

    /**
     * Read type-specific config from NBT
     */
    public abstract void readTypeNBT(NBTTagCompound nbt);

    // ═══════════════════════════════════════════════════════════════════
    // OPTIONAL OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns true if this ability type deals damage.
     * Override in non-damaging subclasses to return false.
     * Used by external mods to determine whether to show
     * damage-related configuration (e.g., DBC stats tab).
     */
    public boolean hasDamage() {
        return true;
    }

    /**
     * Override to provide variant templates for this ability type.
     * When a user creates this ability type, they will be shown a selection dialog
     * with the returned variants. If empty or single-entry, the dialog is skipped.
     */
    public List<AbilityVariant> getVariants() {
        return Collections.emptyList();
    }

    /**
     * Whether this ability type supports the burst system.
     * Override to return false for abilities that shouldn't repeat (Guard, Heal, Sweeper, etc.).
     */
    public boolean allowBurst() {
        return true;
    }

    /**
     * Whether this ability supports overlap mode for burst (entities from previous bursts keep flying).
     * Override to return true for entity-spawning projectile abilities (Orb, Beam, Disc, LaserShot).
     */
    public boolean allowOverlap() {
        return false;
    }

    /**
     * Whether this ability is ready for auto-completion during burst overlap mode.
     * Override to delay auto-completion until all staggered projectiles have been fired.
     * @param activeTick the current tick within the ACTIVE phase
     */
    public boolean isReadyForBurstCompletion(int activeTick) {
        return true;
    }

    /**
     * Called when burst re-fires without replay animations (burstReplayAnimations=false).
     * Entity-spawning abilities should override to spawn and fire entities in one step,
     * since onWindUpTick was not called (windup was skipped).
     * Default: does nothing (ability goes directly to onExecute with existing state).
     */
    public void onBurstRefire(EntityLivingBase caster, EntityLivingBase target, World world) {
        // Override in entity-spawning abilities (Orb, Beam, Disc, LaserShot)
    }

    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
    }

    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
    }

    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
    }

    public void onDamageTaken(EntityLivingBase caster, EntityLivingBase attacker, DamageSource source, float damage) {
    }

    /**
     * Apply damage to an entity with ability hit event support.
     * Fires the abilityHit script event, allowing scripts to modify or cancel the damage.
     *
     * @param caster    The entity executing the ability (NPC or Player)
     * @param hitEntity The entity being hit
     * @param damage    The damage amount
     * @param knockback The horizontal knockback
     * @return true if damage was applied (not cancelled), false if cancelled
     */
    protected boolean applyAbilityDamage(EntityLivingBase caster, EntityLivingBase hitEntity,
                                         float damage, float knockback) {
        double dx = hitEntity.posX - caster.posX;
        double dz = hitEntity.posZ - caster.posZ;
        return applyAbilityDamageInternal(caster, hitEntity, damage, knockback, 0.0f, dx, dz);
    }

    /**
     * Apply damage to an entity with ability hit event support and vertical knockback.
     * Fires the abilityHit script event, allowing scripts to modify or cancel the damage.
     *
     * @param caster      The entity executing the ability (NPC or Player)
     * @param hitEntity   The entity being hit
     * @param damage      The damage amount
     * @param knockback   The horizontal knockback
     * @param knockbackUp The vertical knockback
     * @return true if damage was applied (not cancelled), false if cancelled
     */
    protected boolean applyAbilityDamage(EntityLivingBase caster, EntityLivingBase hitEntity,
                                         float damage, float knockback, float knockbackUp) {
        double dx = hitEntity.posX - caster.posX;
        double dz = hitEntity.posZ - caster.posZ;
        return applyAbilityDamageInternal(caster, hitEntity, damage, knockback, knockbackUp, dx, dz);
    }

    /**
     * Apply damage to an entity with ability hit event support and custom knockback direction.
     * Fires the abilityHit script event, allowing scripts to modify or cancel the damage.
     *
     * @param caster        The entity executing the ability (NPC or Player)
     * @param hitEntity     The entity being hit
     * @param damage        The damage amount
     * @param knockback     The horizontal knockback
     * @param knockbackDirX The X component of knockback direction
     * @param knockbackDirZ The Z component of knockback direction
     * @return true if damage was applied (not cancelled), false if cancelled
     */
    protected boolean applyAbilityDamageWithDirection(EntityLivingBase caster, EntityLivingBase hitEntity,
                                                      float damage, float knockback,
                                                      double knockbackDirX, double knockbackDirZ) {
        return applyAbilityDamageInternal(caster, hitEntity, damage, knockback, 0.0f, knockbackDirX, knockbackDirZ);
    }

    /**
     * Internal unified damage application method.
     * All public damage methods delegate to this.
     * Supports both NPC and Player casters.
     */
    private boolean applyAbilityDamageInternal(EntityLivingBase caster, EntityLivingBase hitEntity,
                                               float damage, float knockback, float knockbackUp,
                                               double knockbackDirX, double knockbackDirZ) {
        // Fire hit event for NPC casters (NPC event system)
        if (caster instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) caster;
            DataAbilities dataAbilities = npc.abilities;
            AbilityEvent.HitEvent event = dataAbilities.fireHitEvent(
                this, currentTarget, hitEntity, damage, knockback, knockbackUp);

            if (event == null) {
                return false; // Cancelled
            }

            damage = event.getDamage();
            knockback = event.getKnockback();
            knockbackUp = event.getKnockbackUp();
        }
        // Fire hit event for Player casters (Player event system)
        else if (caster instanceof EntityPlayer && ScriptController.Instance != null) {
            EntityPlayer player = (EntityPlayer) caster;
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            if (handler != null) {
                IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
                PlayerAbilityEvent.HitEvent event = new PlayerAbilityEvent.HitEvent(
                    iPlayer, this, currentTarget, hitEntity, damage, knockback, knockbackUp);

                if (noppes.npcs.EventHooks.onPlayerAbilityHit(handler, event)) {
                    return false; // Cancelled
                }

                damage = event.getDamage();
                knockback = event.getKnockback();
                knockbackUp = event.getKnockbackUp();
            }
        }

        // Apply damage
        if (damage > 0) {
            // Check for external damage handler first (e.g., DBC Addon)
            IAbilityDamageHandler handler = AbilityController.Instance.getDamageHandler();
            boolean handled = false;
            if (handler != null) {
                handled = handler.handleDamage(this, caster, hitEntity, damage, knockback, knockbackUp,
                                               knockbackDirX, knockbackDirZ);
            }
            if (!handled) {
                // Default damage path
                if (caster instanceof EntityNPCInterface) {
                    hitEntity.attackEntityFrom(new NpcDamageSource("mob", (EntityNPCInterface) caster), damage);
                } else if (caster instanceof EntityPlayer) {
                    hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) caster), damage);
                } else {
                    hitEntity.attackEntityFrom(DamageSource.causeMobDamage(caster), damage);
                }
            }
        }

        // Apply knockback if any
        if (knockback > 0 || knockbackUp > 0) {
            applyKnockback(hitEntity, knockbackDirX, knockbackDirZ, knockback, knockbackUp);
        }

        return true;
    }

    private void applyKnockback(EntityLivingBase hitEntity, double dirX, double dirZ, float knockback, float knockbackUp) {
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double x = 0;
        double z = 0;
        if (len > 0) {
            x = (dirX / len) * knockback * 0.5;
            z = (dirZ / len) * knockback * 0.5;
        }
        double y = knockbackUp > 0 ? knockbackUp : 0.1D;
        hitEntity.addVelocity(x, y, z);
        hitEntity.velocityChanged = true;
    }

    /**
     * Applies all configured effects to the given entity.
     * Call this after damage is applied to the target.
     *
     * @param entity The entity to apply effects to
     */
    protected void applyEffects(EntityLivingBase entity) {
        if (entity == null || effects.isEmpty()) {
            return;
        }
        for (AbilityEffect effect : effects) {
            effect.apply(entity);
        }
    }

    public float getTelegraphRadius() {
        return 5.0f;
    }

    public float getTelegraphLength() {
        return 5.0f;
    }

    public float getTelegraphWidth() {
        return 2.0f;
    }

    public float getTelegraphAngle() {
        return 45.0f;
    }

    /**
     * Returns true if the caster is a player (not an NPC).
     * Use this instead of checking {@code target != null} to make NPC/Player
     * code paths explicit and readable.
     * <p>
     * NPCs always have an aggro target passed to ability methods.
     * Players never do — they use look direction for aiming instead.
     */
    protected final boolean isPlayerCaster(EntityLivingBase caster) {
        return caster instanceof EntityPlayer;
    }

    /**
     * Whether the telegraph should remain visible during the ACTIVE phase.
     * Default: false (telegraph removed at WINDUP→ACTIVE transition).
     * Override for abilities where the active phase IS the attack (e.g., sweeping/spinning attacks)
     * so the telegraph stays visible while damage is being dealt.
     */
    public boolean keepTelegraphDuringActive() {
        return false;
    }

    /**
     * Returns true if this ability controls NPC movement during ACTIVE phase.
     * When true, AI pathfinding will be blocked during ability execution.
     * Override in abilities that move the NPC (Charge, Dash, Slam, etc.)
     */
    public boolean hasAbilityMovement() {
        return false;
    }

    /**
     * Returns true if the targeting mode for this ability type is locked and cannot be changed.
     * Most abilities have fixed targeting (e.g., Heal is always SELF, Charge is always AGGRO_TARGET).
     * Override and return false for abilities with flexible targeting.
     */
    public boolean isTargetingModeLocked() {
        return true;
    }

    /**
     * Returns the allowed targeting modes for this ability, or null if all modes are allowed.
     * Override in abilities that only support specific targeting modes.
     */
    public TargetingMode[] getAllowedTargetingModes() {
        return null;
    }

    /**
     * Returns true if the telegraph type for this ability is locked and cannot be changed.
     * Telegraph type is inherent to how the ability works (e.g., Charge uses LINE, Slam uses CIRCLE).
     */
    public boolean isTelegraphTypeLocked() {
        return true;
    }

    /**
     * Add field definitions for this ability's GUI.
     * Override in subclasses to add type-specific fields. The list already contains
     * the base fields (General, Target, Effects tabs), so subclasses can also
     * modify, insert into, or remove base fields using {@link FieldDef#insertBefore},
     * {@link FieldDef#insertAfter}, or direct list manipulation.
     * <p>
     * Fields added here without an explicit {@code .tab()} will default to the "Type" tab.
     *
     * @param defs The mutable list of field definitions to add to or modify
     */
    @SideOnly(Side.CLIENT)
    public void getAbilityDefinitions(List<FieldDef> defs) {
    }

    /**
     * Builds and returns all field definitions for this ability's GUI.
     * Base fields (General, Target, Effects) are added first, then
     * {@link #getAbilityDefinitions(List)} is called so subclasses can add
     * type-specific fields and modify any existing fields.
     */
    @SideOnly(Side.CLIENT)
    public final List<FieldDef> getAllDefinitions() {
        List<FieldDef> defs = new ArrayList<>();

        // ── General tab ──────────────────────────────────────────────
        defs.add(FieldDef.stringField("gui.name", this::getName, this::setName)
            .tab("General"));
        defs.add(FieldDef.row(
            FieldDef.intField("ability.weight", this::getWeight, this::setWeight).range(1, 1000),
            FieldDef.boolField("gui.enabled", this::isEnabled, this::setEnabled)
        ).tab("General"));
        defs.add(FieldDef.section("ability.section.timing").tab("General"));
        defs.add(FieldDef.intField("ability.windUpTicks", this::getRawWindUpTicks, this::setWindUpTicks)
            .tab("General").range(0, 1000)
            .visibleWhen(() -> !hasWindUpAnimation()));
        defs.add(FieldDef.row(
            FieldDef.intField("ability.windUpTicks", this::getRawWindUpTicks, this::setWindUpTicks).range(0, 1000),
            FieldDef.boolField("ability.syncWindup", this::isSyncWindupWithAnimation, this::setSyncWindupWithAnimation)
                .hover("ability.hover.sync")
        ).tab("General").visibleWhen(() -> hasWindUpAnimation() && !isSyncWindupWithAnimation()));
        defs.add(FieldDef.row(
            FieldDef.labelField("ability.windUpTicks", () -> getWindUpTicks() + "t"),
            FieldDef.boolField("ability.syncWindup", this::isSyncWindupWithAnimation, this::setSyncWindupWithAnimation)
                .hover("ability.hover.sync")
        ).tab("General").visibleWhen(() -> hasWindUpAnimation() && isSyncWindupWithAnimation()));
        defs.add(FieldDef.intField("ability.cooldownTicks", this::getCooldownTicks, this::setCooldownTicks)
            .tab("General").range(0, 10000));
        defs.add(FieldDef.section("ability.section.movement").tab("General"));
        defs.add(FieldDef.stringEnumField("ability.lockMovement", LockMovementType.getDisplayKeys(),
                () -> this.getLockMovement().getDisplayKey(),
                v -> {
                    for (LockMovementType t : LockMovementType.values()) {
                        if (t.getDisplayKey().equals(v)) { this.setLockMovement(t); break; }
                    }
                })
                .hover("ability.hover.lockMovement")
                .tab("General"));
        defs.add(FieldDef.row(
            FieldDef.stringEnumField("ability.rotationMode", RotationMode.getDisplayKeys(),
                () -> this.getRotationMode().getDisplayKey(),
                v -> {
                    for (RotationMode m : RotationMode.values()) {
                        if (m.getDisplayKey().equals(v)) { this.setRotationMode(m); break; }
                    }
                })
                .hover("ability.hover.rotationMode"),
            FieldDef.stringEnumField("ability.rotationPhase", getRotationPhaseKeys(),
                () -> this.getRotationPhase().getDisplayKey(),
                v -> {
                    for (LockMovementType t : LockMovementType.values()) {
                        if (t.getDisplayKey().equals(v)) { this.setRotationPhase(t); break; }
                    }
                })
                .hover("ability.hover.rotationPhase")
                .visibleWhen(() -> this.rotationMode != RotationMode.FREE)
        ).tab("General"));
        defs.add(FieldDef.row(
            FieldDef.boolField("ability.interruptible", this::isInterruptible, this::setInterruptible)
                .hover("ability.hover.interruptible"),
            FieldDef.intField("ability.dazedTicks", this::getDazedTicks, this::setDazedTicks)
                .range(0, 1000).visibleWhen(this::isInterruptible)
        ).tab("General"));

        // ── Burst section ────────────────────────────────────────────
        if (allowBurst()) {
            defs.add(FieldDef.section("ability.section.burst").tab("General"));
            defs.add(FieldDef.boolField("ability.burstEnabled", this::isBurstEnabled, this::setBurstEnabled)
                .tab("General"));
            defs.add(FieldDef.row(
                FieldDef.intField("ability.burstAmount", this::getBurstAmount, this::setBurstAmount).range(1, 100),
                FieldDef.intField("ability.burstDelay", this::getBurstDelay, this::setBurstDelay).range(0, 1000)
            ).tab("General").visibleWhen(this::isBurstEnabled));
            defs.add(FieldDef.boolField("ability.burstReplayAnimations", this::isBurstReplayAnimations, this::setBurstReplayAnimations)
                .tab("General").visibleWhen(this::isBurstEnabled)
                .hover("ability.hover.burstReplay"));
            if (allowOverlap()) {
                defs.add(FieldDef.boolField("ability.burstOverlap", this::isBurstOverlap, this::setBurstOverlap)
                    .tab("General").visibleWhen(this::isBurstEnabled)
                    .hover("ability.hover.burstOverlap"));
            }
        }

        // ── Target tab ───────────────────────────────────────────────
        defs.add(FieldDef.row(
            FieldDef.intField("ability.minRange", () -> (int) getMinRange(), v -> setMinRange(v)).range(0, 100),
            FieldDef.intField("ability.maxRange", () -> (int) getMaxRange(), v -> setMaxRange(v)).range(1, 100)
        ).tab("Target"));
        if (!isTargetingModeLocked()) {
            TargetingMode[] allowed = getAllowedTargetingModes();
            if (allowed != null && allowed.length > 0) {
                String[] allowedKeys = new String[allowed.length];
                for (int i = 0; i < allowed.length; i++) {
                    allowedKeys[i] = allowed[i].name();
                }
                defs.add(FieldDef.stringEnumField("ability.targetingMode", allowedKeys,
                    () -> this.getTargetingMode().name(),
                    v -> {
                        try { this.setTargetingMode(TargetingMode.valueOf(v)); }
                        catch (Exception ignored) {}
                    })
                    .tab("Target").hover("ability.hover.targeting"));
            } else {
                defs.add(FieldDef.enumField("ability.targetingMode", TargetingMode.class,
                    this::getTargetingMode, this::setTargetingMode)
                    .tab("Target").hover("ability.hover.targeting"));
            }
        }

        // ── Effects tab ──────────────────────────────────────────────
        defs.add(FieldDef.section("ability.section.sounds").tab("Effects"));
        defs.add(FieldDef.soundSubGui("ability.windUpSound", this::getWindUpSound, this::setWindUpSound)
            .tab("Effects"));
        defs.add(FieldDef.soundSubGui("ability.activeSound", this::getActiveSound, this::setActiveSound)
            .tab("Effects"));
        defs.add(FieldDef.section("ability.section.animations").tab("Effects"));
        defs.add(FieldDef.animSubGui("ability.windUpAnimation",
            this::getWindUpAnimationId, this::setWindUpAnimationId,
            this::getWindUpAnimationName, this::setWindUpAnimationName)
            .tab("Effects"));
        defs.add(FieldDef.animSubGui("ability.activeAnimation",
            this::getActiveAnimationId, this::setActiveAnimationId,
            this::getActiveAnimationName, this::setActiveAnimationName)
            .tab("Effects"));

        TelegraphType tType = getTelegraphType();
        if (tType != null && tType != TelegraphType.NONE) {
            defs.add(FieldDef.section("ability.section.telegraph").tab("Effects")
                .hover("telegraph." + tType.name().toLowerCase()));
            defs.add(FieldDef.boolField("ability.showTelegraph", this::isShowTelegraph, this::setShowTelegraph)
                .tab("Effects").hover("ability.hover.showTelegraph"));
            defs.add(FieldDef.colorSubGui("ability.windUpColor", this::getWindUpColor, this::setWindUpColor)
                .tab("Effects").visibleWhen(this::isShowTelegraph));
            defs.add(FieldDef.colorSubGui("ability.activeColor", this::getActiveColor, this::setActiveColor)
                .tab("Effects").visibleWhen(this::isShowTelegraph));
        }

        // ── Type-specific fields ─────────────────────────────────────
        int baseSize = defs.size();
        getAbilityDefinitions(defs);
        // Default tab for ability-added fields that don't specify one
        for (int i = baseSize; i < defs.size(); i++) {
            if (defs.get(i).getTab() == null) {
                defs.get(i).tab("Type");
            }
        }

        // External field providers (e.g., DBC Addon injecting a "DBC" tab)
        for (IAbilityFieldProvider provider : AbilityController.Instance.getFieldProviders()) {
            provider.addFieldDefinitions(this, defs);
        }

        return defs;
    }

    /**
     * Creates the GUI for configuring this ability.
     * Override this method to provide a custom GUI subclass for type-specific settings.
     * <p>
     * Third-party mods can extend SubGuiAbilityConfig and override initTypeTab(),
     * handleTypeButton(), and handleTypeTextField() to provide custom type settings.
     *
     * @param callback The callback to notify when the ability is saved
     * @return A SubGuiAbilityConfig instance (or subclass) for configuring this ability
     */
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityConfig(this, callback);
    }

    /**
     * Create a telegraph instance for this ability.
     * Override for custom telegraph shapes.
     *
     * @param caster    The caster
     * @param target The target (for position calculation)
     * @return The telegraph instance, or null if no telegraph
     */
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE) {
            return null;
        }

        Telegraph telegraph;
        double x, y, z;
        float yaw = caster.rotationYaw;

        // Determine position based on targeting mode
        boolean positionAtCaster = targetingMode == TargetingMode.AOE_SELF ||
            targetingMode == TargetingMode.SELF ||
            telegraphType == TelegraphType.LINE ||
            telegraphType == TelegraphType.CONE;

        if (positionAtCaster) {
            x = caster.posX;
            y = findGroundLevel(caster.worldObj, caster.posX, caster.posY, caster.posZ);
            z = caster.posZ;
        } else if (target != null) {
            x = target.posX;
            y = findGroundLevel(caster.worldObj, target.posX, target.posY, target.posZ);
            z = target.posZ;
        } else {
            x = caster.posX;
            y = findGroundLevel(caster.worldObj, caster.posX, caster.posY, caster.posZ);
            z = caster.posZ;
        }

        // Create telegraph based on type
        switch (telegraphType) {
            case CIRCLE:
                telegraph = Telegraph.circle(getTelegraphRadius());
                break;
            case RING:
                telegraph = Telegraph.ring(getTelegraphRadius(), getTelegraphRadius() * 0.5f);
                break;
            case LINE:
                telegraph = Telegraph.line(getTelegraphLength(), getTelegraphWidth());
                break;
            case CONE:
                telegraph = Telegraph.cone(getTelegraphLength(), getTelegraphAngle());
                break;
            case POINT:
                telegraph = new Telegraph("", TelegraphType.POINT);
                break;
            case SQUARE:
                telegraph = Telegraph.square(getTelegraphRadius());
                break;
            default:
                return null;
        }

        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        instance.setCasterEntityId(caster.getEntityId());

        // Set entity to follow based on targeting mode
        if (positionAtCaster) {
            // AOE_SELF abilities: telegraph follows caster during windup
            instance.setEntityIdToFollow(caster.getEntityId());

            // For LINE/CONE telegraphs: track target direction during windup
            // Only track if rotation is NOT locked during windup (NPC can still turn)
            if ((telegraphType == TelegraphType.LINE || telegraphType == TelegraphType.CONE) && target != null) {
                if (!isRotationLockedDuringWindup()) {
                    instance.setTargetEntityId(target.getEntityId());
                }
                // If rotation is locked, yaw stays fixed at creation time
            }
        } else if (target != null) {
            // AOE_TARGET abilities: telegraph follows target during windup
            instance.setEntityIdToFollow(target.getEntityId());
        }

        return instance;
    }

    /**
     * Create all telegraph instances for this ability.
     * Override for abilities that need multiple telegraphs (e.g., zone abilities).
     * Default wraps createTelegraph() in a singleton list.
     */
    public List<TelegraphInstance> createTelegraphs(EntityLivingBase caster, EntityLivingBase target) {
        TelegraphInstance instance = createTelegraph(caster, target);
        if (instance == null) return new ArrayList<>();
        return new ArrayList<>(Collections.singletonList(instance));
    }

    /**
     * Find the ground level at a given position.
     * Searches downward from the given Y to find a solid block.
     *
     * @param world  The world
     * @param x      X coordinate
     * @param startY Starting Y coordinate (entity feet position)
     * @param z      Z coordinate
     * @return The Y coordinate of the ground surface
     */
    public static double findGroundLevel(World world, double x, double startY, double z) {
        if (world == null) return startY;

        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int startBlockY = (int) Math.floor(startY);

        // Search downward for solid ground (max 10 blocks down)
        for (int checkY = startBlockY; checkY >= startBlockY - 10 && checkY >= 0; checkY--) {
            Block block = world.getBlock(blockX, checkY, blockZ);
            if (block != null && block.getMaterial().isSolid()) {
                // Found solid block, telegraph goes on top of it
                return checkY + 1;
            }
        }

        // No ground found, use original position
        return startY;
    }

    /**
     * Calculates offset position near the given coordinates.
     * Used to place effects near a target rather than exactly on them.
     *
     * @param baseX        Base X coordinate
     * @param baseY        Base Y coordinate
     * @param baseZ        Base Z coordinate
     * @param minOffset    Minimum offset distance
     * @param maxOffset    Maximum offset distance
     * @param randomOffset Whether to use random offset within range, or fixed at max
     * @param random       Random instance to use
     * @return Array of [x, y, z] with offset applied
     */
    public static double[] calculateOffsetPosition(double baseX, double baseY, double baseZ,
                                                   float minOffset, float maxOffset,
                                                   boolean randomOffset, java.util.Random random) {
        if (maxOffset <= 0) {
            return new double[]{baseX, baseY, baseZ};
        }

        double offsetDist;
        double offsetAngle;

        if (randomOffset) {
            offsetDist = minOffset + random.nextDouble() * (maxOffset - minOffset);
            offsetAngle = random.nextDouble() * Math.PI * 2;
        } else {
            offsetDist = maxOffset;
            offsetAngle = random.nextDouble() * Math.PI * 2;
        }

        double offsetX = Math.cos(offsetAngle) * offsetDist;
        double offsetZ = Math.sin(offsetAngle) * offsetDist;

        return new double[]{baseX + offsetX, baseY, baseZ + offsetZ};
    }

    /**
     * Get the current telegraph instances.
     */
    public List<TelegraphInstance> getTelegraphInstances() {
        return telegraphInstances;
    }

    /**
     * Set the telegraph instances (called by DataAbilities).
     */
    public void setTelegraphInstances(List<TelegraphInstance> instances) {
        this.telegraphInstances = instances != null ? instances : new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════════════
    // EXECUTION LOGIC (called by CombatHandler)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Start executing this ability.
     * If windUpTicks is 0, skips WINDUP and goes directly to ACTIVE.
     */
    public void start(EntityLivingBase target) {
        this.currentTick = 0;
        this.currentTarget = target;
        this.executionStartTime = System.currentTimeMillis();
        this.burstIndex = 0;
        this.burstEntities.clear();

        if (windUpTicks <= 0) {
            this.phase = AbilityPhase.ACTIVE;
        } else {
            this.phase = AbilityPhase.WINDUP;
        }
    }

    /**
     * Tick the ability forward. Called every game tick while executing.
     * <p>
     * Normal flow: WINDUP -> ACTIVE -> IDLE
     * Interrupted flow: WINDUP -> DAZED -> IDLE (dazed only on interrupt during WINDUP)
     *
     * @return true if phase changed this tick
     */
    public boolean tick() {
        if (phase == AbilityPhase.IDLE) return false;

        currentTick++;

        switch (phase) {
            case WINDUP:
                if (currentTick >= windUpTicks) {
                    phase = AbilityPhase.ACTIVE;
                    currentTick = 0;
                    return true;
                }
                break;
            case ACTIVE:
                // Abilities control their own completion by calling signalCompletion()
                // No auto-complete - each ability determines when it's done
                break;
            case DAZED:
                // Dazed phase is only entered on interrupt during WINDUP
                // NPC cannot attack during this time
                if (currentTick >= dazedTicks) {
                    phase = AbilityPhase.IDLE;
                    currentTick = 0;
                    return true;
                }
                break;

            case BURST_DELAY:
                // Waiting between burst repetitions - free movement/rotation
                if (currentTick >= burstDelay) {
                    if (burstReplayAnimations && getWindUpTicks() > 0) {
                        phase = AbilityPhase.WINDUP;
                    } else {
                        phase = AbilityPhase.ACTIVE;
                    }
                    currentTick = 0;
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Signal that the ability has completed its active phase.
     * Called by abilities when they are done.
     * For example, AbilitySlam calls this when the NPC lands.
     *
     * @return true if phase changed to IDLE
     */
    public boolean signalCompletion() {
        if (phase == AbilityPhase.ACTIVE) {
            // Check if more burst iterations remain
            if (burstEnabled && burstAmount > 0 && burstIndex < burstAmount) {
                if (!burstOverlap) {
                    cleanup();
                }
                burstIndex++;
                phase = AbilityPhase.BURST_DELAY;
                currentTick = 0;
                return true;
            }

            // Final completion - let burst entities die naturally (don't force-kill)
            burstEntities.clear();
            cleanup();
            phase = AbilityPhase.IDLE;
            currentTick = 0;
            return true;
        }
        return false;
    }

    /**
     * Interrupt this ability. Transitions to DAZED phase if interruptible during WINDUP.
     * Called when NPC takes direct damage during WINDUP.
     */
    public void interrupt() {
        cleanupBurstEntities();
        cleanup();
        if (interruptible && (phase == AbilityPhase.WINDUP || phase == AbilityPhase.BURST_DELAY)) {
            // Interrupted during windup or burst delay - go to dazed state
            phase = AbilityPhase.DAZED;
            currentTick = 0;
        } else {
            // Force stop - go directly to IDLE
            phase = AbilityPhase.IDLE;
            currentTick = 0;
        }
        burstIndex = 0;
        currentTarget = null;
        telegraphInstances.clear();
    }

    /**
     * Check if executing
     */
    public boolean isExecuting() {
        return phase != AbilityPhase.IDLE;
    }

    /**
     * Check if can be interrupted by this damage source.
     * Only direct physical hits (not magic, fire, or other indirect damage) can interrupt.
     * Interruption only occurs during WINDUP phase - if ability is already active, it completes.
     *
     * @param source The damage source
     * @return true if the ability should be interrupted
     */
    public boolean canInterrupt(DamageSource source) {
        if (!interruptible || (phase != AbilityPhase.WINDUP && phase != AbilityPhase.BURST_DELAY)) {
            return false;
        }

        // Only direct physical hits can interrupt, not magic, fire, or other indirect damage
        if (source == null) {
            return false;
        }

        // Reject indirect damage types
        if (source.isMagicDamage() || source.isFireDamage() || source.isExplosion()) {
            return false;
        }

        // Reject damage without a direct attacker entity
        if (source.getEntity() == null) {
            return false;
        }

        // Direct hit from an entity - can interrupt
        return true;
    }

    /**
     * Reset all execution state (called on combat end, NPC death, target lost, etc.)
     */
    public void reset() {
        cleanupBurstEntities();
        cleanup();
        phase = AbilityPhase.IDLE;
        currentTick = 0;
        burstIndex = 0;
        currentTarget = null;
        telegraphInstances.clear();
        previewMode = false;
        previewEntityHandler = null;
    }

    /**
     * Cleanup any resources created by this ability.
     * Override this to kill spawned entities, cancel telegraphs, etc.
     * Called when ability ends for any reason (completion, interrupt, reset, NPC death).
     */
    public void cleanup() {
        // Override in subclasses to clean up spawned entities, etc.
    }

    /**
     * Kill all entities tracked during burst overlap and clear the list.
     */
    protected void cleanupBurstEntities() {
        for (Entity e : burstEntities) {
            if (e != null && !e.isDead) {
                killAbilityEntity(e);
            }
        }
        burstEntities.clear();
    }

    // ═══════════════════════════════════════════════════════════════════
    // PREVIEW MODE SUPPORT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Whether this ability is running in preview mode (GUI preview).
     * When true, abilities should skip damage, effects, sounds, and particles
     * but still run their core logic (entity spawning, movement, timing).
     */
    public boolean isPreview() {
        return previewMode;
    }

    public void setPreviewMode(boolean preview) {
        this.previewMode = preview;
    }

    public void setPreviewEntityHandler(PreviewEntityHandler handler) {
        this.previewEntityHandler = handler;
    }

    /**
     * Spawn an entity during ability execution.
     * In preview mode, routes to PreviewEntityHandler instead of world.spawnEntityInWorld().
     */
    protected void spawnAbilityEntity(World world, Entity entity) {
        if (previewMode && previewEntityHandler != null) {
            previewEntityHandler.onEntitySpawned(entity);
        } else {
            world.spawnEntityInWorld(entity);
        }
        if (burstEnabled && burstOverlap) {
            burstEntities.add(entity);
        }
    }

    /**
     * Kill an entity spawned by this ability.
     * In preview mode, notifies PreviewEntityHandler of removal.
     */
    protected void killAbilityEntity(Entity entity) {
        if (entity == null) return;
        entity.setDead();
        if (previewMode && previewEntityHandler != null) {
            previewEntityHandler.onEntityRemoved(entity);
        }
    }

    /**
     * Maximum duration (in ticks) for preview before auto-stopping.
     * Override in movement abilities that have variable duration.
     * Default: 200 ticks (10 seconds).
     */
    public int getMaxPreviewDuration() {
        return 200;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONDITION CHECKING
    // ═══════════════════════════════════════════════════════════════════

    public boolean checkConditions(EntityLivingBase caster, EntityLivingBase target) {
        for (Condition c : conditions) {
            if (!c.check(caster, target)) return false;
        }
        return true;
    }

    /**
     * Check conditions for a player caster, skipping target-requiring conditions.
     */
    public boolean checkConditionsForPlayer(EntityLivingBase caster) {
        for (Condition c : conditions) {
            if (c.requiresTarget()) continue;
            if (!c.check(caster, null)) return false;
        }
        return true;
    }


    // ═══════════════════════════════════════════════════════════════════
    // NBT (config only, execution state is transient)
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("id", id);
        nbt.setString("name", name);
        nbt.setString("typeId", typeId);
        nbt.setInteger("weight", weight);
        nbt.setBoolean("enabled", enabled);
        nbt.setString("targetingMode", targetingMode.name());
        nbt.setFloat("minRange", minRange);
        nbt.setFloat("maxRange", maxRange);
        nbt.setInteger("cooldown", cooldownTicks);
        nbt.setInteger("windUp", windUpTicks);
        nbt.setBoolean("syncWindup", syncWindupWithAnimation);
        nbt.setInteger("recovery", dazedTicks);
        nbt.setBoolean("interruptible", interruptible);
        nbt.setInteger("lockMovement", lockMovement.ordinal());
        nbt.setInteger("rotationMode", rotationMode.ordinal());
        nbt.setInteger("rotationPhase", rotationPhase.ordinal());
        nbt.setInteger("windUpColor", windUpColor);
        nbt.setInteger("activeColor", activeColor);
        nbt.setString("windUpSound", windUpSound);
        nbt.setString("activeSound", activeSound);
        nbt.setInteger("windUpAnimationId", windUpAnimationId);
        nbt.setInteger("activeAnimationId", activeAnimationId);
        nbt.setString("windUpAnimationName", windUpAnimationName);
        nbt.setString("activeAnimationName", activeAnimationName);
        nbt.setBoolean("showTelegraph", showTelegraph);
        nbt.setString("telegraphType", telegraphType.name());
        nbt.setFloat("telegraphHeightOffset", telegraphHeightOffset);
        nbt.setTag("customData", customData);
        nbt.setInteger("allowedBy", allowedBy.ordinal());
        nbt.setBoolean("ignoreCooldown", ignoreCooldown);

        // Burst
        nbt.setBoolean("burstEnabled", burstEnabled);
        nbt.setInteger("burstAmount", burstAmount);
        nbt.setInteger("burstDelay", burstDelay);
        nbt.setBoolean("burstReplayAnimations", burstReplayAnimations);
        nbt.setBoolean("burstOverlap", burstOverlap);

        // Conditions
        NBTTagList condList = new NBTTagList();
        for (Condition c : conditions) condList.appendTag(c.writeNBT());
        nbt.setTag("conditions", condList);

        // Effects
        NBTTagList effectList = new NBTTagList();
        for (AbilityEffect effect : effects) {
            effectList.appendTag(effect.writeNBT());
        }
        nbt.setTag("effects", effectList);

        // Type-specific
        NBTTagCompound typeNBT = new NBTTagCompound();
        writeTypeNBT(typeNBT);
        nbt.setTag("typeData", typeNBT);

        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        id = nbt.getString("id");
        name = nbt.getString("name");
        typeId = nbt.getString("typeId");
        weight = nbt.getInteger("weight");
        enabled = nbt.getBoolean("enabled");
        targetingMode = TargetingMode.valueOf(nbt.getString("targetingMode"));
        minRange = nbt.getFloat("minRange");
        maxRange = nbt.getFloat("maxRange");
        cooldownTicks = nbt.getInteger("cooldown");
        windUpTicks = nbt.getInteger("windUp");
        syncWindupWithAnimation = nbt.getBoolean("syncWindup");
        dazedTicks = nbt.getInteger("recovery");
        interruptible = nbt.getBoolean("interruptible");
        lockMovement = LockMovementType.fromOrdinal(nbt.getInteger("lockMovement"));
        rotationMode = RotationMode.fromOrdinal(nbt.getInteger("rotationMode"));
        rotationPhase = LockMovementType.fromOrdinal(nbt.getInteger("rotationPhase"));
        windUpColor = nbt.getInteger("windUpColor");
        activeColor = nbt.getInteger("activeColor");
        windUpSound = nbt.getString("windUpSound");
        activeSound = nbt.getString("activeSound");
        windUpAnimationId = nbt.getInteger("windUpAnimationId");
        activeAnimationId = nbt.getInteger("activeAnimationId");
        windUpAnimationName = nbt.getString("windUpAnimationName");
        activeAnimationName = nbt.getString("activeAnimationName");
        showTelegraph = nbt.getBoolean("showTelegraph");
        try {
            telegraphType = TelegraphType.valueOf(nbt.getString("telegraphType"));
        } catch (Exception e) {
            telegraphType = TelegraphType.CIRCLE;
        }
        telegraphHeightOffset = nbt.getFloat("telegraphHeightOffset");
        customData = nbt.getCompoundTag("customData");
        allowedBy = UserType.fromOrdinal(nbt.getInteger("allowedBy"));
        ignoreCooldown = nbt.getBoolean("ignoreCooldown");

        // Burst
        burstEnabled = nbt.getBoolean("burstEnabled");
        burstAmount = nbt.getInteger("burstAmount");
        burstDelay = nbt.getInteger("burstDelay");
        burstReplayAnimations = nbt.getBoolean("burstReplayAnimations");
        burstOverlap = nbt.getBoolean("burstOverlap");

        // Conditions
        conditions.clear();
        NBTTagList condList = nbt.getTagList("conditions", 10);
        for (int i = 0; i < condList.tagCount(); i++) {
            Condition c = Condition.fromNBT(condList.getCompoundTagAt(i));
            if (c != null) conditions.add(c);
        }

        // Effects
        effects.clear();
        if (nbt.hasKey("effects")) {
            NBTTagList effectList = nbt.getTagList("effects", 10);
            for (int i = 0; i < effectList.tagCount(); i++) {
                AbilityEffect effect = AbilityEffect.fromNBT(effectList.getCompoundTagAt(i));
                if (effect != null && effect.isValid()) {
                    effects.add(effect);
                }
            }
        }

        readTypeNBT(nbt.getCompoundTag("typeData"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeId() {
        return typeId;
    }

    public boolean isBuiltIn() {
        return false;
    }

    public boolean isNpcInlineEdit() {
        return npcInlineEdit;
    }

    public void setNpcInlineEdit(boolean npcInlineEdit) {
        this.npcInlineEdit = npcInlineEdit;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TargetingMode getTargetingMode() {
        return targetingMode;
    }

    public void setTargetingMode(TargetingMode targetingMode) {
        this.targetingMode = targetingMode;
    }

    public float getMinRange() {
        return minRange;
    }

    public void setMinRange(float minRange) {
        this.minRange = minRange;
    }

    public float getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(float maxRange) {
        this.maxRange = maxRange;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = Math.max(0, cooldownTicks);
    }

    public int getWindUpTicks() {
        return calculateWindupFromAnimation();
    }

    public int getRawWindUpTicks() {
        return windUpTicks;
    }

    public void setWindUpTicks(int windUpTicks) {
        this.windUpTicks = Math.max(0, windUpTicks);
    }

    public boolean isSyncWindupWithAnimation() {
        return syncWindupWithAnimation;
    }

    public boolean hasWindUpAnimation() {
        return (windUpAnimationName != null && !windUpAnimationName.isEmpty()) || windUpAnimationId >= 0;
    }

    public void setSyncWindupWithAnimation(boolean syncWindupWithAnimation) {
        this.syncWindupWithAnimation = syncWindupWithAnimation;
    }

    public int getDazedTicks() {
        return dazedTicks;
    }

    public void setDazedTicks(int dazedTicks) {
        this.dazedTicks = Math.max(0, dazedTicks);
    }

    public boolean isInterruptible() {
        return interruptible;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }

    public LockMovementType getLockMovement() {
        return lockMovement;
    }

    public void setLockMovement(LockMovementType lockMovement) {
        this.lockMovement = lockMovement;
    }

    public RotationMode getRotationMode() {
        return rotationMode;
    }

    public void setRotationMode(RotationMode rotationMode) {
        this.rotationMode = rotationMode;
    }

    public LockMovementType getRotationPhase() {
        return rotationPhase;
    }

    public void setRotationPhase(LockMovementType rotationPhase) {
        this.rotationPhase = rotationPhase;
    }

    /**
     * API method: Get lock movement type as integer.
     * @return 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    @Override
    public int getLockMovementType() {
        return lockMovement.ordinal();
    }

    /**
     * API method: Set lock movement type from integer.
     * @param type 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    @Override
    public void setLockMovementType(int type) {
        this.lockMovement = LockMovementType.fromOrdinal(type);
    }

    /**
     * API method: Get rotation mode as integer.
     * @return 0=FREE, 1=LOCKED, 2=TRACK
     */
    @Override
    public int getRotationModeType() {
        return rotationMode.ordinal();
    }

    /**
     * API method: Set rotation mode from integer.
     * @param type 0=FREE, 1=LOCKED, 2=TRACK
     */
    @Override
    public void setRotationModeType(int type) {
        this.rotationMode = RotationMode.fromOrdinal(type);
    }

    /**
     * API method: Get rotation phase as integer.
     * @return 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    @Override
    public int getRotationPhaseType() {
        return rotationPhase.ordinal();
    }

    /**
     * API method: Set rotation phase from integer.
     * @param type 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    @Override
    public void setRotationPhaseType(int type) {
        this.rotationPhase = LockMovementType.fromOrdinal(type);
    }

    /**
     * Check if movement (pathfinding/motion) should be locked during WINDUP phase.
     */
    @Override
    public boolean isMovementLockedDuringWindup() {
        return lockMovement.locksWindup();
    }

    /**
     * Check if movement (pathfinding/motion) should be locked during ACTIVE phase.
     */
    @Override
    public boolean isMovementLockedDuringActive() {
        return lockMovement.locksActive();
    }

    /**
     * Check if rotation (yaw/pitch) should be locked during WINDUP phase.
     */
    @Override
    public boolean isRotationLockedDuringWindup() {
        return rotationMode == RotationMode.LOCKED && rotationPhase.locksWindup();
    }

    /**
     * Check if rotation (yaw/pitch) should be locked during ACTIVE phase.
     */
    @Override
    public boolean isRotationLockedDuringActive() {
        return rotationMode == RotationMode.LOCKED && rotationPhase.locksActive();
    }

    /**
     * Check if movement should be locked during the current phase.
     */
    public boolean isMovementLockedForCurrentPhase() {
        switch (phase) {
            case WINDUP:
                return isMovementLockedDuringWindup();
            case ACTIVE:
                return isMovementLockedDuringActive();
            case BURST_DELAY:
                return false; // Free movement during burst delay
            default:
                return false;
        }
    }

    /**
     * Check if rotation should be locked during the current phase.
     */
    public boolean isRotationLockedForCurrentPhase() {
        switch (phase) {
            case WINDUP:
                return isRotationLockedDuringWindup();
            case ACTIVE:
                return isRotationLockedDuringActive();
            case BURST_DELAY:
                return false; // Free rotation during burst delay
            default:
                return false;
        }
    }

    /**
     * Check if hit scan (force face target) is active during the current phase.
     */
    public boolean isHitScanForCurrentPhase() {
        if (rotationMode != RotationMode.TRACK) return false;
        switch (phase) {
            case WINDUP:
                return rotationPhase.locksWindup();
            case ACTIVE:
                return rotationPhase.locksActive();
            case BURST_DELAY:
                return false; // Free rotation during burst delay
            default:
                return false;
        }
    }

    public int getWindUpColor() {
        return windUpColor;
    }

    public void setWindUpColor(int windUpColor) {
        this.windUpColor = windUpColor;
    }

    public int getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
    }

    public String getWindUpSound() {
        return windUpSound;
    }

    public void setWindUpSound(String windUpSound) {
        this.windUpSound = windUpSound;
    }

    public String getActiveSound() {
        return activeSound;
    }

    public void setActiveSound(String activeSound) {
        this.activeSound = activeSound;
    }

    public Animation getWindUpAnimation() {
        if (AnimationController.Instance == null) return null;

        // Built-in animation by name takes priority
        if (windUpAnimationName != null && !windUpAnimationName.isEmpty()) {
            return (Animation) AnimationController.Instance.get(windUpAnimationName, true);
        }
        // Fall back to user animation by ID
        if (windUpAnimationId >= 0) {
            return (Animation) AnimationController.Instance.get(windUpAnimationId);
        }
        return null;
    }

    public int getWindUpAnimationId() {
        return windUpAnimationId;
    }

    public void setWindUpAnimationId(int windUpAnimationId) {
        this.windUpAnimationId = windUpAnimationId;
    }

    public String getWindUpAnimationName() {
        return windUpAnimationName;
    }

    public void setWindUpAnimationName(String windUpAnimationName) {
        this.windUpAnimationName = windUpAnimationName != null ? windUpAnimationName : "";
    }

    public Animation getActiveAnimation() {
        if (AnimationController.Instance == null) return null;

        // Built-in animation by name takes priority
        if (activeAnimationName != null && !activeAnimationName.isEmpty()) {
            return (Animation) AnimationController.Instance.get(activeAnimationName, true);
        }
        // Fall back to user animation by ID
        if (activeAnimationId >= 0) {
            return (Animation) AnimationController.Instance.get(activeAnimationId);
        }
        return null;
    }

    public int getActiveAnimationId() {
        return activeAnimationId;
    }

    public void setActiveAnimationId(int activeAnimationId) {
        this.activeAnimationId = activeAnimationId;
    }

    public String getActiveAnimationName() {
        return activeAnimationName;
    }

    public void setActiveAnimationName(String activeAnimationName) {
        this.activeAnimationName = activeAnimationName != null ? activeAnimationName : "";
    }

    public boolean isShowTelegraph() {
        return showTelegraph;
    }

    public void setShowTelegraph(boolean showTelegraph) {
        this.showTelegraph = showTelegraph;
    }

    public TelegraphType getTelegraphType() {
        return telegraphType;
    }

    public void setTelegraphType(TelegraphType telegraphType) {
        this.telegraphType = telegraphType;
    }

    public float getTelegraphHeightOffset() {
        return telegraphHeightOffset;
    }

    public void setTelegraphHeightOffset(float telegraphHeightOffset) {
        this.telegraphHeightOffset = telegraphHeightOffset;
    }

    public AbilityPhase getPhase() {
        return phase;
    }

    /**
     * Get the current execution phase as int (0=IDLE, 1=WINDUP, 2=ACTIVE, 3=DAZED).
     * Required for IAbility interface.
     */
    @Override
    public int getPhaseInt() {
        return phase.ordinal();
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public EntityLivingBase getCurrentTarget() {
        return currentTarget;
    }

    public NBTTagCompound getCustomData() {
        return customData;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void addCondition(Condition c) {
        conditions.add(c);
    }

    public UserType getAllowedBy() {
        return allowedBy;
    }

    public void setAllowedBy(UserType allowedBy) {
        this.allowedBy = allowedBy;
    }

    public boolean isIgnoreCooldown() {
        return ignoreCooldown;
    }

    public void setIgnoreCooldown(boolean ignoreCooldown) {
        this.ignoreCooldown = ignoreCooldown;
    }

    public List<AbilityEffect> getEffects() {
        return effects;
    }

    public void setEffects(List<AbilityEffect> effects) {
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    public void addEffect(AbilityEffect effect) {
        if (effect != null && effect.isValid()) {
            effects.add(effect);
        }
    }

    public void clearEffects() {
        effects.clear();
    }

    // Burst getters/setters

    public boolean isBurstEnabled() {
        return burstEnabled;
    }

    public void setBurstEnabled(boolean burstEnabled) {
        this.burstEnabled = burstEnabled;
    }

    public int getBurstAmount() {
        return burstAmount;
    }

    public void setBurstAmount(int burstAmount) {
        this.burstAmount = Math.max(0, burstAmount);
    }

    public int getBurstDelay() {
        return burstDelay;
    }

    public void setBurstDelay(int burstDelay) {
        this.burstDelay = Math.max(0, burstDelay);
    }

    public boolean isBurstReplayAnimations() {
        return burstReplayAnimations;
    }

    public void setBurstReplayAnimations(boolean burstReplayAnimations) {
        this.burstReplayAnimations = burstReplayAnimations;
    }

    public boolean isBurstOverlap() {
        return burstOverlap;
    }

    public void setBurstOverlap(boolean burstOverlap) {
        this.burstOverlap = burstOverlap;
    }

    public int getBurstIndex() {
        return burstIndex;
    }

    /**
     * Get display keys for rotation phase dropdown (excludes "None" since mode=FREE handles that).
     */
    private static String[] getRotationPhaseKeys() {
        return new String[]{
            "ability.lockMove.windup",
            "ability.lockMove.active",
            "ability.lockMove.both"
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    // SHIT FROM THE ASS
    // ═══════════════════════════════════════════════════════════════════

    private int calculateWindupFromAnimation() {
        if (!syncWindupWithAnimation) {
            return windUpTicks;
        }

        if (AnimationController.Instance == null) {
            return this.windUpTicks;
        }

        Animation animation = null;
        // Check for built-in animation (by name) first
        if (windUpAnimationName != null && !windUpAnimationName.isEmpty()) {
            animation = (Animation) AnimationController.Instance.get(windUpAnimationName, true);
        }
        // Fall back to user animation (by ID)
        else if (windUpAnimationId >= 0) {
            animation = (Animation) AnimationController.Instance.get(windUpAnimationId);
        }

        if (animation == null || animation.frames.isEmpty()) {
            return this.windUpTicks;
        }

        // Calculate total duration by summing all frame durations
        int totalDuration = 0;
        for (Frame frame : animation.frames) {
            totalDuration += frame.getDuration();
        }

        // Return total duration
        return totalDuration;
    }

    // ═══════════════════════════════════════════════════════════════════
    // IAbility API METHODS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public int getAllowedByType() {
        return allowedBy.ordinal();
    }

    @Override
    public void setAllowedByType(int type) {
        this.allowedBy = UserType.fromOrdinal(type);
    }

    @Override
    public INbt getNbt() {
        return NpcAPI.Instance().getINbt(writeNBT());
    }

    @Override
    public void setNbt(INbt nbt) {
        readNBT(nbt.getMCNBT());
    }
}
