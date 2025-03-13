package noppes.npcs.client.gui.custom;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.customgui.CustomGuiButtonPacket;
import kamkeel.npcs.network.packets.player.customgui.CustomGuiClosePacket;
import kamkeel.npcs.network.packets.player.customgui.CustomGuiUnfocusedPacket;
import kamkeel.npcs.network.packets.player.customgui.CustomScrollClickPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.client.gui.custom.components.*;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.scripted.gui.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.*;

public class GuiCustom extends GuiScreen implements ICustomScrollListener, IGuiData {
    ScriptGui gui;
    int xSize;
    int ySize;
    public static int guiLeft;
    public static int guiTop;
    ResourceLocation background;
    public String[] hoverText;
    Map<Integer, IGuiComponent> components = new HashMap();
    List<IClickListener> clickListeners = new ArrayList();
    List<CustomGuiTextField> keyListeners = new ArrayList();
    List<IDataHolder> dataHolders = new ArrayList();

    public Container inventorySlots;
    private Slot theSlot;
    private Slot clickedSlot;
    private boolean isRightMouseClick;
    private ItemStack draggedStack;
    private int field_147011_y;
    private int field_147010_z;
    private Slot returningStackDestSlot;
    private long returningStackTime;
    private ItemStack returningStack;
    private Slot field_146985_D;
    private long field_146986_E;
    protected final Set field_147008_s = new HashSet();
    protected boolean field_147007_t;
    private int field_146987_F;
    private int field_146988_G;
    private boolean field_146995_H;
    private int field_146996_I;
    private long field_146997_J;
    private Slot field_146998_K;
    private int field_146992_L;
    private boolean field_146993_M;
    private ItemStack field_146994_N;

    public boolean closeOnEsc = true;

    public GuiCustom(ContainerCustomGui container) {
        this.inventorySlots = container;
        this.field_146995_H = true;
        this.allowUserInput = true;
    }

    public void initGui() {
        super.initGui();
        this.mc.thePlayer.openContainer = this.inventorySlots;
        if (this.gui != null) {
            guiLeft = (this.width - this.xSize) / 2;
            guiTop = (this.height - this.ySize) / 2;
            this.components.clear();
            this.clickListeners.clear();
            this.keyListeners.clear();
            this.dataHolders.clear();
            Iterator var1 = this.gui.getComponents().iterator();

            while (var1.hasNext()) {
                ICustomGuiComponent c = (ICustomGuiComponent) var1.next();
                this.addComponent(c);
            }
        }

    }

    public void updateScreen() {
        super.updateScreen();

        Iterator var1 = this.dataHolders.iterator();

        while (var1.hasNext()) {
            IDataHolder component = (IDataHolder) var1.next();
            if (component instanceof GuiTextField) {
                ((GuiTextField) component).updateCursorCounter();
            }
        }

        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
            this.mc.thePlayer.closeScreen();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoverText = null;
        //this.drawDefaultBackground();
        if (this.background != null) {
            this.drawBackgroundTexture();
        }

        Iterator var4 = this.components.values().iterator();

        while (var4.hasNext()) {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            IGuiComponent component = (IGuiComponent) var4.next();
            component.onRender(this.mc, mouseX, mouseY, Mouse.getDWheel(), partialTicks);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
        }

        if (this.hoverText != null) {
            this.func_146283_a(Arrays.asList(this.hoverText), mouseX, mouseY);
        }

        drawScreenSuper(mouseX, mouseY, partialTicks);
    }

