package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityTeleport;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityTeleport;

import java.util.List;
import java.util.Random;

/**
 * Teleport ability: Instant reposition with wall/line-of-sight checks.
 */
public class AbilityTeleport extends Ability implements IAbilityTeleport {

    /**
     * Teleport behavior mode.
     */
    public enum TeleportMode {
        BLINK,
        BEHIND,
        SINGLE
    }

    private static final Random RANDOM = new Random();

    // Type-specific parameters
    private TeleportMode mode = TeleportMode.BLINK;
    private int blinkCount = 3;
    private int blinkDelayTicks = 10;
    private float blinkRadius = 8.0f;
    private float behindDistance = 2.0f;
    private boolean requireLineOfSight = true;
    private boolean damageAtStart = false;
    private boolean damageAtEnd = false;
    private float damage = 5.0f;
    private float damageRadius = 2.0f;

    // Runtime state
    private transient int currentBlink = 0;
    private transient int ticksSinceLastBlink = 0;

    public AbilityTeleport() {
        this.typeId = "ability.cnpc.teleport";
        this.name = "Teleport";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 10;
        // No telegraph for teleport - it's instant repositioning
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpSound = "mob.endermen.portal";
        this.activeSound = "mob.endermen.portal";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
        IAbilityConfigCallback callback) {
        return new SubGuiAbilityTeleport(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        currentBlink = 0;
        ticksSinceLastBlink = blinkDelayTicks; // Trigger first blink immediately
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        int blinkLimit = mode == TeleportMode.BLINK ? blinkCount : 1;
        if (currentBlink >= blinkLimit) {
            signalCompletion();
            return;
        }

        ticksSinceLastBlink++;

        if (ticksSinceLastBlink >= blinkDelayTicks) {
            performBlink(npc, target, world);
            currentBlink++;
            ticksSinceLastBlink = 0;

            // Check if this was the last blink
            if (currentBlink >= blinkLimit) {
                signalCompletion();
            }
        }
    }

    private void performBlink(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote) return;

        double oldX = npc.posX;
        double oldY = npc.posY;
        double oldZ = npc.posZ;

        Vec3 destination = calculateDestination(npc, target, world);
        if (destination == null) return;

        boolean mustHaveLOS = mode == TeleportMode.BEHIND || requireLineOfSight;
        if (mustHaveLOS && !hasLineOfSight(world, oldX, oldY + npc.getEyeHeight(), oldZ,
            destination.xCoord, destination.yCoord + npc.getEyeHeight(), destination.zCoord)) {
            destination = findValidPositionAlongLine(world, npc, oldX, oldY, oldZ,
                destination.xCoord, destination.yCoord, destination.zCoord);
            if (destination == null) return;
        }

        destination = findSafeDestination(world, oldX, oldY, oldZ,
            destination.xCoord, destination.yCoord, destination.zCoord);
        if (destination == null) return;

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
        if (target == null) return null;

        double newX;
        double newY;
        double newZ;

        switch (mode) {
            case BEHIND:
                double yaw = Math.toRadians(target.rotationYaw);
                newX = target.posX + Math.sin(yaw) * behindDistance;
                newZ = target.posZ - Math.cos(yaw) * behindDistance;
                newY = target.posY;
                return Vec3.createVectorHelper(newX, newY, newZ);
            case SINGLE:
            case BLINK:
            default:
                double angle = RANDOM.nextDouble() * Math.PI * 2;
                double dist = Math.min(blinkRadius, maxRange);
                double offset = RANDOM.nextDouble() * dist;
                newX = target.posX + Math.cos(angle) * offset;
                newZ = target.posZ + Math.sin(angle) * offset;
                newY = target.posY;
                return Vec3.createVectorHelper(newX, newY, newZ);
        }
    }

    private Vec3 findSafeDestination(World world, double oldX, double oldY, double oldZ,
                                     double destX, double destY, double destZ) {
        int oldBlockX = (int) Math.floor(oldX);
        int oldBlockY = (int) Math.floor(oldY);
        int oldBlockZ = (int) Math.floor(oldZ);
        int destBlockX = (int) Math.floor(destX);
        int destBlockY = (int) Math.floor(destY);
        int destBlockZ = (int) Math.floor(destZ);

        if (oldBlockX == destBlockX && oldBlockY == destBlockY && oldBlockZ == destBlockZ) {
            return null;
        }

        double safeY = findSafeYNear(world, destX, destY, destZ);
        Vec3 safe = Vec3.createVectorHelper(destX, safeY, destZ);

        if (!isSafeLocation(world, (int) Math.floor(safe.xCoord),
            (int) Math.floor(safe.yCoord), (int) Math.floor(safe.zCoord))) {
            return null;
        }

        return safe;
    }

    private double findSafeYNear(World world, double x, double baseY, double z) {
        int[] offsets = new int[]{0, 1, -1, 2, -2};
        for (int offset : offsets) {
            double y = baseY + offset;
            if (isSafeLocation(world, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z))) {
                return y;
            }
        }
        return findSafeY(world, x, baseY, z);
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

            // Skip out-of-world coordinates
            if (blockY < 0 || blockY >= 256) {
                continue;
            }

