package kamkeel.npcs.network.packets.request.linked;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.ScrollSelectedPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.IOException;
import java.util.HashMap;

public final class LinkedGetAllPacket extends AbstractPacket {
    public static String packetName = "Request|LinkedGetAll";

    private Action action;

    public LinkedGetAllPacket() {
    }

    public LinkedGetAllPacket(Action action) {
        this.action = action;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedGetAll;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.action.ordinal());
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        Action action = Action.values()[in.readInt()];
        if (action == Action.NPC) {
            HashMap<String, Integer> list = new HashMap<>();
            for (LinkedNpcController.LinkedData d : LinkedNpcController.Instance.list) {
                list.put(d.name, 0);
            }
            ScrollDataPacket.sendScrollData((EntityPlayerMP) player, list, EnumScrollData.OPTIONAL);
            if (npc != null) {
                ScrollSelectedPacket.setSelectedList((EntityPlayerMP) player, npc.linkedName);
            }
        } else if (action == Action.ITEM) {
            HashMap<String, Integer> list = new HashMap<>();
            for (LinkedItem linkedItem : LinkedItemController.getInstance().linkedItems.values()) {
                list.put(linkedItem.name, linkedItem.id);
            }
            ScrollDataPacket.sendScrollData((EntityPlayerMP) player, list, EnumScrollData.OPTIONAL);
        }
    }

    public static void GetNPCs() {
        PacketClient.sendClient(new LinkedGetAllPacket(Action.NPC));
    }

    public static void GetItems() {
        PacketClient.sendClient(new LinkedGetAllPacket(Action.ITEM));
    }

    private enum Action {
        NPC,
        ITEM
    }
}
