package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.data.EnergyColorData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sweeper ability entity - a rotating beam attached to the NPC.
 * Unlike other projectiles, this handles its own damage logic since it's
 * attached to and rotates around the caster.
 *
 * Design: Similar to Beam trail visuals but rotating around origin.
 */
public class EntityAbilitySweeper extends Entity implements IEntityAdditionalSpawnData {

    // Beam properties
    private float beamLength = 10.0f;
    private float beamWidth = 0.3f;  // Thin like beam trail
    private float beamHeight = 0.8f;
    private int innerColor = 0xFF6600;
    private int outerColor = 0xFF0000;
    private boolean outerColorEnabled = true;
    private float outerColorWidth = 1.8f;

    // Combat properties
    private float damage = 5.0f;
    private int damageInterval = 5;
    private boolean piercing = true;

    // Rotation
    private float currentAngle = 0;
    private float prevAngle = 0;
    private float sweepSpeed = 3.0f;
    private int numberOfRotations = 2;
    private int completedRotations = 0;
    private float baseYaw = 0;  // Starting yaw direction
    private boolean lockOnTarget = false;

    // Owner tracking
    private int ownerEntityId = -1;
    private int targetEntityId = -1;

    // Lifetime
    private int maxTicks = 400;
    private long deathWorldTime = -1;

    // Damage state
    private transient int ticksSinceDamage = 0;
    private transient Set<Integer> hitThisTick = new HashSet<>();

    public EntityAbilitySweeper(World world) {
        super(world);
        this.setSize(0.1f, 0.1f);
        this.noClip = true;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityAbilitySweeper(World world, EntityLivingBase owner, EntityLivingBase target,
                                 float beamLength, float beamWidth, float beamHeight,
                                 EnergyColorData colorData,
                                 float sweepSpeed, int numberOfRotations,
                                 float damage, int damageInterval, boolean piercing,
                                 boolean lockOnTarget) {
        this(world);

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;
        this.beamLength = beamLength;
        this.beamWidth = beamWidth;
        this.beamHeight = beamHeight;
        this.innerColor = colorData.innerColor;
        this.outerColor = colorData.outerColor;
        this.outerColorEnabled = colorData.outerColorEnabled;
        this.outerColorWidth = colorData.outerColorWidth;
        this.sweepSpeed = sweepSpeed;
        this.numberOfRotations = numberOfRotations;
        this.damage = damage;
        this.damageInterval = damageInterval;
        this.piercing = piercing;
        this.lockOnTarget = lockOnTarget;

        // Calculate max ticks based on rotations
        this.maxTicks = (int) ((360.0f * numberOfRotations) / sweepSpeed) + 10;

        // Calculate base yaw
        if (lockOnTarget && target != null) {
            double dx = target.posX - owner.posX;
            double dz = target.posZ - owner.posZ;
            this.baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            this.baseYaw = owner.rotationYaw;
        }

        // Position at owner
        this.setPosition(owner.posX, owner.posY + beamHeight, owner.posZ);

        // Ready to deal damage immediately
        this.ticksSinceDamage = damageInterval;
    }

    @Override
    protected void entityInit() {
        // No DataWatcher needed
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevAngle = this.currentAngle;

        super.onUpdate();

        // Set death time on first tick
        if (deathWorldTime < 0 && worldObj != null) {
            deathWorldTime = worldObj.getTotalWorldTime() + maxTicks;
        }

        // Check lifetime
        if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
            this.setDead();
            return;
        }

        // Follow owner
        Entity owner = getOwner();
        if (owner == null) {
            this.setDead();
            return;
        }

        // Update position to follow owner
        this.setPosition(owner.posX, owner.posY + beamHeight, owner.posZ);

        // Check if sweep is done
        if (completedRotations >= numberOfRotations) {
            this.setDead();
            return;
        }

        // Update rotation
        currentAngle += sweepSpeed;
        if (currentAngle >= 360) {
            currentAngle -= 360;
            completedRotations++;
            if (!worldObj.isRemote) {
                LogWriter.info("[Sweeper] Completed rotation " + completedRotations + "/" + numberOfRotations);
            }
        }

        // Server handles damage
        if (!worldObj.isRemote) {
            handleDamage(owner);
        }
    }

