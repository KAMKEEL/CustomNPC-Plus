package kamkeel.npcs.network.packets.client.sync;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class SyncWeaponPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncWeapon";

    public SyncWeaponPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNC_WEAPON;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Entity entity = player.worldObj.getEntityByID(in.readInt());
        if (!(entity instanceof EntityNPCInterface)) return;

        EntityNPCInterface npc = (EntityNPCInterface) entity;
        int weaponSlotIndex = in.readInt();
        NBTTagCompound nbt = Server.readNBT(in);
        ItemStack stack = ItemStack.loadItemStackFromNBT(nbt);
        npc.inventory.weapons.put(weaponSlotIndex, stack);
    }
}
