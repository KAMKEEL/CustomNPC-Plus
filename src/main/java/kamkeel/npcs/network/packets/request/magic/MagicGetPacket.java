package kamkeel.npcs.network.packets.request.magic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCycle;

import java.io.IOException;

public final class MagicGetPacket extends AbstractPacket {
    public static String packetName = "Request|MagicGet";

    private Action action;
    private int id;

    public MagicGetPacket() {
    }

    public MagicGetPacket(Action action, int id) {
        this.action = action;
        this.id = id;
    }


    @Override
    public Enum getType() {
        return EnumRequestPacket.MagicGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.action.ordinal());
        out.writeInt(this.id);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND))
            return;

        Action action = Action.values()[in.readInt()];
        int id = in.readInt();
        if (action == Action.MAGIC) {
            Magic magic = MagicController.getInstance().getMagic(id);
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound magicCompound = new NBTTagCompound();
            magic.writeNBT(magicCompound);
            compound.setTag("Magic", magicCompound);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        } else if (action == Action.CYCLE) {
            MagicCycle cycle = MagicController.getInstance().getCycle(id);
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound magicCompound = new NBTTagCompound();
            cycle.writeNBT(magicCompound);
            compound.setTag("MagicCycle", magicCompound);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }

    public static void GetMagic(int id) {
        PacketClient.sendClient(new MagicGetPacket(Action.MAGIC, id));
    }

    public static void GetCycle(int id) {
        PacketClient.sendClient(new MagicGetPacket(Action.CYCLE, id));
    }

    private enum Action {
        MAGIC,
        CYCLE
    }
}
