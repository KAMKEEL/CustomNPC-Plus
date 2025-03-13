package kamkeel.npcs.network.packets.request.clone;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ServerCloneController;

import java.io.IOException;

public class MobSpawnerPacket extends AbstractPacket {
    public static final String packetName = "Player|MobSpawner";

    private Action type;
    private int posX;
    private int posY;
    private int posz;

    private String selectedName;
    private int tab;

    private NBTTagCompound compound;

    public MobSpawnerPacket() {
    }

    public MobSpawnerPacket(Action type, int posX, int posY, int posz, String selectedName, int tab) {
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posz = posz;
        this.selectedName = selectedName;
        this.tab = tab;
    }

    public MobSpawnerPacket(Action type, int posX, int posY, int posz, NBTTagCompound compound) {
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posz = posz;
        this.compound = compound;
    }

    public static void Server(int x, int y, int z, String name, int tab) {
        PacketClient.sendClient(new MobSpawnerPacket(Action.Server, x, y, z, name, tab));
    }

    public static void Client(int x, int y, int z, NBTTagCompound compound) {
        PacketClient.sendClient(new MobSpawnerPacket(Action.Client, x, y, z, compound));
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.MobSpawner;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.SPAWNER_CREATE;
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

        if (requestedAction == Action.Server)
            compound = ServerCloneController.Instance.getCloneData(player, ByteBufUtils.readString(in), in.readInt());
        else
            compound = ByteBufUtils.readNBT(in);

        if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT)) {
            return;
        }

        if (compound != null)
            NoppesUtilServer.createMobSpawner(x, y, z, compound, player);
    }

    private enum Action {
        Server,
        Client,
    }
}
