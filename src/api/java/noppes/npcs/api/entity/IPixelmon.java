//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.api.entity;

import net.minecraft.entity.passive.EntityTameable;

public interface IPixelmon<T extends EntityTameable> extends IAnimal<T> {
    boolean getIsShiny();

    void setIsShiny(boolean bo);

    int getLevel();

    void setLevel(int level);

    /**
     * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
     */
    int getIV(int type);


    /**
     * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
     */
    void setIV(int type, int value);

    /**
     * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
     */
    int getEV(int type);


    /**
     * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
     */
    void setEV(int type, int value);

    /**
     * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
     */
    int getStat(int type);


    /**
     * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
     */
    void setStat(int type, int value);

    /**
     * @return type 0:Pygmy, 1:Runt, 2:Small, 3:Normal, 4:Huge, 5:Giant, 6:Enormous, 7:Ginormous, 8:Microscopic
     */
    int getSize();

    /**
     * @param type 0:Pygmy, 1:Runt, 2:Small, 3:Normal, 4:Huge, 5:Giant, 6:Enormous, 7:Ginormous, 8:Microscopic
     */
    void setSize(int type);

    /**
     * @return 0-255
     */
    int getHapiness();

    /**
     * @param value 0-255
     */
    void setHapiness(int value);

    /**
     * @return 0:Hardy, 1:Serious, 2:Docile, 3:Bashful, 4:Quirky, 5:Lonely, 6:Brave, 7:Adamant, 8:Naughty, 9:Bold, 10:Relaxed, 11:Impish, 12:Lax, 13:Timid, 14:Hasty, 15:Jolly, 16:Naive, 17:Modest, 18:Mild, 19:Quiet, 20:Rash, 21:Calm, 22:Gentle, 23:Sassy, 24:Careful
     */
    int getNature();

    /**
     * @param type 0:Hardy, 1:Serious, 2:Docile, 3:Bashful, 4:Quirky, 5:Lonely, 6:Brave, 7:Adamant, 8:Naughty, 9:Bold, 10:Relaxed, 11:Impish, 12:Lax, 13:Timid, 14:Hasty, 15:Jolly, 16:Naive, 17:Modest, 18:Mild, 19:Quiet, 20:Rash, 21:Calm, 22:Gentle, 23:Sassy, 24:Careful
     */
    void setNature(int type);

    /**
     * @return -1:Uncaught, 0:Pokeball, 1:GreatBall, 2:UltraBall, 3:MasterBall, 4:LevelBall, 5:MoonBall, 6:FriendBall, 7:LoveBall, 8:SafariBall, 9:HeavyBall, 10:FastBall, 11:RepeatBall, 12:TimerBall, 13:NestBall, 14:NetBall, 15:DiveBall, 16:LuxuryBall, 17:HealBall, 18:DuskBall, 19:PremierBall, 20:SportBall, 21:QuickBall, 22:ParkBall, 23:LureBall, 24:CherishBall, 25:GSBall
     */
    int getPokeball();

    /**
     * @param type -1:Uncaught, 0:Pokeball, 1:GreatBall, 2:UltraBall, 3:MasterBall, 4:LevelBall, 5:MoonBall, 6:FriendBall, 7:LoveBall, 8:SafariBall, 9:HeavyBall, 10:FastBall, 11:RepeatBall, 12:TimerBall, 13:NestBall, 14:NetBall, 15:DiveBall, 16:LuxuryBall, 17:HealBall, 18:DuskBall, 19:PremierBall, 20:SportBall, 21:QuickBall, 22:ParkBall, 23:LureBall, 24:CherishBall, 25:GSBall
     */
    void setPokeball(int type);

    String getNickname();

    boolean hasNickname();

    void setNickname(String name);

    String getMove(int slot);

    void setMove(int slot, String move);
}
