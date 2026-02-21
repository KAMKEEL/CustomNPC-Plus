package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyDome;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Energy Dome ability: Spawns a spherical barrier centered on the caster.
 * Blocks incoming energy projectiles with configurable damage multipliers.
 * Duration and/or HP based.
 */
public class AbilityDome extends AbilityEnergyBarrier {

    private float domeRadius = 5.0f;
    private boolean followCaster = false;

    public AbilityDome() {
        super(
            new EnergyDisplayData(0x44CCFF, 0x2288FF, true, 0.3f, 0.35f, 0.5f, 0.0f),
            new EnergyBarrierData(100.0f, true, 200, true)
        );
        this.typeId = "ability.cnpc.dome";
        this.name = "Dome";
        this.targetingMode = TargetingMode.SELF;
        this.cooldownTicks = 100;
        this.windUpTicks = 30;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.RING;
        this.showTelegraph = true;
        this.windUpAnimationName = "";
        this.activeAnimationName = "";
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityEnergyBarrier createBarrierEntity(EntityLivingBase caster, EntityLivingBase target) {
        EntityEnergyDome dome = new EntityEnergyDome(
            caster.worldObj, caster,
            caster.posX, caster.posY, caster.posZ,
            domeRadius, displayData.copy(), lightningData.copy(), barrierData.copy()
        );
        dome.setFollowCaster(followCaster);
        dome.setSourceAbility(this);
        return dome;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.SELF};
    }

    @Override
    public float getTelegraphRadius() {
        return domeRadius;
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.small", a -> {
                AbilityDome dome = (AbilityDome) a;
                a.setName("Small Dome");
                dome.setDomeRadius(3.0f);
                dome.setBarrierMaxHealth(60.0f);
            }),
            new AbilityVariant("ability.variant.large", a -> {
                AbilityDome dome = (AbilityDome) a;
                a.setName("Large Dome");
                dome.setDomeRadius(8.0f);
                dome.setBarrierMaxHealth(200.0f);
                dome.setBarrierDuration(300);
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeBarrierTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("domeRadius", domeRadius);
        nbt.setBoolean("followCaster", followCaster);
    }

    @Override
    protected void readBarrierTypeNBT(NBTTagCompound nbt) {
        this.domeRadius = nbt.hasKey("domeRadius") ? nbt.getFloat("domeRadius") : 5.0f;
        this.followCaster = nbt.hasKey("followCaster") && nbt.getBoolean("followCaster");
    }

    // ==================== GETTERS & SETTERS ====================

    public float getDomeRadius() {
        return domeRadius;
    }

    public void setDomeRadius(float radius) {
        this.domeRadius = Math.max(1.0f, radius);
    }

    public boolean isFollowCaster() {
        return followCaster;
    }

    public void setFollowCaster(boolean follow) {
        this.followCaster = follow;
    }

    // ==================== TYPE-SPECIFIC GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addBarrierTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("ability.domeRadius", this::getDomeRadius, this::setDomeRadius)
            .range(1.0f, 50.0f));
        defs.add(FieldDef.boolField("ability.followCaster", this::isFollowCaster, this::setFollowCaster)
            .hover("ability.hover.followCaster"));
    }
}
