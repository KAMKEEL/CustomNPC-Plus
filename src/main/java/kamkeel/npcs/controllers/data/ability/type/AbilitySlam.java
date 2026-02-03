package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilitySlam;
import somehussar.gui.annotationHandling.GuiEditable;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
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

    // Runtime state
    private transient double targetX, targetY, targetZ;
    private transient boolean hasLaunched = false;
    private transient boolean hasLanded = false;
    private transient int airTicks = 0;
    private transient int maxAirTicks = 60; // Timeout to prevent stuck in air

    public AbilitySlam() {
        this.typeId = "ability.cnpc.slam";
        this.name = "Slam";
        this.targetingMode = TargetingMode.AOE_SELF; // Can also be AOE_TARGET to leap to target
        this.windUpTicks = 30;
        this.cooldownTicks = 0;
        this.lockMovement = LockMovementType.WINDUP;
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
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Update target position during windup
        if (targetingMode == TargetingMode.AOE_SELF) {
            // AOE_SELF: slam at caster's current position
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null && !target.isDead) {
            // AOE_TARGET: telegraph follows target via setEntityIdToFollow
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        // Lock in the destination at moment of launch
        if (targetingMode == TargetingMode.AOE_SELF) {
            // AOE_SELF: slam in place (just jump up)
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null && !target.isDead) {
            // AOE_TARGET: leap to target's current position
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        } else {
            // Fallback: slam in place
            targetX = caster.posX;
            targetY = caster.posY;
            targetZ = caster.posZ;
        }

        hasLaunched = false;
        hasLanded = false;
        airTicks = 0;

        // Reset fall distance to prevent fall damage during slam
        caster.fallDistance = 0;

        // Calculate and apply leap velocity
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
            if (caster instanceof EntityNPCInterface) {
                ((EntityNPCInterface) caster).setNpcJumpingState(true);
            }
            caster.velocityChanged = true;
            caster.worldObj.playSoundAtEntity(caster, "mob.irongolem.throw", 0.8f, 0.8f);
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

        // Speed scales horizontal velocity to cover ground faster
        vHorizontal *= speedFactor;

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
        if (caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(true);
        }
        caster.velocityChanged = true;

        // Face the target
        float targetYaw = (float) (Math.atan2(-dx, dz) * 180.0D / Math.PI);
        caster.rotationYaw = targetYaw;
        caster.rotationYawHead = targetYaw;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (!hasLaunched) return;
        if (hasLanded) return;

        airTicks++;

        // Continuously reset fall distance during slam to prevent fall damage
        caster.fallDistance = 0;

        // Check for landing
        if (caster.onGround && airTicks > 3) {
            // Landed!
            onLanding(caster, world);
            return;
        }

        // Timeout protection - force landing after max air time
        if (airTicks >= maxAirTicks) {
            onLanding(caster, world);
            return;
        }

        // While in air, face toward target
        double dx = targetX - caster.posX;
        double dz = targetZ - caster.posZ;
        float targetYaw = (float) (Math.atan2(-dx, dz) * 180.0D / Math.PI);
        caster.rotationYaw = targetYaw;
        caster.rotationYawHead = targetYaw;
    }

    /**
     * Called when caster lands - deal AOE damage and signal completion.
     */
    private void onLanding(EntityLivingBase caster, World world) {
        hasLanded = true;
        if (caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(false);
        }

        // Signal that the ability has completed its active phase
        signalCompletion();

        // Reset fall distance to prevent fall damage on landing
        caster.fallDistance = 0;

        // Stop horizontal momentum
        caster.motionX = 0;
        caster.motionZ = 0;
        caster.velocityChanged = true;

        if (world.isRemote) return;

        // Play slam impact sound on landing
        world.playSoundAtEntity(caster, "random.explode", 1.0f, 1.0f);

        // Find all entities in radius
        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(
            caster, caster.boundingBox.expand(radius, radius / 2, radius));

        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase && entity != caster) {
                EntityLivingBase livingTarget = (EntityLivingBase) entity;

                // Check if actually within radius (bounding box is a cube, we want a circle)
                double dx = livingTarget.posX - caster.posX;
                double dz = livingTarget.posZ - caster.posZ;
                if (dx * dx + dz * dz <= radius * radius) {
                    // Apply damage with scripted event support
                    applyAbilityDamage(caster, livingTarget, damage, knockbackStrength);
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
        if (caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(false);
        }
        hasLaunched = false;
        hasLanded = false;
        airTicks = 0;
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        if (caster instanceof EntityNPCInterface) {
            ((EntityNPCInterface) caster).setNpcJumpingState(false);
        }
        hasLaunched = false;
        hasLanded = false;
        airTicks = 0;
    }

    @Override
    public void reset() {
        super.reset();
        hasLaunched = false;
        hasLanded = false;
        airTicks = 0;
    }

    // ==================== PREVIEW MODE ====================

    private transient double previewVelX, previewVelY, previewVelZ;
    private transient boolean previewLaunched = false;
    private transient double previewGroundY;

    @Override
    @SideOnly(Side.CLIENT)
    public void onPreviewExecute(EntityNPCInterface npc) {
        previewLaunched = false;

        double tx, ty, tz;
        if (previewTarget != null && targetingMode == TargetingMode.AOE_TARGET) {
            tx = previewTarget.posX;
            ty = previewTarget.posY;
            tz = previewTarget.posZ;
        } else {
            tx = npc.posX;
            ty = npc.posY;
            tz = npc.posZ;
        }

        previewGroundY = npc.posY;

        double dx = tx - npc.posX;
        double dz = tz - npc.posZ;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Calculate vertical velocity for exact leapHeight
        double arcHeight = Math.max(1.0, leapHeight);
        double vy = calculateLaunchVelocity(arcHeight);

        if (horizontalDist < 0.5) {
            previewVelX = 0;
            previewVelZ = 0;
            previewVelY = vy;
        } else {
            if (horizontalDist > maxRange) {
                double scale = maxRange / horizontalDist;
                dx *= scale;
                dz *= scale;
                horizontalDist = maxRange;
            }

            double speedFactor = Math.max(0.1, leapSpeed);
            int flightTicks = (int) Math.max(10, Math.min(horizontalDist * 1.5 / speedFactor, 40));
            double drag = 0.91;
            double dragPowN = Math.pow(drag, flightTicks);
            double vHorizontal = horizontalDist * (1.0 - drag) / (1.0 - dragPowN) * speedFactor;

            double dirX = dx / horizontalDist;
            double dirZ = dz / horizontalDist;
            previewVelX = dirX * vHorizontal;
            previewVelZ = dirZ * vHorizontal;
            previewVelY = vy;
        }
        previewLaunched = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onPreviewActiveTick(EntityNPCInterface npc, int tick) {
        if (!previewLaunched) return;

        // Apply velocity
        npc.prevPosX = npc.posX;
        npc.prevPosY = npc.posY;
        npc.prevPosZ = npc.posZ;

        npc.posX += previewVelX;
        npc.posY += previewVelY;
        npc.posZ += previewVelZ;

        // Gravity and drag
        previewVelY -= 0.08;
        previewVelY *= 0.98;
        previewVelX *= 0.91;
        previewVelZ *= 0.91;

        // Ground clamp - stop falling below starting Y
        if (npc.posY < previewGroundY && previewVelY < 0) {
            npc.posY = previewGroundY;
            previewVelY = 0;
            previewVelX = 0;
            previewVelZ = 0;
            previewLaunched = false;
        }
    }

    @Override
    public int getPreviewActiveDuration() {
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

        // AOE_TARGET: telegraph follows target during windup
        if (targetingMode == TargetingMode.AOE_TARGET && target != null) {
            instance.setEntityIdToFollow(target.getEntityId());
        }
        // AOE_SELF: telegraph stays at caster position, no follow

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
        damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 10.0f;
        radius = nbt.hasKey("radius") ? nbt.getFloat("radius") : 5.0f;
        knockbackStrength = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.5f;
        leapSpeed = nbt.hasKey("leapSpeed") ? nbt.getFloat("leapSpeed") : 1.0f;
        leapHeight = nbt.hasKey("leapHeight") ? nbt.getFloat("leapHeight") : 4.0f;
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
