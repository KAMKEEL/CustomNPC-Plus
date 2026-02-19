package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilitySweeper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.type.IAbilitySweeper;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Sweeper ability: A low sweeping beam that rotates around the NPC.
 * Players can jump over it. The beam completes a set number of rotations before ending.
 * Telegraph shows a large circle on the ground indicating range.
 * <p>
 * All damage logic is handled by the EntityAbilitySweeper entity to ensure
 * visual and damage hitbox alignment.
 */
public class AbilitySweeper extends AbilityEnergy implements IAbilitySweeper {

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

    // Runtime state (transient)
    private transient EntityAbilitySweeper activeEntity = null;

    public AbilitySweeper() {
        super(new EnergyDisplayData(0xFF6600, 0xFF0000, true, 1.8f, 0.5f, 0.0f));
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
    public boolean allowBurst() {
        return false;
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
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        // Spawn the sweeper entity that handles BOTH visuals AND damage.
        // NPC: target is the aggro target — lockOnTarget tracks it during sweep.
        // Player: target is null — sweep rotates around caster's facing direction, lockOnTarget has no effect.
        activeEntity = new EntityAbilitySweeper(caster.worldObj, caster, target,
            beamLength, beamWidth, beamHeight,
            displayData,
            sweepSpeed, numberOfRotations,
            damage, damageInterval, piercing,
            lockOnTarget);

        if (isPreview()) {
            activeEntity.setupPreview(caster);
        }

        spawnAbilityEntity(activeEntity);
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        // Signal completion when entity dies
        if (activeEntity == null || activeEntity.isDead) {
            activeEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, net.minecraft.util.DamageSource source, float damage) {
        cleanup();
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
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE) {
            return null;
        }

        // Create circle telegraph centered on caster
        Telegraph telegraph = Telegraph.circle(beamLength);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at caster, not target
        TelegraphInstance instance = new TelegraphInstance(telegraph, caster.posX, caster.posY, caster.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(caster.getEntityId());  // Follow caster

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
        writeEnergyNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.beamLength = nbt.getFloat("beamLength");
        this.beamWidth = nbt.getFloat("beamWidth");
        this.beamHeight = nbt.getFloat("beamHeight");
        this.damage = nbt.getFloat("damage");
        this.damageInterval = nbt.getInteger("damageInterval");
        this.piercing = nbt.getBoolean("piercing");
        this.sweepSpeed = nbt.getFloat("sweepSpeed");
        this.numberOfRotations = nbt.getInteger("numberOfRotations");
        this.lockOnTarget = nbt.getBoolean("lockOnTarget");
        readEnergyNBT(nbt);
    }

    // Getters & Setters (type-specific only; color/lightning inherited from AbilityEnergy)
    public float getBeamLength() {
        return beamLength;
    }

    public void setBeamLength(float beamLength) {
        this.beamLength = beamLength;
    }

    public float getBeamWidth() {
        return beamWidth;
    }

    public void setBeamWidth(float beamWidth) {
        this.beamWidth = beamWidth;
    }

    public float getBeamHeight() {
        return beamHeight;
    }

    public void setBeamHeight(float beamHeight) {
        this.beamHeight = beamHeight;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public int getDamageInterval() {
        return damageInterval;
    }

    public void setDamageInterval(int damageInterval) {
        this.damageInterval = damageInterval;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public float getSweepSpeed() {
        return sweepSpeed;
    }

    public void setSweepSpeed(float sweepSpeed) {
        this.sweepSpeed = sweepSpeed;
    }

    public int getNumberOfRotations() {
        return numberOfRotations;
    }

    public void setNumberOfRotations(int numberOfRotations) {
        this.numberOfRotations = numberOfRotations;
    }

    public boolean isLockOnTarget() {
        return lockOnTarget;
    }

    public void setLockOnTarget(boolean lockOnTarget) {
        this.lockOnTarget = lockOnTarget;
    }

    @Override
    public int getMaxPreviewDuration() {
        return (int) ((360.0f * numberOfRotations) / sweepSpeed) + 10;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.intField("ability.damageInterval", this::getDamageInterval, this::setDamageInterval)
            ),
            FieldDef.section("ability.section.beam"),
            FieldDef.row(
                FieldDef.floatField("gui.length", this::getBeamLength, this::setBeamLength).range(0.5f, 100.0f),
                FieldDef.floatField("gui.width", this::getBeamWidth, this::setBeamWidth).range(0.1f, 100.0f)
            ),
            FieldDef.row(
                FieldDef.floatField("gui.height", this::getBeamHeight, this::setBeamHeight).range(0.1f, 100.0f),
                FieldDef.floatField("ability.sweepSpeed", this::getSweepSpeed, this::setSweepSpeed).range(0.1f, 30.0f)
            ),
            FieldDef.intField("ability.rotations", this::getNumberOfRotations, this::setNumberOfRotations).range(1, 20),
            FieldDef.row(
                FieldDef.boolField("ability.piercing", this::isPiercing, this::setPiercing)
                    .hover("ability.hover.piercing"),
                FieldDef.boolField("ability.lockTarget", this::isLockOnTarget, this::setLockOnTarget)
                    .hover("ability.hover.lockTarget")
            ),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));

        // Visual tab - colors + effects (from AbilityEnergy)
        addEnergyVisualDefinitions(defs);
    }
}
