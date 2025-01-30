package kamkeel.npcs.network.packets.data.npc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class WeaponNpcPacket extends AbstractPacket {
    public static final String packetName = "Client|WeaponNpc";

    private int entityId;
    private int weaponIndex;
    private NBTTagCompound compound;

    public WeaponNpcPacket() {}

    public WeaponNpcPacket(int entityId, int weaponIndex, NBTTagCompound compound) {
        this.entityId = entityId;
        this.weaponIndex = weaponIndex;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.WEAPON_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityId);
        out.writeInt(this.weaponIndex);

        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(in.readInt());
        if(!(entity instanceof EntityNPCInterface))
            return;
        EntityNPCInterface npc = (EntityNPCInterface) entity;
        int weaponSlotIndex = in.readInt();
        ItemStack stack = ItemStack.loadItemStackFromNBT(ByteBufUtils.readNBT(in));
        npc.inventory.weapons.put(weaponSlotIndex,stack);
    }
}
