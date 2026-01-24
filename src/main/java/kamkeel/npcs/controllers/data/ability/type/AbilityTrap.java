package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityTrap;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Trap ability: Places a proximity-triggered trap.
 * Features arm time, trigger radius, and various effects on trigger.
 */
public class AbilityTrap extends Ability {

    public enum TrapPlacement {
        AT_CASTER,
        AT_TARGET,
        AHEAD_OF_CASTER
    }

    private TrapPlacement placement = TrapPlacement.AT_TARGET;
    private float placementDistance = 5.0f;
    private float triggerRadius = 2.0f;
    private int armTime = 20;
    private int maxTriggers = 1;
    private int triggerCooldown = 20;
    private float damage = 6.0f;
    private float damageRadius = 0.0f;
    private float knockback = 0.5f;
    private int stunDuration = 0;
    private int rootDuration = 40;
    private int slowDuration = 0;
    private int slowLevel = 1;
    private int poisonDuration = 0;
    private int poisonLevel = 0;
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
        this.lockMovement = true;
        this.cooldownTicks = 150;
        this.windUpTicks = 20;
        this.activeTicks = 200;
        this.recoveryTicks = 10;
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
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
        TelegraphInstance instance = super.createTelegraph(npc, target);
        if (instance == null) return null;

        // Control telegraph following based on placement mode
        switch (placement) {
            case AT_CASTER:
            case AHEAD_OF_CASTER:
                // Telegraph at caster or ahead, no following
                instance.setEntityIdToFollow(-1);
                if (placement == TrapPlacement.AHEAD_OF_CASTER) {
                    double yaw = Math.toRadians(npc.rotationYaw);
                    instance.setX(npc.posX - Math.sin(yaw) * placementDistance);
                    instance.setY(npc.posY);
                    instance.setZ(npc.posZ + Math.cos(yaw) * placementDistance);
                } else {
                    instance.setX(npc.posX);
                    instance.setY(npc.posY);
                    instance.setZ(npc.posZ);
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

    /**
     * Calculates offset position near the given coordinates.
     */
    private double[] calculateOffsetPosition(double baseX, double baseY, double baseZ) {
        if (maxOffset <= 0) {
            return new double[]{baseX, baseY, baseZ};
        }

        double offsetDist;
        double offsetAngle;

        if (randomOffset) {
            offsetDist = minOffset + RANDOM.nextDouble() * (maxOffset - minOffset);
            offsetAngle = RANDOM.nextDouble() * Math.PI * 2;
        } else {
            offsetDist = maxOffset;
            offsetAngle = RANDOM.nextDouble() * Math.PI * 2;
        }

        double offsetX = Math.cos(offsetAngle) * offsetDist;
        double offsetZ = Math.sin(offsetAngle) * offsetDist;

        return new double[]{baseX + offsetX, baseY, baseZ + offsetZ};
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        armed = false;
        triggerCount = 0;
        ticksSinceLastTrigger = armTime;
        triggeredEntities.clear();

        // Use telegraph position if available (for AT_TARGET, it was following target)
        TelegraphInstance telegraph = getTelegraphInstance();

        switch (placement) {
            case AT_CASTER:
                trapX = npc.posX;
                trapY = npc.posY;
                trapZ = npc.posZ;
                break;
            case AT_TARGET:
                // Use telegraph position with offset
                if (telegraph != null) {
                    double[] pos = calculateOffsetPosition(telegraph.getX(), telegraph.getY(), telegraph.getZ());
                    trapX = pos[0];
                    trapY = pos[1];
                    trapZ = pos[2];
                } else if (target != null) {
                    double[] pos = calculateOffsetPosition(target.posX, target.posY, target.posZ);
                    trapX = pos[0];
                    trapY = pos[1];
                    trapZ = pos[2];
                } else {
                    trapX = npc.posX;
                    trapY = npc.posY;
                    trapZ = npc.posZ;
                }
                break;
            case AHEAD_OF_CASTER:
                double yaw = Math.toRadians(npc.rotationYaw);
                trapX = npc.posX - Math.sin(yaw) * placementDistance;
                trapY = npc.posY;
                trapZ = npc.posZ + Math.cos(yaw) * placementDistance;
                break;
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (!armed) {
            if (tick >= armTime) {
                armed = true;
            }
            return;
        }

        ticksSinceLastTrigger++;

        if (maxTriggers > 0 && triggerCount >= maxTriggers) {
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
            if (entity == npc) continue;
            if (entity.isDead) continue;
            if (maxTriggers == 1 && triggeredEntities.contains(entity.getUniqueID())) continue;

            double dx = entity.posX - trapX;
            double dz = entity.posZ - trapZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist <= triggerRadius) {
                triggerTrap(npc, entity, world);
                return;
            }
        }
    }

    private void triggerTrap(EntityNPCInterface npc, EntityLivingBase triggerer, World world) {
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
                if (entity == npc) continue;
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
            boolean wasHit = applyAbilityDamage(npc, entity, damage, knockback);

            // Only apply effects if the hit wasn't cancelled
            if (wasHit) {
                if (stunDuration > 0) {
                    entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
                    entity.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
                }

                if (rootDuration > 0) {
                    entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, rootDuration, 127));
                }

                if (slowDuration > 0) {
                    entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, slowDuration, slowLevel));
                }

