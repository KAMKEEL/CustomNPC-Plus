package kamkeel.npcs.controllers.data.telegraph;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Telegraph configuration - defines a visual warning shape.
 * Telegraphs warn players about incoming attacks or mark areas of effect.
 */
public class Telegraph {

    // Identity
    private String id = "";
    private TelegraphType type = TelegraphType.CIRCLE;

    // Shape parameters
    private float radius = 3.0f;
    private float innerRadius = 0.0f;
    private float length = 5.0f;
    private float width = 2.0f;
    private float angle = 45.0f;

    // Timing
    private int durationTicks = 40;

    // Visuals
    private int color = 0x80FF0000;           // Semi-transparent red
    private int warningColor = 0xC0FF0000;    // More opaque red
    private int warningStartTick = 10;
    private boolean animated = true;
    private float heightOffset = 0.1f;

    // Positioning
    private boolean atTarget = true;
    private boolean followsTarget = false;
    private boolean followsCaster = false;
    private float offsetForward = 0.0f;

    public Telegraph() {
    }

    public Telegraph(String id, TelegraphType type) {
        this.id = id;
        this.type = type;
    }

    public Telegraph(Telegraph other) {
        this.id = other.id;
        this.type = other.type;
        this.radius = other.radius;
        this.innerRadius = other.innerRadius;
        this.length = other.length;
        this.width = other.width;
        this.angle = other.angle;
        this.durationTicks = other.durationTicks;
        this.color = other.color;
        this.warningColor = other.warningColor;
        this.warningStartTick = other.warningStartTick;
        this.animated = other.animated;
        this.heightOffset = other.heightOffset;
        this.atTarget = other.atTarget;
        this.followsTarget = other.followsTarget;
        this.followsCaster = other.followsCaster;
        this.offsetForward = other.offsetForward;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("id", id);
        nbt.setString("type", type.name());
        nbt.setFloat("radius", radius);
        nbt.setFloat("innerRadius", innerRadius);
        nbt.setFloat("length", length);
        nbt.setFloat("width", width);
        nbt.setFloat("angle", angle);
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setInteger("color", color);
        nbt.setInteger("warningColor", warningColor);
        nbt.setInteger("warningStartTick", warningStartTick);
        nbt.setBoolean("animated", animated);
        nbt.setFloat("heightOffset", heightOffset);
        nbt.setBoolean("atTarget", atTarget);
        nbt.setBoolean("followsTarget", followsTarget);
        nbt.setBoolean("followsCaster", followsCaster);
        nbt.setFloat("offsetForward", offsetForward);
        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        this.id = nbt.getString("id");
        if (nbt.hasKey("type")) {
            try {
                this.type = TelegraphType.valueOf(nbt.getString("type"));
            } catch (Exception e) {
                this.type = TelegraphType.CIRCLE;
            }
        }
        this.radius = nbt.getFloat("radius");
        this.innerRadius = nbt.getFloat("innerRadius");
        this.length = nbt.getFloat("length");
        this.width = nbt.getFloat("width");
        this.angle = nbt.getFloat("angle");
        this.durationTicks = nbt.getInteger("durationTicks");
        this.color = nbt.getInteger("color");
        this.warningColor = nbt.getInteger("warningColor");
        this.warningStartTick = nbt.getInteger("warningStartTick");
        this.animated = nbt.getBoolean("animated");
        this.heightOffset = nbt.getFloat("heightOffset");
        this.atTarget = nbt.getBoolean("atTarget");
        this.followsTarget = nbt.getBoolean("followsTarget");
        this.followsCaster = nbt.getBoolean("followsCaster");
        this.offsetForward = nbt.getFloat("offsetForward");
    }

    // ═══════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════

    public static Telegraph circle(float radius) {
        Telegraph t = new Telegraph("", TelegraphType.CIRCLE);
        t.radius = radius;
        return t;
    }

    public static Telegraph ring(float outerRadius, float innerRadius) {
        Telegraph t = new Telegraph("", TelegraphType.RING);
        t.radius = outerRadius;
        t.innerRadius = innerRadius;
        return t;
    }

    public static Telegraph line(float length, float width) {
        Telegraph t = new Telegraph("", TelegraphType.LINE);
        t.length = length;
        t.width = width;
        return t;
    }

    public static Telegraph cone(float length, float angle) {
        Telegraph t = new Telegraph("", TelegraphType.CONE);
        t.length = length;
        t.angle = angle;
        return t;
    }

    public static Telegraph point() {
        return new Telegraph("", TelegraphType.POINT);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TelegraphType getType() {
        return type;
    }

    public void setType(TelegraphType type) {
        this.type = type;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(float innerRadius) {
        this.innerRadius = innerRadius;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getWarningColor() {
        return warningColor;
    }

    public void setWarningColor(int warningColor) {
        this.warningColor = warningColor;
    }

    public int getWarningStartTick() {
        return warningStartTick;
    }

    public void setWarningStartTick(int warningStartTick) {
        this.warningStartTick = warningStartTick;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public float getHeightOffset() {
        return heightOffset;
    }

    public void setHeightOffset(float heightOffset) {
        this.heightOffset = heightOffset;
    }

    public boolean isAtTarget() {
        return atTarget;
    }

    public void setAtTarget(boolean atTarget) {
        this.atTarget = atTarget;
    }

    public boolean isFollowsTarget() {
        return followsTarget;
    }

    public void setFollowsTarget(boolean followsTarget) {
        this.followsTarget = followsTarget;
    }

    public boolean isFollowsCaster() {
        return followsCaster;
    }

    public void setFollowsCaster(boolean followsCaster) {
        this.followsCaster = followsCaster;
    }

    public float getOffsetForward() {
        return offsetForward;
    }

    public void setOffsetForward(float offsetForward) {
        this.offsetForward = offsetForward;
    }
}
