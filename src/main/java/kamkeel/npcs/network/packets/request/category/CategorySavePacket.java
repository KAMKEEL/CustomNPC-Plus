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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import kamkeel.npcs.controllers.AbilityController;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.CategoryManager;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.Category;
import noppes.npcs.wrapper.nbt.MC1710NBTCompound;

import java.io.IOException;

public final class CategorySavePacket extends AbstractPacket {
    public static String packetName = "NPC|CatSave";

    private int catType;
    private NBTTagCompound categoryNBT;

    public CategorySavePacket(int catType, NBTTagCompound categoryNBT) {
        this.catType = catType;
        this.categoryNBT = categoryNBT;
    }

    public CategorySavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CategorySave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(catType);
        ByteBufUtils.writeNBT(out, categoryNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int type = in.readInt();
        NBTTagCompound compound = ByteBufUtils.readNBT(in);

        CategoryManager cm = getManager(type);
        if (cm == null)
            return;

        Category cat = new Category();
        cat.readNBT(new MC1710NBTCompound(compound));

        if (cat.id <= 0) {
            cm.createCategory(cat.title);
        } else {
            cm.saveCategory(cat);
        }

        saveController(type);
        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, cm.getCategoryScrollData(), EnumScrollData.CATEGORY_LIST);
    }

    public static CategoryManager getManager(int type) {
        switch (type) {
            case EnumCategoryType.EFFECT: return CustomEffectController.getInstance().categoryManager;
            case EnumCategoryType.ANIMATION: return AnimationController.getInstance().categoryManager;
            case EnumCategoryType.LINKED_ITEM: return LinkedItemController.getInstance().categoryManager;
            case EnumCategoryType.ABILITY: return AbilityController.Instance.customAbilityCategories;
            case EnumCategoryType.CHAINED_ABILITY: return AbilityController.Instance.chainedAbilityCategories;
        }
        return null;
    }

    public static void saveController(int type) {
        switch (type) {
            case EnumCategoryType.EFFECT: CustomEffectController.getInstance().saveEffectLoadMap(); break;
            case EnumCategoryType.ANIMATION: AnimationController.getInstance().saveAnimationMap(); break;
            case EnumCategoryType.LINKED_ITEM: LinkedItemController.getInstance().saveLinkedItemsMap(); break;
            case EnumCategoryType.ABILITY: break; // abilities save per-file, no bulk save needed
            case EnumCategoryType.CHAINED_ABILITY: break;
        }
    }
}