                if (poisonDuration > 0 && poisonLevel >= 0) {
                    entity.addPotionEffect(new PotionEffect(Potion.poison.id, poisonDuration, poisonLevel));
                }
            }
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        armed = false;
        triggerCount = 0;
        triggeredEntities.clear();
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        armed = false;
        triggerCount = 0;
        triggeredEntities.clear();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setString("placement", placement.name());
        nbt.setFloat("placementDistance", placementDistance);
        nbt.setFloat("triggerRadius", triggerRadius);
        nbt.setInteger("armTime", armTime);
        nbt.setInteger("maxTriggers", maxTriggers);
        nbt.setInteger("triggerCooldown", triggerCooldown);
        nbt.setFloat("damage", damage);
        nbt.setFloat("damageRadius", damageRadius);
        nbt.setFloat("knockback", knockback);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("rootDuration", rootDuration);
        nbt.setInteger("slowDuration", slowDuration);
        nbt.setInteger("slowLevel", slowLevel);
        nbt.setInteger("poisonDuration", poisonDuration);
        nbt.setInteger("poisonLevel", poisonLevel);
        nbt.setBoolean("visible", visible);
        nbt.setFloat("minOffset", minOffset);
        nbt.setFloat("maxOffset", maxOffset);
        nbt.setBoolean("randomOffset", randomOffset);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
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
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.rootDuration = nbt.hasKey("rootDuration") ? nbt.getInteger("rootDuration") : 40;
        this.slowDuration = nbt.hasKey("slowDuration") ? nbt.getInteger("slowDuration") : 0;
        this.slowLevel = nbt.hasKey("slowLevel") ? nbt.getInteger("slowLevel") : 1;
        this.poisonDuration = nbt.hasKey("poisonDuration") ? nbt.getInteger("poisonDuration") : 0;
        this.poisonLevel = nbt.hasKey("poisonLevel") ? nbt.getInteger("poisonLevel") : 0;
        this.visible = !nbt.hasKey("visible") || nbt.getBoolean("visible");
        this.minOffset = nbt.hasKey("minOffset") ? nbt.getFloat("minOffset") : 0.0f;
        this.maxOffset = nbt.hasKey("maxOffset") ? nbt.getFloat("maxOffset") : 1.5f;
        this.randomOffset = !nbt.hasKey("randomOffset") || nbt.getBoolean("randomOffset");
    }

    // Getters & Setters
    public TrapPlacement getPlacement() {
        return placement;
    }

    public void setPlacement(TrapPlacement placement) {
        this.placement = placement;
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

    public int getStunDuration() {
        return stunDuration;
    }

    public void setStunDuration(int stunDuration) {
        this.stunDuration = stunDuration;
    }

    public int getRootDuration() {
        return rootDuration;
    }

    public void setRootDuration(int rootDuration) {
        this.rootDuration = rootDuration;
    }

    public int getSlowDuration() {
        return slowDuration;
    }

    public void setSlowDuration(int slowDuration) {
        this.slowDuration = slowDuration;
    }

    public int getSlowLevel() {
        return slowLevel;
    }

    public void setSlowLevel(int slowLevel) {
        this.slowLevel = slowLevel;
    }

    public int getPoisonDuration() {
        return poisonDuration;
    }

    public void setPoisonDuration(int poisonDuration) {
        this.poisonDuration = poisonDuration;
    }

    public int getPoisonLevel() {
        return poisonLevel;
    }

    public void setPoisonLevel(int poisonLevel) {
        this.poisonLevel = poisonLevel;
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
