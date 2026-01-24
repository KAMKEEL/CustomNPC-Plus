package kamkeel.npcs.controllers.data.telegraph;

import net.minecraft.block.Block;
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

    // Position
    private double x;
    private double y;
    private double z;
    private float yaw;

    // Entity tracking
    private int entityIdToFollow = -1;
    private int casterEntityId = -1;

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

        remainingTicks--;

        // Update position if following an entity
        if (entityIdToFollow >= 0 && world != null) {
            Entity entity = world.getEntityByID(entityIdToFollow);
            if (entity != null) {
                this.x = entity.posX;
                this.z = entity.posZ;
                // Find ground level below the entity for proper telegraph placement
                this.y = findGroundLevel(world, entity.posX, entity.posY, entity.posZ);
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
     * Find the ground level at a given position.
     * Searches downward from the given Y to find a solid block.
     *
     * @param world  The world
     * @param x      X coordinate
     * @param startY Starting Y coordinate (entity feet position)
     * @param z      Z coordinate
     * @return The Y coordinate of the ground surface
     */
    private double findGroundLevel(World world, double x, double startY, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int startBlockY = (int) Math.floor(startY);

        // Search downward for solid ground (max 10 blocks down)
        for (int checkY = startBlockY; checkY >= startBlockY - 10 && checkY >= 0; checkY--) {
            Block block = world.getBlock(blockX, checkY, blockZ);
            if (block != null && block.getMaterial().isSolid()) {
                // Found solid block, telegraph goes on top of it
                return checkY + 1;
            }
        }

        // No ground found, use original position
        return startY;
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
     * Get alpha-modulated color for pulsing animation.
     */
    public int getAnimatedColor(float partialTicks) {
        int baseColor = getCurrentColor();
        if (telegraph == null || !telegraph.isAnimated()) {
            return baseColor;
        }

        // Pulsing animation: modulate alpha based on time
        float time = (totalTicks - remainingTicks) + partialTicks;
        float pulse = (float) (Math.sin(time * 0.3) * 0.3 + 0.7); // 0.4 to 1.0 range

        int alpha = (baseColor >> 24) & 0xFF;
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        alpha = (int) (alpha * pulse);

        return (alpha << 24) | (r << 16) | (g << 8) | b;
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
     * Locks the telegraph at its current position.
     * Stops following any entity - used when ability commits to action.
     */
    public void lockPosition() {
        this.entityIdToFollow = -1;
    }

    public int getCasterEntityId() {
        return casterEntityId;
    }

    public void setCasterEntityId(int casterEntityId) {
        this.casterEntityId = casterEntityId;
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
