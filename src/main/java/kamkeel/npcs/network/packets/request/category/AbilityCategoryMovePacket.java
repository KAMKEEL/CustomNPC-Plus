package kamkeel.npcs.network.packets.request.category;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.constants.EnumCategoryType;

import java.io.IOException;

public final class AbilityCategoryMovePacket extends AbstractPacket {
    public static String packetName = "NPC|AbCatMove";

    private int catType;
    private String name;
    private int catId;

    public AbilityCategoryMovePacket(int catType, String name, int catId) {
        this.catType = catType;
        this.name = name;
        this.catId = catId;
    }

    public AbilityCategoryMovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.AbilityCategoryMoveItem;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(catType);
        ByteBufUtils.writeString(out, name);
        out.writeInt(catId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int type = in.readInt();
        String itemName = ByteBufUtils.readString(in);
        int cat = in.readInt();

        switch (type) {
            case EnumCategoryType.ABILITY:
                AbilityController.Instance.moveCustomAbilityToCategory(itemName, cat);
                break;
            case EnumCategoryType.CHAINED_ABILITY:
                AbilityController.Instance.moveChainedAbilityToCategory(itemName, cat);
                break;
        }
    }
}
