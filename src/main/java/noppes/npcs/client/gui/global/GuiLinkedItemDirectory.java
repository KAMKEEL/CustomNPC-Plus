package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.category.CategoryItemsRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryListRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryMoveItemPacket;
import kamkeel.npcs.network.packets.request.category.CategoryRemovePacket;
import kamkeel.npcs.network.packets.request.category.CategorySavePacket;
import kamkeel.npcs.network.packets.request.linked.LinkedGetPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemBuildPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemRemovePacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemSavePacket;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.item.SubGuiLinkedItem;
import noppes.npcs.client.gui.util.GuiDirectoryCategorized;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.controllers.data.Category;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

public class GuiLinkedItemDirectory extends GuiDirectoryCategorized {
    public LinkedItem linkedItem = null;
    public EntityNPCInterface npc;

    public GuiLinkedItemDirectory(EntityNPCInterface npc) {
        super();
        this.npc = npc;
    }

    @Override
    protected String getTitle() { return "Linked Items"; }

    @Override
    protected void requestCategoryList() {
        PacketClient.sendClient(new CategoryListRequestPacket(EnumCategoryType.LINKED_ITEM));
    }

    @Override
    protected void requestItemsInCategory(int catId) {
        PacketClient.sendClient(new CategoryItemsRequestPacket(EnumCategoryType.LINKED_ITEM, catId));
    }

    @Override
    protected void requestItemData(int itemId) {
        LinkedGetPacket.GetItem(itemId);
    }

    @Override
    protected void onSaveCategory(Category cat) {
        PacketClient.sendClient(new CategorySavePacket(EnumCategoryType.LINKED_ITEM, cat.writeNBT(new NBTTagCompound())));
    }

    @Override
    protected void onRemoveCategory(int catId) {
        PacketClient.sendClient(new CategoryRemovePacket(EnumCategoryType.LINKED_ITEM, catId));
    }

    @Override
    protected void onAddItem(int catId) {
        String name = "New";
        while (itemData.containsKey(name)) name += "_";
        LinkedItem newItem = new LinkedItem(name);
        PacketClient.sendClient(new LinkedItemSavePacket(newItem.writeToNBT(false), ""));
    }

    @Override
    protected void onRemoveItem(int itemId) {
        PacketClient.sendClient(new LinkedItemRemovePacket(itemId));
        linkedItem = null;
    }

    @Override
    protected void onEditItem() {
        if (linkedItem != null && linkedItem.id >= 0) {
            setSubGui(new SubGuiLinkedItem(this, linkedItem));
        }
    }

    @Override
    protected void onCloneItem() {
        if (linkedItem != null && linkedItem.id >= 0) {
            LinkedItem clone = linkedItem.clone();
            while (itemData.containsKey(clone.name)) clone.name += "_";
            clone.id = -1;
            PacketClient.sendClient(new LinkedItemSavePacket(clone.writeToNBT(false), ""));
        }
    }

    @Override
    protected void onItemReceived(NBTTagCompound compound) {
        linkedItem = new LinkedItem();
        linkedItem.readFromNBT(compound);
        setPrevItemName(linkedItem.name);
    }

    @Override
    protected boolean hasSelectedItem() {
        return linkedItem != null && linkedItem.id >= 0;
    }

    @Override
    protected int getSelectedItemId() {
        return linkedItem != null ? linkedItem.id : -1;
    }

    @Override
    protected void sendMovePacket(int itemId, int destCatId) {
        PacketClient.sendClient(new CategoryMoveItemPacket(EnumCategoryType.LINKED_ITEM, itemId, destCatId));
    }

    @Override
    protected GuiScreen getWindowedVariant() {
        return new GuiNPCManageLinked(npc);
    }

    @Override
    protected void saveCurrentItem() {
        if (linkedItem != null && linkedItem.id >= 0 && prevItemName != null && !prevItemName.isEmpty()) {
            PacketClient.sendClient(new LinkedItemSavePacket(linkedItem.writeToNBT(false), prevItemName));
            prevItemName = linkedItem.name;
        }
    }

