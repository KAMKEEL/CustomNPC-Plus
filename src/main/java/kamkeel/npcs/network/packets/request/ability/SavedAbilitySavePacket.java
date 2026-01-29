package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

/**
 * Request packet to save an ability preset.
 */
public final class SavedAbilitySavePacket extends AbstractPacket {
    public static String packetName = "Request|SavedAbilitySave";

    private NBTTagCompound abilityNBT;

    public SavedAbilitySavePacket() {
    }

    public SavedAbilitySavePacket(NBTTagCompound abilityNBT) {
        this.abilityNBT = abilityNBT;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.SavedAbilitySave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_ABILITY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, abilityNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Ability ability = AbilityController.Instance.fromNBT(compound);
        if (ability != null) {
            AbilityController.Instance.saveAbility(ability);
            NoppesUtilServer.sendSavedAbilitiesData((EntityPlayerMP) player);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, ability.writeNBT());
        }
    }
}
