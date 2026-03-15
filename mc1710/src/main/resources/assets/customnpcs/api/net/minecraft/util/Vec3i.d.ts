/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.util
 */

/**
 * This code is owned by Minecraft
  * @javaFqn net.minecraft.util.Vec3i
*/
export class Vec3i {
    /**
     * X coordinate
     */
    private final int x;
    /**
     * Y coordinate
     */
    private final int y;
    /**
     * Z coordinate
     */
    private final int z;
    
    /**
     * Double X coordinate
     */
    private final double xd;
    /**
     * Double Y coordinate
     */
    private final double yd;
    /**
     * Double Z coordinate
     */
    private final double zd;
    
    public Vec3i(int xIn, int yIn, int zIn) {
    this((double) xIn, (double) yIn, (double) zIn);
    }
    
    public Vec3i(double xIn, double yIn, double zIn) {
    this.x = MathHelper.floor_double(xIn);
    this.y = MathHelper.floor_double(yIn);
    this.z = MathHelper.floor_double(zIn);
    this.xd = xIn;
    this.yd = yIn;
    this.zd = zIn;
    }
    
    public boolean equals(Object p_equals_1_) {
    if (this == p_equals_1_) {
    return true;
    } else if (!(p_equals_1_ instanceof Vec3i)) {
    return false;
    } else {
    Vec3i vec3I = (Vec3i) p_equals_1_;
    return this.getX() != vec3I.getX() ? false : (this.getY() != vec3I.getY() ? false : this.getZ() == vec3I.getZ());
    }
    }
    
    public int hashCode() {
    return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }
    
    public int compareTo(Vec3i p_compareTo_1_) {
    return this.getY() == p_compareTo_1_.getY() ? (this.getZ() == p_compareTo_1_.getZ() ? this.getX() - p_compareTo_1_.getX() : this.getZ() - p_compareTo_1_.getZ()) : this.getY() - p_compareTo_1_.getY();
    }
    
    /**
     * Get the X coordinate
     * @return the X component
     */
    getX(): import('./int').int;
    /**
     * Get the Y coordinate
     * @return the Y component
     */
    getY(): import('./int').int;
    /**
     * Get the Z coordinate
     * @return the Z component
     */
    getZ(): import('./int').int;
    /**
     * Get the X coordinate as a double
     * @return the X component as a double
     */
    getXD(): import('./double').double;
    /**
     * Get the Y coordinate as a double
     * @return the Y component as a double
     */
    getYD(): import('./double').double;
    /**
     * Get the Z coordinate as a double
     * @return the Z component as a double
     */
    getZD(): import('./double').double;
    /**
     * Calculate the cross product of this and the given Vector
     * @param vec the other vector
     * @return the cross product of this vector and the given vector
     */
    crossProduct(vec: import('./Vec3i').Vec3i): import('./Vec3i').Vec3i;
    toString(): String;
    /**
     * X coordinate
     */
    x: import('./int').int;
    /**
     * Y coordinate
     */
    y: import('./int').int;
    /**
     * Z coordinate
     */
    z: import('./int').int;
    /**
     * Double X coordinate
     */
    xd: import('./double').double;
    /**
     * Double Y coordinate
     */
    yd: import('./double').double;
    /**
     * Double Z coordinate
     */
    zd: import('./double').double;
}
