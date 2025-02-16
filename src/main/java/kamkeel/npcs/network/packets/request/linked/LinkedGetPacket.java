package kamkeel.npcs.network.packets.request.linked;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.IOException;

public final class LinkedGetPacket extends AbstractPacket {
    public static String packetName = "Request|LinkedGet";

    private Action action;
    private int id;
    private String name;

    public LinkedGetPacket() {}

    public LinkedGetPacket(Action action, int id) {
        this.action = action;
        this.id = id;
    }

    public LinkedGetPacket(Action action, String name) {
        this.action = action;
        this.name = name;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.action.ordinal());
        if(action == Action.NPC){
            ByteBufUtils.writeString(out, this.name);
        } else {
            out.writeInt(this.id);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        Action action = Action.values()[in.readInt()];
        if(action == Action.NPC){
            String name = ByteBufUtils.readString(in);
            LinkedNpcController.LinkedData data = LinkedNpcController.Instance.getData(name);
            NBTTagCompound compound = data.getNBT();
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        } else if (action == Action.ITEM) {
            int id = in.readInt();
            LinkedItem data = LinkedItemController.getInstance().get(id);
            NBTTagCompound compound = data.writeToNBT();
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }

    public static void GetNPC(String name) {
        PacketClient.sendClient(new LinkedGetPacket(Action.NPC, name));
    }

    public static void GetItem(int id) {
        PacketClient.sendClient(new LinkedGetPacket(Action.ITEM, id));
    }

    private enum Action {
        NPC,
        ITEM
    }
}
