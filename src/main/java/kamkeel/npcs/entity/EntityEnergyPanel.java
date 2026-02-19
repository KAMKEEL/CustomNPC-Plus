package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.EnergyPanelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Energy Panel entity - a flat rectangular barrier used by Wall and Shield abilities.
 * Supports three modes: PLACED (stationary), HELD (tracks caster), LAUNCHED (moves forward).
 * Extends EntityEnergyBarrier for shared barrier logic.
 */
public class EntityEnergyPanel extends EntityEnergyBarrier {

    public enum PanelMode {
        PLACED,     // Stationary wall
        HELD,       // Follows caster, rotates with caster look
        LAUNCHED    // Moves forward, deals damage/knockback on contact
    }

    // ==================== PANEL-SPECIFIC PROPERTIES ====================
    protected EnergyPanelData panelData = new EnergyPanelData();
    protected PanelMode mode = PanelMode.PLACED;
    protected float panelYaw = 0.0f; // Rotation of the panel face (degrees)

    // ==================== CHARGING (panel-specific targets) ====================
    protected float targetPanelWidth;
    protected float targetPanelHeight;

    public EntityEnergyPanel(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
    }

    public EntityEnergyPanel(World world, EntityLivingBase owner, double x, double y, double z,
                             float yaw, PanelMode mode,
                             EnergyDisplayData display, EnergyLightningData lightning,
                             EnergyBarrierData barrier, EnergyPanelData panel) {
        this(world);
        this.setPosition(x, y + panel.heightOffset, z);
        this.ownerEntityId = owner.getEntityId();
        this.panelYaw = yaw;
        this.mode = mode;
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.panelData = panel;
        this.currentHealth = barrier.maxHealth;

        // For launched panels, set initial velocity
        if (mode == PanelMode.LAUNCHED) {
            float yawRad = (float) Math.toRadians(yaw);
            this.motionX = -Math.sin(yawRad) * panel.launchSpeed;
            this.motionY = 0;
            this.motionZ = Math.cos(yawRad) * panel.launchSpeed;
        }
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();

        // Handle charging animation (both sides for smooth rendering)
        if (isCharging()) {
            chargeTick++;
            float progress = getChargeProgress();
            panelData.panelWidth = targetPanelWidth * progress;
            panelData.panelHeight = targetPanelHeight * progress;

            // During charging, held panels still track owner
            if (mode == PanelMode.HELD) {
                updateHeld();
            }
            return; // Don't tick duration/death during charging
        }

        ticksAlive++;

        if (updateBarrierTick()) return;

        // Knockback (not during launched mode)
        if (!worldObj.isRemote && barrierData.knockbackEnabled && mode != PanelMode.LAUNCHED) {
            knockbackEntities();
        }

        // Mode-specific updates
        switch (mode) {
            case HELD:
                updateHeld();
                break;
            case LAUNCHED:
                updateLaunched();
                break;
            case PLACED:
            default:
                // Stationary, no update needed
                break;
        }
    }

    private void updateHeld() {
        Entity owner = getOwnerEntity();
        if (owner == null) return;

        // Update yaw BEFORE position calc to avoid 1-tick lag
        this.panelYaw = owner.rotationYaw;

        // Follow owner position
        float frontDist = 1.5f;
        float yawRad = (float) Math.toRadians(panelYaw);
        double newX = owner.posX + (-Math.sin(yawRad) * frontDist);
        double newY = owner.posY + panelData.heightOffset + (owner.height * 0.5f);
        double newZ = owner.posZ + (Math.cos(yawRad) * frontDist);

        this.setPosition(newX, newY, newZ);
    }

    private void updateLaunched() {
        // Move forward on both sides for smooth client rendering
        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;
        this.setPosition(posX, posY, posZ);

        // Damage and knockback are server-only
        if (!worldObj.isRemote) {
            // Check entity collision for damage
            float halfW = panelData.panelWidth * 0.5f;
            float halfH = panelData.panelHeight * 0.5f;
            float halfD = 0.5f; // Panel collision depth
            float searchRadius = Math.max(halfW, halfD);
            AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
                posX - searchRadius, posY - halfH, posZ - searchRadius,
                posX + searchRadius, posY + halfH, posZ + searchRadius
            );

            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);
            Entity owner = getOwnerEntity();

