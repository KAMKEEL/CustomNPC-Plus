package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.api.ability.type.IAbilitySlam;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Arrays;
import java.util.List;

/**
 * Slam ability: NPC leaps toward target in an arc and slams down, dealing AOE damage on landing.
 * <p>
 * Phases:
 * - WINDUP: NPC prepares to leap, telegraph shows landing zone
 * - ACTIVE: NPC is in the air, traveling in arc toward destination
 * - Landing: When NPC hits ground, deal AOE damage
 * - RECOVERY: Landing recovery animation
 */
public class AbilitySlam extends Ability implements IAbilitySlam {

    // Type-specific config
    private float damage = 10.0f;
    private float radius = 5.0f;
    private float knockbackStrength = 1.5f;
    private float leapSpeed = 1.0f;
    private float leapHeight = 4.0f;

    /**
     * Grace period (in active ticks) before landing detection activates.
     * Players need more time because:
     * - Server sets motionY and sends S12 velocity packet to client
     * - Client receives S12 and starts moving upward
     * - Client sends C03 position packet back to server with onGround=false
     * Until the round trip completes, the server's player.onGround (from stale C03)
     * remains true, which would falsely trigger landing detection.
     */
    private static final int PLAYER_LANDING_GRACE_TICKS = 10;
    private static final int NPC_LANDING_GRACE_TICKS = 3;

    // Runtime state
    private transient double targetX, targetY, targetZ;
    private transient double startY;       // Y position at launch for rise detection
    private transient boolean hasLaunched = false;
    private transient boolean hasLanded = false;
    private transient boolean hasRisen = false; // Whether entity has risen above launch position
    private transient int airTicks = 0;
    private transient int maxAirTicks = 60; // Timeout to prevent stuck in air

    public AbilitySlam() {
        this.typeId = "ability.cnpc.slam";
        this.name = "Slam";
        this.targetingMode = TargetingMode.AOE_SELF; // Can also be AOE_TARGET to leap to target
        this.windUpTicks = 30;
        this.cooldownTicks = 0;
        this.lockMovement = LockMode.WINDUP;
        this.minRange = 2.0f;
        this.maxRange = 15.0f;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "mob.irongolem.throw";
        this.activeSound = "";
        // Default built-in animations
        this.windUpAnimationName = "Ability_Slam_Windup";
        this.activeAnimationName = "Ability_Slam_Active";
    }

