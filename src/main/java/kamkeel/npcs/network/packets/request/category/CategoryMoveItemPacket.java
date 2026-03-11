package kamkeel.npcs.network.packets.request.category;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import kamkeel.npcs.controllers.AbilityController;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.CategoryManager;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.LinkedItemController;

import java.io.IOException;

public final class CategoryMoveItemPacket extends AbstractPacket {
    public static String packetName = "NPC|CatMove";

    private int catType;
    private int itemId;
    private int catId;

    public CategoryMoveItemPacket(int catType, int itemId, int catId) {
        this.catType = catType;
        this.itemId = itemId;
        this.catId = catId;
    }

    public CategoryMoveItemPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CategoryMoveItem;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(catType);
        out.writeInt(itemId);
        out.writeInt(catId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int type = in.readInt();
        int item = in.readInt();
        int cat = in.readInt();

        // Validate target category exists (0 = Uncategorized is always valid)
        CategoryManager cm = CategorySavePacket.getManager(type);
        if (cm == null) return;
        if (cat != CategoryManager.UNCATEGORIZED_ID && cm.getCategory(cat) == null) return;

        switch (type) {
            case EnumCategoryType.EFFECT: CustomEffectController.getInstance().moveItemToCategory(item, cat); break;
            case EnumCategoryType.ANIMATION: AnimationController.getInstance().moveItemToCategory(item, cat); break;
            case EnumCategoryType.LINKED_ITEM: LinkedItemController.getInstance().moveItemToCategory(item, cat); break;
        }
    }
}
