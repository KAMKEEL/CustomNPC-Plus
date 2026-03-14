package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IUser;
import kamkeel.npcs.platform.entity.IStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

/**
 * MC 1.7.10 implementation of {@link IUser}.
 * Wraps a raw {@link EntityPlayerMP} instance.
 */
public class PlayerWrapper extends LivingWrapper implements IUser {

    private final EntityPlayerMP player;

    public PlayerWrapper(EntityPlayerMP player) {
        super(player);
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getCommandSenderName();
    }

    @Override
    public String getUniqueID() {
        return player.getUniqueID().toString();
    }

    @Override
    public boolean isOp() {
        return player.mcServer.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }

    @Override
    public void sendMessage(String text) {
        player.addChatMessage(new ChatComponentText(text));
    }

    @Override
    public IStack getHeldItem() {
        if (player.getHeldItem() == null) return null;
        return new StackWrapper(player.getHeldItem());
    }
}
