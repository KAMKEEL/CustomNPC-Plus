package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilitySlam;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

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
public class AbilitySlam extends Ability {

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
        this.activeTicks = 60; // Max time in air + landing
        this.recoveryTicks = 10;
        this.cooldownTicks = 100;
        this.lockMovement = true;
        this.minRange = 2.0f;
        this.maxRange = 15.0f;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "mob.irongolem.throw";
        this.activeSound = "random.explode";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilitySlam(this, callback);
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
    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Update target position during windup
        if (targetingMode == TargetingMode.AOE_SELF) {
            // AOE_SELF: slam at NPC's current position
            targetX = npc.posX;
            targetY = npc.posY;
            targetZ = npc.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null && !target.isDead) {
            // AOE_TARGET: telegraph follows target via setEntityIdToFollow
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        }
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        // Lock in the destination at moment of launch
        if (targetingMode == TargetingMode.AOE_SELF) {
            // AOE_SELF: slam in place (just jump up)
            targetX = npc.posX;
            targetY = npc.posY;
            targetZ = npc.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null && !target.isDead) {
            // AOE_TARGET: leap to target's current position
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        } else {
            // Fallback: slam in place
            targetX = npc.posX;
            targetY = npc.posY;
            targetZ = npc.posZ;
        }

        hasLaunched = false;
        hasLanded = false;
        airTicks = 0;

        // Reset fall distance to prevent fall damage during slam
        npc.fallDistance = 0;

