package kamkeel.npcs.network.packets.data.energycharge;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.energycharge.EnergyChargePreviewManager;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

/**
 * Removes a client-only charging preview entity.
 */
public final class EnergyChargeRemovePacket extends AbstractPacket {
    public static final String packetName = "Data|EnergyChargeRemove";

    private String instanceId;

    public EnergyChargeRemovePacket() {
    }

    public EnergyChargeRemovePacket(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ENERGY_CHARGE_REMOVE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, instanceId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String id = ByteBufUtils.readString(in);
        if (id != null && EnergyChargePreviewManager.ClientInstance != null) {
            EnergyChargePreviewManager.ClientInstance.removePreview(id);
        }
    }

    public static void sendToTracking(String instanceId, Entity trackingEntity) {
        if (instanceId == null || trackingEntity == null) return;
        PacketHandler.Instance.sendTracking(new EnergyChargeRemovePacket(instanceId), trackingEntity);
    }
}
