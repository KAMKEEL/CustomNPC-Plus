package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyPanelData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Energy Wall ability: Spawns a placeable flat energy barrier.
 * Can float above ground (configurable height offset).
 * Optional launching mode fires the wall forward with damage/knockback.
 */
public class AbilityWall extends AbilityEnergyBarrier {

    private final EnergyPanelData panelData;

    public AbilityWall() {
        super(
            new EnergyDisplayData(0x44FFCC, 0x22CCAA, true, 0.3f, 0.4f, 0.5f, 0.0f),
            new EnergyBarrierData(80.0f, true, 160, true)
        );
        this.panelData = new EnergyPanelData(4.0f, 3.0f, 0.0f);
        this.typeId = "ability.cnpc.wall";
        this.name = "Wall";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.minRange = 2.0f;
        this.cooldownTicks = 80;
        this.windUpTicks = 20;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpAnimationName = "";
        this.activeAnimationName = "";
    }

    @Override
    public boolean hasDamage() {
        return panelData.launching; // Only has damage when launching
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityEnergyBarrier createBarrierEntity(EntityLivingBase caster, EntityLivingBase target) {
        // Place wall between caster and target
        double placeX, placeY, placeZ;
        float yaw;

        if (target != null) {
            double dx = target.posX - caster.posX;
            double dz = target.posZ - caster.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < 0.01) {
                // Target at same position — fall through to look-direction placement
                float yawRad = (float) Math.toRadians(caster.rotationYaw);
                placeX = caster.posX + (-Math.sin(yawRad) * 3.0);
                placeY = caster.posY;
                placeZ = caster.posZ + (Math.cos(yawRad) * 3.0);
                yaw = caster.rotationYaw;
            } else {
                double placeDist = Math.min(dist * 0.5, 5.0);
                placeX = caster.posX + (dx / dist) * placeDist;
                placeY = caster.posY;
                placeZ = caster.posZ + (dz / dist) * placeDist;
                yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
            }
        } else {
            float yawRad = (float) Math.toRadians(caster.rotationYaw);
            placeX = caster.posX + (-Math.sin(yawRad) * 3.0);
            placeY = caster.posY;
            placeZ = caster.posZ + (Math.cos(yawRad) * 3.0);
            yaw = caster.rotationYaw;
        }

        EntityEnergyPanel.PanelMode mode = panelData.launching
            ? EntityEnergyPanel.PanelMode.LAUNCHED
            : EntityEnergyPanel.PanelMode.PLACED;

        EntityEnergyPanel panel = new EntityEnergyPanel(
            caster.worldObj, caster,
            placeX, placeY, placeZ, yaw, mode,
            displayData.copy(), lightningData.copy(), barrierData.copy(), panelData.copy()
        );
        panel.setSourceAbility(this);
        return panel;
    }

    @Override
    public float getTelegraphRadius() {
        return Math.max(panelData.panelWidth, panelData.panelHeight) * 0.5f;
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.static", a -> {
                a.setName("Static Wall");
            }),
            new AbilityVariant("ability.variant.launched", a -> {
                AbilityWall wall = (AbilityWall) a;
                a.setName("Launched Wall");
                wall.panelData.launching = true;
                wall.panelData.launchSpeed = 0.6f;
                wall.panelData.launchDamage = 10.0f;
                wall.panelData.launchKnockback = 3.0f;
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeBarrierTypeNBT(NBTTagCompound nbt) {
        panelData.writeNBT(nbt);
    }

    @Override
    protected void readBarrierTypeNBT(NBTTagCompound nbt) {
        panelData.readNBT(nbt);
    }

    // ==================== GETTERS & SETTERS ====================

    public EnergyPanelData getPanelData() {
        return panelData;
    }

    public float getPanelWidth() {
        return panelData.panelWidth;
    }

    public void setPanelWidth(float width) {
        panelData.setPanelWidth(width);
    }

    public float getPanelHeight() {
        return panelData.panelHeight;
    }

    public void setPanelHeight(float height) {
        panelData.setPanelHeight(height);
    }

    public float getHeightOffset() {
        return panelData.heightOffset;
    }

    public void setHeightOffset(float offset) {
        panelData.heightOffset = offset;
    }

    public boolean isLaunching() {
        return panelData.launching;
    }

    public void setLaunching(boolean launching) {
        panelData.launching = launching;
    }

    public float getLaunchSpeed() {
        return panelData.launchSpeed;
    }

    public void setLaunchSpeed(float speed) {
        panelData.setLaunchSpeed(speed);
    }

    public float getLaunchDamage() {
        return panelData.launchDamage;
    }

    public void setLaunchDamage(float damage) {
        panelData.setLaunchDamage(damage);
    }

    public float getLaunchKnockback() {
        return panelData.launchKnockback;
    }

    public void setLaunchKnockback(float kb) {
        panelData.setLaunchKnockback(kb);
    }

    // ==================== TYPE-SPECIFIC GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addBarrierTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.section("ability.section.panel"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.width", this::getPanelWidth, this::setPanelWidth).range(0.5f, 20.0f),
            FieldDef.floatField("gui.height", this::getPanelHeight, this::setPanelHeight).range(0.5f, 20.0f)
        ));
        defs.add(FieldDef.floatField("ability.heightOffset", this::getHeightOffset, this::setHeightOffset)
            .min(Float.NEGATIVE_INFINITY));
        defs.add(FieldDef.section("ability.section.launch"));
        defs.add(FieldDef.boolField("gui.enabled", this::isLaunching, this::setLaunching)
            .hover("ability.hover.launching"));
        defs.add(FieldDef.row(
            FieldDef.floatField("stats.speed", this::getLaunchSpeed, this::setLaunchSpeed)
                .visibleWhen(this::isLaunching),
            FieldDef.floatField("enchantment.damage", this::getLaunchDamage, this::setLaunchDamage)
                .visibleWhen(this::isLaunching)
        ));
        defs.add(FieldDef.floatField("ability.knockback", this::getLaunchKnockback, this::setLaunchKnockback)
            .visibleWhen(this::isLaunching));
    }
}
