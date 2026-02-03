package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.util.Vec3;
import noppes.npcs.api.ability.data.IEnergyTrajectoryData;

import java.util.ArrayList;
import java.util.List;

public class EnergyTrajectoryData implements IEnergyTrajectoryData {
    public float speed = 0.5f;
    public int currentPath = 0;
    private List<Path> trajectory = new ArrayList<>();

    public int getDelay(int path) {
        if (path < 0 || path >= trajectory.size()) return -1;
        return trajectory.get(path).delayTicks;
    }

    public Vec3 getVec(int path) {
        if (path < 0 || path >= trajectory.size()) return null;
        return trajectory.get(path).coords;
    }

    public double getX(int path) {
        if (getVec(path) == null) return 0;
        return getVec(path).xCoord;
    }

    public double getY(int path) {
        if (getVec(path) == null) return 0;
        return getVec(path).yCoord;
    }

    public double getZ(int path) {
        if (getVec(path) == null) return 0;
        return getVec(path).zCoord;
    }

    public static class Path {
        public Vec3 coords;
        public int delayTicks;

        public Path(Vec3 coords, int delayTicks) {
            this.coords = coords;
            this.delayTicks = delayTicks;
        }
    }
}
