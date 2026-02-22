package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Short-lived visual explosion entity for energy projectile impacts.
 * This is render-only and never applies damage itself.
 */
public class EntityEnergyExplosion extends EntityEnergyAbility {

    private float maxRadius = 2.0f;
    private int durationTicks = 10;
    private float prevRenderRadius = 0.0f;
    private float renderRadius = 0.0f;
    private long renderSeed = 0L;

    public EntityEnergyExplosion(World world) {
        super(world);
        this.noClip = true;
        this.setSize(0.1f, 0.1f);
    }

    public EntityEnergyExplosion(World world, EntityEnergyProjectile source, float radius) {
        this(world);

        if (source != null) {
            this.ownerEntityId = source.getOwnerEntityId();
            EnergyDisplayData sourceDisplay = source.getDisplayData();
            this.displayData = sourceDisplay != null ? sourceDisplay.copy() : new EnergyDisplayData();
            EnergyLightningData sourceLightning = source.getLightningData();
            this.lightningData = sourceLightning != null ? sourceLightning.copy() : new EnergyLightningData();
            this.setPosition(source.posX, source.posY, source.posZ);
        }

        setExplosionRadius(radius);
        this.renderSeed = world != null ? world.rand.nextLong() : 0L;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public void onUpdate() {
        this.prevRenderRadius = this.renderRadius;

        super.onUpdate();

        float progress = getLifeProgress(0.0f);
        float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
        this.renderRadius = maxRadius * eased;

        if (ticksExisted >= durationTicks) {
            setDead();
        }
    }

    public void setExplosionRadius(float radius) {
        this.maxRadius = Math.max(0.5f, sanitize(radius, 2.0f, MAX_ENTITY_RADIUS));
        this.durationTicks = Math.max(6, Math.min(18, (int) (6 + this.maxRadius * 1.8f)));
        this.setSize(this.maxRadius * 2.0f, this.maxRadius * 2.0f);
    }

    public float getInterpolatedRadius(float partialTicks) {
        return this.prevRenderRadius + (this.renderRadius - this.prevRenderRadius) * partialTicks;
    }

    public float getLifeProgress(float partialTicks) {
        if (durationTicks <= 0) return 1.0f;
        float age = (ticksExisted + partialTicks) / durationTicks;
        return MathHelper.clamp_float(age, 0.0f, 1.0f);
    }

    public long getRenderSeed() {
        return renderSeed;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = Math.max(16.0D, maxRadius * 64.0D);
        return distance < d * d;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        // Intentionally empty - transient visual entity.
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty - transient visual entity.
    }

    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
        nbt.setFloat("ExplosionRadius", maxRadius);
        nbt.setInteger("ExplosionDuration", durationTicks);
        nbt.setLong("ExplosionSeed", renderSeed);
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
        setExplosionRadius(nbt.hasKey("ExplosionRadius") ? nbt.getFloat("ExplosionRadius") : 2.0f);
        this.durationTicks = nbt.hasKey("ExplosionDuration") ? nbt.getInteger("ExplosionDuration") : durationTicks;
        if (durationTicks <= 0) durationTicks = 10;
        this.renderSeed = nbt.hasKey("ExplosionSeed") ? nbt.getLong("ExplosionSeed") : 0L;
        this.renderRadius = 0.0f;
        this.prevRenderRadius = 0.0f;
    }
}

