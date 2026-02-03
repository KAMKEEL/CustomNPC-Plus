package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.util.Vec3;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ability.data.IEnergyTrajectoryData;
import noppes.npcs.scripted.NpcAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnergyTrajectoryData implements IEnergyTrajectoryData {
    public int currentPath = 0;
    private final List<Path> trajectory = new ArrayList<>();

    public EnergyTrajectoryData() {}

    public EnergyTrajectoryData(int startingPath, Path... paths) {
        trajectory.addAll(Arrays.asList(paths));
        this.currentPath = startingPath < trajectory.size() && startingPath >= 0 ? startingPath : 0;
    }

    public int getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(int currentPath) {
        this.currentPath = currentPath;
    }

    public int getDelay(int path) {
        if (getPath(path) == null) return -1;
        return getPath(path).delayTicks;
    }

    public void setDelay(int path, int ticks) {
        if (getPath(path) == null) return;
        getPath(path).delayTicks = ticks;
    }

    public IPos getPos(int path) {
        if (getPath(path) == null) return null;
        return getPath(path).coords;
    }

    public void setPos(int path, IPos coords) {
        if (getPath(path) == null) return;
        getPath(path).coords = coords;
    }

    public double getX(int path) {
        if (getPos(path) == null) return 0;
        return getPos(path).getX();
    }

    public void setX(int path, double x) {
        if (getPos(path) == null) return;
        setPos(path, NpcAPI.Instance().getIPos(x, getY(path), getZ(path)));
    }

    public double getY(int path) {
        if (getPos(path) == null) return 0;
        return getPos(path).getY();
    }

    public void setY(int path, double y) {
        if (getPos(path) == null) return;
        setPos(path, NpcAPI.Instance().getIPos(getX(path), y, getZ(path)));
    }

    public double getZ(int path) {
        if (getPos(path) == null) return 0;
        return getPos(path).getZ();
    }

    public void setZ(int path, double z) {
        if (getPos(path) == null) return;
        setPos(path, NpcAPI.Instance().getIPos(getX(path), getY(path), z));
    }

    public Path getPath(int path) {
        if (path < 0 || path >= trajectory.size()) return null;
        return trajectory.get(path);
    }

    public void setPath(int path, IPos pos) {
        setPath(path, pos, 0);
    }

    public void setPath(int path, IPos pos, int delay) {
        setPath(path, pos.getX(), pos.getY(), pos.getZ(), delay);
    }

    public void setPath(int path, double x, double y, double z) {
        setPath(path, x, y, z, 0);
    }

    public void setPath(int path, double x, double y, double z, int delay) {
        Path newPath = createPath(x, y, z, delay);

        if (trajectory.isEmpty()) {
            trajectory.add(newPath);
        } else {
            trajectory.set(path, newPath);
        }
    }

    public Path createPath(IPos coords) {
        return new Path(coords, 0);
    }

    public Path createPath(IPos coords, int delay) {
        return new Path(coords, delay);
    }

    public Path createPath(double x, double y, double z) {
        return createPath(x, y, z, 0);
    }

    public Path createPath(double x, double y, double z, int delay) {
        return new Path(NpcAPI.Instance().getIPos(x, y, z), delay);
    }

    public static class Path {
        public IPos coords;
        public int delayTicks;

        public Path(IPos coords, int delayTicks) {
            this.coords = coords;
            this.delayTicks = delayTicks;
        }
    }
}
