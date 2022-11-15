//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api;

import net.minecraft.util.DamageSource;
import noppes.npcs.api.entity.IEntity;

public interface IDamageSource {
    /**
     *
     * @return The damage type of the damage source as a string. Ex: "lava", "explosion", "magic", "outOfWorld", etc.
     */
    String getType();

    boolean isUnblockable();

    boolean isProjectile();

    /**
     *
     * @return The entity source of where the damage source originated. If a player was shot by an arrow from a skeleton, this would return an IEntity object of the skeleton.
     */
    IEntity getTrueSource();

    /**
     *
     * @return The entity source of where the damage source originated. If a player was shot by an arrow from a skeleton, this would return an IEntity object of the arrow.
     */
    IEntity getImmediateSource();

    /**
     *
     * @return An obfuscated MC damage source object.
     */
    DamageSource getMCDamageSource();
}
