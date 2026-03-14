package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IPlatformPlayer;
import kamkeel.npcs.platform.entity.IPlatformStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

/**
 * MC 1.7.10 implementation of {@link IPlatformPlayer}.
 * Wraps a raw {@link EntityPlayerMP} instance.
 */
public class MC1710PlatformPlayer extends MC1710PlatformLiving implements IPlatformPlayer {

    private final EntityPlayerMP player;

    public MC1710PlatformPlayer(EntityPlayerMP player) {
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
    public IPlatformStack getHeldItem() {
        if (player.getHeldItem() == null) return null;
        return new MC1710PlatformStack(player.getHeldItem());
    }
}