            for (EntityLivingBase target : entities) {
                if (target == owner) continue;
                if (owner instanceof EntityNPCInterface && target instanceof EntityNPCInterface) {
                    if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) target).faction.id) continue;
                }

                // Apply damage
                if (panelData.launchDamage > 0) {
                    if (owner instanceof EntityNPCInterface) {
                        target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), panelData.launchDamage);
                    } else if (owner instanceof EntityPlayer) {
                        target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), panelData.launchDamage);
                    }
                }

                // Apply knockback
                if (panelData.launchKnockback > 0) {
                    double dx = target.posX - posX;
                    double dz = target.posZ - posZ;
                    double len = Math.sqrt(dx * dx + dz * dz);
                    if (len > 0) {
                        target.addVelocity(
                            (dx / len) * panelData.launchKnockback * 0.5,
                            0.1,
                            (dz / len) * panelData.launchKnockback * 0.5
                        );
                        target.velocityChanged = true;
                    }
                }
            }

            // Max lifetime check
            if (ticksAlive > 200) {
                this.setDead();
            }
        }
    }

    // ==================== INCOMING CHECK ====================

    /**
     * Check if a projectile hits this panel surface.
     * Only blocks incoming projectiles (not from the panel's owner).
     */
    @Override
    public boolean isIncomingProjectile(EntityEnergyProjectile projectile) {
        // Don't block during charging phase
        if (isCharging()) return false;
        if (projectile.getOwnerEntityId() == this.ownerEntityId) return false;

        // Faction check
        Entity owner = getOwnerEntity();
        Entity projOwner = projectile.getOwnerEntity();
        if (owner instanceof EntityNPCInterface && projOwner instanceof EntityNPCInterface) {
            if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) projOwner).faction.id) {
                return false;
            }
        }

        // Check if projectile is within panel bounds
        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        float panelThickness = 0.5f; // Collision thickness

        // Transform projectile position to panel-local space
        float yawRad = (float) Math.toRadians(panelYaw);
        double relX = projectile.posX - this.posX;
        double relY = projectile.posY - this.posY;
        double relZ = projectile.posZ - this.posZ;

        // Rotate into panel space (panel faces along -sin(yaw), cos(yaw))
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        double localForward = relX * (-sin) + relZ * cos;  // Distance along panel normal
        double localRight = relX * cos + relZ * sin;        // Distance along panel width

        // Check bounds
        if (Math.abs(localForward) > panelThickness) return false;
        if (Math.abs(localRight) > halfW) return false;
        if (Math.abs(relY) > halfH) return false;

        // Check incoming direction via dot product with panel normal
        double normalX = -Math.sin(yawRad);
        double normalZ = Math.cos(yawRad);
        double dot = projectile.motionX * normalX + projectile.motionZ * normalZ;

        // Determine which side of the panel the projectile is on
        if (localForward > 0 && dot >= 0) return false; // Moving away from front side
        if (localForward < 0 && dot <= 0) return false; // Moving away from back side

        return true;
    }

    // ==================== CHARGING ====================

    @Override
    public void setupCharging(int duration) {
        this.targetPanelWidth = panelData.panelWidth;
        this.targetPanelHeight = panelData.panelHeight;
        panelData.panelWidth = 0.01f;
        panelData.panelHeight = 0.01f;
        this.chargeDuration = duration;
        this.chargeTick = 0;
        setCharging(true);
    }

    @Override
    public void finishCharging() {
        panelData.panelWidth = targetPanelWidth;
        panelData.panelHeight = targetPanelHeight;
        setCharging(false);
    }

    // ==================== KNOCKBACK ====================

    /**
     * Push entities away from the panel surface.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void knockbackEntities() {
        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        float margin = 1.0f;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - halfW - margin, posY - halfH - margin, posZ - halfW - margin,
            posX + halfW + margin, posY + halfH + margin, posZ + halfW + margin
        );

        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        float yawRad = (float) Math.toRadians(panelYaw);
        float normalX = -(float) Math.sin(yawRad);
        float normalZ = (float) Math.cos(yawRad);

        for (EntityLivingBase ent : entities) {
            if (ent.getEntityId() == ownerEntityId) continue;
            if (!isKnockbackTarget(ent)) continue;

            // Transform entity position to panel-local space
            double dx = ent.posX - posX;
            double dy = (ent.posY + ent.height * 0.5) - posY;
            double dz = ent.posZ - posZ;

            // Check proximity to panel surface
            float localForward = (float) (dx * normalX + dz * normalZ);
            float localRight = (float) (dx * (-normalZ) + dz * normalX);

            if (Math.abs(localForward) < margin && Math.abs(localRight) < halfW + margin && Math.abs(dy) < halfH + margin) {
                // Push entity away from the panel face
                float pushDir = localForward >= 0 ? 1.0f : -1.0f;
                double pushStrength = barrierData.knockbackStrength * 0.5;
                ent.addVelocity(
                    normalX * pushDir * pushStrength,
                    0.1,
                    normalZ * pushDir * pushStrength
                );
                ent.velocityChanged = true;
            }
        }
    }

    // ==================== BOUNDING BOX ====================

    @Override
    public AxisAlignedBB getBoundingBox() {
        if (barrierData.meleeEnabled) {
            float halfW = panelData.panelWidth * 0.5f;
            float halfH = panelData.panelHeight * 0.5f;
            float extent = Math.max(halfW, 0.5f);
            return AxisAlignedBB.getBoundingBox(
                posX - extent, posY - halfH, posZ - extent,
                posX + extent, posY + halfH, posZ + extent
            );
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = Math.max(panelData.panelWidth, panelData.panelHeight) * 8.0D;
        d *= 64.0D;
        return distance < d * d;
    }

    // ==================== GETTERS ====================

    public PanelMode getMode() {
        return mode;
    }

    public float getPanelYaw() {
        return panelYaw;
    }

    public EnergyPanelData getPanelData() {
        return panelData;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        readBarrierBaseNBT(nbt);
        this.panelYaw = nbt.getFloat("PanelYaw");
        int modeOrdinal = nbt.getInteger("PanelMode");
        this.mode = (modeOrdinal >= 0 && modeOrdinal < PanelMode.values().length)
            ? PanelMode.values()[modeOrdinal] : PanelMode.PLACED;
        panelData.readNBT(nbt);
        this.targetPanelWidth = nbt.hasKey("TargetPanelWidth") ? nbt.getFloat("TargetPanelWidth") : panelData.panelWidth;
        this.targetPanelHeight = nbt.hasKey("TargetPanelHeight") ? nbt.getFloat("TargetPanelHeight") : panelData.panelHeight;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        writeBarrierBaseNBT(nbt);
        nbt.setFloat("PanelYaw", panelYaw);
        nbt.setInteger("PanelMode", mode.ordinal());
        nbt.setFloat("TargetPanelWidth", targetPanelWidth);
        nbt.setFloat("TargetPanelHeight", targetPanelHeight);
        panelData.writeNBT(nbt);
    }
}
