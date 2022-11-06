//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.scripted.interfaces.IPos;

public class ScriptBlockPos implements IPos {
    private BlockPos blockPos;

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
        return new ScriptBlockPos(this.blockPos.up());
    }

    public IPos up(int n) {
        return new ScriptBlockPos(this.blockPos.up(n));
    }

    public IPos down() {
        return new ScriptBlockPos(this.blockPos.down());
    }

    public IPos down(int n) {
        return new ScriptBlockPos(this.blockPos.down(n));
    }

    public IPos north() {
        return new ScriptBlockPos(this.blockPos.north());
    }

    public IPos north(int n) {
        return new ScriptBlockPos(this.blockPos.north(n));
    }

    public IPos east() {
        return new ScriptBlockPos(this.blockPos.east());
    }

    public IPos east(int n) {
        return new ScriptBlockPos(this.blockPos.east(n));
    }

    public IPos south() {
        return new ScriptBlockPos(this.blockPos.south());
    }

    public IPos south(int n) {
        return new ScriptBlockPos(this.blockPos.south(n));
    }

    public IPos west() {
        return new ScriptBlockPos(this.blockPos.west());
    }

    public IPos west(int n) {
        return new ScriptBlockPos(this.blockPos.west(n));
    }

    public IPos add(int x, int y, int z) {
        return new ScriptBlockPos(this.blockPos.add(x, y, z));
    }

    public IPos add(IPos pos) {
        return new ScriptBlockPos(this.blockPos.add(this.blockPos));
    }

    public IPos subtract(int x, int y, int z) {
        return new ScriptBlockPos(this.blockPos.add(-x, -y, -z));
    }

    public IPos subtract(IPos pos) {
        return new ScriptBlockPos(this.blockPos.add(-pos.getX(), -pos.getY(), -pos.getZ()));
    }

    public IPos offset(int direction) {
        return new ScriptBlockPos(this.blockPos.offset(EnumFacing.values()[direction]));
    }

    public IPos offset(int direction, int n) {
        return new ScriptBlockPos(this.blockPos.offset(EnumFacing.values()[direction], n));
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
}
