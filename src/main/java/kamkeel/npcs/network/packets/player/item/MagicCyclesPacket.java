package kamkeel.npcs.network.packets.player.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.MagicCycle;

import java.io.IOException;
import java.util.HashMap;

import static kamkeel.npcs.network.packets.data.large.ScrollDataPacket.sendScrollData;

public class MagicCyclesPacket extends AbstractPacket {
    public static String packetName = "Player|MagicCycles";

    private int id;

    public MagicCyclesPacket() {
    }

    public MagicCyclesPacket(int id) {
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.MagicCycles;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.id);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.MAGIC_BOOK))
            return;

        int id = in.readInt();
        if (id == -1) {
            HashMap<String, Integer> map = new HashMap<>();
            for (MagicCycle magicCycle : MagicController.getInstance().cycles.values()) {
                map.put(magicCycle.displayName.replace("&", "\u00A7"), magicCycle.id);
            }
            sendScrollData((EntityPlayerMP) player, map, EnumScrollData.MAGIC_CYCLES);
        } else {
            MagicCycle cycle = MagicController.getInstance().getCycle(id);
            if (cycle == null)
                return;
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound magicCompound = new NBTTagCompound();
            cycle.writeNBT(magicCompound);
            compound.setTag("MagicCycle", magicCompound);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }

    public static void GetAll() {
        PacketClient.sendClient(new MagicCyclesPacket(-1));
    }

    public static void GetCycle(int id) {
        PacketClient.sendClient(new MagicCyclesPacket(id));
    }
}