    /**
     * Slam can be AOE_SELF (jump in place) or AOE_TARGET (leap to target).
     */
    @Override
    public boolean isTargetingModeLocked() {
        return false;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AOE_SELF, TargetingMode.AOE_TARGET};
    }

    @Override
    public boolean hasAbilityMovement() {
        return true; // This ability moves the NPC
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (isPlayerCaster(caster)) {
            // Player: update target along look direction during windup
            if (targetingMode == TargetingMode.AOE_SELF) {
                targetX = caster.posX;
                targetY = caster.posY;
                targetZ = caster.posZ;
            } else {
                float yawRad = (float) Math.toRadians(caster.rotationYaw);
                double dirX = -Math.sin(yawRad);
                double dirZ = Math.cos(yawRad);
                double launchDist = Math.max(4.0, maxRange * 0.5);
                targetX = caster.posX + dirX * launchDist;
                targetY = caster.posY;
                targetZ = caster.posZ + dirZ * launchDist;
            }
        } else {
            // NPC: update target position during windup for telegraph tracking
            if (targetingMode == TargetingMode.AOE_SELF) {
                targetX = caster.posX;
                targetY = caster.posY;
                targetZ = caster.posZ;
            } else if (targetingMode == TargetingMode.AOE_TARGET && target != null && !target.isDead) {
                targetX = target.posX;
                targetY = target.posY;
                targetZ = target.posZ;
            }
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        hasLaunched = false;
        hasLanded = false;
        hasRisen = false;
        airTicks = 0;
        startY = caster.posY;
        caster.fallDistance = 0;

        if (isPlayerCaster(caster)) {
            executePlayerSlam(caster);
        } else {
            executeNpcSlam(caster, target);
        }
    }

    /**
     * NPC slam: Lock destination based on targeting mode, then launch in a ballistic arc toward it.
     */
    private void executeNpcSlam(EntityLivingBase caster, EntityLivingBase target) {
        if (targetingMode == TargetingMode.AOE_SELF) {
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null && !target.isDead) {
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        } else {
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        }
        launchTowardTarget(caster);
    }

    /**
     * Player slam: Launch in look direction using same ballistic arc as NPC slam.
     * AOE damage triggers wherever the player lands.
     */
    private void executePlayerSlam(EntityLivingBase caster) {
        // Calculate target position along player's look direction (horizontal only)
        float yawRad = (float) Math.toRadians(caster.rotationYaw);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        double launchDist = Math.max(4.0, maxRange * 0.5);

        targetX = caster.posX + dirX * launchDist;
        targetY = caster.posY;
        targetZ = caster.posZ + dirZ * launchDist;

        launchTowardTarget(caster);
    }

    /**
     * Calculate the initial vertical velocity needed to reach a target height
     * in Minecraft's physics (gravity=0.08, vertical drag=0.98).
     * Uses iterative simulation since the drag makes a closed-form solution impractical.
     */
    private static double calculateLaunchVelocity(double targetHeight) {
        // Binary search for the initial velocity that reaches targetHeight
        double lo = 0.0, hi = 10.0;
        for (int iter = 0; iter < 30; iter++) {
            double mid = (lo + hi) / 2.0;
            double maxY = simulateMaxHeight(mid);
            if (maxY < targetHeight) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return (lo + hi) / 2.0;
    }

    /**
     * Simulate Minecraft vertical physics to find peak height reached
     * with a given initial upward velocity.
     */
    private static double simulateMaxHeight(double vy) {
        double y = 0;
        double maxY = 0;
        for (int t = 0; t < 200 && vy > 0; t++) {
            y += vy;
            vy -= 0.08;  // gravity
            vy *= 0.98;  // vertical drag
            if (y > maxY) maxY = y;
        }
        return maxY;
    }

    /**
     * Calculate ballistic arc and launch caster toward target.
     * leapHeight = exact peak height in blocks above launch point.
     * leapSpeed = velocity multiplier (faster motion, same height).
     */
    private void launchTowardTarget(EntityLivingBase caster) {
        double dx = targetX - caster.posX;
        double dy = targetY - caster.posY;
        double dz = targetZ - caster.posZ;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Calculate vertical velocity to reach exact leapHeight
        double arcHeight = Math.max(1.0, leapHeight);
        double vy = calculateLaunchVelocity(arcHeight);

        // For AOE_SELF or very close targets, just hop in place
        if (horizontalDist < 0.5) {
            caster.motionX = 0;
            caster.motionZ = 0;
            caster.motionY = vy;
            hasLaunched = true;
            if (!isPreview() && caster instanceof EntityNPCInterface) {
                ((EntityNPCInterface) caster).setNpcJumpingState(true);
            }
            if (!isPreview()) {
                caster.velocityChanged = true;
                caster.worldObj.playSoundAtEntity(caster, "mob.irongolem.throw", 0.8f, 0.8f);
            }
            return;
        }

        // Cap the distance if too far
        if (horizontalDist > maxRange) {
            double scale = maxRange / horizontalDist;
            dx *= scale;
            dz *= scale;
            horizontalDist = maxRange;
            targetX = caster.posX + dx;
            targetZ = caster.posZ + dz;
        }

        // Choose flight time based on distance, scaled by speed (faster = fewer ticks)
        double speedFactor = Math.max(0.1, leapSpeed);
        int flightTicks = (int) Math.max(10, Math.min(horizontalDist * 1.5 / speedFactor, 40));

        // Minecraft applies drag of 0.91 per tick to horizontal motion in air
        // Total distance traveled with initial velocity v0 over n ticks:
        // distance = v0 * (1 - drag^n) / (1 - drag)
        // Solving for v0: v0 = distance * (1 - drag) / (1 - drag^n)
        double drag = 0.91;
        double dragPowN = Math.pow(drag, flightTicks);
        double vHorizontal = horizontalDist * (1.0 - drag) / (1.0 - dragPowN);

        // Adjust vertical velocity for height difference between start and target
        if (dy > 0) {
            vy += dy / flightTicks * 1.5; // Going up - need more velocity
        } else if (dy < 0) {
            vy += dy / flightTicks * 0.3; // Going down - gravity helps
        }

        // Normalize horizontal direction and apply velocity
        double dirX = dx / horizontalDist;
        double dirZ = dz / horizontalDist;

        caster.motionX = dirX * vHorizontal;
        caster.motionZ = dirZ * vHorizontal;
        caster.motionY = vy;

        hasLaunched = true;
        if (!isPreview() && caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(true);
        }
        if (!isPreview()) {
            caster.velocityChanged = true;
        }

        // Face the target
        float targetYaw = (float) (Math.atan2(-dx, dz) * 180.0D / Math.PI);
        caster.rotationYaw = targetYaw;
        caster.rotationYawHead = targetYaw;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (!hasLaunched) return;
        if (hasLanded) return;

        airTicks++;

        // Continuously reset fall distance during slam to prevent fall damage
        if (!isPreview()) {
            caster.fallDistance = 0;
        }

        // Prevent NPC navigator from interfering with ballistic arc.
        // lockMovement=WINDUP means applyMovementControl() doesn't clear
        // the navigator during ACTIVE, so AI pathfinding could add motion.
        if (!isPreview() && caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).getNavigator().clearPathEntity();
        }

        // Track if entity has risen above launch position.
        // This prevents premature landing detection for players, where
        // server-side caster.onGround remains true from stale C03 packets
        // until the velocity S12 round-trip completes (can take 4-8+ ticks).
        if (!hasRisen && caster.posY > startY + 0.5) {
            hasRisen = true;
        }

        // Landing detection uses different grace periods for NPC vs Player.
        // NPCs: server physics is authoritative, onGround is accurate after 3 ticks.
        // Players: onGround depends on C03 packets, needs longer grace + rise confirmation.
        int graceTicks = isPlayerCaster(caster) ? PLAYER_LANDING_GRACE_TICKS : NPC_LANDING_GRACE_TICKS;
        if (airTicks > graceTicks) {
            if (hasRisen && (caster.onGround || caster.isCollidedHorizontally)) {
                onLanding(caster, caster.worldObj);
                return;
            }
            // Safety: if entity hasn't risen after triple the grace period,
            // the launch may have failed (blocked by ceiling, etc.) - force landing
            if (!hasRisen && airTicks > graceTicks * 3) {
                onLanding(caster, caster.worldObj);
                return;
            }
        }

        // Timeout protection - force landing after max air time
        if (airTicks >= maxAirTicks) {
            onLanding(caster, caster.worldObj);
            return;
        }

        // Face toward target destination while in air (both NPC and Player)
        if (!isPreview()) {
            double dx = targetX - caster.posX;
            double dz = targetZ - caster.posZ;
            if (dx * dx + dz * dz > 0.25) {
                float targetYaw = (float) (Math.atan2(-dx, dz) * 180.0D / Math.PI);
                caster.rotationYaw = targetYaw;
                caster.rotationYawHead = targetYaw;
                if (isPlayerCaster(caster)) {
                    caster.velocityChanged = true;
                }
            }
        }
    }

    /**
     * Called when caster lands - deal AOE damage and signal completion.
     */
    private void onLanding(EntityLivingBase caster, World world) {
        hasLanded = true;
        if (!isPreview() && caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(false);
        }

        // Signal that the ability has completed its active phase
        signalCompletion();

        // Stop horizontal momentum
        caster.motionX = 0;
        caster.motionZ = 0;

        if (!isPreview()) {
            // Reset fall distance to prevent fall damage on landing
            caster.fallDistance = 0;
            caster.velocityChanged = true;
        }

        if (world.isRemote && !isPreview()) return;
        if (isPreview()) return;

        // Play slam impact sound on landing
        world.playSoundAtEntity(caster, "random.explode", 1.0f, 1.0f);

        // Find all entities in radius
        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(
            caster, caster.boundingBox.expand(radius, radius / 2, radius));

        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase && entity != caster) {
                EntityLivingBase livingTarget = (EntityLivingBase) entity;
                if (!AbilityTargetHelper.shouldAffect(caster, livingTarget, TargetFilter.ENEMIES, false)) continue;

                // Check if actually within radius (bounding box is a cube, we want a circle)
                double dx = livingTarget.posX - caster.posX;
                double dz = livingTarget.posZ - caster.posZ;
                if (dx * dx + dz * dz <= radius * radius) {
                    // Apply damage with scripted event support
                    boolean wasHit = applyAbilityDamage(caster, livingTarget, damage, knockbackStrength);
                    if (wasHit) {
                        applyEffects(livingTarget);
                    }
                }
            }
        }

        // Spawn particles
        spawnSlamParticles(world, caster.posX, caster.posY, caster.posZ);
    }

    /**
     * Spawn visual particles for the slam effect.
     */
    private void spawnSlamParticles(World world, double x, double y, double z) {
        // Spawn explosion particles in a ring
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double px = x + Math.cos(angle) * radius * 0.8;
            double pz = z + Math.sin(angle) * radius * 0.8;
            world.spawnParticle("explode", px, y + 0.5, pz, 0, 0.1, 0);
        }

        // Spawn smoke at center
        for (int i = 0; i < 10; i++) {
            world.spawnParticle("smoke", x, y + 0.2, z,
                (world.rand.nextDouble() - 0.5) * 0.3,
                world.rand.nextDouble() * 0.2,
                (world.rand.nextDouble() - 0.5) * 0.3);
        }

        // Ground crack particles - check if block is valid
        net.minecraft.block.Block groundBlock = world.getBlock((int) x, (int) y - 1, (int) z);
        if (groundBlock != null && groundBlock.getMaterial().isSolid()) {
            int blockId = net.minecraft.block.Block.getIdFromBlock(groundBlock);
            if (blockId > 0) {
                for (int i = 0; i < 15; i++) {
                    double angle = world.rand.nextDouble() * Math.PI * 2;
                    double dist = world.rand.nextDouble() * radius * 0.6;
                    double px = x + Math.cos(angle) * dist;
                    double pz = z + Math.sin(angle) * dist;
                    world.spawnParticle("blockcrack_" + blockId + "_0", px, y + 0.1, pz, 0, 0.2, 0);
                }
            }
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        if (!isPreview() && caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(false);
        }
        hasLaunched = false;
        hasLanded = false;
        hasRisen = false;
        airTicks = 0;
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        if (!isPreview() && caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(false);
        }
        hasLaunched = false;
        hasLanded = false;
        hasRisen = false;
        airTicks = 0;
    }

    @Override
    public void cleanup() {
        hasLaunched = false;
        hasLanded = false;
        hasRisen = false;
        airTicks = 0;
    }

    @Override
    public void reset() {
        super.reset();
        hasLaunched = false;
        hasLanded = false;
        hasRisen = false;
        airTicks = 0;
    }

    @Override
    public int getMaxPreviewDuration() {
        return 60;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        // Check if telegraph should be shown
        if (!isShowTelegraph() || getTelegraphType() == TelegraphType.NONE) {
            return null;
        }

        // Telegraph shows at landing zone
        if (targetingMode == TargetingMode.AOE_SELF) {
            // AOE_SELF: telegraph at caster position, does NOT follow
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null) {
            // AOE_TARGET: telegraph at target position, follows target during windup
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        } else {
            // Fallback to caster position
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        }

        // Create telegraph at the appropriate position
        double groundY = findGroundLevel(caster.worldObj, targetX, targetY, targetZ);

        Telegraph telegraph = Telegraph.circle(radius);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        TelegraphInstance instance = new TelegraphInstance(telegraph, targetX, groundY, targetZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());

        // NPC AOE_TARGET: telegraph follows target during windup
        // Player: telegraph stays at caster position (no target to follow)
        if (!isPlayerCaster(caster) && targetingMode == TargetingMode.AOE_TARGET && target != null) {
            instance.setEntityIdToFollow(target.getEntityId());
        }

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return radius;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("radius", radius);
        nbt.setFloat("knockback", knockbackStrength);
        nbt.setFloat("leapSpeed", leapSpeed);
        nbt.setFloat("leapHeight", leapHeight);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        damage = nbt.getFloat("damage");
        radius = nbt.getFloat("radius");
        knockbackStrength = nbt.getFloat("knockback");
        leapSpeed = nbt.getFloat("leapSpeed");
        leapHeight = nbt.getFloat("leapHeight");
    }

    // Getters & Setters
    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getKnockbackStrength() {
        return knockbackStrength;
    }

    public void setKnockbackStrength(float knockbackStrength) {
        this.knockbackStrength = knockbackStrength;
    }

    public float getLeapSpeed() {
        return leapSpeed;
    }

    public void setLeapSpeed(float leapSpeed) {
        this.leapSpeed = leapSpeed;
    }

    public float getLeapHeight() {
        return leapHeight;
    }

    public void setLeapHeight(float leapHeight) {
        this.leapHeight = leapHeight;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getRadius, this::setRadius),
                FieldDef.floatField("ability.knockback", this::getKnockbackStrength, this::setKnockbackStrength)
            ),
            FieldDef.section("ability.section.leap"),
            FieldDef.row(
                FieldDef.floatField("gui.speed", this::getLeapSpeed, this::setLeapSpeed),
                FieldDef.floatField("gui.height", this::getLeapHeight, this::setLeapHeight)
            ),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
