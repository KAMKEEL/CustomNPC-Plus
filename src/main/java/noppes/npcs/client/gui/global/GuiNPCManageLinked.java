package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.linked.LinkedGetAllPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedGetPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemBuildPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemRemovePacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemSavePacket;
import kamkeel.npcs.network.packets.request.linked.LinkedNPCAddPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedNPCRemovePacket;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.item.SubGuiLinkedItem;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageLinked extends GuiNPCInterface2 implements IScrollData, ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback {
    private static int tab = 0;
    private boolean loadedNPC = false;
    private GuiCustomScroll scroll;

    public HashMap<String, Integer> data = new HashMap<>();
    private String selected = null;

    private LinkedItem linkedItem = null;
    public String originalName = "";

    private String search = "";

    private float zoomed = 36, rotation;

    public GuiNPCManageLinked(EntityNPCInterface npc) {
        super(npc);
        resetNPC();
        if (tab == 0)
            LinkedGetAllPacket.GetNPCs();
        else if (tab == 1)
            LinkedGetAllPacket.GetItems();
    }

    public void resetNPC() {
        this.npc = new EntityCustomNpc(Minecraft.getMinecraft().theWorld);
        this.npc.display.name = "Linked NPC";
        this.npc.height = 1.62f;
        this.npc.width = 0.43f;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 8;

        this.addButton(new GuiNpcButton(10, guiLeft + 368, y, 45, 20, "gui.npcs"));
        this.addButton(new GuiNpcButton(11, guiLeft + 368, y += 22, 45, 20, "gui.items"));
        getButton(10).enabled = tab == 1;
        getButton(11).enabled = tab == 0;

        this.addButton(new GuiNpcButton(1, guiLeft + 368, y += 40, 45, 20, "gui.add"));
        this.addButton(new GuiNpcButton(2, guiLeft + 368, y += 22, 45, 20, "gui.remove"));

        this.addButton(new GuiNpcButton(3, guiLeft + 368, y += 22, 45, 20, "gui.edit"));
        this.addButton(new GuiNpcButton(4, guiLeft + 368, y += 22, 45, 20, "gui.copy"));
        this.addButton(new GuiNpcButton(5, guiLeft + 368, y += 22, 45, 20, "gui.build"));
        getButton(3).enabled = tab == 1;
        getButton(4).enabled = tab == 1;
        getButton(5).enabled = tab == 1;

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0, 0);
            scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 4;
        scroll.setList(getSearchList());
        this.addScroll(scroll);
        this.addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scroll.setList(getSearchList());
                scroll.resetScroll();
            }
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        if (hasSubGui())
            return;

        if (tab == 0) {
            // (Existing NPC rendering code...)
            if (isMouseOverRenderer(i, j)) {
                zoomed += Mouse.getDWheel() * 0.035f;
                if (zoomed > 100)
                    zoomed = 100;
                if (zoomed < 10)
                    zoomed = 10;
                if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
                    rotation -= Mouse.getDX() * 0.75f;
                }
            }
            GL11.glColor4f(1, 1, 1, 1);
            EntityLivingBase entity = this.npc;
            int l = guiLeft + 150;
            int i1 = guiTop + 198;
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glPushMatrix();
            GL11.glTranslatef(l, i1, 60F);
            GL11.glScalef(-zoomed, zoomed, zoomed);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f2 = entity.renderYawOffset;
            float f3 = entity.rotationYaw;
            float f4 = entity.rotationPitch;
            float f7 = entity.rotationYawHead;
            float f5 = (float) (l) - i;
            float f6 = (float) (i1 - 50) - j;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f6 / 800F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.prevRenderYawOffset = entity.renderYawOffset = rotation;
            entity.prevRotationYaw = entity.rotationYaw = (float) Math.atan(f5 / 80F) * 40F + rotation;
            entity.rotationPitch = -(float) Math.atan(f6 / 80F) * 20F;
            entity.prevRotationYawHead = entity.rotationYawHead = entity.rotationYaw;
            GL11.glTranslatef(0.0F, entity.yOffset, 1F);
            RenderManager.instance.playerViewY = 180F;
            GL11.glPushMatrix();
            try {
                RenderManager.instance.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F);
            } catch (Exception ignored) {
            }
            GL11.glPopMatrix();
            entity.prevRenderYawOffset = entity.renderYawOffset = f2;
            entity.prevRotationYaw = entity.rotationYaw = f3;
            entity.rotationPitch = f4;
            entity.prevRotationYawHead = entity.rotationYawHead = f7;
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glPopMatrix();
        } else if (tab == 1) {
            // Render the linked item image in the top-left area
            if (linkedItem != null) {
                int x = guiLeft + 155;
                int y = guiTop + 30;
                int iconRenderSize = 64;
                TextureManager textureManager = mc.getTextureManager();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                ImageData imageData = ClientCacheHandler.getImageData(linkedItem.display.texture);
                if (imageData.imageLoaded()) {
                    float[] colors = ColorUtil.hexToRGB(this.linkedItem.display.itemColor);
                    GL11.glColor3f(colors[0], colors[1], colors[2]);
                    imageData.bindTexture();
                    int iconX = 0, iconY = 0;
                    int iconWidth = imageData.getTotalWidth();
                    int iconHeight = imageData.getTotalHeight();
                    int width = imageData.getTotalWidth();
                    int height = imageData.getTotalHeight();
                    func_152125_a(x, y, iconX, iconY, iconWidth, iconHeight, iconRenderSize, iconRenderSize, width, height);
                } else {
                    textureManager.bindTexture(new ResourceLocation("customnpcs", "textures/marks/question.png"));
                    func_152125_a(x, y, 0, 0, 1, 1, iconRenderSize, iconRenderSize, 1, 1);
                }
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<String>(this.data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.data.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void buttonEvent(GuiButton button) {
        if (button.id == 1) {
            if (tab == 0) {
                setSubGui(new SubGuiEditText("New"));
            } else {
                String name = "New";
                while (data.containsKey(name))
                    name += "_";
                LinkedItem linkedItem = new LinkedItem(name);
                PacketClient.sendClient(new LinkedItemSavePacket(linkedItem.writeToNBT(false), ""));
            }
        }
        if (button.id == 2) {
            if (tab == 0) {
                if (data.containsKey(scroll.getSelected())) {
                    GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 0);
                    displayGuiScreen(guiyesno);
                }
            } else {
                if (data.containsKey(scroll.getSelected())) {
                    GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 1);
                    displayGuiScreen(guiyesno);
                }
            }
        }
        if (button.id == 10) {
            tab = 0;
            resetNPC();
            LinkedGetAllPacket.GetNPCs();
            scroll.setSelected("");
        }
        if (button.id == 11) {
            tab = 1;
            LinkedGetAllPacket.GetItems();
            scroll.setSelected("");
        }

        if (linkedItem == null)
            return;

        if (button.id == 3) {
            setSubGui(new SubGuiLinkedItem(this, this.linkedItem));
        }
        if (button.id == 4) {
            if (data.containsKey(scroll.getSelected()) && linkedItem != null && linkedItem.id >= 0) {
                String name = linkedItem.name;
                while (data.containsKey(name))
                    name += "_";
                LinkedItem linkedItemClone = this.linkedItem.clone();
                linkedItemClone.name = name;
                linkedItemClone.id = -1;
                PacketClient.sendClient(new LinkedItemSavePacket(linkedItemClone.writeToNBT(false), ""));
            }
        }
        if (button.id == 5) {
            // Build Linked Item
            if (tab == 1)
                PacketClient.sendClient(new LinkedItemBuildPacket(this.linkedItem.getId()));

        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 0) {
            if (data.containsKey(scroll.getSelected())) {
                PacketClient.sendClient(new LinkedNPCRemovePacket(scroll.getSelected()));
                initGui();
            }
        }
        if (id == 1) {
            if (data.containsKey(scroll.getSelected())) {
                PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));
                initGui();
            }
        }
    }

    @Override
    public void drawBackground() {
        super.drawBackground();
        renderScreen();
    }


    private void renderScreen() {
        // Draw the common background bars
        drawGradientRect(guiLeft + 5, guiTop + 4, guiLeft + 218, guiTop + 24, 0xC0101010, 0xC0101010);
        drawHorizontalLine(guiLeft + 5, guiLeft + 218, guiTop + 25, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        drawGradientRect(guiLeft + 5, guiTop + 27, guiLeft + 218, guiTop + ySize + 9, 0xA0101010, 0xA0101010);

        if (tab == 0 && loadedNPC && this.npc != null) {
            // Top bar: display NPC display name (centered)
            String topBarText = npc.display.getName();
            int textWidth = getStringWidthWithoutColor(topBarText);
            int centerX = guiLeft + 5 + ((218 - 10 - textWidth) / 2);
            fontRendererObj.drawString(topBarText, centerX, guiTop + 10, npc.getFaction().color, true);

            // Lower section: display NPC properties as label and value pairs
            int y = guiTop + 30;
            int xLabel = guiLeft + 8;
            int xValue = guiLeft + 120;
            int valueColor = 0xFFFFFF;
            String label, value;

            // Health
            label = StatCollector.translateToLocal("stats.health") + ": ";
            value = "" + npc.stats.maxHealth;
            fontRendererObj.drawString(label, xLabel, y, 0x29d6b9, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Damage (using getAttackStrength)
            label = StatCollector.translateToLocal("stats.meleestrength") + ": ";
            value = "" + npc.stats.getAttackStrength();
            fontRendererObj.drawString(label, xLabel, y, 0xff5714, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Attack Speed
            label = StatCollector.translateToLocal("stats.meleespeed") + ": ";
            value = "" + npc.stats.attackSpeed;
            fontRendererObj.drawString(label, xLabel, y, 0xf7ca28, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // AI Type (npc.ai.onAttack: 0 = fight, 1 = panic, 2 = retreat, 3 = nothing)
            label = StatCollector.translateToLocal("menu.ai") + ": ";
            int onAttack = npc.ais.onAttack;
            switch (onAttack) {
                case 0:
                    value = StatCollector.translateToLocal("gui.retaliate");
                    break;
                case 1:
                    value = StatCollector.translateToLocal("gui.panic");
                    break;
                case 2:
                    value = StatCollector.translateToLocal("gui.retreat");
                    break;
                case 3:
                default:
                    value = StatCollector.translateToLocal("gui.nothing");
                    break;
            }
            fontRendererObj.drawString(label, xLabel, y, 0xce75fa, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Walk Speed
            label = StatCollector.translateToLocal("stats.speed") + ": ";
            value = "" + npc.ais.getWalkingSpeed();
            fontRendererObj.drawString(label, xLabel, y, 0xffae0d, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Movement Type (0 = Ground, 1 = Flying)
            label = StatCollector.translateToLocal("movement.type") + ": ";
            int movementType = npc.ais.movementType;
            if (movementType == 0) {
                value = StatCollector.translateToLocal("movement.ground");
            } else {
                value = StatCollector.translateToLocal("movement.flying");
            }
            fontRendererObj.drawString(label, xLabel, y, 0x7cff54, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;
        }
        if (tab == 1 && this.linkedItem != null) {
            // Top bar: display the linked item's ID and name (centered)
            String topBarText = StatCollector.translateToLocal("gui.id") + ": " + linkedItem.id + " - " + linkedItem.name;
            int textWidth = getStringWidthWithoutColor(topBarText);
            int centerX = guiLeft + 5 + ((218 - 10 - textWidth) / 2);
            fontRendererObj.drawString(topBarText, centerX, guiTop + 10, 0xFFFFFF, true);

            // Lower section: draw each property with a label (in a dimmer color) and its value (in white)
            int y = guiTop + 30;
            int xLabel = guiLeft + 8;
            int xValue = guiLeft + 100;
            int labelColor = 0xffae0d;
            int valueColor = 0xFFFFFF;
            String label, value;

            // Version
            label = StatCollector.translateToLocal("display.version") + ": ";
            value = "" + linkedItem.version;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Max Stack Size
            labelColor = 0xff5714;
            label = StatCollector.translateToLocal("display.maxStack") + ": ";
            value = "" + linkedItem.stackSize;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Dig Speed
            labelColor = 0xf7ca28;
            label = StatCollector.translateToLocal("display.digSpeed") + ": ";
            value = "" + linkedItem.digSpeed;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            labelColor = 0x29d6b9;
            // Use Action
            String[] useActions = {
                StatCollector.translateToLocal("use_action.none"),
                StatCollector.translateToLocal("use_action.block"),
                StatCollector.translateToLocal("use_action.eat"),
                StatCollector.translateToLocal("use_action.drink"),
                StatCollector.translateToLocal("use_action.bow")
            };
            int useActionIndex;
            switch (linkedItem.itemUseAction) {
                case 0:
                    useActionIndex = 0;
                    break;
                case 1:
                    useActionIndex = 1;
                    break;
                case 2:
                    useActionIndex = 4;
                    break;
                case 3:
                    useActionIndex = 2;
                    break;
                case 4:
                    useActionIndex = 3;
                    break;
                default:
                    useActionIndex = 0;
                    break;
            }
            label = StatCollector.translateToLocal("display.useAction") + ": ";
            value = useActions[useActionIndex];
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Armor Type
            String[] armorOptions = {
                StatCollector.translateToLocal("armor_type.none"),
                StatCollector.translateToLocal("armor_type.all"),
                StatCollector.translateToLocal("armor_type.head"),
                StatCollector.translateToLocal("armor_type.chestplate"),
                StatCollector.translateToLocal("armor_type.leggings"),
                StatCollector.translateToLocal("armor_type.boots")
            };
            int armorIndex;
            if (linkedItem.armorType == -2) {
                armorIndex = 0;
            } else if (linkedItem.armorType == -1) {
                armorIndex = 1;
            } else {
                armorIndex = linkedItem.armorType + 2;
            }
            label = StatCollector.translateToLocal("display.armor") + ": ";
            value = armorOptions[armorIndex];
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            labelColor = 0x7cff54;
            // isTool
            label = StatCollector.translateToLocal("display.isTool") + ": ";
            value = ("" + linkedItem.isTool).toUpperCase();
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // isNormalItem
            label = StatCollector.translateToLocal("display.isNormalItem") + ": ";
            value = ("" + linkedItem.isNormalItem).toUpperCase();
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            labelColor = 0xce75fa;

            // Scale
            label = StatCollector.translateToLocal("model.scale") + ": ";
            value = linkedItem.display.scaleX + ", " + linkedItem.display.scaleY + ", " + linkedItem.display.scaleZ;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Rotation
            label = StatCollector.translateToLocal("model.rotate") + ": ";
            value = linkedItem.display.rotationX + ", " + linkedItem.display.rotationY + ", " + linkedItem.display.rotationZ;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
            y += 15;

            // Translate
            label = StatCollector.translateToLocal("model.translate") + ": ";
            value = linkedItem.display.translateX + ", " + linkedItem.display.translateY + ", " + linkedItem.display.translateZ;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            fontRendererObj.drawString(value, xValue, y, valueColor, false);
        }
    }

    public int getStringWidthWithoutColor(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§') {
                if (i < text.length() - 1) {
                    i += 1;
                }
            } else {
                // If not a color code, calculate the width
                width += fontRendererObj.getCharWidth(c);
            }
        }
        return width;
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiEditText) {
            if (!((SubGuiEditText) subgui).cancelled) {
                PacketClient.sendClient(new LinkedNPCAddPacket(((SubGuiEditText) subgui).text));
            }
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scroll.getSelected();
        this.data = data;
        scroll.setList(getSearchList());

        if (name != null)
            scroll.setSelected(name);

        initGui();
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scroll.setSelected(selected);
        originalName = scroll.getSelected();
    }

    @Override
    public void save() {
    }

    public boolean isMouseOverRenderer(int x, int y) {
        return x >= guiLeft + 10 && x <= guiLeft + 10 + 200 && y >= guiTop + 6 && y <= guiTop + 6 + 204;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            loadedNPC = false;
            selected = scroll.getSelected();
            originalName = scroll.getSelected();
            if (selected != null && !selected.isEmpty()) {
                if (tab == 0)
                    LinkedGetPacket.GetNPC(selected);
                else
                    LinkedGetPacket.GetItem(data.get(selected));
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }

    public void setGuiData(NBTTagCompound compound) {
        loadedNPC = false;
        this.linkedItem = null;
        if (compound.hasKey("NPCData")) {
            // Linked NPC
            this.npc.display.readToNBT(compound.getCompoundTag("NPCData"));
            this.npc.stats.readToNBT(compound.getCompoundTag("NPCData"));
            this.npc.ais.readToNBT(compound.getCompoundTag("NPCData"));
            loadedNPC = true;
        } else {
            this.linkedItem = new LinkedItem();
            this.linkedItem.readFromNBT(compound);
        }
        initGui();
    }
}
