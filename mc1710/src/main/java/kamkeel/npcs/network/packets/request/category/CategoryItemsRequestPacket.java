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
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import kamkeel.npcs.controllers.AbilityController;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.TagController;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public final class CategoryItemsRequestPacket extends AbstractPacket {
    public static String packetName = "NPC|CatItems";

    private int catType;
    private int catId;

    public CategoryItemsRequestPacket(int catType, int catId) {
        this.catType = catType;
        this.catId = catId;
    }

    public CategoryItemsRequestPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CategoryItemsRequest;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(catType);
        out.writeInt(catId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int type = in.readInt();
        int categoryId = in.readInt();

        Map<String, Integer> items = getItemsByCat(type, categoryId);
        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, items, EnumScrollData.CATEGORY_GROUP);

        // Send tag map for the items
        HashMap<String, HashSet<UUID>> tagMap = getTagMapByCat(type, categoryId);
        if (tagMap != null && !tagMap.isEmpty()) {
            TagController.sendCategoryTagMap((EntityPlayerMP) player, tagMap);
        }
    }

    private static Map<String, Integer> getItemsByCat(int type, int catId) {
        switch (type) {
            case EnumCategoryType.EFFECT: return CustomEffectController.getInstance().getItemsByCategoryScrollData(catId);
            case EnumCategoryType.ANIMATION: return AnimationController.getInstance().getItemsByCategoryScrollData(catId);
            case EnumCategoryType.LINKED_ITEM: return LinkedItemController.getInstance().getItemsByCategoryScrollData(catId);
            case EnumCategoryType.ABILITY: return AbilityController.Instance.getCustomAbilityItemsByCategoryScrollData(catId);
            case EnumCategoryType.CHAINED_ABILITY: return AbilityController.Instance.getChainedAbilityItemsByCategoryScrollData(catId);
        }
        return new HashMap<>();
    }

    private static HashMap<String, HashSet<UUID>> getTagMapByCat(int type, int catId) {
        switch (type) {
            case EnumCategoryType.EFFECT: return CustomEffectController.getInstance().getItemTagMapForCategory(catId);
            case EnumCategoryType.LINKED_ITEM: return LinkedItemController.getInstance().getItemTagMapForCategory(catId);
            case EnumCategoryType.ABILITY: return AbilityController.Instance.getCustomAbilityTagMapForCategory(catId);
        }
        return null;
    }
}
