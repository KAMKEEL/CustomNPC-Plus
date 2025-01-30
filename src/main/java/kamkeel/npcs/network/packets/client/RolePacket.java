package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class RolePacket extends AbstractPacket {
    public static final String packetName = "Client|Role";

    private NBTTagCompound compound;

    public RolePacket() {}

    public RolePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.ROLE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            npc.advanced.setRole(compound.getInteger("Role"));
            npc.roleInterface.readFromNBT(compound);
            NoppesUtil.setLastNpc(npc);
        }
    }
}