    /**
     * Handle damage detection and application.
     * This runs on server only.
     */
    private void handleDamage(Entity owner) {
        hitThisTick.clear();
        ticksSinceDamage++;

        if (ticksSinceDamage < damageInterval) {
            return;
        }
        ticksSinceDamage = 0;

        // Calculate beam direction using the ACTUAL render angle
        float beamYaw = baseYaw + currentAngle;
        float yawRad = (float) Math.toRadians(beamYaw);

        double startX = posX;
        double startY = posY;
        double startZ = posZ;

        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        // Sample points along the beam for collision
        int samples = (int) (beamLength / 0.5);
        for (int i = 1; i <= samples; i++) {
            double progress = (double) i / samples;
            double checkX = startX + dirX * beamLength * progress;
            double checkY = startY;
            double checkZ = startZ + dirZ * beamLength * progress;

            // Narrow hitbox for thin beam
            AxisAlignedBB checkBox = AxisAlignedBB.getBoundingBox(
                checkX - beamWidth * 2, checkY - 0.5, checkZ - beamWidth * 2,
                checkX + beamWidth * 2, checkY + 1.0, checkZ + beamWidth * 2
            );

            @SuppressWarnings("unchecked")
            List<Entity> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, checkBox);

            for (Entity entity : entities) {
                if (!(entity instanceof EntityLivingBase)) continue;
                if (entity == owner) continue;
                if (hitThisTick.contains(entity.getEntityId())) continue;

                EntityLivingBase livingEntity = (EntityLivingBase) entity;

                // Check if actually in beam path
                if (isInBeamPath(livingEntity, startX, startY, startZ, dirX, dirZ)) {
                    hitThisTick.add(entity.getEntityId());
                    applyDamage(livingEntity, owner);

                    if (!piercing) {
                        return;
                    }
                }
            }
        }
    }

    private boolean isInBeamPath(EntityLivingBase entity, double startX, double startY, double startZ,
                                 double dirX, double dirZ) {
        double entityX = entity.posX - startX;
        double entityZ = entity.posZ - startZ;

        // Dot product to find projection distance
        double projectionDist = entityX * dirX + entityZ * dirZ;

        // Check if within beam length
        if (projectionDist < 0 || projectionDist > beamLength) {
            return false;
        }

        // Calculate perpendicular distance
        double projX = dirX * projectionDist;
        double projZ = dirZ * projectionDist;
        double perpX = entityX - projX;
        double perpZ = entityZ - projZ;
        double perpDist = Math.sqrt(perpX * perpX + perpZ * perpZ);

        // Check if within beam width (use wider hitbox than visual)
        if (perpDist > beamWidth * 3 + entity.width / 2) {
            return false;
        }

        // Height check - entity's feet must be below beam top (allows jumping over)
        double entityFeetY = entity.posY;
        double beamTopY = startY + 0.5;

        return entityFeetY < beamTopY;
    }

    private void applyDamage(EntityLivingBase target, Entity owner) {
        if (owner instanceof EntityNPCInterface) {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), damage);
        } else if (owner instanceof EntityPlayer) {
            target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), damage);
        } else if (owner instanceof EntityLivingBase) {
            target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), damage);
        } else {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", null), damage);
        }

        // Small upward knockback to help escape
        target.addVelocity(0, 0.2, 0);
        target.velocityChanged = true;
    }

    private Entity getOwner() {
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    // ==================== RENDERING ====================

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 16384.0D; // 128 blocks squared
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1; // Translucent pass
    }

    @Override
    public float getBrightness(float partialTicks) {
        return 1.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    // ==================== GETTERS FOR RENDERER ====================

    public float getBeamLength() {
        return beamLength;
    }

    public float getBeamWidth() {
        return beamWidth;
    }

    public float getBeamHeight() {
        return beamHeight;
    }

    public int getInnerColor() {
        return innerColor;
    }

    public int getOuterColor() {
        return outerColor;
    }

    public boolean isOuterColorEnabled() {
        return outerColorEnabled;
    }

    public float getOuterColorWidth() {
        return outerColorWidth;
    }

    /**
     * Get the full interpolated angle INCLUDING base yaw for rendering.
     */
    public float getInterpolatedAngle(float partialTicks) {
        // Handle wrap-around for the rotating part
        float diff = currentAngle - prevAngle;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;
        float interpAngle = prevAngle + diff * partialTicks;
        // Add base yaw for correct world orientation
        return baseYaw + interpAngle;
    }

    public float getCurrentAngle() {
        return currentAngle;
    }

    public float getBaseYaw() {
        return baseYaw;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.beamLength = nbt.getFloat("BeamLength");
        this.beamWidth = nbt.getFloat("BeamWidth");
        this.beamHeight = nbt.getFloat("BeamHeight");
        this.innerColor = nbt.getInteger("InnerColor");
        this.outerColor = nbt.getInteger("OuterColor");
        this.outerColorEnabled = !nbt.hasKey("OuterColorEnabled") || nbt.getBoolean("OuterColorEnabled");
        this.outerColorWidth = nbt.hasKey("OuterColorWidth") ? nbt.getFloat("OuterColorWidth") : 1.8f;
        this.sweepSpeed = nbt.getFloat("SweepSpeed");
        this.numberOfRotations = nbt.getInteger("NumRotations");
        this.completedRotations = nbt.getInteger("CompletedRotations");
        this.maxTicks = nbt.getInteger("MaxTicks");
        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.targetEntityId = nbt.getInteger("TargetId");
        this.currentAngle = nbt.getFloat("CurrentAngle");
        this.baseYaw = nbt.getFloat("BaseYaw");
        this.deathWorldTime = nbt.getLong("DeathWorldTime");
        this.damage = nbt.getFloat("Damage");
        this.damageInterval = nbt.getInteger("DamageInterval");
        this.piercing = nbt.getBoolean("Piercing");
        this.lockOnTarget = nbt.getBoolean("LockOnTarget");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setFloat("BeamLength", beamLength);
        nbt.setFloat("BeamWidth", beamWidth);
        nbt.setFloat("BeamHeight", beamHeight);
        nbt.setInteger("InnerColor", innerColor);
        nbt.setInteger("OuterColor", outerColor);
        nbt.setBoolean("OuterColorEnabled", outerColorEnabled);
        nbt.setFloat("OuterColorWidth", outerColorWidth);
        nbt.setFloat("SweepSpeed", sweepSpeed);
        nbt.setInteger("NumRotations", numberOfRotations);
        nbt.setInteger("CompletedRotations", completedRotations);
        nbt.setInteger("MaxTicks", maxTicks);
        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setInteger("TargetId", targetEntityId);
        nbt.setFloat("CurrentAngle", currentAngle);
        nbt.setFloat("BaseYaw", baseYaw);
        nbt.setLong("DeathWorldTime", deathWorldTime);
        nbt.setFloat("Damage", damage);
        nbt.setInteger("DamageInterval", damageInterval);
        nbt.setBoolean("Piercing", piercing);
        nbt.setBoolean("LockOnTarget", lockOnTarget);
    }

    // ==================== SPAWN DATA ====================

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeFloat(beamLength);
        buffer.writeFloat(beamWidth);
        buffer.writeFloat(beamHeight);
        buffer.writeInt(innerColor);
        buffer.writeInt(outerColor);
        buffer.writeBoolean(outerColorEnabled);
        buffer.writeFloat(outerColorWidth);
        buffer.writeFloat(sweepSpeed);
        buffer.writeInt(numberOfRotations);
        buffer.writeInt(completedRotations);
        buffer.writeInt(maxTicks);
        buffer.writeInt(ownerEntityId);
        buffer.writeInt(targetEntityId);
        buffer.writeFloat(currentAngle);
        buffer.writeFloat(baseYaw);
        buffer.writeFloat(damage);
        buffer.writeInt(damageInterval);
        buffer.writeBoolean(piercing);
        buffer.writeBoolean(lockOnTarget);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.beamLength = buffer.readFloat();
        this.beamWidth = buffer.readFloat();
        this.beamHeight = buffer.readFloat();
        this.innerColor = buffer.readInt();
        this.outerColor = buffer.readInt();
        this.outerColorEnabled = buffer.readBoolean();
        this.outerColorWidth = buffer.readFloat();
        this.sweepSpeed = buffer.readFloat();
        this.numberOfRotations = buffer.readInt();
        this.completedRotations = buffer.readInt();
        this.maxTicks = buffer.readInt();
        this.ownerEntityId = buffer.readInt();
        this.targetEntityId = buffer.readInt();
        this.currentAngle = buffer.readFloat();
        this.baseYaw = buffer.readFloat();
        this.damage = buffer.readFloat();
        this.damageInterval = buffer.readInt();
        this.piercing = buffer.readBoolean();
        this.lockOnTarget = buffer.readBoolean();
        this.prevAngle = this.currentAngle;
    }
}
