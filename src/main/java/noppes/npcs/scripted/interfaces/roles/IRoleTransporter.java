package noppes.npcs.scripted.interfaces.roles;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.scripted.interfaces.entity.IPlayer;
import noppes.npcs.scripted.interfaces.handler.data.ITransportLocation;

public interface IRoleTransporter extends IRole {
    String getName();

    int getTransportId();

    void unlock(IPlayer<EntityPlayerMP> player, ITransportLocation location);

    ITransportLocation getTransport();

    boolean hasTransport();

    void setTransport(ITransportLocation location);
}
