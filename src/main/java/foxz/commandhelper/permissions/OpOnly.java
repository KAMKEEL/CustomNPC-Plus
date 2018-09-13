package foxz.commandhelper.permissions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import foxz.commandhelper.AbstractCommandHelper;

public class OpOnly extends AbstractPermission {

    @Override
    public String errorMsg() {
        return "Op Only";
    }

    @Override
    public boolean delegate(AbstractCommandHelper parent, String[] args) {
    	if(!(parent.pcParam instanceof EntityPlayer))
    		return true;
        return MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)parent.pcParam).getGameProfile());
    }

}
