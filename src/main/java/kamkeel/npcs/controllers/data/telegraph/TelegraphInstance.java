package kamkeel.npcs.controllers.data.telegraph;

import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Runtime instance of an active telegraph.
 * Tracks position, timing, and state for rendering.
 */
public class TelegraphInstance {

    // Identity
    private String instanceId;
    private Telegraph telegraph;

    // Position (current)
    private double x;
    private double y;
    private double z;
    private float yaw;

    // Position (previous tick - for interpolation)
    private double prevX;
    private double prevY;
    private double prevZ;
    private float prevYaw;

    // Entity tracking
    private int entityIdToFollow = -1;
    private int casterEntityId = -1;
    private int targetEntityId = -1;  // For LINE telegraphs: entity to face (yaw tracking)

    // Timing
    private int remainingTicks;
    private int totalTicks;

    // State
    private boolean isWarning = false;

    // Server-side tracking (for removal)
    private transient World world;
    private transient int dimensionId;

    public TelegraphInstance() {
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
    }

    public TelegraphInstance(Telegraph telegraph, double x, double y, double z, float yaw) {
        this();
        this.telegraph = new Telegraph(telegraph);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        // Initialize prev positions to current for first frame
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.prevYaw = yaw;
        this.remainingTicks = telegraph.getDurationTicks();
        this.totalTicks = telegraph.getDurationTicks();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TICK LOGIC
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Called every tick to update telegraph state.
     *
     * @param world The world (for entity lookup)
     * @return false if telegraph should be removed
     */
    public boolean tick(World world) {
        if (remainingTicks <= 0) {
            return false;
        }

        // Save previous position for interpolation
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;

        remainingTicks--;

        // Update position if following an entity
        if (entityIdToFollow >= 0 && world != null) {
            Entity entity = world.getEntityByID(entityIdToFollow);
            if (entity != null) {
                this.x = entity.posX;
                this.z = entity.posZ;
                // Find ground level below the entity for proper telegraph placement
                this.y = Ability.findGroundLevel(world, entity.posX, entity.posY, entity.posZ);
            }
        }

        // Update yaw to face target entity (for LINE telegraphs)
        if (targetEntityId >= 0 && world != null) {
            Entity target = world.getEntityByID(targetEntityId);
            if (target != null) {
                double dx = target.posX - this.x;
                double dz = target.posZ - this.z;
                this.yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
            }
        }

        // Check for warning phase transition
        if (!isWarning && telegraph != null) {
            if (remainingTicks <= telegraph.getWarningStartTick()) {
                isWarning = true;
            }
        }

        return true;
    }

    /**
     * Get progress from 0.0 (just started) to 1.0 (finished).
     */
    public float getProgress() {
        if (totalTicks <= 0) return 1.0f;
        return 1.0f - ((float) remainingTicks / (float) totalTicks);
    }

    /**
     * Get the current display color (normal or warning).
     */
    public int getCurrentColor() {
        if (telegraph == null) return 0x80FF0000;
        return isWarning ? telegraph.getWarningColor() : telegraph.getColor();
    }

    /**
     * Get alpha-modulated color for breathing animation.
     */
    public int getAnimatedColor(float partialTicks) {
        int baseColor = getCurrentColor();
        if (telegraph == null || !telegraph.isAnimated()) {
            return baseColor;
        }

        // Breathing animation: slow, smooth modulation of alpha
        float time = (totalTicks - remainingTicks) + partialTicks;
        // Slower breathing: 0.08 for ~4 second cycle, smoother sine curve
        float breathPhase = (float) Math.sin(time * 0.08);
        // Smooth easing: square the sine for more organic feel, range 0.5 to 1.0
        float pulse = 0.5f + (breathPhase * breathPhase * 0.5f * Math.signum(breathPhase));

        int alpha = (baseColor >> 24) & 0xFF;
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        alpha = (int) (alpha * pulse);

        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERPOLATED POSITION (for smooth rendering)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get interpolated X position for smooth rendering.
     */
    public double getInterpolatedX(float partialTicks) {
        return prevX + (x - prevX) * partialTicks;
    }

    /**
     * Get interpolated Y position for smooth rendering.
     */
    public double getInterpolatedY(float partialTicks) {
        return prevY + (y - prevY) * partialTicks;
    }

    /**
     * Get interpolated Z position for smooth rendering.
     */
    public double getInterpolatedZ(float partialTicks) {
        return prevZ + (z - prevZ) * partialTicks;
    }

    /**
     * Get interpolated yaw for smooth rendering.
     * Handles angle wrapping for smooth rotation.
     */
    public float getInterpolatedYaw(float partialTicks) {
        // Handle angle wrapping to prevent spinning the wrong way
        float diff = yaw - prevYaw;
        while (diff > 180.0f) diff -= 360.0f;
        while (diff < -180.0f) diff += 360.0f;
        return prevYaw + diff * partialTicks;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("instanceId", instanceId);

        if (telegraph != null) {
            nbt.setTag("telegraph", telegraph.writeNBT());
        }

        nbt.setDouble("x", x);
        nbt.setDouble("y", y);
        nbt.setDouble("z", z);
        nbt.setFloat("yaw", yaw);

        nbt.setInteger("entityIdToFollow", entityIdToFollow);
        nbt.setInteger("casterEntityId", casterEntityId);
        nbt.setInteger("targetEntityId", targetEntityId);

        nbt.setInteger("remainingTicks", remainingTicks);
        nbt.setInteger("totalTicks", totalTicks);

        nbt.setBoolean("isWarning", isWarning);

        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        this.instanceId = nbt.getString("instanceId");

        if (nbt.hasKey("telegraph")) {
            this.telegraph = new Telegraph();
            this.telegraph.readNBT(nbt.getCompoundTag("telegraph"));
        }

        this.x = nbt.getDouble("x");
        this.y = nbt.getDouble("y");
        this.z = nbt.getDouble("z");
        this.yaw = nbt.getFloat("yaw");

        this.entityIdToFollow = nbt.getInteger("entityIdToFollow");
        this.casterEntityId = nbt.getInteger("casterEntityId");
        this.targetEntityId = nbt.getInteger("targetEntityId");

        this.remainingTicks = nbt.getInteger("remainingTicks");
        this.totalTicks = nbt.getInteger("totalTicks");

        this.isWarning = nbt.getBoolean("isWarning");
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Telegraph getTelegraph() {
        return telegraph;
    }

    public void setTelegraph(Telegraph telegraph) {
        this.telegraph = telegraph;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public int getEntityIdToFollow() {
        return entityIdToFollow;
    }

    public void setEntityIdToFollow(int entityIdToFollow) {
        this.entityIdToFollow = entityIdToFollow;
    }

    /**
     * Locks the telegraph at its current position and direction.
     * Stops following any entity - used when ability commits to action.
     */
    public void lockPosition() {
        this.entityIdToFollow = -1;
        this.targetEntityId = -1;
    }

    public int getCasterEntityId() {
        return casterEntityId;
    }

    public void setCasterEntityId(int casterEntityId) {
        this.casterEntityId = casterEntityId;
    }

    public int getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public void setRemainingTicks(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public void setTotalTicks(int totalTicks) {
        this.totalTicks = totalTicks;
    }

    public boolean isWarning() {
        return isWarning;
    }

    public void setWarning(boolean warning) {
        isWarning = warning;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
        if (world != null) {
            this.dimensionId = world.provider.dimensionId;
        }
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(int dimensionId) {
        this.dimensionId = dimensionId;
    }
}
