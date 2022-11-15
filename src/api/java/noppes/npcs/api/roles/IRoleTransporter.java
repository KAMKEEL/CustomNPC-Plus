package noppes.npcs.api.roles;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ITransportLocation;

public interface IRoleTransporter extends IRole {
    String getName();

    int getTransportId();

    void unlock(IPlayer<EntityPlayerMP> player, ITransportLocation location);

    ITransportLocation getTransport();

    boolean hasTransport();

    void setTransport(ITransportLocation location);
}
