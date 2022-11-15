//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.IPos;

public class ScriptBlockPos implements IPos {
    public BlockPos blockPos;

    public ScriptBlockPos(BlockPos pos) {
        this.blockPos = pos;
    }

    public int getX() {
        return this.blockPos.getX();
    }

    public int getY() {
        return this.blockPos.getY();
    }

    public int getZ() {
        return this.blockPos.getZ();
    }

    public IPos up() {
        return NpcAPI.Instance().getIPos(this.blockPos.up());
    }

    public IPos up(int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.up(n));
    }

    public IPos down() {
        return NpcAPI.Instance().getIPos(this.blockPos.down());
    }

    public IPos down(int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.down(n));
    }

    public IPos north() {
        return NpcAPI.Instance().getIPos(this.blockPos.north());
    }

    public IPos north(int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.north(n));
    }

    public IPos east() {
        return NpcAPI.Instance().getIPos(this.blockPos.east());
    }

    public IPos east(int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.east(n));
    }

    public IPos south() {
        return NpcAPI.Instance().getIPos(this.blockPos.south());
    }

    public IPos south(int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.south(n));
    }

    public IPos west() {
        return NpcAPI.Instance().getIPos(this.blockPos.west());
    }

    public IPos west(int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.west(n));
    }

    public IPos add(int x, int y, int z) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(x, y, z));
    }

    public IPos add(IPos pos) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(this.blockPos));
    }

    public IPos subtract(int x, int y, int z) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(-x, -y, -z));
    }

    public IPos subtract(IPos pos) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(-pos.getX(), -pos.getY(), -pos.getZ()));
    }

    public IPos offset(int direction) {
        return NpcAPI.Instance().getIPos(this.blockPos.offset(EnumFacing.values()[direction]));
    }

    public IPos offset(int direction, int n) {
        return NpcAPI.Instance().getIPos(this.blockPos.offset(EnumFacing.values()[direction], n));
    }

    public IPos crossProduct(int x, int y, int z) {
        return this.crossProduct(NpcAPI.Instance().getIPos(x,y,z));
    }

    public IPos crossProduct(IPos pos) {
        return NpcAPI.Instance().getIPos(this.blockPos.crossProduct(new Vec3i(-pos.getX(), -pos.getY(), -pos.getZ())));
    }

    public IPos divide(float scalar) {
        return NpcAPI.Instance().getIPos(
        getX()/scalar , getY()/scalar, getZ()/scalar
        );
    }

    public long toLong() {
        return blockPos.toLong();
    }

    public IPos fromLong(long serialized) {
        return NpcAPI.Instance().getIPos(BlockPos.fromLong(serialized));
    }

    public double[] normalize() {
        double d = Math.sqrt((double)(this.blockPos.getX() * this.blockPos.getX() + this.blockPos.getY() * this.blockPos.getY() + this.blockPos.getZ() * this.blockPos.getZ()));
        return new double[]{(double)this.getX() / d, (double)this.getY() / d, (double)this.getZ() / d};
    }

    public double distanceTo(IPos pos) {
        double d0 = (double)(this.getX() - pos.getX());
        double d1 = (double)(this.getY() - pos.getY());
        double d2 = (double)(this.getZ() - pos.getZ());
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public BlockPos getMCPos() {
        return this.blockPos;
    }

    public String toString() {
        return "(" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ")";
    }
}
