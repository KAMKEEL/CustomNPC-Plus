package kamkeel.npcs.controllers.data.ability.type.energy;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import java.util.Arrays;
import java.util.List;

/**
 * Energy Dome ability: Spawns a spherical barrier centered on the caster.
 * Blocks incoming energy projectiles with configurable damage multipliers.
 * Duration and/or HP based.
 */
public class AbilityDome extends AbilityBarrier {

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
        this.lockMovement = LockMode.WINDUP;
        this.telegraphType = TelegraphType.RING;
        this.showTelegraph = true;
        this.windUpAnimationName = "";
        this.activeAnimationName = "";

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/dome.png",
                () -> isOuterColorEnabled() ? getOuterColor() : getInnerColor())
        };
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityEnergyBarrier createBarrierEntity(IEntityLivingBase caster, IEntityLivingBase target) {
        EntityEnergyDome dome = new EntityEnergyDome(
            caster.worldObj, caster,
            caster.posX + offsetX, caster.posY + offsetY, caster.posZ + offsetZ,
            domeRadius, displayData.copy(), lightningData.copy(), barrierData.copy()
        );
        dome.setFollowCaster(followCaster);
        dome.setOffsets(offsetX, offsetY, offsetZ);
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
    protected void writeBarrierTypeNBT(INbt nbt) {
        nbt.setFloat("domeRadius", domeRadius);
        nbt.setBoolean("followCaster", followCaster);
    }

    @Override
    protected void readBarrierTypeNBT(INbt nbt) {
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

    @ClientOnly
    @Override
    protected void addBarrierTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("ability.domeRadius", this::getDomeRadius, this::setDomeRadius)
            .range(1.0f, 50.0f));
        defs.add(FieldDef.boolField("ability.followCaster", this::isFollowCaster, this::setFollowCaster)
            .hover("ability.hover.followCaster"));
    }
}
