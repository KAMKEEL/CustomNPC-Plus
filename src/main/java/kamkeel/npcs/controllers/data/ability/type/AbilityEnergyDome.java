package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityEnergyDome;
import net.minecraft.entity.Entity;
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
public class AbilityEnergyDome extends AbstractEnergyBarrierAbility {

    private float domeRadius = 5.0f;

    public AbilityEnergyDome() {
        super(
            new EnergyDisplayData(0x44CCFF, 0x2288FF, true, 0.3f, 0.35f, 0.0f),
            new EnergyBarrierData(100.0f, true, 200, true)
        );
        this.typeId = "ability.cnpc.energy_dome";
        this.name = "Energy Dome";
        this.targetingMode = TargetingMode.SELF;
        this.maxRange = 0;
        this.minRange = 0;
        this.cooldownTicks = 100;
        this.windUpTicks = 30;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.RING;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_EnergyDome_Windup";
        this.activeAnimationName = "Ability_EnergyDome_Active";
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected Entity createBarrierEntity(EntityLivingBase caster, EntityLivingBase target) {
        EntityEnergyDome dome = new EntityEnergyDome(
            caster.worldObj, caster,
            caster.posX, caster.posY, caster.posZ,
            domeRadius, displayData.copy(), lightningData.copy(), barrierData.copy()
        );
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
                AbilityEnergyDome dome = (AbilityEnergyDome) a;
                a.setName("Small Energy Dome");
                dome.setDomeRadius(3.0f);
                dome.setBarrierMaxHealth(60.0f);
            }),
            new AbilityVariant("ability.variant.large", a -> {
                AbilityEnergyDome dome = (AbilityEnergyDome) a;
                a.setName("Large Energy Dome");
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
    }

    @Override
    protected void readBarrierTypeNBT(NBTTagCompound nbt) {
        this.domeRadius = nbt.hasKey("domeRadius") ? nbt.getFloat("domeRadius") : 5.0f;
    }

    // ==================== GETTERS & SETTERS ====================

    public float getDomeRadius() { return domeRadius; }
    public void setDomeRadius(float radius) { this.domeRadius = Math.max(1.0f, radius); }

    // ==================== TYPE-SPECIFIC GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addBarrierTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("ability.domeRadius", this::getDomeRadius, this::setDomeRadius)
            .range(1.0f, 30.0f));
    }
}
