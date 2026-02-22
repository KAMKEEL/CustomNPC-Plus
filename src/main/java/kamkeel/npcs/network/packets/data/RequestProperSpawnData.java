//package kamkeel.npcs.network.packets.data;
//
//import cpw.mods.fml.relauncher.Side;
//import cpw.mods.fml.relauncher.SideOnly;
//import io.netty.buffer.ByteBuf;
//import kamkeel.npcs.network.AbstractPacket;
//import kamkeel.npcs.network.PacketChannel;
//import kamkeel.npcs.network.PacketClient;
//import kamkeel.npcs.network.PacketHandler;
//import kamkeel.npcs.network.enums.EnumDataPacket;
//import kamkeel.npcs.network.packets.data.npc.UpdateNpcPacket;
//import net.minecraft.client.Minecraft;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.nbt.NBTTagCompound;
//import noppes.npcs.entity.EntityNPCInterface;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
//public class RequestProperSpawnData extends AbstractPacket {
//    public static final String packetName = "Data|SpawnData";
//    private static final int BATCH_LIMIT = 5;
//
//    @SideOnly(Side.CLIENT)
//    private static final Set<Integer> entitiesToFix = new HashSet<>();
//
//    @SideOnly(Side.CLIENT)
//    private static long lastBatchAttemptMillis = 0L;
//
//    private int entityId;
//
//    public RequestProperSpawnData() {}
//
//    @SideOnly(Side.CLIENT)
//    public RequestProperSpawnData(int entityId) {
//        this.entityId = entityId;
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void clear() {
//        entitiesToFix.clear();
//        lastBatchAttemptMillis = 0L;
//    }
//
//    @Override
//    public Enum getType() {
//        return EnumDataPacket.REQUEST_SPAWN_DATA;
//    }
//
//    @Override
//    public PacketChannel getChannel() {
//        return PacketHandler.DATA_PACKET;
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void sendData(ByteBuf out) throws IOException {
//        out.writeInt(this.entityId);
//    }
//
//    @Override
//    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
//        if (!(player instanceof EntityPlayerMP) || player.worldObj == null || player.worldObj.isRemote)
//            return;
//
//        int entityId = in.readInt();
//        Entity entity = player.worldObj.getEntityByID(entityId);
//        if (entity instanceof EntityNPCInterface) {
//            EntityNPCInterface npc = (EntityNPCInterface) entity;
//            NBTTagCompound compound = npc.writeSpawnData();
//            compound.setInteger("EntityId", npc.getEntityId());
//            PacketHandler.Instance.sendToPlayer(new UpdateNpcPacket(compound), (EntityPlayerMP) player);
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void reportMissingData(EntityNPCInterface npc) {
//        if (npc == null || npc.worldObj == null || !npc.worldObj.isRemote)
//            return;
//
//        int entityID = npc.getEntityId();
//        if (entityID <= 0)
//            return;
//
//        if (npc.immediateSpawnDataFixAttempts++ < 3) {
//            PacketClient.sendClient(new RequestProperSpawnData(entityID));
//        } else {
//            entitiesToFix.add(entityID);
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static boolean canDoBatchUpdate() {
//        return System.currentTimeMillis() - lastBatchAttemptMillis >= 5000;
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void handleBacklog() {
//        Minecraft mc = Minecraft.getMinecraft();
//        if (mc == null || mc.theWorld == null || mc.thePlayer == null)
//            return;
//
//        if (entitiesToFix.isEmpty())
//            return;
//        // Remove entity checks if they're no longer loaded
//        entitiesToFix.removeIf(value -> !(mc.theWorld.getEntityByID(value) instanceof EntityNPCInterface));
//
//        Iterator<Integer> it = entitiesToFix.iterator();
//        int count = 0;
//
//        while (it.hasNext() && count < BATCH_LIMIT) {
//            PacketClient.sendClient(new RequestProperSpawnData(it.next()));
//            it.remove();
//            count++;
//        }
//
//        if (count != 0) {
//            lastBatchAttemptMillis = System.currentTimeMillis();
//        }
//    }
//}
