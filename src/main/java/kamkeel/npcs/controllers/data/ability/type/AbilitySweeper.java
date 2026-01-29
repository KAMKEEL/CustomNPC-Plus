package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyColorData;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilitySweeper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilitySweeper;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.api.ability.type.IAbilitySweeper;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Sweeper ability: A low sweeping beam that rotates around the NPC.
 * Players can jump over it. The beam completes a set number of rotations before ending.
 * Telegraph shows a large circle on the ground indicating range.
 *
 * All damage logic is handled by the EntityAbilitySweeper entity to ensure
 * visual and damage hitbox alignment.
 */
public class AbilitySweeper extends Ability implements IAbilitySweeper {

    // Type-specific parameters
    private float beamLength = 10.0f;
    private float beamWidth = 0.3f;  // Thin like beam trail
    private float beamHeight = 0.5f;  // Height above ground (low enough to jump over)
    private float damage = 5.0f;
    private int damageInterval = 5;
    private boolean piercing = true;
    private float sweepSpeed = 3.0f;
    private int numberOfRotations = 2;  // How many full rotations before ability ends
    private boolean lockOnTarget = false;

    // Visual properties
    private EnergyColorData colorData = new EnergyColorData(0xFF6600, 0xFF0000, true, 1.8f, 0.5f, 0.0f);

    // Runtime state (transient)
    private transient EntityAbilitySweeper activeEntity = null;

    public AbilitySweeper() {
        this.typeId = "ability.cnpc.sweeper";
        this.name = "Sweeper";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.minRange = 0.0f;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 60;
        // Circle telegraph to show range
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilitySweeper(this, callback);
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
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        // Spawn the entity that handles BOTH visuals AND damage
        activeEntity = new EntityAbilitySweeper(world, npc, target,
            beamLength, beamWidth, beamHeight,
            colorData,
            sweepSpeed, numberOfRotations,
            damage, damageInterval, piercing,
            lockOnTarget);
        world.spawnEntityInWorld(activeEntity);

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (activeEntity == null || activeEntity.isDead) {
            activeEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void cleanup() {
        // Clean up entity if still alive
        if (activeEntity != null && !activeEntity.isDead) {
            activeEntity.setDead();
        }
        activeEntity = null;
    }

    @Override
    public float getTelegraphRadius() {
        return beamLength;
    }

    /**
     * Override to position telegraph at NPC (not target) since the sweep is centered on NPC.
     */
    @Override
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE) {
            return null;
        }

        // Create circle telegraph centered on NPC
        Telegraph telegraph = Telegraph.circle(beamLength);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at NPC, not target
        TelegraphInstance instance = new TelegraphInstance(telegraph, npc.posX, npc.posY, npc.posZ, npc.rotationYaw);
        instance.setCasterEntityId(npc.getEntityId());
        instance.setEntityIdToFollow(npc.getEntityId());  // Follow NPC

        return instance;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("beamLength", beamLength);
        nbt.setFloat("beamWidth", beamWidth);
        nbt.setFloat("beamHeight", beamHeight);
        nbt.setFloat("damage", damage);
        nbt.setInteger("damageInterval", damageInterval);
        nbt.setBoolean("piercing", piercing);
        nbt.setFloat("sweepSpeed", sweepSpeed);
        nbt.setInteger("numberOfRotations", numberOfRotations);
        nbt.setBoolean("lockOnTarget", lockOnTarget);
        colorData.writeNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.beamLength = nbt.hasKey("beamLength") ? nbt.getFloat("beamLength") : 10.0f;
        this.beamWidth = nbt.hasKey("beamWidth") ? nbt.getFloat("beamWidth") : 0.3f;
        this.beamHeight = nbt.hasKey("beamHeight") ? nbt.getFloat("beamHeight") : 0.5f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 5.0f;
        this.damageInterval = nbt.hasKey("damageInterval") ? nbt.getInteger("damageInterval") : 5;
        this.piercing = !nbt.hasKey("piercing") || nbt.getBoolean("piercing");
        this.sweepSpeed = nbt.hasKey("sweepSpeed") ? nbt.getFloat("sweepSpeed") : 3.0f;
        this.numberOfRotations = nbt.hasKey("numberOfRotations") ? nbt.getInteger("numberOfRotations") : 2;
        this.lockOnTarget = nbt.hasKey("lockOnTarget") && nbt.getBoolean("lockOnTarget");
        colorData.readNBT(nbt);
    }

    // Getters & Setters
    public float getBeamLength() { return beamLength; }
    public void setBeamLength(float beamLength) { this.beamLength = beamLength; }
    public float getBeamWidth() { return beamWidth; }
    public void setBeamWidth(float beamWidth) { this.beamWidth = beamWidth; }
    public float getBeamHeight() { return beamHeight; }
    public void setBeamHeight(float beamHeight) { this.beamHeight = beamHeight; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public int getDamageInterval() { return damageInterval; }
    public void setDamageInterval(int damageInterval) { this.damageInterval = damageInterval; }
    public boolean isPiercing() { return piercing; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }
    public float getSweepSpeed() { return sweepSpeed; }
    public void setSweepSpeed(float sweepSpeed) { this.sweepSpeed = sweepSpeed; }
    public int getNumberOfRotations() { return numberOfRotations; }
    public void setNumberOfRotations(int numberOfRotations) { this.numberOfRotations = numberOfRotations; }
    public boolean isLockOnTarget() { return lockOnTarget; }
    public void setLockOnTarget(boolean lockOnTarget) { this.lockOnTarget = lockOnTarget; }
    public int getInnerColor() { return colorData.innerColor; }
    public void setInnerColor(int innerColor) { colorData.innerColor = innerColor; }
    public int getOuterColor() { return colorData.outerColor; }
    public void setOuterColor(int outerColor) { colorData.outerColor = outerColor; }
    public float getOuterColorWidth() { return colorData.outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { colorData.outerColorWidth = outerColorWidth; }
    public boolean isOuterColorEnabled() { return colorData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { colorData.outerColorEnabled = outerColorEnabled; }
}
