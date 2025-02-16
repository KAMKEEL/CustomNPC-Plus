package kamkeel.npcs.network.packets.request.linked;

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.scripted.item.ScriptLinkedItem;

import java.io.IOException;

public final class LinkedItemBuildPacket extends AbstractPacket {
    public static String packetName = "Request|LinkedItemBuild";

    private int id;

    public LinkedItemBuildPacket(){}

    public LinkedItemBuildPacket(int id){
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedItemBuild;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_LINKED;
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
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        LinkedItem linkedItem = LinkedItemController.getInstance().get(id);
        if(linkedItem == null)
            return;

        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        ItemStack stack = new ItemStack(CustomItems.linked_item, 1);
        ScriptLinkedItem scriptLinkedItem = new ScriptLinkedItem(stack, linkedItem.writeToNBT());
        scriptLinkedItem.linkedVersion = linkedItem.version;
        scriptLinkedItem.saveItemData();
        playerMP.inventory.addItemStackToInventory(stack.copy());
    }
}
