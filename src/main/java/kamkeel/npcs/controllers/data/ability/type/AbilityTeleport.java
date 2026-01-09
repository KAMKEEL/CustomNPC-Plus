package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;
import java.util.Random;

/**
 * Teleport ability: Instant reposition with wall/line-of-sight checks.
 */
public class AbilityTeleport extends Ability {

    /**
     * Pattern for teleport destination.
     */
    public enum TeleportPattern {
        RANDOM,
        TOWARD_TARGET,
        AWAY_FROM_TARGET,
        BEHIND_TARGET,
        IN_FRONT_OF_TARGET,
        AROUND_TARGET,
        TO_TARGET
    }

    private static final Random RANDOM = new Random();

    // Type-specific parameters
    private int blinkCount = 1;
    private int blinkDelayTicks = 10;
    private float blinkRadius = 8.0f;
    private float minBlinkRadius = 3.0f;
    private TeleportPattern pattern = TeleportPattern.RANDOM;
    private boolean requireLineOfSight = true;
    private boolean damageAtStart = false;
    private boolean damageAtEnd = false;
    private float damage = 5.0f;
    private float damageRadius = 2.0f;

    // Runtime state
    private transient int currentBlink = 0;
    private transient int ticksSinceLastBlink = 0;

    public AbilityTeleport() {
        this.typeId = "cnpc:teleport";
        this.name = "Teleport";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 100;
        this.windUpTicks = 10;
        this.activeTicks = 40;
        this.recoveryTicks = 10;
        // No telegraph for teleport - it's instant repositioning
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        currentBlink = 0;
        ticksSinceLastBlink = blinkDelayTicks; // Trigger first blink immediately
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (currentBlink >= blinkCount) return;

        ticksSinceLastBlink++;

        if (ticksSinceLastBlink >= blinkDelayTicks) {
            performBlink(npc, target, world);
            currentBlink++;
            ticksSinceLastBlink = 0;
        }
    }

    private void performBlink(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote) return;

        double oldX = npc.posX;
        double oldY = npc.posY;
        double oldZ = npc.posZ;

        Vec3 destination = calculateDestination(npc, target, world);
        if (destination == null) return;

        // Verify line of sight if required
        if (requireLineOfSight && !hasLineOfSight(world, oldX, oldY + npc.getEyeHeight(), oldZ,
                                                   destination.xCoord, destination.yCoord + npc.getEyeHeight(), destination.zCoord)) {
            destination = findValidPositionAlongLine(world, npc, oldX, oldY, oldZ,
                                                      destination.xCoord, destination.yCoord, destination.zCoord);
            if (destination == null) return;
        }

        // Verify destination is safe
        if (!isSafeLocation(world, (int)Math.floor(destination.xCoord),
                           (int)Math.floor(destination.yCoord),
                           (int)Math.floor(destination.zCoord))) {
            double safeY = findSafeY(world, destination.xCoord, destination.yCoord, destination.zCoord);
            destination = Vec3.createVectorHelper(destination.xCoord, safeY, destination.zCoord);

            if (!isSafeLocation(world, (int)Math.floor(destination.xCoord),
                               (int)Math.floor(destination.yCoord),
                               (int)Math.floor(destination.zCoord))) {
                return;
            }
        }

        // Damage at origin
        if (damageAtStart) {
            dealDamageAt(npc, world, oldX, oldY, oldZ);
        }

        // Spawn particles at origin
        spawnTeleportParticles(world, oldX, oldY, oldZ);

        // Teleport
        npc.setPositionAndUpdate(destination.xCoord, destination.yCoord, destination.zCoord);
        npc.fallDistance = 0;

        // Spawn particles at destination
        spawnTeleportParticles(world, destination.xCoord, destination.yCoord, destination.zCoord);

        // Play teleport sound
        world.playSoundAtEntity(npc, "mob.endermen.portal", 1.0f, 1.0f);

        // Damage at destination
        if (damageAtEnd) {
            dealDamageAt(npc, world, destination.xCoord, destination.yCoord, destination.zCoord);
        }

