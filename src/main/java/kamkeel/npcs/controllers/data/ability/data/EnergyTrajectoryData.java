package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ability.data.IEnergyTrajectoryData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class EnergyTrajectoryData implements IEnergyTrajectoryData {
    public int currentPath = 0;
    private final List<Path> trajectory = new ArrayList<>();

    public EnergyTrajectoryData() {}

    public EnergyTrajectoryData(int startingPath, Path... paths) {
        trajectory.addAll(Arrays.asList(paths));
        this.currentPath = startingPath < trajectory.size() && startingPath >= 0 ? startingPath : 0;
    }

    @Override
    public int getCurrentPath() {
        return currentPath;
    }

    @Override
    public void setCurrentPath(int currentPath) {
        this.currentPath = currentPath;
    }

    @Override
    public int getDelay(int path) {
        if (getPath(path) == null) return -1;
        return getPath(path).getDelay();
    }

    @Override
    public void setDelay(int path, int ticks) {
        if (getPath(path) == null) return;
        getPath(path).setDelay(ticks);
    }

    @Override
    public boolean isConcluded(int path) {
        IPath p = getPath(path);
        return p != null && p.isConcluded();
    }

    @Override
    public void setConcluded(int path, boolean concluded) {
        IPath p = getPath(path);
        if (p != null) p.setConcluded(concluded);
    }

    @Override
    public double getX(int path) {
        IPath p = getPath(path);
        return p == null ? 0 : p.getX();
    }

    @Override
    public double getY(int path) {
        IPath p = getPath(path);
        return p == null ? 0 : p.getY();
    }

    @Override
    public double getZ(int path) {
        IPath p = getPath(path);
        return p == null ? 0 : p.getZ();
    }
    @Override
    public void setX(int path, double x) {
        IPath p = getPath(path);
        if (p != null) p.setX(x);
    }

    @Override
    public void setY(int path, double y) {
        IPath p = getPath(path);
        if (p != null) p.setY(y);
    }

    @Override
    public void setZ(int path, double z) {
        IPath p = getPath(path);
        if (p != null) p.setZ(z);
    }

    public IPath getPath(int path) {
        if (path < 0 || path >= trajectory.size()) return null;
        return trajectory.get(path);
    }

    @Override
    public void setPath(int path, double x, double y, double z) {
        setPath(path, x, y, z, 0);
    }

    @Override
    public void setPath(int path, double x, double y, double z, int delay) {
        Path newPath = new Path(x, y, z, delay);

        while (trajectory.size() <= path) {
            trajectory.add(null);
        }

        trajectory.set(path, newPath);
    }

    @Override
    public IPath createPath(double x, double y, double z) {
        return createPath(x, y, z, 0);
    }

    @Override
    public IPath createPath(double x, double y, double z, int delay) {
        return new Path(x, y, z, delay);
    }

    @Override
    public void forEach(BiConsumer<Integer, IPath> lambda) {
        for (int i = 0; i < trajectory.size(); i++) {
            lambda.accept(i, trajectory.get(i));
        }
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("currentPath", currentPath);

        for (int i = 0; i < trajectory.size(); i++) {
            Path path = trajectory.get(i);
            NBTTagCompound pathNbt = new NBTTagCompound();
            path.writeNBT(pathNbt);
            nbt.setTag("Path_" + i, pathNbt);
        }
    }

    public void readNBT(NBTTagCompound nbt) {
        this.currentPath = nbt.hasKey("currentPath") ? nbt.getInteger("currentPath") : 0;

        int i = 0;
        while (nbt.hasKey("Path_" + i, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound pathNbt = nbt.getCompoundTag("Path_" + i);
            Path path = (Path) createPath(0, 0, 0);
            path.readNBT(pathNbt);
            trajectory.add(path);
            i++;
        }
    }

    public EnergyTrajectoryData copy() {
        return new EnergyTrajectoryData(currentPath, Collections.singletonList(trajectory).toArray(trajectory.toArray(new Path[0])));
    }

    public static class Path implements IEnergyTrajectoryData.IPath{
        public Vec3 pos;
        public int delayTicks;
        public boolean concluded;

        public Path(double x, double y, double z, int delayTicks) {
            this.pos = Vec3.createVectorHelper(x,y,z);
            this.delayTicks = delayTicks;
        }

        @Override
        public int getDelay() {
            return delayTicks;
        }

        @Override
        public void setDelay(int delayTicks) {
            this.delayTicks = delayTicks;
        }

        @Override
        public boolean isConcluded() {
            return concluded;
        }

        @Override
        public void setConcluded(boolean concluded) {
            this.concluded = concluded;
        }

        @Override
        public double getX() {
            return pos.xCoord;
        }

        @Override
        public void setX(double x) {
            this.pos.xCoord = x;
        }

        @Override
        public double getY() {
            return pos.yCoord;
        }

        @Override
        public void setY(double y) {
            this.pos.yCoord = y;
        }

        @Override
        public double getZ() {
            return pos.zCoord;
        }

        @Override
        public void setZ(double z) {
            this.pos.zCoord = z;
        }

        public void writeNBT(NBTTagCompound nbt) {
            nbt.setInteger("delay", delayTicks);
            nbt.setBoolean("concluded", concluded);
            nbt.setDouble("X", getX());
            nbt.setDouble("Y", getY());
            nbt.setDouble("Z", getZ());
        }

        public void readNBT(NBTTagCompound nbt) {
            this.delayTicks = nbt.hasKey("delay") ? nbt.getInteger("delay") : 0;
            this.concluded = nbt.hasKey("concluded") && nbt.getBoolean("concluded");

            double x = nbt.getDouble("X");
            double y = nbt.getDouble("Y");
            double z = nbt.getDouble("Z");

            this.pos = Vec3.createVectorHelper(x, y, z);
        }
    }
}
