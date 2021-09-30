//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity.data;

import noppes.npcs.scripted.entity.data.INPCMelee;
import noppes.npcs.scripted.entity.data.INPCRanged;

public interface INPCStats {
    int getMaxHealth();

    void setMaxHealth(int var1);

    float getResistance(int var1);

    void setResistance(int var1, float var2);

    int getCombatRegen();

    void setCombatRegen(int var1);

    int getHealthRegen();

    void setHealthRegen(int var1);

    INPCMelee getMelee();

    INPCRanged getRanged();

    boolean getImmune(int var1);

    void setImmune(int var1, boolean var2);

    void setCreatureType(int var1);

    int getCreatureType();

    int getRespawnType();

    void setRespawnType(int var1);

    int getRespawnTime();

    void setRespawnTime(int var1);

    boolean getHideDeadBody();

    void setHideDeadBody(boolean var1);

    int getAggroRange();

    void setAggroRange(int var1);
}