    @Override
    protected void onSubGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiLinkedItem) {
            if (linkedItem != null && linkedItem.id >= 0) {
                setPrevItemName(linkedItem.name);
                if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
            }
        }
    }

    @Override
    protected void initRightPanel(int startY) {
        // Reserve space for: edit+build row, copy+remove row, ID label
        int bottomH = (btnH + gap) * 2 + 14;
        previewX = rightX;
        previewY = contentY;
        previewW = rightPanelW;
        previewH = contentH - bottomH - gap;

        int halfW = (rightPanelW - gap) / 2;

        // Edit + Build on first row
        int row1Y = contentY + contentH - btnH * 2 - gap;
        GuiNpcButton editBtn = new GuiNpcButton(51, rightX, row1Y, halfW, btnH, "gui.edit");
        editBtn.enabled = hasSelectedItem() && movePhase == 0;
        addButton(editBtn);

        GuiNpcButton buildBtn = new GuiNpcButton(56, rightX + halfW + gap, row1Y, halfW, btnH, "gui.build");
        buildBtn.enabled = hasSelectedItem() && movePhase == 0;
        addButton(buildBtn);

        // Copy + Remove on second row
        int row2Y = row1Y + btnH + gap;
        GuiNpcButton cloneBtn = new GuiNpcButton(52, rightX, row2Y, halfW, btnH, "gui.copy");
        cloneBtn.enabled = hasSelectedItem() && movePhase == 0;
        addButton(cloneBtn);

        GuiNpcButton removeBtn = new GuiNpcButton(53, rightX + halfW + gap, row2Y, halfW, btnH, "gui.remove");
        removeBtn.enabled = hasSelectedItem() && movePhase == 0;
        removeBtn.setTextColor(0xFF5555);
        addButton(removeBtn);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 56 && linkedItem != null && linkedItem.id >= 0) {
            PacketClient.sendClient(new LinkedItemBuildPacket(linkedItem.getId()));
            return;
        }
        super.actionPerformed(guibutton);
    }

    @Override
    protected void drawItemPreview(int centerX, int centerY, int mouseX, int mouseY, float partialTicks) {
        if (linkedItem == null || linkedItem.id < 0) return;

        int iconRenderSize = Math.min(previewW, previewH) / 3;
        iconRenderSize = Math.max(32, Math.min(iconRenderSize, 128));
        int x = centerX - iconRenderSize / 2;
        int y = centerY - iconRenderSize;

        TextureManager textureManager = mc.getTextureManager();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        ImageData imageData = ClientCacheHandler.getImageData(linkedItem.display.texture);
        if (imageData.imageLoaded()) {
            float[] colors = ColorUtil.hexToRGB(linkedItem.display.itemColor);
            GL11.glColor3f(colors[0], colors[1], colors[2]);
            imageData.bindTexture();
            int iconWidth = imageData.getTotalWidth();
            int iconHeight = imageData.getTotalHeight();
            func_152125_a(x, y, 0, 0, iconWidth, iconHeight, iconRenderSize, iconRenderSize, iconWidth, iconHeight);
        } else {
            textureManager.bindTexture(new ResourceLocation("customnpcs", "textures/marks/question.png"));
            func_152125_a(x, y, 0, 0, 1, 1, iconRenderSize, iconRenderSize, 1, 1);
        }
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void drawItemDetails(int x, int y, int w) {
        if (linkedItem == null || linkedItem.id < 0) return;

        fontRendererObj.drawString(linkedItem.name, x, y, CustomNpcResourceListener.DefaultTextColor, true);
        y += 14;

        int labelColor = 0xffae0d;
        fontRendererObj.drawString(StatCollector.translateToLocal("display.version") + ": " + linkedItem.version, x, y, labelColor, false);
        y += 12;

        labelColor = 0xff5714;
        fontRendererObj.drawString(StatCollector.translateToLocal("display.maxStack") + ": " + linkedItem.stackSize, x, y, labelColor, false);
        y += 12;

        labelColor = 0xf7ca28;
        fontRendererObj.drawString(StatCollector.translateToLocal("display.digSpeed") + ": " + linkedItem.digSpeed, x, y, labelColor, false);
        y += 12;

        labelColor = 0x29d6b9;
        String[] useActions = {
            StatCollector.translateToLocal("use_action.none"),
            StatCollector.translateToLocal("use_action.block"),
            StatCollector.translateToLocal("use_action.eat"),
            StatCollector.translateToLocal("use_action.drink"),
            StatCollector.translateToLocal("use_action.bow")
        };
        int useActionIndex;
        switch (linkedItem.itemUseAction) {
            case 0: useActionIndex = 0; break;
            case 1: useActionIndex = 1; break;
            case 2: useActionIndex = 4; break;
            case 3: useActionIndex = 2; break;
            case 4: useActionIndex = 3; break;
            default: useActionIndex = 0; break;
        }
        fontRendererObj.drawString(StatCollector.translateToLocal("display.useAction") + ": " + useActions[useActionIndex], x, y, labelColor, false);
        y += 12;

        String[] armorOptions = {
            StatCollector.translateToLocal("armor_type.none"),
            StatCollector.translateToLocal("armor_type.all"),
            StatCollector.translateToLocal("armor_type.head"),
            StatCollector.translateToLocal("armor_type.chestplate"),
            StatCollector.translateToLocal("armor_type.leggings"),
            StatCollector.translateToLocal("armor_type.boots")
        };
        int armorIndex;
        if (linkedItem.armorType == -2) armorIndex = 0;
        else if (linkedItem.armorType == -1) armorIndex = 1;
        else armorIndex = linkedItem.armorType + 2;
        fontRendererObj.drawString(StatCollector.translateToLocal("display.armor") + ": " + armorOptions[armorIndex], x, y, labelColor, false);
        y += 12;

        labelColor = 0x7cff54;
        fontRendererObj.drawString(StatCollector.translateToLocal("display.isTool") + ": " + ("" + linkedItem.isTool).toUpperCase(), x, y, labelColor, false);
        y += 12;

        fontRendererObj.drawString(StatCollector.translateToLocal("display.isNormalItem") + ": " + ("" + linkedItem.isNormalItem).toUpperCase(), x, y, labelColor, false);
        y += 12;

        labelColor = 0xce75fa;
        fontRendererObj.drawString(StatCollector.translateToLocal("model.scale") + ": " + linkedItem.display.scaleX + ", " + linkedItem.display.scaleY + ", " + linkedItem.display.scaleZ, x, y, labelColor, false);
        y += 12;

        fontRendererObj.drawString(StatCollector.translateToLocal("model.rotate") + ": " + linkedItem.display.rotationX + ", " + linkedItem.display.rotationY + ", " + linkedItem.display.rotationZ, x, y, labelColor, false);
        y += 12;

        fontRendererObj.drawString(StatCollector.translateToLocal("model.translate") + ": " + linkedItem.display.translateX + ", " + linkedItem.display.translateY + ", " + linkedItem.display.translateZ, x, y, labelColor, false);
    }

}
