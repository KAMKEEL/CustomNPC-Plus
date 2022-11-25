package noppes.npcs.api.jobs;

public interface IJobPuppet extends IJob {

    int getRotationX(int part);

    int getRotationY(int part);

    int getRotationZ(int part);

    void setRotationX(int part, int rotation);

    void setRotationY(int part, int rotation);

    void setRotationZ(int part, int rotation);

    boolean isEnabled(int part);

    void setEnabled(int part, boolean bo);
}
