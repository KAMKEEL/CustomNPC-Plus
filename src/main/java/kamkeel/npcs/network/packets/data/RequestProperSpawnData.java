package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.npc.UpdateNpcPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public class RequestProperSpawnData extends AbstractPacket {
    public static final String packetName = "Player|SpawnData";
    private static final long REQUEST_COOLDOWN_MILLIS = 1000L;
    private int entityId;

    public RequestProperSpawnData() {}

    public RequestProperSpawnData(int entityId) {
        this.entityId = entityId;
    }

    @SideOnly(Side.CLIENT)
    public static void clear() {
        // No backlog queue to clear.
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.RequestSpawnData;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP) || player.worldObj == null || player.worldObj.isRemote)
            return;

        int entityId = in.readInt();
        Entity entity = player.worldObj.getEntityByID(entityId);
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            NBTTagCompound compound = npc.writeSpawnData();
            compound.setInteger("EntityId", npc.getEntityId());
            PacketHandler.Instance.sendToPlayer(new UpdateNpcPacket(compound), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void reportMissingData(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null || !npc.worldObj.isRemote)
            return;

        int entityID = npc.getEntityId();
        if (entityID <= 0)
            return;

        long now = System.currentTimeMillis();
        if (now - npc.clientFixLastRequestMillis < REQUEST_COOLDOWN_MILLIS)
            return;
        if (npc.clientFixAttempts >= 3)
            return;

        npc.clientFixLastRequestMillis = now;
        npc.clientFixAttempts++;
        PacketClient.sendClient(new RequestProperSpawnData(entityID));
    }

    @SideOnly(Side.CLIENT)
    public static boolean canDoBatchUpdate() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void handleBacklog() {
        // No batch backlog. Requests are immediate and throttled in reportMissingData().
    }
}
