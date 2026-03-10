package kamkeel.npcs.client.renderer.lightning;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

/**
 * 3D Vector helper class for lightning calculations.
 * Adapted from Botania's Vector3 by ChickenBones.
 */
public class Vec3d {
    public double x;
    public double y;
    public double z;

    public Vec3d() {
    }

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vec3d(Vec3 vec) {
        this.x = vec.xCoord;
        this.y = vec.yCoord;
        this.z = vec.zCoord;
    }

    public Vec3d copy() {
        return new Vec3d(this);
    }

    public static Vec3d fromEntity(Entity e) {
        return new Vec3d(e.posX, e.posY, e.posZ);
    }

    public static Vec3d fromEntityCenter(Entity e) {
        return new Vec3d(e.posX, e.posY - e.yOffset + e.height / 2, e.posZ);
    }

    public Vec3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3d set(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public double dotProduct(Vec3d vec) {
        double d = vec.x * x + vec.y * y + vec.z * z;
        if (d > 1 && d < 1.00001) d = 1;
        else if (d < -1 && d > -1.00001) d = -1;
        return d;
    }

    public Vec3d crossProduct(Vec3d vec) {
        double d = y * vec.z - z * vec.y;
        double d1 = z * vec.x - x * vec.z;
        double d2 = x * vec.y - y * vec.x;
        x = d;
        y = d1;
        z = d2;
        return this;
    }

    public Vec3d add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3d add(Vec3d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3d subtract(Vec3d vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3d multiply(double d) {
        this.x *= d;
        this.y *= d;
        this.z *= d;
        return this;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double magSquared() {
        return x * x + y * y + z * z;
    }

    public Vec3d normalize() {
        double d = mag();
        if (d != 0) {
            multiply(1 / d);
        }
        return this;
    }

    public Vec3d perpendicular() {
        if (z == 0) {
            return zCrossProduct();
        }
        return xCrossProduct();
    }

    public Vec3d xCrossProduct() {
        double d = z;
        double d1 = -y;
        x = 0;
        y = d;
        z = d1;
        return this;
    }

    public Vec3d zCrossProduct() {
        double d = y;
        double d1 = -x;
        x = d;
        y = d1;
        z = 0;
        return this;
    }

    public Vec3 toVec3() {
        return Vec3.createVectorHelper(x, y, z);
    }

    public double angle(Vec3d vec) {
        return Math.acos(copy().normalize().dotProduct(vec.copy().normalize()));
    }

    public Vec3d negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vec3d rotate(double angle, Vec3d axis) {
        Quaternion.aroundAxis(axis.copy().normalize(), angle).rotate(this);
        return this;
    }

    public void glVertex() {
        GL11.glVertex3d(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3d(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vec3d)) return false;
        Vec3d v = (Vec3d) o;
        return x == v.x && y == v.y && z == v.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
