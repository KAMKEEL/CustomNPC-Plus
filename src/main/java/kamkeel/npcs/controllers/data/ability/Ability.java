package kamkeel.npcs.controllers.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
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
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.AbilityEvent;

import java.util.ArrayList;
import java.util.List;

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
    protected int cooldownTicks = 100;
    protected int windUpTicks = 20;
    protected int activeTicks = 10;
    protected int recoveryTicks = 20;

    // Interruption
    protected boolean interruptible = true;

    // Feedback
    protected boolean lockMovement = true;
    protected int windUpColor = 0x80FF4400;   // Telegraph color during wind up
    protected int activeColor = 0xC0FF0000;   // Telegraph warning/active color

    // Sounds
    protected String windUpSound = "";        // Sound to play when wind up starts
    protected String activeSound = "";        // Sound to play when active phase starts

    // Animations (global animation IDs, -1 = none)
    protected int windUpAnimationId = -1;     // Animation to play during wind up
    protected int activeAnimationId = -1;     // Animation to play during active phase

    // Telegraph configuration
    protected boolean showTelegraph = true;
    protected TelegraphType telegraphType = TelegraphType.CIRCLE;
    protected float telegraphHeightOffset = 0.1f;

    // Custom data for external mods
    protected NBTTagCompound customData = new NBTTagCompound();

    // ═══════════════════════════════════════════════════════════════════
    // EXECUTION STATE (not saved, reset each combat)
    // ═══════════════════════════════════════════════════════════════════

    protected transient AbilityPhase phase = AbilityPhase.IDLE;
    protected transient int currentTick = 0;
    protected transient long cooldownEndTime = 0;
    protected transient EntityLivingBase currentTarget;
    protected transient long executionStartTime;
    protected transient TelegraphInstance telegraphInstance;

    // ═══════════════════════════════════════════════════════════════════
    // ABSTRACT METHODS
    // ═══════════════════════════════════════════════════════════════════

    /** Called first tick of ACTIVE phase */
    public abstract void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world);

    /** Called every tick of ACTIVE phase */
    public abstract void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick);

    /** Write type-specific config to NBT */
    public abstract void writeTypeNBT(NBTTagCompound nbt);

    /** Read type-specific config from NBT */
    public abstract void readTypeNBT(NBTTagCompound nbt);

    // ═══════════════════════════════════════════════════════════════════
    // OPTIONAL OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {}
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {}
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {}

    /**
     * Apply damage to an entity with ability hit event support.
     * Fires the abilityHit script event, allowing scripts to modify or cancel the damage.
     *
     * @param npc The NPC executing the ability
     * @param hitEntity The entity being hit
     * @param damage The damage amount
     * @param knockback The horizontal knockback
     * @param knockbackUp The vertical knockback
     * @return true if damage was applied (not cancelled), false if cancelled
     */
    protected boolean applyAbilityDamage(EntityNPCInterface npc, EntityLivingBase hitEntity,
                                         float damage, float knockback, float knockbackUp) {
        DataAbilities dataAbilities = npc.abilities;
        AbilityEvent.HitEvent event = dataAbilities.fireHitEvent(
            this, currentTarget, hitEntity, damage, knockback, knockbackUp);

        if (event == null) {
            return false; // Cancelled
        }

        // Apply damage with potentially modified values
        float finalDamage = event.getDamage();
        float finalKnockback = event.getKnockback();
        float finalKnockbackUp = event.getKnockbackUp();

        // Apply damage
        if (finalDamage > 0) {
            hitEntity.attackEntityFrom(new NpcDamageSource("mob", npc), finalDamage);
        }

        // Apply knockback if any
        if (finalKnockback > 0 || finalKnockbackUp > 0) {
            double dx = hitEntity.posX - npc.posX;
            double dz = hitEntity.posZ - npc.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0 && finalKnockback > 0) {
                dx /= len;
                dz /= len;
                hitEntity.motionX += dx * finalKnockback;
                hitEntity.motionZ += dz * finalKnockback;
            }
            hitEntity.motionY += finalKnockbackUp;
            hitEntity.velocityChanged = true;
        }

        return true;
    }

    /**
     * Apply damage to an entity with ability hit event support and custom knockback direction.
     * Fires the abilityHit script event, allowing scripts to modify or cancel the damage.
     *
     * @param npc The NPC executing the ability
     * @param hitEntity The entity being hit
     * @param damage The damage amount
     * @param knockback The horizontal knockback
     * @param knockbackUp The vertical knockback
     * @param knockbackDirX The X component of knockback direction (normalized)
     * @param knockbackDirZ The Z component of knockback direction (normalized)
     * @return true if damage was applied (not cancelled), false if cancelled
     */
    protected boolean applyAbilityDamageWithDirection(EntityNPCInterface npc, EntityLivingBase hitEntity,
                                                       float damage, float knockback, float knockbackUp,
                                                       double knockbackDirX, double knockbackDirZ) {
        DataAbilities dataAbilities = npc.abilities;
        AbilityEvent.HitEvent event = dataAbilities.fireHitEvent(
            this, currentTarget, hitEntity, damage, knockback, knockbackUp);

        if (event == null) {
            return false; // Cancelled
        }

        // Apply damage with potentially modified values
        float finalDamage = event.getDamage();
        float finalKnockback = event.getKnockback();
        float finalKnockbackUp = event.getKnockbackUp();

        // Apply damage
        if (finalDamage > 0) {
            hitEntity.attackEntityFrom(DamageSource.causeMobDamage(npc), finalDamage);
        }

        // Apply knockback in specified direction
        if (finalKnockback > 0 || finalKnockbackUp > 0) {
            hitEntity.motionX = knockbackDirX * finalKnockback;
            hitEntity.motionY = finalKnockbackUp;
            hitEntity.motionZ = knockbackDirZ * finalKnockback;
            hitEntity.velocityChanged = true;
        }

        return true;
    }
    public float getTelegraphRadius() { return 5.0f; }
    public float getTelegraphLength() { return 5.0f; }
    public float getTelegraphWidth() { return 2.0f; }
    public float getTelegraphAngle() { return 45.0f; }

    /**
     * Returns true if this ability controls NPC movement during ACTIVE phase.
     * When true, AI pathfinding will be blocked during ability execution.
     * Override in abilities that move the NPC (Charge, Dash, Slam, etc.)
     */
    public boolean hasAbilityMovement() { return false; }

    /**
     * Returns true if the targeting mode for this ability type is locked and cannot be changed.
     * Most abilities have fixed targeting (e.g., Heal is always SELF, Charge is always AGGRO_TARGET).
     * Override and return false for abilities with flexible targeting.
     */
    public boolean isTargetingModeLocked() { return true; }

    /**
     * Returns the allowed targeting modes for this ability, or null if all modes are allowed.
     * Override in abilities that only support specific targeting modes.
     */
    public TargetingMode[] getAllowedTargetingModes() { return null; }

    /**
     * Returns true if the telegraph type for this ability is locked and cannot be changed.
     * Telegraph type is inherent to how the ability works (e.g., Charge uses LINE, Slam uses CIRCLE).
     */
    public boolean isTelegraphTypeLocked() { return true; }

    /**
     * Returns true if this ability type has custom settings that need a Type-specific tab.
     * Override to return true if the ability has settings beyond the base Ability fields.
     */
    public boolean hasTypeSettings() { return false; }

    /**
     * Get the number of rows needed for type-specific settings in the GUI.
     * Each row is approximately 24 pixels. Used for GUI layout.
     */
    public int getTypeSettingsRowCount() { return 0; }

    /**
     * Creates the GUI for configuring this ability.
     * Override this method to provide a custom GUI subclass for type-specific settings.
     *
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
     * @param npc The caster
     * @param target The target (for position calculation)
     * @return The telegraph instance, or null if no telegraph
     */
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE) {
            return null;
        }

        Telegraph telegraph;
        double x, y, z;
        float yaw = npc.rotationYaw;

        // Determine position based on targeting mode
        boolean positionAtNpc = targetingMode == TargetingMode.AOE_SELF ||
                                targetingMode == TargetingMode.SELF ||
                                telegraphType == TelegraphType.LINE ||
                                telegraphType == TelegraphType.CONE;

        if (positionAtNpc) {
            x = npc.posX;
            y = findGroundLevel(npc.worldObj, npc.posX, npc.posY, npc.posZ);
            z = npc.posZ;
        } else if (target != null) {
            x = target.posX;
            y = findGroundLevel(npc.worldObj, target.posX, target.posY, target.posZ);
            z = target.posZ;
        } else {
            x = npc.posX;
            y = findGroundLevel(npc.worldObj, npc.posX, npc.posY, npc.posZ);
            z = npc.posZ;
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
        instance.setCasterEntityId(npc.getEntityId());

        // Set entity to follow based on targeting mode
        if (positionAtNpc) {
            // AOE_SELF abilities: telegraph follows NPC during windup
            instance.setEntityIdToFollow(npc.getEntityId());
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
     * @param world The world
     * @param x X coordinate
     * @param startY Starting Y coordinate (entity feet position)
     * @param z Z coordinate
     * @return The Y coordinate of the ground surface
     */
    protected double findGroundLevel(World world, double x, double startY, double z) {
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

    /** Start executing this ability */
    public void start(EntityLivingBase target) {
        this.phase = AbilityPhase.WINDUP;
        this.currentTick = 0;
        this.currentTarget = target;
        this.executionStartTime = System.currentTimeMillis();
    }

    /** Tick the ability. Returns true if phase changed. */
    public boolean tick() {
        if (phase == AbilityPhase.IDLE) return false;

        currentTick++;
        AbilityPhase oldPhase = phase;

        switch (phase) {
            case WINDUP:
                if (currentTick >= windUpTicks) {
                    phase = AbilityPhase.ACTIVE;
                    currentTick = 0;
                    return true;
                }
                break;
            case ACTIVE:
                if (currentTick >= activeTicks) {
                    phase = AbilityPhase.RECOVERY;
                    currentTick = 0;
                    return true;
                }
                break;
            case RECOVERY:
                if (currentTick >= recoveryTicks) {
                    phase = AbilityPhase.IDLE;
                    startCooldown();
                    return true;
                }
                break;
        }
        return false;
    }

    /** Interrupt this ability */
    public void interrupt() {
        phase = AbilityPhase.IDLE;
        currentTick = 0;
        currentTarget = null;
        telegraphInstance = null;
    }

    /** Start cooldown timer */
    public void startCooldown() {
        cooldownEndTime = System.currentTimeMillis() + (cooldownTicks * 50L);
    }

    /** Check if on cooldown */
    public boolean isOnCooldown() {
        return System.currentTimeMillis() < cooldownEndTime;
    }

    /** Check if executing */
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

    /** Reset all execution state (called on combat end) */
    public void reset() {
        phase = AbilityPhase.IDLE;
        currentTick = 0;
        currentTarget = null;
        cooldownEndTime = 0;
        telegraphInstance = null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONDITION CHECKING
    // ═══════════════════════════════════════════════════════════════════

    public boolean checkConditions(EntityNPCInterface npc, EntityLivingBase target) {
        for (Condition c : conditions) {
            if (!c.check(npc, target)) return false;
        }
        return true;
    }

    /** Full eligibility check */
    public boolean canUse(EntityNPCInterface npc, EntityLivingBase target) {
        if (!enabled) return false;
        if (isOnCooldown()) return false;
        if (isExecuting()) return false;

        float distance = npc.getDistanceToEntity(target);
        if (distance < minRange || distance > maxRange) return false;

        return checkConditions(npc, target);
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
        nbt.setInteger("active", activeTicks);
        nbt.setInteger("recovery", recoveryTicks);
        nbt.setBoolean("interruptible", interruptible);
        nbt.setBoolean("lockMovement", lockMovement);
        nbt.setInteger("windUpColor", windUpColor);
        nbt.setInteger("activeColor", activeColor);
        nbt.setString("windUpSound", windUpSound);
        nbt.setString("activeSound", activeSound);
        nbt.setInteger("windUpAnimationId", windUpAnimationId);
        nbt.setInteger("activeAnimationId", activeAnimationId);
        nbt.setBoolean("showTelegraph", showTelegraph);
        nbt.setString("telegraphType", telegraphType.name());
        nbt.setFloat("telegraphHeightOffset", telegraphHeightOffset);
        nbt.setTag("customData", customData);

        // Conditions
        NBTTagList condList = new NBTTagList();
        for (Condition c : conditions) condList.appendTag(c.writeNBT());
        nbt.setTag("conditions", condList);

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
        activeTicks = nbt.getInteger("active");
        recoveryTicks = nbt.getInteger("recovery");
        interruptible = nbt.getBoolean("interruptible");
        lockMovement = nbt.getBoolean("lockMovement");
        // Support old telegraphColor key for backwards compatibility
        if (nbt.hasKey("windUpColor")) {
            windUpColor = nbt.getInteger("windUpColor");
        } else if (nbt.hasKey("telegraphColor")) {
            windUpColor = nbt.getInteger("telegraphColor");
        }
        if (nbt.hasKey("activeColor")) {
            activeColor = nbt.getInteger("activeColor");
        } else {
            // Default active color to windUpColor with higher alpha
            activeColor = (windUpColor & 0x00FFFFFF) | 0xC0000000;
        }
        // Sound fields (with backwards compatibility for old castSound)
        if (nbt.hasKey("windUpSound")) {
            windUpSound = nbt.getString("windUpSound");
        } else if (nbt.hasKey("castSound")) {
            windUpSound = nbt.getString("castSound"); // Migrate old field
        } else {
            windUpSound = "";
        }
        activeSound = nbt.hasKey("activeSound") ? nbt.getString("activeSound") : "";
        // Animation fields (with backwards compatibility for old animationId)
        if (nbt.hasKey("windUpAnimationId")) {
            windUpAnimationId = nbt.getInteger("windUpAnimationId");
        } else if (nbt.hasKey("animationId")) {
            windUpAnimationId = nbt.getInteger("animationId"); // Migrate old field
        } else {
            windUpAnimationId = -1;
        }
        activeAnimationId = nbt.hasKey("activeAnimationId") ? nbt.getInteger("activeAnimationId") : -1;
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

        // Conditions
        conditions.clear();
        NBTTagList condList = nbt.getTagList("conditions", 10);
        for (int i = 0; i < condList.tagCount(); i++) {
            Condition c = Condition.fromNBT(condList.getCompoundTagAt(i));
            if (c != null) conditions.add(c);
        }

        readTypeNBT(nbt.getCompoundTag("typeData"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTypeId() { return typeId; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public TargetingMode getTargetingMode() { return targetingMode; }
    public void setTargetingMode(TargetingMode targetingMode) { this.targetingMode = targetingMode; }

    public float getMinRange() { return minRange; }
    public void setMinRange(float minRange) { this.minRange = minRange; }

    public float getMaxRange() { return maxRange; }
    public void setMaxRange(float maxRange) { this.maxRange = maxRange; }

    public int getCooldownTicks() { return cooldownTicks; }
    public void setCooldownTicks(int cooldownTicks) { this.cooldownTicks = cooldownTicks; }

    public int getWindUpTicks() { return windUpTicks; }
    public void setWindUpTicks(int windUpTicks) { this.windUpTicks = windUpTicks; }

    public int getActiveTicks() { return activeTicks; }
    public void setActiveTicks(int activeTicks) { this.activeTicks = activeTicks; }

    public int getRecoveryTicks() { return recoveryTicks; }
    public void setRecoveryTicks(int recoveryTicks) { this.recoveryTicks = recoveryTicks; }

    public boolean isInterruptible() { return interruptible; }
    public void setInterruptible(boolean interruptible) { this.interruptible = interruptible; }

    public boolean isLockMovement() { return lockMovement; }
    public void setLockMovement(boolean lockMovement) { this.lockMovement = lockMovement; }

    public int getWindUpColor() { return windUpColor; }
    public void setWindUpColor(int windUpColor) { this.windUpColor = windUpColor; }

    public int getActiveColor() { return activeColor; }
    public void setActiveColor(int activeColor) { this.activeColor = activeColor; }

    /** @deprecated Use getWindUpColor() instead */
    @Deprecated
    public int getTelegraphColor() { return windUpColor; }
    /** @deprecated Use setWindUpColor() instead */
    @Deprecated
    public void setTelegraphColor(int telegraphColor) { this.windUpColor = telegraphColor; }

    public String getWindUpSound() { return windUpSound; }
    public void setWindUpSound(String windUpSound) { this.windUpSound = windUpSound; }

    public String getActiveSound() { return activeSound; }
    public void setActiveSound(String activeSound) { this.activeSound = activeSound; }

    public int getWindUpAnimationId() { return windUpAnimationId; }
    public void setWindUpAnimationId(int windUpAnimationId) { this.windUpAnimationId = windUpAnimationId; }

    public int getActiveAnimationId() { return activeAnimationId; }
    public void setActiveAnimationId(int activeAnimationId) { this.activeAnimationId = activeAnimationId; }

    /** @deprecated Use getWindUpSound() instead */
    @Deprecated
    public String getCastSound() { return windUpSound; }
    /** @deprecated Use setWindUpSound() instead */
    @Deprecated
    public void setCastSound(String castSound) { this.windUpSound = castSound; }

    /** @deprecated Use getWindUpAnimationId() instead */
    @Deprecated
    public int getAnimationId() { return windUpAnimationId; }
    /** @deprecated Use setWindUpAnimationId() instead */
    @Deprecated
    public void setAnimationId(int animationId) { this.windUpAnimationId = animationId; }

    public boolean isShowTelegraph() { return showTelegraph; }
    public void setShowTelegraph(boolean showTelegraph) { this.showTelegraph = showTelegraph; }

    public TelegraphType getTelegraphType() { return telegraphType; }
    public void setTelegraphType(TelegraphType telegraphType) { this.telegraphType = telegraphType; }

    public float getTelegraphHeightOffset() { return telegraphHeightOffset; }
    public void setTelegraphHeightOffset(float telegraphHeightOffset) { this.telegraphHeightOffset = telegraphHeightOffset; }

    public AbilityPhase getPhase() { return phase; }

    /**
     * Get the current execution phase as int (0=IDLE, 1=WINDUP, 2=ACTIVE, 3=RECOVERY).
     * Required for IAbility interface.
     */
    @Override
    public int getPhaseInt() { return phase.ordinal(); }

    public int getCurrentTick() { return currentTick; }
    public EntityLivingBase getCurrentTarget() { return currentTarget; }
    public NBTTagCompound getCustomData() { return customData; }

    public List<Condition> getConditions() { return conditions; }
    public void addCondition(Condition c) { conditions.add(c); }

    // ═══════════════════════════════════════════════════════════════════
    // IAbility API METHODS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public INbt getNbt() {
        return NpcAPI.Instance().getINbt(writeNBT());
    }

    @Override
    public void setNbt(INbt nbt) {
        readNBT(nbt.getMCNBT());
    }
}
