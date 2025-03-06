package kamkeel.npcs.network.packets.request.clone;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public class SpawnMobPacket extends AbstractPacket {
    public static final String packetName = "Player|SpawnMob";

    private Action type;
    private int posX;
    private int posY;
    private int posz;

    private String selectedName;
    private int tab;

    private NBTTagCompound compound;

    public SpawnMobPacket() {}

    public SpawnMobPacket(Action type, int posX, int posY, int posz, String selectedName, int tab) {
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posz = posz;
        this.selectedName = selectedName;
        this.tab = tab;
    }

    public SpawnMobPacket(Action type, int posX, int posY, int posz, NBTTagCompound compound) {
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posz = posz;
        this.compound = compound;
    }

    public static SpawnMobPacket Server(int x, int y, int z, String name, int tab) {
        return new SpawnMobPacket(Action.Server, x, y, z, name, tab);
    }
    public static SpawnMobPacket Client(int x, int y, int z, NBTTagCompound compound) {
        return new SpawnMobPacket(Action.Client, x, y, z, compound);
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.SpawnMob;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SPAWNER_MOB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());
        out.writeInt(posX);
        out.writeInt(posY);
        out.writeInt(posz);

        if (type == Action.Server) {
            ByteBufUtils.writeString(out, this.selectedName);
            out.writeInt(this.tab);
        } else {
            ByteBufUtils.writeNBT(out, this.compound);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.CLONER))
            return;

        Action requestedAction = Action.values()[in.readInt()];
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        NBTTagCompound compound;

        if(requestedAction == Action.Server)
            compound = ServerCloneController.Instance.getCloneData(player, ByteBufUtils.readString(in), in.readInt());
        else
            compound = ByteBufUtils.readNBT(in);

        if(compound == null)
            return;
        Entity entity = NoppesUtilServer.spawnClone(compound, x, y, z, player.worldObj);
        if(entity == null){
            player.addChatMessage(new ChatComponentText("Failed to create an entity out of your clone"));
            return;
        }

        if (entity instanceof EntityNPCInterface && !ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT)) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            npc.script.setEnabled(false);
        }

        if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
            LogWriter.script(String.format("[%s] (Player) %s SPAWNED ENTITY %s", "CLONER", player.getCommandSenderName(), entity));
        }
    }

    private enum Action {
        Server,
        Client,
    }
}
