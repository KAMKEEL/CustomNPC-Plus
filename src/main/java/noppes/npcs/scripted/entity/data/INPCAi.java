//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity.data;

public interface INPCAi {
    int getAnimation();

    void setAnimation(int var1);

    int getCurrentAnimation();

    void setReturnsHome(boolean var1);

    boolean getReturnsHome();

    int getRetaliateType();

    void setRetaliateType(int var1);

    int getMovingType();

    void setMovingType(int var1);

    int getNavigationType();

    void setNavigationType(int var1);

    int getStandingType();

    void setStandingType(int var1);

    boolean getAttackInvisible();

    void setAttackInvisible(boolean var1);

    int getWanderingRange();

    void setWanderingRange(int var1);

    boolean getInteractWithNPCs();

    void setInteractWithNPCs(boolean var1);

    boolean getStopOnInteract();

    void setStopOnInteract(boolean var1);

    int getWalkingSpeed();

    void setWalkingSpeed(int var1);

    int getMovingPathType();

    boolean getMovingPathPauses();

    void setMovingPathType(int var1, boolean var2);

    int getDoorInteract();

    void setDoorInteract(int var1);

    boolean getCanSwim();

    void setCanSwim(boolean var1);

    int getSheltersFrom();

    void setSheltersFrom(int var1);

    boolean getAttackLOS();

    void setAttackLOS(boolean var1);

    boolean getAvoidsWater();

    void setAvoidsWater(boolean var1);

    boolean getLeapAtTarget();

    void setLeapAtTarget(boolean var1);

    int getTacticalType();

    void setTacticalType(int var1);

    int getTacticalRange();

    void setTacticalRange(int var1);
}
