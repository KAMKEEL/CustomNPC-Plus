package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityTrap;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityTrap;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Trap ability: Places a proximity-triggered trap.
 * Features arm time, trigger radius, and various effects on trigger.
 */
public class AbilityTrap extends Ability implements IAbilityTrap {

    public enum TrapPlacement {
        AT_CASTER,
        AT_TARGET,
        AHEAD_OF_CASTER
    }

    private int durationTicks = 200;
    private TrapPlacement placement = TrapPlacement.AT_TARGET;
    private float placementDistance = 5.0f;
    private float triggerRadius = 2.0f;
    private int armTime = 20;
    private int maxTriggers = 1;
    private int triggerCooldown = 20;
    private float damage = 6.0f;
    private float damageRadius = 0.0f;
    private float knockback = 0.5f;
    private boolean visible = true;

    // Offset parameters - trap spawns near target, not exactly on them
    private float minOffset = 0.0f;
    private float maxOffset = 1.5f;
    private boolean randomOffset = true;

    // Runtime state
    private static final Random RANDOM = new Random();
    private transient double trapX, trapY, trapZ;
    private transient boolean armed = false;
    private transient int triggerCount = 0;
    private transient int ticksSinceLastTrigger = 0;
    private transient Set<UUID> triggeredEntities = new HashSet<>();

    public AbilityTrap() {
        this.typeId = "ability.cnpc.trap";
        this.name = "Trap";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.lockMovement = LockMovementType.WINDUP;
        this.cooldownTicks = 0;
        this.windUpTicks = 20;
        this.telegraphType = TelegraphType.CIRCLE;
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityTrap(this, callback);
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
    public float getTelegraphRadius() {
        return triggerRadius;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        TelegraphInstance instance = super.createTelegraph(caster, target);
        if (instance == null) return null;

        // Control telegraph following based on placement mode
        switch (placement) {
            case AT_CASTER:
            case AHEAD_OF_CASTER:
                // Telegraph at caster or ahead, no following
                instance.setEntityIdToFollow(-1);
                if (placement == TrapPlacement.AHEAD_OF_CASTER) {
                    double yaw = Math.toRadians(caster.rotationYaw);
                    instance.setX(caster.posX - Math.sin(yaw) * placementDistance);
                    instance.setY(caster.posY);
                    instance.setZ(caster.posZ + Math.cos(yaw) * placementDistance);
                } else {
                    instance.setX(caster.posX);
                    instance.setY(caster.posY);
                    instance.setZ(caster.posZ);
                }
                break;
            case AT_TARGET:
                // Telegraph follows target during windup, locks on execute
                if (target != null) {
                    instance.setEntityIdToFollow(target.getEntityId());
                }
                break;
        }
        return instance;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        armed = false;
        triggerCount = 0;
        ticksSinceLastTrigger = armTime;
        triggeredEntities.clear();

        // Use telegraph position if available (for AT_TARGET, it was following target)
        TelegraphInstance telegraph = getTelegraphInstance();

        switch (placement) {
            case AT_CASTER:
                trapX = caster.posX;
                trapY = caster.posY;
                trapZ = caster.posZ;
                break;
            case AT_TARGET:
                // Use telegraph position with offset
                if (telegraph != null) {
                    double[] pos = Ability.calculateOffsetPosition(telegraph.getX(), telegraph.getY(), telegraph.getZ(),
                        minOffset, maxOffset, randomOffset, RANDOM);
                    trapX = pos[0];
                    trapY = pos[1];
                    trapZ = pos[2];
                    // Update telegraph to show actual trap position
                    telegraph.setX(trapX);
                    telegraph.setY(trapY);
                    telegraph.setZ(trapZ);
                } else if (target != null) {
                    double[] pos = Ability.calculateOffsetPosition(target.posX, target.posY, target.posZ,
                        minOffset, maxOffset, randomOffset, RANDOM);
                    trapX = pos[0];
                    trapY = pos[1];
                    trapZ = pos[2];
                } else {
                    trapX = caster.posX;
                    trapY = caster.posY;
                    trapZ = caster.posZ;
                }
                break;
            case AHEAD_OF_CASTER:
                double yaw = Math.toRadians(caster.rotationYaw);
                trapX = caster.posX - Math.sin(yaw) * placementDistance;
                trapY = caster.posY;
                trapZ = caster.posZ + Math.cos(yaw) * placementDistance;
                break;
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Check if trap duration has ended
        if (tick >= durationTicks) {
            signalCompletion();
            return;
        }

        if (!armed) {
            if (tick >= armTime) {
                armed = true;
            }
            return;
        }

        ticksSinceLastTrigger++;

        if (maxTriggers > 0 && triggerCount >= maxTriggers) {
            signalCompletion(); // All triggers used, trap is done
            return;
        }

        if (ticksSinceLastTrigger < triggerCooldown) {
            return;
        }

        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            trapX - triggerRadius, trapY - 1, trapZ - triggerRadius,
            trapX + triggerRadius, trapY + 2, trapZ + triggerRadius
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);

        for (EntityLivingBase entity : entities) {
            if (entity == caster) continue;
            if (entity.isDead) continue;
            if (maxTriggers == 1 && triggeredEntities.contains(entity.getUniqueID())) continue;

            double dx = entity.posX - trapX;
            double dz = entity.posZ - trapZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist <= triggerRadius) {
                triggerTrap(caster, entity, world);
                return;
            }
        }
    }

