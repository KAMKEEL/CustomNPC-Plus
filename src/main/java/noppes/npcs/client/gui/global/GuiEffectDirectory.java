package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.category.CategoryItemsRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryListRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryMoveItemPacket;
import kamkeel.npcs.network.packets.request.category.CategoryRemovePacket;
import kamkeel.npcs.network.packets.request.category.CategorySavePacket;
import kamkeel.npcs.network.packets.request.effects.EffectGetPacket;
import kamkeel.npcs.network.packets.request.effects.EffectRemovePacket;
import kamkeel.npcs.network.packets.request.effects.EffectSavePacket;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.SubGuiEffectGeneral;
import noppes.npcs.client.gui.util.GuiDirectoryCategorized;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.Category;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class GuiEffectDirectory extends GuiDirectoryCategorized {
    public CustomEffect effect = new CustomEffect();
    public EntityNPCInterface npc;

    public GuiEffectDirectory(EntityNPCInterface npc) {
        super();
        this.npc = npc;
    }

    @Override
    protected String getTitle() { return "Effects"; }

    @Override
    protected void requestCategoryList() {
        PacketClient.sendClient(new CategoryListRequestPacket(EnumCategoryType.EFFECT));
    }

    @Override
    protected void requestItemsInCategory(int catId) {
        PacketClient.sendClient(new CategoryItemsRequestPacket(EnumCategoryType.EFFECT, catId));
    }

    @Override
    protected void requestItemData(int itemId) {
        PacketClient.sendClient(new EffectGetPacket(itemId));
    }

    @Override
    protected void onSaveCategory(Category cat) {
        PacketClient.sendClient(new CategorySavePacket(EnumCategoryType.EFFECT, cat.writeNBT(new NBTTagCompound())));
    }

    @Override
    protected void onRemoveCategory(int catId) {
        PacketClient.sendClient(new CategoryRemovePacket(EnumCategoryType.EFFECT, catId));
    }

    @Override
    protected void onAddItem(int catId) {
        String name = "New";
        while (itemData.containsKey(name)) name += "_";
        CustomEffect newEffect = new CustomEffect(-1, name);
        PacketClient.sendClient(new EffectSavePacket(newEffect.writeToNBT(false), ""));
    }

    @Override
    protected void onRemoveItem(int itemId) {
        PacketClient.sendClient(new EffectRemovePacket(itemId));
        effect = new CustomEffect();
    }

    @Override
    protected void onEditItem() {
        if (effect != null && effect.id >= 0) {
            setSubGui(new SubGuiEffectGeneral(this, effect));
        }
    }

    @Override
    protected void onCloneItem() {
        if (effect != null && effect.id >= 0) {
            CustomEffect clone = effect.cloneEffect();
            while (itemData.containsKey(clone.name)) clone.name += "_";
            PacketClient.sendClient(new EffectSavePacket(clone.writeToNBT(false), ""));
        }
    }

    @Override
    protected void onItemReceived(NBTTagCompound compound) {
        effect = new CustomEffect();
        effect.readFromNBT(compound);
        setPrevItemName(effect.name);
        if (effect.id != -1) {
            CustomEffectController.getInstance().getCustomEffects().replace(effect.id, effect);
        }
    }

    @Override
    protected boolean hasSelectedItem() {
        return effect != null && effect.id >= 0;
    }

    @Override
    protected int getSelectedItemId() {
        return effect != null ? effect.id : -1;
    }

    @Override
    protected void sendMovePacket(int itemId, int destCatId) {
        PacketClient.sendClient(new CategoryMoveItemPacket(EnumCategoryType.EFFECT, itemId, destCatId));
    }

    @Override
    protected GuiScreen getWindowedVariant() {
        return new GuiNPCManageEffects(npc);
    }

    @Override
    protected void saveCurrentItem() {
        if (effect != null && effect.id >= 0 && prevItemName != null && !prevItemName.isEmpty()) {
            PacketClient.sendClient(new EffectSavePacket(effect.writeToNBT(false), prevItemName));
            prevItemName = effect.name;
        }
    }

    @Override
    protected void onSubGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiEffectGeneral) {
            if (effect != null && effect.id >= 0) {
                setPrevItemName(effect.name);
                if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
            }
        }
    }

    @Override
    protected void drawItemPreview(int centerX, int centerY, int mouseX, int mouseY, float partialTicks) {
        if (effect == null || effect.id == -1) return;

        int iconRenderSize = Math.min(previewW, previewH) / 3;
        iconRenderSize = Math.max(32, Math.min(iconRenderSize, 128));
        int x = centerX - iconRenderSize / 2;
        int y = centerY - iconRenderSize;

        TextureManager textureManager = mc.getTextureManager();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        ImageData data = ClientCacheHandler.getImageData(effect.icon);
        if (data.imageLoaded()) {
            data.bindTexture();
            int iconX = effect.iconX;
            int iconY = effect.iconY;
            int iconWidth = effect.getWidth();
            int iconHeight = effect.getHeight();
            int width = data.getTotalWidth();
            int height = data.getTotalHeight();
            func_152125_a(x, y, iconX, iconY, iconWidth, iconHeight, iconRenderSize, iconRenderSize, width, height);
        } else {
            textureManager.bindTexture(new ResourceLocation("customnpcs", "textures/marks/question.png"));
            func_152125_a(x, y, 0, 0, 1, 1, iconRenderSize, iconRenderSize, 1, 1);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        textureManager.bindTexture(specialIcons);
        func_152125_a(x, y, 0, 224, 16, 16, iconRenderSize, iconRenderSize, 256, 256);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    protected void drawItemDetails(int x, int y, int w) {
        if (effect == null || effect.id == -1) return;

        String drawString = effect.getMenuName();
        fontRendererObj.drawString(drawString, x, y, CustomNpcResourceListener.DefaultTextColor, true);

        y += 14;
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.name") + ": " + effect.name, x, y, 0xFFFFFF, false);
        y += 12;
        fontRendererObj.drawString(StatCollector.translateToLocal("effect.runsEveryX") + ": " + effect.everyXTick + "t", x, y, 0xB5B5B5, false);
        y += 12;
        fontRendererObj.drawString(StatCollector.translateToLocal("effect.defaultLength") + ": " + effect.length + "s", x, y, 0xB5B5B5, false);
    }

}
