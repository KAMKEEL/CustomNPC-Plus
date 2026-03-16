package kamkeel.npcs.controllers.data.ability.type;

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
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import noppes.npcs.api.ability.type.IAbilityHazard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityHazard extends AbilityZone implements IAbilityHazard {

    private float radius = 2.0f;
    private float damagePerSecond = 1.0f;
    private int damageInterval = 20;
    private boolean affectsCaster = false;

    public AbilityHazard() {
        super(300, new EnergyDisplayData(0x00CC00, 0x006600, true, 1.0f, 0.5f, 1.5f));
        this.typeId = "ability.cnpc.hazard";
        this.name = "Hazard";
        this.windUpTicks = 30;
        this.windUpAnimationName = "Ability_Zone_Windup";
        this.activeAnimationName = "Ability_Zone_Active";

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/hazard.png"),
            new DefaultIconLayer("customnpcs:textures/gui/ability/hazard_overlay.png",
                this::getActiveColor)
        };
    }

    @Override
    public boolean isConcurrentCapable() {
        return true;
    }

    @Override
    public float getZoneRadius() {
        return radius;
    }

    @Override
    public void onExecute(IEntityLivingBase caster, IEntityLivingBase target) {
        activeEntities.clear();

        List<double[]> positions;
        if (!preCalculatedPositions.isEmpty() && preCalculatedPositions.size() == zoneCount) {
            positions = new ArrayList<>(preCalculatedPositions);
            preCalculatedPositions.clear();
        } else {
            positions = new ArrayList<>();
            List<double[]> placedPositions = new ArrayList<>();
            float minSeparation = radius * 2.0f;
            for (int i = 0; i < zoneCount; i++) {
                double[] pos = findSpawnPosition(caster, placedPositions, minSeparation);
                placedPositions.add(pos);
                positions.add(pos);
            }
        }

        for (int i = 0; i < zoneCount; i++) {
            double[] pos = positions.get(i);

            EntityAbilityZone IEntity = EntityAbilityZone.createHazard(caster.worldObj, caster,
                pos[0], caster.posY, pos[1],
                zoneShape,
                radius,
                damagePerSecond, damageInterval,
                isIgnoreIFrames(), affectsCaster,
                durationTicks,
                colorData.innerColor, colorData.outerColor, colorData.outerColorEnabled,
                zoneHeight,
                particleDensity, particleScale, animSpeed, lightningDensity,
                getEffects());
            IEntity.setSourceAbility(this);

            applyVisualToEntity(IEntity);

            if (isPreview()) {
                IEntity.setupPreview(caster);
            }

            spawnAbilityEntity(IEntity);
            activeEntities.add(IEntity);
        }
    }

    @Override
    public void writeTypeNBT(INbt nbt) {
        writeZoneNBT(nbt);
        nbt.setFloat("radius", radius);
        nbt.setFloat("damagePerSecond", damagePerSecond);
        nbt.setInteger("damageInterval", damageInterval);
        nbt.setBoolean("affectsCaster", affectsCaster);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
        readZoneNBT(nbt, 300);
        this.radius = nbt.getFloat("radius");
        this.damagePerSecond = nbt.getFloat("damagePerSecond");
        this.damageInterval = nbt.getInteger("damageInterval");
        this.affectsCaster = nbt.getBoolean("affectsCaster");
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getDamagePerSecond() {
        return damagePerSecond;
    }

    public void setDamagePerSecond(float damagePerSecond) {
        this.damagePerSecond = damagePerSecond;
    }

    @Override
    public float getDisplayDamage() { return damagePerSecond; }

    @Override
    public boolean isDisplayDamageDPS() { return true; }

    public int getDamageInterval() {
        return damageInterval;
    }

    public void setDamageInterval(int damageInterval) {
        this.damageInterval = damageInterval;
    }

    public boolean isAffectsCaster() {
        return affectsCaster;
    }

    public void setAffectsCaster(boolean affectsCaster) {
        this.affectsCaster = affectsCaster;
    }

    @ClientOnly
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        addPresetFieldDef(defs);

        defs.addAll(Arrays.asList(
            FieldDef.row(
                FieldDef.intField("ability.duration", this::getDurationTicks, this::setDurationTicks).range(1, 2000),
                FieldDef.enumField("ability.zoneShape", ZoneShape.class, this::getZoneShapeEnum, this::setZoneShapeEnum)
            ),
            FieldDef.section("ability.section.zone"),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getSpawnRadius, this::setSpawnRadius),
                FieldDef.intField("gui.count", this::getZoneCount, this::setZoneCount).range(1, 20)
            ),
            FieldDef.floatField("gui.height", this::getZoneHeight, this::setZoneHeight),
            FieldDef.section("ability.section.area"),
            FieldDef.floatField("gui.radius", this::getRadius, this::setRadius),
            FieldDef.section("ability.section.damage"),
            FieldDef.row(
                FieldDef.floatField("gui.dps", this::getDamagePerSecond, this::setDamagePerSecond),
                FieldDef.intField("gui.interval", this::getDamageInterval, this::setDamageInterval)
            ),
            FieldDef.boolField("ability.affectsCaster", this::isAffectsCaster, this::setAffectsCaster)
                .hover("ability.hover.affectsCaster"),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));

        addVisualFieldDefs(defs);
    }
}
