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
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.AbilityEvent;
import noppes.npcs.scripted.event.player.PlayerAbilityEvent;

import java.util.ArrayList;
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
    protected int dazedTicks = 80;        // Only used when interrupted during WINDUP (if interruptible)

    // Interruption
    protected boolean interruptible = true;

    // Feedback
    protected LockMovementType lockMovement = LockMovementType.WINDUP;
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

    // ═══════════════════════════════════════════════════════════════════
    // EXECUTION STATE (not saved, reset each combat)
    // ═══════════════════════════════════════════════════════════════════

    protected transient AbilityPhase phase = AbilityPhase.IDLE;
    protected transient int currentTick = 0;
    protected transient EntityLivingBase currentTarget;
    protected transient long executionStartTime;
    protected transient TelegraphInstance telegraphInstance;

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

    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
    }

    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
    }

    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
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
            if (caster instanceof EntityNPCInterface) {
                hitEntity.attackEntityFrom(new NpcDamageSource("mob", (EntityNPCInterface) caster), damage);
            } else if (caster instanceof EntityPlayer) {
                hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) caster), damage);
            } else {
                hitEntity.attackEntityFrom(DamageSource.causeMobDamage(caster), damage);
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
     * Returns true if this ability type has custom settings that need a Type-specific tab.
     * Override to return true if the ability has settings beyond the base Ability fields.
     */
    public boolean hasTypeSettings() {
        return false;
    }

    /**
     * Get the number of rows needed for type-specific settings in the GUI.
     * Each row is approximately 24 pixels. Used for GUI layout.
     */
    public int getTypeSettingsRowCount() {
        return 0;
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
     * @param npc    The caster
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
            if ((telegraphType == TelegraphType.LINE || telegraphType == TelegraphType.CONE) && target != null) {
                instance.setTargetEntityId(target.getEntityId());
            }
        } else if (target != null) {
            // AOE_TARGET abilities: telegraph follows target during windup
            instance.setEntityIdToFollow(target.getEntityId());
        }

        return instance;
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
     * Get the current telegraph instance.
     */
    public TelegraphInstance getTelegraphInstance() {
        return telegraphInstance;
    }

    /**
     * Set the telegraph instance (called by DataAbilities).
     */
    public void setTelegraphInstance(TelegraphInstance instance) {
        this.telegraphInstance = instance;
    }

    // ═══════════════════════════════════════════════════════════════════
    // EXECUTION LOGIC (called by CombatHandler)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Start executing this ability
     */
    public void start(EntityLivingBase target) {
        this.phase = AbilityPhase.WINDUP;
        this.currentTick = 0;
        this.currentTarget = target;
        this.executionStartTime = System.currentTimeMillis();
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
        cleanup();
        if (interruptible && phase == AbilityPhase.WINDUP) {
            // Interrupted during windup - go to dazed state
            phase = AbilityPhase.DAZED;
            currentTick = 0;
        } else {
            // Force stop - go directly to IDLE
            phase = AbilityPhase.IDLE;
            currentTick = 0;
        }
        currentTarget = null;
        telegraphInstance = null;
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
        if (!interruptible || phase != AbilityPhase.WINDUP) {
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
        cleanup();
        phase = AbilityPhase.IDLE;
        currentTick = 0;
        currentTarget = null;
        telegraphInstance = null;
    }

    /**
     * Cleanup any resources created by this ability.
     * Override this to kill spawned entities, cancel telegraphs, etc.
     * Called when ability ends for any reason (completion, interrupt, reset, NPC death).
     */
    public void cleanup() {
        // Override in subclasses to clean up spawned entities, etc.
    }

    // ═══════════════════════════════════════════════════════════════════
    // CLIENT-SIDE PREVIEW METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get total active phase duration for preview (in ticks).
     * Override in subclasses to provide accurate preview duration.
     *
     * @return Duration of active phase in ticks (default: 40 = 2 seconds)
     */
    public int getPreviewActiveDuration() {
        return 40;
    }

    /**
     * Called during preview windup tick (client-side only).
     * Override for visual updates during windup preview.
     * Do NOT apply damage or world effects here.
     *
     * @param npc  The preview NPC
     * @param tick Current tick within windup phase
     */
    @SideOnly(Side.CLIENT)
    public void onPreviewWindUpTick(EntityNPCInterface npc, int tick) {
        // Default: no-op. Override in subclasses for visual effects.
    }

    /**
     * Called during preview active tick (client-side only).
     * Override for visual updates during active preview.
     * Do NOT apply damage or world effects here.
     *
     * @param npc  The preview NPC
     * @param tick Current tick within active phase
     */
    @SideOnly(Side.CLIENT)
    public void onPreviewActiveTick(EntityNPCInterface npc, int tick) {
        // Default: no-op. Override in subclasses for visual effects.
    }

    /**
     * Create a preview entity for GUI display (client-side only).
     * The entity should be in preview mode and not apply damage.
     * Override in abilities that spawn entities (Beam, Orb, Disc, etc.)
     *
     * @param npc The preview NPC
     * @return Preview entity, or null if ability doesn't spawn entities
     */
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        return null; // Override in entity-spawning abilities
    }

    /**
     * Whether to spawn preview entity during WINDUP phase (true) or ACTIVE phase (false).
     * Most abilities (Orb, Beam, Disc) spawn during windup for charging effect.
     * Laser spawns at active phase since it has no charging state.
     */
    public boolean spawnPreviewDuringWindup() {
        return true; // Default: spawn during windup for charging
    }

    // ── Preview target (set by executor for abilities that need targeting) ──

    protected transient EntityLivingBase previewTarget;

    /**
     * Set the fake target entity for preview mode.
     * Called by AbilityPreviewExecutor before starting preview.
     */
    public void setPreviewTarget(EntityLivingBase target) {
        this.previewTarget = target;
    }

    public EntityLivingBase getPreviewTarget() {
        return previewTarget;
    }

    /**
     * Called once when transitioning from WINDUP to ACTIVE in preview mode.
     * Override in movement-based abilities to initiate movement.
     */
    @SideOnly(Side.CLIENT)
    public void onPreviewExecute(EntityNPCInterface npc) {
        // Default: no-op. Override in movement abilities.
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
        nbt.setInteger("recovery", dazedTicks);
        nbt.setBoolean("interruptible", interruptible);
        nbt.setInteger("lockMovement", lockMovement.ordinal());
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
        dazedTicks = nbt.getInteger("recovery");
        interruptible = nbt.getBoolean("interruptible");
        lockMovement = LockMovementType.fromOrdinal(nbt.getInteger("lockMovement"));
        windUpColor = nbt.getInteger("windUpColor");
        activeColor = nbt.getInteger("activeColor");
        windUpSound = nbt.getString("windUpSound");
        activeSound = nbt.getString("activeSound");
        windUpAnimationId = nbt.hasKey("windUpAnimationId") ? nbt.getInteger("windUpAnimationId") : -1;
        activeAnimationId = nbt.hasKey("activeAnimationId") ? nbt.getInteger("activeAnimationId") : -1;
        windUpAnimationName = nbt.getString("windUpAnimationName");
        activeAnimationName = nbt.getString("activeAnimationName");
        showTelegraph = !nbt.hasKey("showTelegraph") || nbt.getBoolean("showTelegraph");
        if (nbt.hasKey("telegraphType")) {
            try {
                telegraphType = TelegraphType.valueOf(nbt.getString("telegraphType"));
            } catch (Exception e) {
                telegraphType = TelegraphType.CIRCLE;
            }
        }
        telegraphHeightOffset = nbt.hasKey("telegraphHeightOffset") ? nbt.getFloat("telegraphHeightOffset") : 0.1f;
        customData = nbt.getCompoundTag("customData");
        allowedBy = nbt.hasKey("allowedBy") ? UserType.fromOrdinal(nbt.getInteger("allowedBy")) : UserType.BOTH;
        ignoreCooldown = nbt.hasKey("ignoreCooldown") && nbt.getBoolean("ignoreCooldown");

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
        return windUpTicks;
    }

    public void setWindUpTicks(int windUpTicks) {
        this.windUpTicks = Math.max(0, windUpTicks);
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
     * Check if movement should be locked during WINDUP phase.
     */
    @Override
    public boolean isMovementLockedDuringWindup() {
        return lockMovement.locksWindup();
    }

    /**
     * Check if movement should be locked during ACTIVE phase.
     */
    @Override
    public boolean isMovementLockedDuringActive() {
        return lockMovement.locksActive();
    }

    /**
     * Check if movement should be locked during the current phase.
     */
    public boolean isMovementLockedForCurrentPhase() {
        switch (phase) {
            case WINDUP:
                return lockMovement.locksWindup();
            case ACTIVE:
                return lockMovement.locksActive();
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
            return (Animation) AnimationController.Instance.get(windUpAnimationName);
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
            return (Animation) AnimationController.Instance.get(activeAnimationName);
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
