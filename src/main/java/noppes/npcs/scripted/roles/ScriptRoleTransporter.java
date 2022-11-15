package noppes.npcs.scripted.roles;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ITransportLocation;
import noppes.npcs.api.roles.IRoleTransporter;

public class ScriptRoleTransporter extends ScriptRoleInterface implements IRoleTransporter {
	private final RoleTransporter role;

	public ScriptRoleTransporter(EntityNPCInterface npc) {
		super(npc);
		role = (RoleTransporter) npc.roleInterface;

	}

	public String getName() {
		return role.name;
	}

	public int getTransportId() {
		return role.transportId;
	}

	public void unlock(IPlayer<EntityPlayerMP> player, ITransportLocation location) {
		this.role.unlock(player.getMCEntity(), location);
	}

	public ITransportLocation getTransport(){
		return this.role.getLocation();
	}

	public boolean hasTransport(){
		return this.role.hasTransport();
	}

	public void setTransport(ITransportLocation location) {
		this.role.setTransport(location);
	}

	@Override
	public int getType(){
		return RoleType.TRANSPORTER;
	}
}
