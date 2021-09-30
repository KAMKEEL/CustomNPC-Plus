//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity.data;

public interface INPCRanged {
    int getStrength();

    void setStrength(int var1);

    int getSpeed();

    void setSpeed(int var1);

    int getBurst();

    void setBurst(int var1);

    int getBurstDelay();

    void setBurstDelay(int var1);

    int getKnockback();

    void setKnockback(int var1);

    int getSize();

    void setSize(int var1);

    boolean getRender3D();

    void setRender3D(boolean var1);

    boolean getSpins();

    void setSpins(boolean var1);

    boolean getSticks();

    void setSticks(boolean var1);

    boolean getHasGravity();

    void setHasGravity(boolean var1);

    boolean getAccelerate();

    void setAccelerate(boolean var1);

    int getExplodeSize();

    void setExplodeSize(int var1);

    int getEffectType();

    int getEffectTime();

    int getEffectStrength();

    void setEffect(int var1, int var2, int var3);

    boolean getGlows();

    void setGlows(boolean var1);

    int getParticle();

    void setParticle(int var1);

    String getSound(int var1);

    void setSound(int var1, String var2);

    int getShotCount();

    void setShotCount(int var1);

    boolean getHasAimAnimation();

    void setHasAimAnimation(boolean var1);

    int getAccuracy();

    void setAccuracy(int var1);

    int getRange();

    void setRange(int var1);

    int getDelayMin();

    int getDelayMax();

    int getDelayRNG();

    void setDelay(int var1, int var2);

    int getFireType();

    void setFireType(int var1);

    int getMeleeRange();

    void setMeleeRange(int var1);
}