    private void triggerTrap(EntityLivingBase caster, EntityLivingBase triggerer, World world) {
        triggerCount++;
        ticksSinceLastTrigger = 0;
        triggeredEntities.add(triggerer.getUniqueID());

        Set<EntityLivingBase> affected = new HashSet<>();

        if (damageRadius > 0) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                trapX - damageRadius, trapY - 1, trapZ - damageRadius,
                trapX + damageRadius, trapY + 3, trapZ + damageRadius
            );

            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);

            for (EntityLivingBase entity : entities) {
                if (entity == caster) continue;
                double dx = entity.posX - trapX;
                double dz = entity.posZ - trapZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= damageRadius) {
                    affected.add(entity);
                }
            }
        } else {
            affected.add(triggerer);
        }

        for (EntityLivingBase entity : affected) {
            // Apply damage with scripted event support
            boolean wasHit = applyAbilityDamage(caster, entity, damage, knockback);

            // Apply effects if the hit wasn't cancelled
            if (wasHit) {
                applyEffects(entity);
            }
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        armed = false;
        triggerCount = 0;
        triggeredEntities.clear();
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        armed = false;
        triggerCount = 0;
        triggeredEntities.clear();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setString("placement", placement.name());
        nbt.setFloat("placementDistance", placementDistance);
        nbt.setFloat("triggerRadius", triggerRadius);
        nbt.setInteger("armTime", armTime);
        nbt.setInteger("maxTriggers", maxTriggers);
        nbt.setInteger("triggerCooldown", triggerCooldown);
        nbt.setFloat("damage", damage);
        nbt.setFloat("damageRadius", damageRadius);
        nbt.setFloat("knockback", knockback);
        nbt.setBoolean("visible", visible);
        nbt.setFloat("minOffset", minOffset);
        nbt.setFloat("maxOffset", maxOffset);
        nbt.setBoolean("randomOffset", randomOffset);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.hasKey("durationTicks") ? nbt.getInteger("durationTicks") : 200;
        try {
            this.placement = TrapPlacement.valueOf(nbt.getString("placement"));
        } catch (Exception e) {
            this.placement = TrapPlacement.AT_TARGET;
        }
        this.placementDistance = nbt.hasKey("placementDistance") ? nbt.getFloat("placementDistance") : 5.0f;
        this.triggerRadius = nbt.hasKey("triggerRadius") ? nbt.getFloat("triggerRadius") : 2.0f;
        this.armTime = nbt.hasKey("armTime") ? nbt.getInteger("armTime") : 20;
        this.maxTriggers = nbt.hasKey("maxTriggers") ? nbt.getInteger("maxTriggers") : 1;
        this.triggerCooldown = nbt.hasKey("triggerCooldown") ? nbt.getInteger("triggerCooldown") : 20;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 6.0f;
        this.damageRadius = nbt.hasKey("damageRadius") ? nbt.getFloat("damageRadius") : 0.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 0.5f;
        this.visible = !nbt.hasKey("visible") || nbt.getBoolean("visible");
        this.minOffset = nbt.hasKey("minOffset") ? nbt.getFloat("minOffset") : 0.0f;
        this.maxOffset = nbt.hasKey("maxOffset") ? nbt.getFloat("maxOffset") : 1.5f;
        this.randomOffset = !nbt.hasKey("randomOffset") || nbt.getBoolean("randomOffset");
    }

    // Getters & Setters
    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public TrapPlacement getPlacementEnum() {
        return placement;
    }

    public void setPlacementEnum(TrapPlacement placement) {
        this.placement = placement;
    }

    @Override
    public int getPlacement() {
        return placement.ordinal();
    }

    @Override
    public void setPlacement(int placement) {
        TrapPlacement[] values = TrapPlacement.values();
        this.placement = placement >= 0 && placement < values.length ? values[placement] : TrapPlacement.AT_TARGET;
    }

    public float getPlacementDistance() {
        return placementDistance;
    }

    public void setPlacementDistance(float placementDistance) {
        this.placementDistance = placementDistance;
    }

    public float getTriggerRadius() {
        return triggerRadius;
    }

    public void setTriggerRadius(float triggerRadius) {
        this.triggerRadius = triggerRadius;
    }

    public int getArmTime() {
        return armTime;
    }

    public void setArmTime(int armTime) {
        this.armTime = armTime;
    }

    public int getMaxTriggers() {
        return maxTriggers;
    }

    public void setMaxTriggers(int maxTriggers) {
        this.maxTriggers = maxTriggers;
    }

    public int getTriggerCooldown() {
        return triggerCooldown;
    }

    public void setTriggerCooldown(int triggerCooldown) {
        this.triggerCooldown = triggerCooldown;
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

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(float minOffset) {
        this.minOffset = minOffset;
    }

    public float getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(float maxOffset) {
        this.maxOffset = maxOffset;
    }

    public boolean isRandomOffset() {
        return randomOffset;
    }

    public void setRandomOffset(boolean randomOffset) {
        this.randomOffset = randomOffset;
    }

    // Runtime getters
    public double getTrapX() {
        return trapX;
    }

    public double getTrapY() {
        return trapY;
    }

    public double getTrapZ() {
        return trapZ;
    }

    public boolean isArmed() {
        return armed;
    }
}
