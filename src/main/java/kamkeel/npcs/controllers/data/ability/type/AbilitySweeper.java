package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
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
    private int innerColor = 0xFF6600;
    private int outerColor = 0xFF0000;
    private float outerColorWidth = 1.8f;
    private boolean outerColorEnabled = true;

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

        LogWriter.info("[Sweeper] onExecute called at NPC pos " + npc.posX + ", " + npc.posY + ", " + npc.posZ);

        // Spawn the entity that handles BOTH visuals AND damage
        activeEntity = new EntityAbilitySweeper(world, npc, target,
            beamLength, beamWidth, beamHeight,
            innerColor, outerColor, outerColorEnabled, outerColorWidth,
            sweepSpeed, numberOfRotations,
            damage, damageInterval, piercing,
            lockOnTarget);
        world.spawnEntityInWorld(activeEntity);

        LogWriter.info("[Sweeper] Spawned sweeper entity");
        // Entity manages itself - completion signaled from onActiveTick when entity dies
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Entity handles everything - check if entity finished its rotations
        if (activeEntity == null || activeEntity.isDead) {
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
        nbt.setInteger("innerColor", innerColor);
        nbt.setInteger("outerColor", outerColor);
        nbt.setFloat("outerColorWidth", outerColorWidth);
        nbt.setBoolean("outerColorEnabled", outerColorEnabled);
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
        this.innerColor = nbt.hasKey("innerColor") ? nbt.getInteger("innerColor") : 0xFF6600;
        this.outerColor = nbt.hasKey("outerColor") ? nbt.getInteger("outerColor") : 0xFF0000;
        this.outerColorWidth = nbt.hasKey("outerColorWidth") ? nbt.getFloat("outerColorWidth") : 1.8f;
        this.outerColorEnabled = !nbt.hasKey("outerColorEnabled") || nbt.getBoolean("outerColorEnabled");
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
    public int getInnerColor() { return innerColor; }
    public void setInnerColor(int innerColor) { this.innerColor = innerColor; }
    public int getOuterColor() { return outerColor; }
    public void setOuterColor(int outerColor) { this.outerColor = outerColor; }
    public float getOuterColorWidth() { return outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { this.outerColorWidth = outerColorWidth; }
    public boolean isOuterColorEnabled() { return outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { this.outerColorEnabled = outerColorEnabled; }
}
