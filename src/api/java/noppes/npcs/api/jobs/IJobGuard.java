package noppes.npcs.api.jobs;

public interface IJobGuard extends IJob {
    boolean attackCreepers();
    void attackCreepers(boolean value);

    boolean attacksAnimals();
    void attacksAnimals(boolean value);

    boolean attackHostileMobs();
    void attackHostileMobs(boolean value);
}
