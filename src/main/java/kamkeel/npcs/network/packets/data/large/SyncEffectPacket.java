package kamkeel.npcs.network.packets.data.large;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

import java.io.IOException;

public final class SyncEffectPacket extends LargeAbstractPacket {

    private NBTTagCompound syncData;

    public SyncEffectPacket() {}

    public SyncEffectPacket(NBTTagCompound syncData) {
        this.syncData = syncData;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.SYNC_EFFECTS;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeBigNBT(buffer, syncData);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        if (CustomNpcs.side() != Side.CLIENT)
            return;
        try {
            NBTTagCompound tag = ByteBufUtils.readBigNBT(data);
            SyncController.clientSyncEffects(tag);
        }
        catch (RuntimeException e){
            LogWriter.error(String.format("Attempted to Sync Effects but it was too big"));
        }
    }
}