            Block block = world.getBlock(blockX, blockY, blockZ);
            if (block != null && block.getMaterial().isSolid() && block.isOpaqueCube()) {
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

        if (distance < 1.0) return null;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        Vec3 lastValid = null;
        double step = 0.5;

        for (double d = 1.0; d < distance; d += step) {
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
        // Bounds check - ensure we're within valid world coordinates
        if (y < 1 || y >= 255) {
            return false;
        }

        Block groundBlock = world.getBlock(x, y - 1, z);
        Block feetBlock = world.getBlock(x, y, z);
        Block headBlock = world.getBlock(x, y + 1, z);

        // Defensive null checks (shouldn't happen in MC 1.7.10, but safe)
        if (groundBlock == null || feetBlock == null || headBlock == null) {
            return false;
        }

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
                boolean wasHit = applyAbilityDamage(npc, living, damage, 0);
                if (wasHit) {
                    applyEffects(living);
                }
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
        nbt.setString("mode", mode.name());
        nbt.setInteger("blinkCount", blinkCount);
        nbt.setInteger("blinkDelayTicks", blinkDelayTicks);
        nbt.setFloat("blinkRadius", blinkRadius);
        nbt.setFloat("behindDistance", behindDistance);
        nbt.setBoolean("requireLineOfSight", requireLineOfSight);
        nbt.setBoolean("damageAtStart", damageAtStart);
        nbt.setBoolean("damageAtEnd", damageAtEnd);
        nbt.setFloat("damage", damage);
        nbt.setFloat("damageRadius", damageRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("mode")) {
            try {
                this.mode = TeleportMode.valueOf(nbt.getString("mode"));
            } catch (Exception e) {
                this.mode = TeleportMode.BLINK;
            }
        } else if (nbt.hasKey("pattern")) {
            String legacy = nbt.getString("pattern");
            if ("BEHIND_TARGET".equals(legacy)) {
                this.mode = TeleportMode.BEHIND;
            } else if ("RANDOM".equals(legacy)) {
                this.mode = TeleportMode.BLINK;
            } else {
                this.mode = TeleportMode.SINGLE;
            }
        } else {
            this.mode = TeleportMode.BLINK;
        }
        this.blinkCount = nbt.hasKey("blinkCount") ? nbt.getInteger("blinkCount") : 3;
        this.blinkDelayTicks = nbt.hasKey("blinkDelayTicks") ? nbt.getInteger("blinkDelayTicks") : 10;
        this.blinkRadius = nbt.hasKey("blinkRadius") ? nbt.getFloat("blinkRadius") : 8.0f;
        this.behindDistance = nbt.hasKey("behindDistance") ? nbt.getFloat("behindDistance") : 2.0f;
        this.requireLineOfSight = !nbt.hasKey("requireLineOfSight") || nbt.getBoolean("requireLineOfSight");
        this.damageAtStart = nbt.hasKey("damageAtStart") && nbt.getBoolean("damageAtStart");
        this.damageAtEnd = nbt.hasKey("damageAtEnd") && nbt.getBoolean("damageAtEnd");
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 5.0f;
        this.damageRadius = nbt.hasKey("damageRadius") ? nbt.getFloat("damageRadius") : 2.0f;
    }

    // Getters & Setters
    public TeleportMode getModeEnum() {
        return mode;
    }

    public void setModeEnum(TeleportMode mode) {
        this.mode = mode;
    }

    @Override
    public int getMode() {
        return mode.ordinal();
    }

    @Override
    public void setMode(int mode) {
        TeleportMode[] values = TeleportMode.values();
        this.mode = mode >= 0 && mode < values.length ? values[mode] : TeleportMode.BLINK;
    }

    public int getBlinkCount() {
        return blinkCount;
    }

    public void setBlinkCount(int blinkCount) {
        this.blinkCount = blinkCount;
    }

    public int getBlinkDelayTicks() {
        return blinkDelayTicks;
    }

    public void setBlinkDelayTicks(int blinkDelayTicks) {
        this.blinkDelayTicks = blinkDelayTicks;
    }

    public float getBlinkRadius() {
        return blinkRadius;
    }

    public void setBlinkRadius(float blinkRadius) {
        this.blinkRadius = blinkRadius;
    }

    public float getBehindDistance() {
        return behindDistance;
    }

    public void setBehindDistance(float behindDistance) {
        this.behindDistance = behindDistance;
    }

    public boolean isRequireLineOfSight() {
        return requireLineOfSight;
    }

    public void setRequireLineOfSight(boolean requireLineOfSight) {
        this.requireLineOfSight = requireLineOfSight;
    }

    public boolean isDamageAtStart() {
        return damageAtStart;
    }

    public void setDamageAtStart(boolean damageAtStart) {
        this.damageAtStart = damageAtStart;
    }

    public boolean isDamageAtEnd() {
        return damageAtEnd;
    }

    public void setDamageAtEnd(boolean damageAtEnd) {
        this.damageAtEnd = damageAtEnd;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamageRadius() {
        return damageRadius;
    }

    public void setDamageRadius(float damageRadius) {
        this.damageRadius = damageRadius;
    }
}