        // Calculate and apply leap velocity
        launchTowardTarget(npc);
    }

    /**
     * Calculate ballistic arc and launch NPC toward target.
     * Accounts for Minecraft's drag physics to actually reach the destination.
     */
    private void launchTowardTarget(EntityNPCInterface npc) {
        double dx = targetX - npc.posX;
        double dy = targetY - npc.posY;
        double dz = targetZ - npc.posZ;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // For AOE_SELF or very close targets, just hop in place
        if (horizontalDist < 0.5) {
            npc.motionX = 0;
            npc.motionZ = 0;
            npc.motionY = 0.8 * leapSpeed;
            hasLaunched = true;
            npc.setNpcJumpingState(true);
            npc.velocityChanged = true;
            npc.worldObj.playSoundAtEntity(npc, "mob.irongolem.throw", 0.8f, 0.8f);
            return;
        }

        // Cap the distance if too far
        if (horizontalDist > maxRange) {
            double scale = maxRange / horizontalDist;
            dx *= scale;
            dz *= scale;
            horizontalDist = maxRange;
            targetX = npc.posX + dx;
            targetZ = npc.posZ + dz;
        }

        // Choose flight time based on distance - shorter distances = faster
        int flightTicks = (int) Math.max(15, Math.min(horizontalDist * 1.5, 30));

        // Minecraft applies drag of 0.91 per tick to horizontal motion in air
        // Total distance traveled with initial velocity v0 over n ticks:
        // distance = v0 * (1 - drag^n) / (1 - drag)
        // Solving for v0: v0 = distance * (1 - drag) / (1 - drag^n)
        double drag = 0.91;
        double dragPowN = Math.pow(drag, flightTicks);
        double vHorizontal = horizontalDist * (1.0 - drag) / (1.0 - dragPowN);

        // Scale by leap speed
        vHorizontal *= leapSpeed;

        // Vertical velocity calculation:
        // Minecraft gravity is 0.08 per tick, with 0.98 drag on vertical motion
        // For a nice arc, we want to peak roughly in the middle of the flight
        double gravity = 0.08;
        double verticalDrag = 0.98;

        // Calculate required upward velocity to create a nice arc and land at target height
        // Peak height should be proportional to horizontal distance for visual appeal
        double arcHeight = Math.max(1.0, leapHeight) * leapSpeed;

        // Approximate the required initial vertical velocity
        // Account for gravity and drag - use a simplified model
        double peakTicks = flightTicks * 0.4; // Peak around 40% through flight
        double vy = (arcHeight * 2.0 / peakTicks) + (gravity * peakTicks * 0.5);

        // Adjust for height difference
        if (dy > 0) {
            vy += dy / flightTicks * 1.5; // Going up - need more velocity
        } else if (dy < 0) {
            vy += dy / flightTicks * 0.3; // Going down - gravity helps
        }

        // Ensure minimum upward velocity for visible jump
        vy = Math.max(vy, 0.6 * leapSpeed);

        // Normalize horizontal direction and apply velocity
        double dirX = dx / horizontalDist;
        double dirZ = dz / horizontalDist;

        npc.motionX = dirX * vHorizontal;
        npc.motionZ = dirZ * vHorizontal;
        npc.motionY = vy;

        hasLaunched = true;
        npc.setNpcJumpingState(true);
        npc.velocityChanged = true;

        // Face the target
        float targetYaw = (float) (Math.atan2(-dx, dz) * 180.0D / Math.PI);
        npc.rotationYaw = targetYaw;
        npc.rotationYawHead = targetYaw;

    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (!hasLaunched) return;
        if (hasLanded) return;

        airTicks++;

        // Continuously reset fall distance during slam to prevent fall damage
        npc.fallDistance = 0;

        // Check for landing
        if (npc.onGround && airTicks > 3) {
            // Landed!
            onLanding(npc, world);
            return;
        }

        // Timeout protection - force landing after max air time
        if (airTicks >= maxAirTicks) {
            onLanding(npc, world);
            return;
        }

        // While in air, face toward target
        double dx = targetX - npc.posX;
        double dz = targetZ - npc.posZ;
        float targetYaw = (float) (Math.atan2(-dx, dz) * 180.0D / Math.PI);
        npc.rotationYaw = targetYaw;
        npc.rotationYawHead = targetYaw;
    }

    /**
     * Called when NPC lands - deal AOE damage.
     */
    private void onLanding(EntityNPCInterface npc, World world) {
        hasLanded = true;
        npc.setNpcJumpingState(false);

        // Reset fall distance to prevent fall damage on landing
        npc.fallDistance = 0;

        // Stop horizontal momentum
        npc.motionX = 0;
        npc.motionZ = 0;
        npc.velocityChanged = true;

        if (world.isRemote) return;

        // Find all entities in radius
        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(
            npc, npc.boundingBox.expand(radius, radius / 2, radius));

        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase && entity != npc) {
                EntityLivingBase livingTarget = (EntityLivingBase) entity;

                // Check if actually within radius (bounding box is a cube, we want a circle)
                double dx = livingTarget.posX - npc.posX;
                double dz = livingTarget.posZ - npc.posZ;
                if (dx * dx + dz * dz <= radius * radius) {
                    // Apply damage with scripted event support
                    applyAbilityDamage(npc, livingTarget, damage, knockbackStrength);
                }
            }
        }

        // Spawn particles
        spawnSlamParticles(world, npc.posX, npc.posY, npc.posZ);
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
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        npc.setNpcJumpingState(false);
        hasLaunched = false;
        hasLanded = false;
        airTicks = 0;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        npc.setNpcJumpingState(false);
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

    @Override
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
        // Check if telegraph should be shown
        if (!isShowTelegraph() || getTelegraphType() == TelegraphType.NONE) {
            return null;
        }

        // Telegraph shows at landing zone
        if (targetingMode == TargetingMode.AOE_SELF) {
            // AOE_SELF: telegraph at NPC position, does NOT follow
            targetX = npc.posX;
            targetY = npc.posY;
            targetZ = npc.posZ;
        } else if (targetingMode == TargetingMode.AOE_TARGET && target != null) {
            // AOE_TARGET: telegraph at target position, follows target during windup
            targetX = target.posX;
            targetY = target.posY;
            targetZ = target.posZ;
        } else {
            // Fallback to NPC position
            targetX = npc.posX;
            targetY = npc.posY;
            targetZ = npc.posZ;
        }

        // Create telegraph at the appropriate position
        double groundY = findGroundLevel(npc.worldObj, targetX, targetY, targetZ);

        Telegraph telegraph = Telegraph.circle(radius);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        TelegraphInstance instance = new TelegraphInstance(telegraph, targetX, groundY, targetZ, npc.rotationYaw);
        instance.setCasterEntityId(npc.getEntityId());

        // AOE_TARGET: telegraph follows target during windup
        if (targetingMode == TargetingMode.AOE_TARGET && target != null) {
            instance.setEntityIdToFollow(target.getEntityId());
        }
        // AOE_SELF: telegraph stays at NPC position, no follow

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
}