        // Face target after teleport
        if (target != null) {
            double dx = target.posX - destination.xCoord;
            double dz = target.posZ - destination.zCoord;
            float newYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            npc.rotationYaw = newYaw;
            npc.rotationYawHead = newYaw;
        }
    }

    private void spawnTeleportParticles(World world, double x, double y, double z) {
        for (int i = 0; i < 20; i++) {
            world.spawnParticle("portal",
                x + (RANDOM.nextDouble() - 0.5) * 2,
                y + RANDOM.nextDouble() * 2,
                z + (RANDOM.nextDouble() - 0.5) * 2,
                (RANDOM.nextDouble() - 0.5) * 0.5,
                RANDOM.nextDouble() * 0.5,
                (RANDOM.nextDouble() - 0.5) * 0.5);
        }
    }

    private Vec3 calculateDestination(EntityNPCInterface npc, EntityLivingBase target, World world) {
        double newX, newY, newZ;

        switch (pattern) {
            case TOWARD_TARGET:
                if (target != null) {
                    double dx = target.posX - npc.posX;
                    double dz = target.posZ - npc.posZ;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > 0) {
                        double blinkDist = Math.min(blinkRadius, dist - 2.0);
                        blinkDist = Math.max(minBlinkRadius, blinkDist);
                        newX = npc.posX + (dx / dist) * blinkDist;
                        newZ = npc.posZ + (dz / dist) * blinkDist;
                        newY = findSafeY(world, newX, npc.posY, newZ);
                        return Vec3.createVectorHelper(newX, newY, newZ);
                    }
                }
                return null;

            case AWAY_FROM_TARGET:
                if (target != null) {
                    double dx = npc.posX - target.posX;
                    double dz = npc.posZ - target.posZ;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > 0) {
                        dx /= dist;
                        dz /= dist;
                    } else {
                        dx = RANDOM.nextDouble() - 0.5;
                        dz = RANDOM.nextDouble() - 0.5;
                        double len = Math.sqrt(dx * dx + dz * dz);
                        dx /= len;
                        dz /= len;
                    }
                    double blinkDist = minBlinkRadius + RANDOM.nextDouble() * (blinkRadius - minBlinkRadius);
                    newX = npc.posX + dx * blinkDist;
                    newZ = npc.posZ + dz * blinkDist;
                    newY = findSafeY(world, newX, npc.posY, newZ);
                    return Vec3.createVectorHelper(newX, newY, newZ);
                }
                return null;

            case BEHIND_TARGET:
                if (target != null) {
                    float targetYaw = (float) Math.toRadians(target.rotationYaw);
                    double behindX = target.posX + Math.sin(targetYaw) * 3.0;
                    double behindZ = target.posZ - Math.cos(targetYaw) * 3.0;
                    newY = findSafeY(world, behindX, target.posY, behindZ);
                    return Vec3.createVectorHelper(behindX, newY, behindZ);
                }
                return null;

            case IN_FRONT_OF_TARGET:
                if (target != null) {
                    float targetYaw = (float) Math.toRadians(target.rotationYaw);
                    double frontX = target.posX - Math.sin(targetYaw) * 3.0;
                    double frontZ = target.posZ + Math.cos(targetYaw) * 3.0;
                    newY = findSafeY(world, frontX, target.posY, frontZ);
                    return Vec3.createVectorHelper(frontX, newY, frontZ);
                }
                return null;

            case AROUND_TARGET:
                if (target != null) {
                    double angle = RANDOM.nextDouble() * Math.PI * 2;
                    double dist = minBlinkRadius + RANDOM.nextDouble() * (blinkRadius - minBlinkRadius);
                    newX = target.posX + Math.cos(angle) * dist;
                    newZ = target.posZ + Math.sin(angle) * dist;
                    newY = findSafeY(world, newX, target.posY, newZ);
                    return Vec3.createVectorHelper(newX, newY, newZ);
                }
                return null;

            case TO_TARGET:
                if (target != null) {
                    return Vec3.createVectorHelper(target.posX, target.posY, target.posZ);
                }
                return null;

            case RANDOM:
            default:
                double angle = RANDOM.nextDouble() * Math.PI * 2;
                double blinkDist = minBlinkRadius + RANDOM.nextDouble() * (blinkRadius - minBlinkRadius);
                newX = npc.posX + Math.cos(angle) * blinkDist;
                newZ = npc.posZ + Math.sin(angle) * blinkDist;
                newY = findSafeY(world, newX, npc.posY, newZ);
                return Vec3.createVectorHelper(newX, newY, newZ);
        }
    }

    private boolean hasLineOfSight(World world, double x1, double y1, double z1,
                                   double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.1) return true;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        double step = 0.5;
        for (double d = step; d < distance; d += step) {
            double checkX = x1 + dx * d;
            double checkY = y1 + dy * d;
            double checkZ = z1 + dz * d;

            int blockX = MathHelper.floor_double(checkX);
            int blockY = MathHelper.floor_double(checkY);
            int blockZ = MathHelper.floor_double(checkZ);

            Block block = world.getBlock(blockX, blockY, blockZ);
            if (block.getMaterial().isSolid() && block.isOpaqueCube()) {
                return false;
            }
        }

        return true;
    }

    private Vec3 findValidPositionAlongLine(World world, EntityNPCInterface npc,
                                            double x1, double y1, double z1,
                                            double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < minBlinkRadius) return null;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        Vec3 lastValid = null;
        double step = 0.5;

        for (double d = minBlinkRadius; d < distance; d += step) {
            double checkX = x1 + dx * d;
            double checkY = y1 + dy * d;
            double checkZ = z1 + dz * d;

            if (hasLineOfSight(world, x1, y1 + npc.getEyeHeight(), z1,
                              checkX, checkY + npc.getEyeHeight(), checkZ)) {
                int blockX = MathHelper.floor_double(checkX);
                int blockY = MathHelper.floor_double(checkY);
                int blockZ = MathHelper.floor_double(checkZ);

                if (isSafeLocation(world, blockX, blockY, blockZ)) {
                    lastValid = Vec3.createVectorHelper(checkX, checkY, checkZ);
                }
            } else {
                break;
            }
        }

        return lastValid;
    }

    private double findSafeY(World world, double x, double baseY, double z) {
        int blockX = MathHelper.floor_double(x);
        int blockZ = MathHelper.floor_double(z);
        int blockY = MathHelper.floor_double(baseY);

        for (int offset = 0; offset <= 5; offset++) {
            if (isSafeLocation(world, blockX, blockY + offset, blockZ)) {
                return blockY + offset;
            }
            if (offset > 0 && isSafeLocation(world, blockX, blockY - offset, blockZ)) {
                return blockY - offset;
            }
        }

        return baseY;
    }

    private boolean isSafeLocation(World world, int x, int y, int z) {
        Block groundBlock = world.getBlock(x, y - 1, z);
        Block feetBlock = world.getBlock(x, y, z);
        Block headBlock = world.getBlock(x, y + 1, z);

        return groundBlock.getMaterial().isSolid() &&
               !feetBlock.getMaterial().isSolid() &&
               !headBlock.getMaterial().isSolid();
    }

    private void dealDamageAt(EntityNPCInterface npc, World world, double x, double y, double z) {
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            x - damageRadius, y - 1, z - damageRadius,
            x + damageRadius, y + 2, z + damageRadius
        );

        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase)) continue;
            if (entity == npc) continue;

            EntityLivingBase living = (EntityLivingBase) entity;
            double dist = Math.sqrt(Math.pow(living.posX - x, 2) + Math.pow(living.posZ - z, 2));
            if (dist <= damageRadius) {
                // Apply damage with scripted event support (no knockback for teleport damage)
                applyAbilityDamage(npc, living, damage, 0, 0);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        currentBlink = 0;
        ticksSinceLastBlink = 0;
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for teleport
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("blinkCount", blinkCount);
        nbt.setInteger("blinkDelayTicks", blinkDelayTicks);
        nbt.setFloat("blinkRadius", blinkRadius);
        nbt.setFloat("minBlinkRadius", minBlinkRadius);
        nbt.setString("pattern", pattern.name());
        nbt.setBoolean("requireLineOfSight", requireLineOfSight);
        nbt.setBoolean("damageAtStart", damageAtStart);
        nbt.setBoolean("damageAtEnd", damageAtEnd);
        nbt.setFloat("damage", damage);
        nbt.setFloat("damageRadius", damageRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.blinkCount = nbt.hasKey("blinkCount") ? nbt.getInteger("blinkCount") : 1;
        this.blinkDelayTicks = nbt.hasKey("blinkDelayTicks") ? nbt.getInteger("blinkDelayTicks") : 10;
        this.blinkRadius = nbt.hasKey("blinkRadius") ? nbt.getFloat("blinkRadius") : 8.0f;
        this.minBlinkRadius = nbt.hasKey("minBlinkRadius") ? nbt.getFloat("minBlinkRadius") : 3.0f;
        try {
            this.pattern = TeleportPattern.valueOf(nbt.getString("pattern"));
        } catch (Exception e) {
            this.pattern = TeleportPattern.RANDOM;
        }
        this.requireLineOfSight = !nbt.hasKey("requireLineOfSight") || nbt.getBoolean("requireLineOfSight");
        this.damageAtStart = nbt.hasKey("damageAtStart") && nbt.getBoolean("damageAtStart");
        this.damageAtEnd = nbt.hasKey("damageAtEnd") && nbt.getBoolean("damageAtEnd");
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 5.0f;
        this.damageRadius = nbt.hasKey("damageRadius") ? nbt.getFloat("damageRadius") : 2.0f;
    }

    // Getters & Setters
    public int getBlinkCount() { return blinkCount; }
    public void setBlinkCount(int blinkCount) { this.blinkCount = blinkCount; }

    public int getBlinkDelayTicks() { return blinkDelayTicks; }
    public void setBlinkDelayTicks(int blinkDelayTicks) { this.blinkDelayTicks = blinkDelayTicks; }

    public float getBlinkRadius() { return blinkRadius; }
    public void setBlinkRadius(float blinkRadius) { this.blinkRadius = blinkRadius; }

    public float getMinBlinkRadius() { return minBlinkRadius; }
    public void setMinBlinkRadius(float minBlinkRadius) { this.minBlinkRadius = minBlinkRadius; }

    public TeleportPattern getPattern() { return pattern; }
    public void setPattern(TeleportPattern pattern) { this.pattern = pattern; }

    public boolean isRequireLineOfSight() { return requireLineOfSight; }
    public void setRequireLineOfSight(boolean requireLineOfSight) { this.requireLineOfSight = requireLineOfSight; }

    public boolean isDamageAtStart() { return damageAtStart; }
    public void setDamageAtStart(boolean damageAtStart) { this.damageAtStart = damageAtStart; }

    public boolean isDamageAtEnd() { return damageAtEnd; }
    public void setDamageAtEnd(boolean damageAtEnd) { this.damageAtEnd = damageAtEnd; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getDamageRadius() { return damageRadius; }
    public void setDamageRadius(float damageRadius) { this.damageRadius = damageRadius; }
}
