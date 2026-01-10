package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Random;

/**
 * Dash ability: Quick evasive sidestep with NO telegraph.
 * Defensive repositioning move to evade attacks.
 */
public class AbilityDash extends Ability {

    /**
     * Dash behavior mode.
     */
    public enum DashMode {
        AGGRESSIVE,
        DEFENSIVE
    }

    /**
     * Specific dash direction.
     */
    public enum DashDirection {
        FORWARD(0),
        DIAGONAL_FORWARD_LEFT(45),
        DIAGONAL_FORWARD_RIGHT(-45),
        LEFT(90),
        RIGHT(-90),
        DIAGONAL_BACK_LEFT(135),
        DIAGONAL_BACK_RIGHT(-135),
        BACK(180);

        private final float angleOffset;

        DashDirection(float angleOffset) {
            this.angleOffset = angleOffset;
        }

        public float getAngleOffset() {
            return angleOffset;
        }
    }

    private static final Random RANDOM = new Random();

    private static final DashDirection[] AGGRESSIVE_DIRECTIONS = {
        DashDirection.FORWARD,
        DashDirection.DIAGONAL_FORWARD_LEFT,
        DashDirection.DIAGONAL_FORWARD_RIGHT
    };

    private static final DashDirection[] DEFENSIVE_DIRECTIONS = {
        DashDirection.LEFT,
        DashDirection.RIGHT,
        DashDirection.BACK,
        DashDirection.DIAGONAL_BACK_LEFT,
        DashDirection.DIAGONAL_BACK_RIGHT
    };

    // Type-specific parameters
    private DashMode dashMode = DashMode.DEFENSIVE;
    private float dashDistance = 4.0f;
    private float dashSpeed = 1.5f;

    // Runtime state
    private transient Vec3 dashDirection;
    private transient double startX, startY, startZ;
    private transient DashDirection chosenDirection;

    public AbilityDash() {
        this.typeId = "cnpc:dash";
        this.name = "Dash";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 0.0f;
        this.lockMovement = false;
        this.cooldownTicks = 40;
        this.windUpTicks = 5;
        this.activeTicks = 8;
        this.recoveryTicks = 10;
        // No telegraph for dash - it's a quick evasive move
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public noppes.npcs.client.gui.advanced.SubGuiAbilityConfig createConfigGui(
            noppes.npcs.client.gui.advanced.IAbilityConfigCallback callback) {
        return new noppes.npcs.client.gui.advanced.ability.SubGuiAbilityDash(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public boolean hasAbilityMovement() {
        return true; // This ability moves the NPC
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        startX = npc.posX;
        startY = npc.posY;
        startZ = npc.posZ;

        // Choose random direction based on mode
        DashDirection[] directions = dashMode == DashMode.AGGRESSIVE
            ? AGGRESSIVE_DIRECTIONS
            : DEFENSIVE_DIRECTIONS;
        chosenDirection = directions[RANDOM.nextInt(directions.length)];

        // Calculate dash direction relative to target
        float baseYaw;
        if (target != null) {
            double dx = target.posX - npc.posX;
            double dz = target.posZ - npc.posZ;
            baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            baseYaw = npc.rotationYaw;
        }

        // Apply direction offset
        float dashYaw = baseYaw + chosenDirection.getAngleOffset();
        float yawRad = (float) Math.toRadians(dashYaw);

        dashDirection = Vec3.createVectorHelper(
            -Math.sin(yawRad),
            0,
            Math.cos(yawRad)
        );

        // Play dash sound
        world.playSoundAtEntity(npc, "mob.endermen.portal", 0.5f, 1.5f);
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (dashDirection == null) return;

        // Calculate distance traveled
        double distanceTraveled = Math.sqrt(
            Math.pow(npc.posX - startX, 2) +
            Math.pow(npc.posZ - startZ, 2)
        );

        // Check if reached max distance
        if (distanceTraveled >= dashDistance) {
            npc.motionX = 0;
            npc.motionZ = 0;
            npc.velocityChanged = true;
            return;
        }

        // Move NPC
        npc.motionX = dashDirection.xCoord * dashSpeed;
        npc.motionY = 0;
        npc.motionZ = dashDirection.zCoord * dashSpeed;
        npc.velocityChanged = true;

        // Trail particles
        world.spawnParticle("smoke", npc.posX, npc.posY + 0.5, npc.posZ, 0, 0, 0);
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        npc.motionX = 0;
        npc.motionZ = 0;
        npc.velocityChanged = true;
        dashDirection = null;
        chosenDirection = null;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, net.minecraft.util.DamageSource source, float damage) {
        npc.motionX = 0;
        npc.motionZ = 0;
        npc.velocityChanged = true;
        dashDirection = null;
        chosenDirection = null;
    }

    @Override
    public void reset() {
        super.reset();
        dashDirection = null;
        chosenDirection = null;
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for dash
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setString("dashMode", dashMode.name());
        nbt.setFloat("dashDistance", dashDistance);
        nbt.setFloat("dashSpeed", dashSpeed);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        try {
            this.dashMode = DashMode.valueOf(nbt.getString("dashMode"));
        } catch (Exception e) {
            this.dashMode = DashMode.DEFENSIVE;
        }
        this.dashDistance = nbt.hasKey("dashDistance") ? nbt.getFloat("dashDistance") : 4.0f;
        this.dashSpeed = nbt.hasKey("dashSpeed") ? nbt.getFloat("dashSpeed") : 1.5f;
    }

    // Getters & Setters
    public DashMode getDashMode() { return dashMode; }
    public void setDashMode(DashMode dashMode) { this.dashMode = dashMode; }

    public float getDashDistance() { return dashDistance; }
    public void setDashDistance(float dashDistance) { this.dashDistance = dashDistance; }

    public float getDashSpeed() { return dashSpeed; }
    public void setDashSpeed(float dashSpeed) { this.dashSpeed = dashSpeed; }

    public DashDirection getChosenDirection() { return chosenDirection; }
}
