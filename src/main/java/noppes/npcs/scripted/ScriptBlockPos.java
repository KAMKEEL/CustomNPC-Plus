package noppes.npcs.scripted;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.IPos;

import java.util.Objects;

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

    public double getXD() {
        return this.blockPos.getXD();
    }

    public double getYD() {
        return this.blockPos.getYD();
    }

    public double getZD() {
        return this.blockPos.getZD();
    }

    public IPos up() {
        return NpcAPI.Instance().getIPos(this.blockPos.up());
    }

    public IPos up(double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.up(n));
    }

    public IPos down() {
        return NpcAPI.Instance().getIPos(this.blockPos.down());
    }

    public IPos down(double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.down(n));
    }

    public IPos north() {
        return NpcAPI.Instance().getIPos(this.blockPos.north());
    }

    public IPos north(double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.north(n));
    }

    public IPos east() {
        return NpcAPI.Instance().getIPos(this.blockPos.east());
    }

    public IPos east(double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.east(n));
    }

    public IPos south() {
        return NpcAPI.Instance().getIPos(this.blockPos.south());
    }

    public IPos south(double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.south(n));
    }

    public IPos west() {
        return NpcAPI.Instance().getIPos(this.blockPos.west());
    }

    public IPos west(double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.west(n));
    }

    public IPos add(double x, double y, double z) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(x, y, z));
    }

    public IPos add(IPos pos) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(pos.getXD(), pos.getYD(), pos.getZD()));
    }

    public IPos subtract(double x, double y, double z) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(-x, -y, -z));
    }

    public IPos subtract(IPos pos) {
        return NpcAPI.Instance().getIPos(this.blockPos.add(-pos.getXD(), -pos.getYD(), -pos.getZD()));
    }

    public IPos offset(int direction) {
        return NpcAPI.Instance().getIPos(this.blockPos.offset(EnumFacing.values()[direction]));
    }

    public IPos offset(int direction, double n) {
        return NpcAPI.Instance().getIPos(this.blockPos.offset(EnumFacing.values()[direction], n));
    }

    public IPos crossProduct(double x, double y, double z) {
        return this.crossProduct(NpcAPI.Instance().getIPos(x, y, z));
    }

    public IPos crossProduct(IPos pos) {
        return NpcAPI.Instance().getIPos(this.blockPos.crossProduct(new Vec3i(-pos.getXD(), -pos.getYD(), -pos.getZD())));
    }

    public IPos divide(double scalar) {
        return NpcAPI.Instance().getIPos(
            getXD() / scalar, getYD() / scalar, getZD() / scalar
        );
    }

    public long toLong() {
        return blockPos.toLong();
    }

    public IPos fromLong(long serialized) {
        this.blockPos = BlockPos.fromLong(serialized);
        return this;
    }

    public IPos normalize() {
        double d = Math.sqrt(this.blockPos.getXD() * this.blockPos.getXD() + this.blockPos.getYD() * this.blockPos.getYD() + this.blockPos.getZD() * this.blockPos.getZD());
        return NpcAPI.Instance().getIPos(this.getXD() / d, this.getYD() / d, this.getZD() / d);
    }

    public double[] normalizeDouble() {
        double d = Math.sqrt(this.blockPos.getXD() * this.blockPos.getXD() + this.blockPos.getYD() * this.blockPos.getYD() + this.blockPos.getZD() * this.blockPos.getZD());
        return new double[]{this.getXD() / d, this.getYD() / d, this.getZD() / d};
    }

    public double distanceTo(IPos pos) {
        double d0 = this.getXD() - pos.getXD();
        double d1 = this.getYD() - pos.getYD();
        double d2 = this.getZD() - pos.getZD();
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double distanceTo(double x, double y, double z) {
        double d0 = this.getXD() - x;
        double d1 = this.getYD() - y;
        double d2 = this.getZD() - z;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public BlockPos getMCPos() {
        return this.blockPos;
    }

    public String toString() {
        double xd = Math.floor(this.getXD() * 1000) / 1000.0D;
        double yd = Math.floor(this.getYD() * 1000) / 1000.0D;
        double zd = Math.floor(this.getZD() * 1000) / 1000.0D;
        return "(" + xd + ", " + yd + ", " + zd + ")";
    }

    public boolean equals(Object object) {
        return object instanceof IPos && ((IPos) object).toLong() == this.toLong();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(blockPos);
    }
}
