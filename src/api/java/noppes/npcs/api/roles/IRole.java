package noppes.npcs.api.roles;

import net.minecraft.entity.INpc;

public interface IRole {

    INpc getNpc();

    int getType();
}
