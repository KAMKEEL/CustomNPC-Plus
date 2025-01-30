package kamkeel.npcs.network.packets.data.npc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class DialogPacket extends AbstractPacket {
    public static final String packetName = "Client|Dialog";

    private String dummyName;
    private int entityID;
    private NBTTagCompound compound;

    public DialogPacket() {}

    public DialogPacket(String dummyName, int entityID, NBTTagCompound compound) {
        this.dummyName = dummyName;
        this.entityID = entityID;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.DIALOG;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityID);
        ByteBufUtils.writeString(out, this.dummyName);
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int entityID = in.readInt();
        String name = ByteBufUtils.readString(in);
        NBTTagCompound tagCompound = ByteBufUtils.readNBT(in);

        // Dummy NPC
        if(entityID == -1){
            EntityDialogNpc npc = new EntityDialogNpc(player.worldObj);
            npc.display.name = name;
            NoppesUtil.openDialog(tagCompound, npc, player);
        }
        else {
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityID);
            if (!(entity instanceof EntityNPCInterface)) {
                return;
            }
            NoppesUtil.openDialog(tagCompound, (EntityNPCInterface) entity, player);
        }
    }
}
