package kamkeel.npcs.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class UpdateAnimationsPacket extends AbstractPacket {
    public static final String packetName = "Client|UpdateAnimations";

    public UpdateAnimationsPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.UPDATE_ANIMATIONS;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = Server.readNBT(in);
        AnimationData animationData = null;

        if (nbt.hasKey("EntityId")) {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(nbt.getInteger("EntityId"));
            if (entity instanceof EntityNPCInterface) {
                animationData = ((EntityNPCInterface) entity).display.animationData;
            }
        } else {
            String playerName = Server.readString(in);
            EntityPlayer sendingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);
            if (sendingPlayer != null) {
                if (!ClientCacheHandler.playerAnimations.containsKey(sendingPlayer.getUniqueID())) {
                    ClientCacheHandler.playerAnimations.put(sendingPlayer.getUniqueID(), new AnimationData(sendingPlayer));
                }
                animationData = ClientCacheHandler.playerAnimations.get(sendingPlayer.getUniqueID());
                animationData.parent = sendingPlayer;
            }
        }

        if (animationData != null) {
            int animationId;
            if (nbt.hasKey("AnimationID")) {
                animationId = nbt.getInteger("AnimationID");
            } else {
                Animation animation = new Animation();
                animation.readFromNBT(nbt.getCompoundTag("Animation"));
                ClientCacheHandler.animationCache.put(animation.getID(), animation);
                animationId = animation.getID();
            }

            animationData.setAnimation(ClientCacheHandler.animationCache.get(animationId));
            animationData.readFromNBT(nbt);
        }
    }
}