    public void drawScreenSuper(int mouseX, int mouseY, float partialTicks) {
        //this.drawDefaultBackground();
        int k = guiLeft;
        int l = guiTop;
        //this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) k, (float) l, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        this.theSlot = null;
        short short1 = 240;
        short short2 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) short1, (float) short2);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k1;

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i1);
            this.drawSlot(slot);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.func_111238_b()) {
                this.theSlot = slot;
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                int j1 = slot.xDisplayPosition;
                k1 = slot.yDisplayPosition;
                GL11.glColorMask(true, true, true, false);
                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                GL11.glColorMask(true, true, true, true);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }

        //Forge: Force lighting to be disabled as there are some issue where lighting would
        //incorrectly be applied based on items that are in the inventory.
        GL11.glDisable(GL11.GL_LIGHTING);
        //this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GL11.glEnable(GL11.GL_LIGHTING);
        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = this.draggedStack == null ? inventoryplayer.getItemStack() : this.draggedStack;

        if (itemstack != null) {
            byte b0 = 8;
            k1 = this.draggedStack == null ? 8 : 16;
            String s = null;

            if (this.draggedStack != null && this.isRightMouseClick) {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_float_int((float) itemstack.stackSize / 2.0F);
            } else if (this.field_147007_t && this.field_147008_s.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.stackSize = this.field_146996_I;

                if (itemstack.stackSize == 0) {
                    s = EnumChatFormatting.YELLOW + "0";
                }
            }

            this.drawItemStack(itemstack, mouseX - k - b0, mouseY - l - k1, s);
        }

        if (this.returningStack != null) {
            float f1 = (float) (Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;

            if (f1 >= 1.0F) {
                f1 = 1.0F;
                this.returningStack = null;
            }

            k1 = this.returningStackDestSlot.xDisplayPosition - this.field_147011_y;
            int j2 = this.returningStackDestSlot.yDisplayPosition - this.field_147010_z;
            int l1 = this.field_147011_y + (int) ((float) k1 * f1);
            int i2 = this.field_147010_z + (int) ((float) j2 * f1);
            this.drawItemStack(this.returningStack, l1, i2, null);
        }

        GL11.glPopMatrix();

        if (inventoryplayer.getItemStack() == null && this.theSlot != null && this.theSlot.getHasStack()) {
            ItemStack itemstack1 = this.theSlot.getStack();
            this.renderToolTip(itemstack1, mouseX, mouseY);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
    }

    private void drawItemStack(ItemStack p_146982_1_, int p_146982_2_, int p_146982_3_, String p_146982_4_) {
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (p_146982_1_ != null) font = p_146982_1_.getItem().getFontRenderer(p_146982_1_);
        if (font == null) font = fontRendererObj;
        itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), p_146982_1_, p_146982_2_, p_146982_3_);
        itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), p_146982_1_, p_146982_2_, p_146982_3_ - (this.draggedStack == null ? 0 : 8), p_146982_4_);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    private boolean isMouseOverSlot(Slot slot, int posX, int posY) {
        return this.isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, posX, posY);
    }

    protected boolean isPointInRegion(int slotDisplayX, int slotDisplayY, int slotSizeX, int slotSizeY, int posX, int posY) {
        int k1 = guiLeft;
        int l1 = guiTop;
        posX -= k1;
        posY -= l1;
        return posX >= slotDisplayX - 1 && posX < slotDisplayX + slotSizeX + 1 && posY >= slotDisplayY - 1 && posY < slotDisplayY + slotSizeY + 1;
    }

    private void drawSlot(Slot p_146977_1_) {
        int i = p_146977_1_.xDisplayPosition;
        int j = p_146977_1_.yDisplayPosition;
        ItemStack itemstack = p_146977_1_.getStack();
        boolean flag = false;
        boolean flag1 = p_146977_1_ == this.clickedSlot && this.draggedStack != null && !this.isRightMouseClick;
        ItemStack itemstack1 = this.mc.thePlayer.inventory.getItemStack();
        String s = null;

        if (p_146977_1_ == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick && itemstack != null) {
            itemstack = itemstack.copy();
            itemstack.stackSize /= 2;
        } else if (this.field_147007_t && this.field_147008_s.contains(p_146977_1_) && itemstack1 != null) {
            if (this.field_147008_s.size() == 1) {
                return;
            }

            if (Container.func_94527_a(p_146977_1_, itemstack1, true) && this.inventorySlots.canDragIntoSlot(p_146977_1_)) {
                itemstack = itemstack1.copy();
                flag = true;
                Container.func_94525_a(this.field_147008_s, this.field_146987_F, itemstack, p_146977_1_.getStack() == null ? 0 : p_146977_1_.getStack().stackSize);

                if (itemstack.stackSize > itemstack.getMaxStackSize()) {
                    s = EnumChatFormatting.YELLOW + "" + itemstack.getMaxStackSize();
                    itemstack.stackSize = itemstack.getMaxStackSize();
                }

                if (itemstack.stackSize > p_146977_1_.getSlotStackLimit()) {
                    s = EnumChatFormatting.YELLOW + "" + p_146977_1_.getSlotStackLimit();
                    itemstack.stackSize = p_146977_1_.getSlotStackLimit();
                }
            } else {
                this.field_147008_s.remove(p_146977_1_);
                this.func_146980_g();
            }
        }

        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        if (itemstack == null) {
            IIcon iicon = p_146977_1_.getBackgroundIconIndex();

            if (iicon != null) {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND); // Forge: Blending needs to be enabled for this.
                this.mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
                this.drawTexturedModelRectFromIcon(i, j, iicon, 16, 16);
                GL11.glDisable(GL11.GL_BLEND); // Forge: And clean that up
                GL11.glEnable(GL11.GL_LIGHTING);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                drawRect(i, j, i + 16, j + 16, -2130706433);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i, j);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i, j, s);
        }

        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    private void func_146980_g() {
        ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

        if (itemstack != null && this.field_147007_t) {
            this.field_146996_I = itemstack.stackSize;
            ItemStack itemstack1;
            int i;

            for (Iterator iterator = this.field_147008_s.iterator(); iterator.hasNext(); this.field_146996_I -= itemstack1.stackSize - i) {
                Slot slot = (Slot) iterator.next();
                itemstack1 = itemstack.copy();
                i = slot.getStack() == null ? 0 : slot.getStack().stackSize;
                Container.func_94525_a(this.field_147008_s, this.field_146987_F, itemstack1, i);

                if (itemstack1.stackSize > itemstack1.getMaxStackSize()) {
                    itemstack1.stackSize = itemstack1.getMaxStackSize();
                }

                if (itemstack1.stackSize > slot.getSlotStackLimit()) {
                    itemstack1.stackSize = slot.getSlotStackLimit();
                }
            }
        }
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    }

    void drawBackgroundTexture() {
        this.mc.getTextureManager().bindTexture(this.background);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
    }

    private void addComponent(ICustomGuiComponent component) {
        ScriptGuiComponent c = (ScriptGuiComponent) component;
        switch (c.getType()) {
            case 0:
                CustomGuiButton button = CustomGuiButton.fromComponent((ScriptGuiButton) component);
                button.setParent(this);
                this.components.put(button.getID(), button);
                this.addClickListener(button);
                break;
            case 1:
                CustomGuiLabel lbl = CustomGuiLabel.fromComponent((ScriptGuiLabel) component);
                lbl.setParent(this);
                this.components.put(lbl.getID(), lbl);
                break;
            case 2:
                CustomGuiTexturedRect rect = CustomGuiTexturedRect.fromComponent((ScriptGuiTexturedRect) component);
                rect.setParent(this);
                this.components.put(rect.getID(), rect);
                break;
            case 3:
                CustomGuiTextField textField = CustomGuiTextField.fromComponent((ScriptGuiTextField) component);
                textField.setParent(this);
                this.components.put(textField.getID(), textField);
                this.addDataHolder(textField);
                this.addClickListener(textField);
                this.addKeyListener(textField);
                break;
            case 4:
                CustomGuiScrollComponent scroll = new CustomGuiScrollComponent(this.mc, this, component.getID(), ((ScriptGuiScroll) component).isMultiSelect());
                scroll.fromComponent((ScriptGuiScroll) component);
                scroll.setParent(this);
                this.components.put(scroll.getID(), scroll);
                this.addDataHolder(scroll);
                this.addClickListener(scroll);
                break;
            case 6:
                CustomGuiLine line = CustomGuiLine.fromComponent((ScriptGuiLine) component);
                this.components.put(line.getID(), line);
                break;
        }

    }

    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        PacketClient.sendClient(new CustomGuiButtonPacket(button.id, this.updateGui().toNBT()));
    }

    public void buttonClick(CustomGuiButton button) {
        PacketClient.sendClient(new CustomGuiButtonPacket(button.id, this.updateGui().toNBT()));
    }

    public String prevScrollClicked = null;

    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        PacketClient.sendClient(new CustomScrollClickPacket(this.updateGui().toNBT(), scroll.id, scroll.selected, this.getScrollSelection((CustomGuiScrollComponent) scroll), false));
        if (Integer.toString(scroll.selected).equals(prevScrollClicked)) {
            PacketClient.sendClient(new CustomScrollClickPacket(this.updateGui().toNBT(), scroll.id, scroll.selected, this.getScrollSelection((CustomGuiScrollComponent) scroll), true));
        }
        prevScrollClicked = Integer.toString(scroll.selected);
    }

    public void onGuiClosed() {
        if (this.gui != null) {
            PacketClient.sendClient(new CustomGuiClosePacket(this.updateGui().toNBT()));
        }

        if (this.mc.thePlayer != null) {
            this.inventorySlots.onContainerClosed(this.mc.thePlayer);
        }
    }

    public void onTextFieldUnfocused(CustomGuiTextField textField) {
        PacketClient.sendClient(new CustomGuiUnfocusedPacket(textField.getID(), this.updateGui().toNBT()));
    }

    public ScriptGui updateGui() {
        Iterator var1 = this.dataHolders.iterator();

        while (var1.hasNext()) {
            IDataHolder component = (IDataHolder) var1.next();
            this.gui.updateComponent(component.toComponent());
        }

        return this.gui;
    }

    public NBTTagCompound getScrollSelection(CustomGuiScrollComponent scroll) {
        NBTTagList list = new NBTTagList();
        if (scroll.multiSelect) {
            Iterator var3 = scroll.getSelectedList().iterator();

            while (var3.hasNext()) {
                String s = (String) var3.next();
                list.appendTag(new NBTTagString(s));
            }
        } else {
            list.appendTag(new NBTTagString(scroll.getSelected()));
        }

        NBTTagCompound selection = new NBTTagCompound();
        selection.setTag("selection", list);
        return selection;
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 && closeOnEsc) {
            this.mc.thePlayer.closeScreen();
        }

        this.checkHotbarKeys(keyCode);

        if (this.theSlot != null && this.theSlot.getHasStack()) {
            if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode()) {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, 3);
            } else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode()) {
                this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
            }
        }

        Iterator var3 = this.keyListeners.iterator();

        while (var3.hasNext()) {
            CustomGuiTextField listener = (CustomGuiTextField) var3.next();
            listener.keyTyped(typedChar, keyCode);
        }

        if (this.mc.gameSettings.keyBindInventory.getKeyCode() != keyCode && !this.mc.gameSettings.keyBindInventory.getIsKeyPressed()) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        boolean flag = mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100;
        Slot slot = this.getSlotAtPosition(mouseX, mouseY);
        long l = Minecraft.getSystemTime();
        this.field_146993_M = this.field_146998_K == slot && l - this.field_146997_J < 250L && this.field_146992_L == mouseButton;
        this.field_146995_H = false;

        if (mouseButton == 0 || mouseButton == 1 || flag) {
            boolean flag1 = true;
            int k1 = -1;

            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
                Slot invSlot = (Slot) this.inventorySlots.inventorySlots.get(i);

                if (invSlot.equals(slot)) {
                    flag1 = false;
                    break;
                }
            }

            if (slot != null) {
                k1 = slot.slotNumber;
            }

            if (flag1) {
                k1 = -999;
            }

            if (this.mc.gameSettings.touchscreen && flag1 && this.mc.thePlayer.inventory.getItemStack() == null) {
                this.mc.displayGuiScreen(null);
                return;
            }

            if (k1 != -1) {
                if (this.mc.gameSettings.touchscreen) {
                    if (slot != null && slot.getHasStack()) {
                        this.clickedSlot = slot;
                        this.draggedStack = null;
                        this.isRightMouseClick = mouseButton == 1;
                    } else {
                        this.clickedSlot = null;
                    }
                } else if (!this.field_147007_t) {
                    if (this.mc.thePlayer.inventory.getItemStack() == null) {
                        if (mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
                            this.handleMouseClick(slot, k1, mouseButton, 3);
                        } else {
                            boolean flag2 = k1 != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));
                            byte b0 = 0;

                            if (flag2) {
                                this.field_146994_N = slot != null && slot.getHasStack() ? slot.getStack() : null;
                                b0 = 1;
                            } else if (k1 == -999) {
                                b0 = 4;
                            }

                            this.handleMouseClick(slot, k1, mouseButton, b0);
                        }

                        this.field_146995_H = true;
                    } else {
                        this.field_147007_t = true;
                        this.field_146988_G = mouseButton;
                        this.field_147008_s.clear();

                        if (mouseButton == 0) {
                            this.field_146987_F = 0;
                        } else if (mouseButton == 1) {
                            this.field_146987_F = 1;
                        }
                    }
                }
            }
        }

        this.field_146998_K = slot;
        this.field_146997_J = l;
        this.field_146992_L = mouseButton;

        Iterator var4 = this.clickListeners.iterator();

        while (var4.hasNext()) {
            IClickListener listener = (IClickListener) var4.next();
            listener.mouseClicked(this, mouseX, mouseY, mouseButton);
        }

    }

    public boolean doesGuiPauseGame() {
        return this.gui == null || this.gui.doesPauseGame();
    }

    public void addDataHolder(IDataHolder component) {
        this.dataHolders.add(component);
    }

    public void addKeyListener(CustomGuiTextField component) {
        this.keyListeners.add(component);
    }

    public void addClickListener(IClickListener component) {
        this.clickListeners.add(component);
    }

    public void setGuiData(NBTTagCompound compound) {
        Minecraft mc = Minecraft.getMinecraft();
        ScriptGui gui = (ScriptGui) (new ScriptGui()).fromNBT(compound);
        ((ContainerCustomGui) this.inventorySlots).setGui(gui, mc.thePlayer);
        this.gui = gui;
        this.xSize = gui.getWidth();
        this.ySize = gui.getHeight();
        this.closeOnEsc = gui.doesCloseOnEscape();
        if (!gui.getBackgroundTexture().isEmpty()) {
            this.background = new ResourceLocation(gui.getBackgroundTexture());
        }

        this.initGui();
    }

    /**
     * This function is what controls the hotbar shortcut check when you press a number key when hovering a stack.
     */
    protected boolean checkHotbarKeys(int p_146983_1_) {
        if (this.mc.thePlayer.inventory.getItemStack() == null && this.theSlot != null) {
            for (int j = 0; j < 9; ++j) {
                if (p_146983_1_ == this.mc.gameSettings.keyBindsHotbar[j].getKeyCode()) {
                    this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, j, 2);
                    return true;
                }
            }
        }

        return false;
    }

    protected void handleMouseClick(Slot slot, int index, int p_146984_3_, int p_146984_4_) {
        if (slot != null) {
            index = slot.slotNumber;
        }

        this.mc.playerController.windowClick(this.inventorySlots.windowId, index, p_146984_3_, p_146984_4_, this.mc.thePlayer);
    }

    protected void mouseMovedOrUp(int p_146286_1_, int p_146286_2_, int p_146286_3_) {
        super.mouseMovedOrUp(p_146286_1_, p_146286_2_, p_146286_3_); //Forge, Call parent to release buttons
        Slot slot = this.getSlotAtPosition(p_146286_1_, p_146286_2_);
        int l = guiLeft;
        int i1 = guiTop;
        boolean flag = p_146286_1_ < l || p_146286_2_ < i1 || p_146286_1_ >= l + this.xSize || p_146286_2_ >= i1 + this.ySize;
        int j1 = -1;

        if (slot != null) {
            j1 = slot.slotNumber;
        }

        if (flag) {
            j1 = -999;
        }

        Slot slot1;
        Iterator iterator;

        if (this.field_146993_M && slot != null && p_146286_3_ == 0 && this.inventorySlots.func_94530_a(null, slot)) {
            if (isShiftKeyDown()) {
                if (slot != null && slot.inventory != null && this.field_146994_N != null) {
                    iterator = this.inventorySlots.inventorySlots.iterator();

                    while (iterator.hasNext()) {
                        slot1 = (Slot) iterator.next();

                        if (slot1 != null && slot1.canTakeStack(this.mc.thePlayer) && slot1.getHasStack() && slot1.inventory == slot.inventory && Container.func_94527_a(slot1, this.field_146994_N, true)) {
                            this.handleMouseClick(slot1, slot1.slotNumber, p_146286_3_, 1);
                        }
                    }
                }
            } else {
                this.handleMouseClick(slot, j1, p_146286_3_, 6);
            }

            this.field_146993_M = false;
            this.field_146997_J = 0L;
        } else {
            if (this.field_147007_t && this.field_146988_G != p_146286_3_) {
                this.field_147007_t = false;
                this.field_147008_s.clear();
                this.field_146995_H = true;
                return;
            }

            if (this.field_146995_H) {
                this.field_146995_H = false;
                return;
            }

            boolean flag1;

            if (this.clickedSlot != null && this.mc.gameSettings.touchscreen) {
                if (p_146286_3_ == 0 || p_146286_3_ == 1) {
                    if (this.draggedStack == null && slot != this.clickedSlot) {
                        this.draggedStack = this.clickedSlot.getStack();
                    }

                    flag1 = Container.func_94527_a(slot, this.draggedStack, false);

                    if (j1 != -1 && this.draggedStack != null && flag1) {
                        this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, p_146286_3_, 0);
                        this.handleMouseClick(slot, j1, 0, 0);

                        if (this.mc.thePlayer.inventory.getItemStack() != null) {
                            this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, p_146286_3_, 0);
                            this.field_147011_y = p_146286_1_ - l;
                            this.field_147010_z = p_146286_2_ - i1;
                            this.returningStackDestSlot = this.clickedSlot;
                            this.returningStack = this.draggedStack;
                            this.returningStackTime = Minecraft.getSystemTime();
                        } else {
                            this.returningStack = null;
                        }
                    } else if (this.draggedStack != null) {
                        this.field_147011_y = p_146286_1_ - l;
                        this.field_147010_z = p_146286_2_ - i1;
                        this.returningStackDestSlot = this.clickedSlot;
                        this.returningStack = this.draggedStack;
                        this.returningStackTime = Minecraft.getSystemTime();
                    }

                    this.draggedStack = null;
                    this.clickedSlot = null;
                }
            } else if (this.field_147007_t && !this.field_147008_s.isEmpty()) {
                this.handleMouseClick(null, -999, Container.func_94534_d(0, this.field_146987_F), 5);
                iterator = this.field_147008_s.iterator();

                while (iterator.hasNext()) {
                    slot1 = (Slot) iterator.next();
                    this.handleMouseClick(slot1, slot1.slotNumber, Container.func_94534_d(1, this.field_146987_F), 5);
                }

                this.handleMouseClick(null, -999, Container.func_94534_d(2, this.field_146987_F), 5);
            } else if (this.mc.thePlayer.inventory.getItemStack() != null) {
                if (p_146286_3_ == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
                    this.handleMouseClick(slot, j1, p_146286_3_, 3);
                } else {
                    flag1 = j1 != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54));

                    if (flag1) {
                        this.field_146994_N = slot != null && slot.getHasStack() ? slot.getStack() : null;
                    }

                    this.handleMouseClick(slot, j1, p_146286_3_, flag1 ? 1 : 0);
                }
            }
        }

        if (this.mc.thePlayer.inventory.getItemStack() == null) {
            this.field_146997_J = 0L;
        }

        this.field_147007_t = false;
    }

    private Slot getSlotAtPosition(int posX, int posY) {
        for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(k);

            if (this.isMouseOverSlot(slot, posX, posY)) {
                return slot;
            }
        }

        return null;
    }

    protected void mouseClickMove(int p_146273_1_, int p_146273_2_, int p_146273_3_, long p_146273_4_) {
        Slot slot = this.getSlotAtPosition(p_146273_1_, p_146273_2_);
        ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();

        if (this.clickedSlot != null && this.mc.gameSettings.touchscreen) {
            if (p_146273_3_ == 0 || p_146273_3_ == 1) {
                if (this.draggedStack == null) {
                    if (slot != this.clickedSlot) {
                        this.draggedStack = this.clickedSlot.getStack().copy();
                    }
                } else if (this.draggedStack.stackSize > 1 && slot != null && Container.func_94527_a(slot, this.draggedStack, false)) {
                    long i1 = Minecraft.getSystemTime();

                    if (this.field_146985_D == slot) {
                        if (i1 - this.field_146986_E > 500L) {
                            this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, 0);
                            this.handleMouseClick(slot, slot.slotNumber, 1, 0);
                            this.handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, 0);
                            this.field_146986_E = i1 + 750L;
                            --this.draggedStack.stackSize;
                        }
                    } else {
                        this.field_146985_D = slot;
                        this.field_146986_E = i1;
                    }
                }
            }
        } else if (this.field_147007_t && slot != null && itemstack != null && itemstack.stackSize > this.field_147008_s.size() && Container.func_94527_a(slot, itemstack, true) && slot.isItemValid(itemstack) && this.inventorySlots.canDragIntoSlot(slot)) {
            this.field_147008_s.add(slot);
            this.func_146980_g();
        }
    }
}
