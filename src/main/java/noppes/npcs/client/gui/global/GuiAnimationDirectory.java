package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.animation.AnimationGetPacket;
import kamkeel.npcs.network.packets.request.animation.AnimationRemovePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationClonePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationSavePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationsGetPacket;
import kamkeel.npcs.network.packets.request.animation.BuiltInAnimationGetPacket;
import kamkeel.npcs.network.packets.request.category.CategoryItemsRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryListRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryMoveItemPacket;
import kamkeel.npcs.network.packets.request.category.CategoryRemovePacket;
import kamkeel.npcs.network.packets.request.category.CategorySavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiDirectoryCategorized;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiTexturedButton;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Category;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.HashMap;
import java.util.Vector;

public class GuiAnimationDirectory extends GuiDirectoryCategorized {
    public Animation animation = new Animation();
    public EntityNPCInterface npc;
    private EntityNPCInterface originalNpc;

    // Animation playback
    public boolean playingAnimation = false;
    private long prevTick;

    // Built-in mode
    private boolean showingBuiltIn = false;
    private boolean currentIsBuiltIn = false;
    private HashMap<String, Integer> builtInData = new HashMap<>();

    public GuiAnimationDirectory(EntityNPCInterface npc) {
        super();
        this.originalNpc = npc;
        this.npc = createFakeNPC(npc);
        zoomed = 70;
        PacketClient.sendClient(new AnimationsGetPacket());
    }

    private static EntityNPCInterface createFakeNPC(EntityNPCInterface original) {
        EntityCustomNpc fake = new EntityCustomNpc(Minecraft.getMinecraft().theWorld);
        fake.display.readToNBT(original.display.writeToNBT(new NBTTagCompound()));
        fake.display.name = "anim preview";
        fake.height = original.height;
        fake.width = original.width;
        fake.display.animationData.setEnabled(true);
        return fake;
    }

    @Override
    protected boolean hasCategories() { return !showingBuiltIn; }

    @Override
    protected void computeLayout() {
        if (showingBuiltIn) {
            leftPanelPercent = 0.0f;
            minLeftPanelW = 0;
        } else {
            leftPanelPercent = 0.15f;
            minLeftPanelW = 120;
        }
        super.computeLayout();
    }

    @Override
    protected void drawPanels() {
        if (showingBuiltIn) {
            if (rightPanelW > 0) {
                GuiUtil.drawRectD(rightX - 1, contentY - 1, rightX + rightPanelW + 1, originY + usableH + 1, panelBorder);
            }
        } else {
            super.drawPanels();
        }
    }

    @Override
    protected void initLeftPanel() {
        if (showingBuiltIn) return;
        super.initLeftPanel();
    }

    @Override
    protected String getTitle() {
        return showingBuiltIn ? "Animations (Built-in)" : "Animations";
    }

    @Override
    protected void requestCategoryList() {
        if (!showingBuiltIn) {
            PacketClient.sendClient(new CategoryListRequestPacket(EnumCategoryType.ANIMATION));
        }
    }

    @Override
    protected void requestItemsInCategory(int catId) {
        if (!showingBuiltIn) {
            PacketClient.sendClient(new CategoryItemsRequestPacket(EnumCategoryType.ANIMATION, catId));
        }
    }

    @Override
    protected void requestItemData(int itemId) {
        PacketClient.sendClient(new AnimationGetPacket(itemId));
    }

    @Override
    protected void onSaveCategory(Category cat) {
        PacketClient.sendClient(new CategorySavePacket(EnumCategoryType.ANIMATION, cat.writeNBT(new NBTTagCompound())));
    }

    @Override
    protected void onRemoveCategory(int catId) {
        PacketClient.sendClient(new CategoryRemovePacket(EnumCategoryType.ANIMATION, catId));
    }

    @Override
    protected void onAddItem(int catId) {
        if (showingBuiltIn) return;
        String name = "New";
        while (itemData.containsKey(name)) name += "_";
        Animation newAnim = new Animation(-1, name);
        PacketClient.sendClient(new AnimationSavePacket(newAnim.writeToNBT()));
    }

    @Override
    protected void onRemoveItem(int itemId) {
        if (showingBuiltIn) return;
        PacketClient.sendClient(new AnimationRemovePacket(itemId));
        animation = new Animation();
    }

    @Override
    protected void onEditItem() {
        if (animation != null && animation.id >= 0 && !currentIsBuiltIn) {
            NoppesUtil.openGUI(player, new GuiNPCEditAnimation(this, animation, npc));
        }
    }

    @Override
    protected void onCloneItem() {
        if (showingBuiltIn || currentIsBuiltIn) return;
        if (animation != null && animation.id >= 0) {
            PacketClient.sendClient(new AnimationClonePacket(animation.id));
        }
    }

    @Override
    protected void onItemReceived(NBTTagCompound compound) {
        animation = new Animation();
        animation.readFromNBT(compound);
        setPrevItemName(animation.name);

        currentIsBuiltIn = compound.hasKey("BuiltIn") && compound.getBoolean("BuiltIn");

        playingAnimation = false;
        showFirstFrame();
    }

    private void showFirstFrame() {
        if (animation.id != -1 || currentIsBuiltIn) {
            AnimationData data = npc.display.animationData;
            data.setAnimation(new Animation());
            data.animation.smooth = animation.smooth;
            data.animation.loop = 0;
            if (!animation.frames.isEmpty()) {
                Frame firstFrame = new Frame();
                firstFrame.parent = data.animation;
                firstFrame.readFromNBT(animation.frames.get(0).writeToNBT());
                data.animation.addFrame(firstFrame);
            }
        }
    }

    @Override
    protected boolean hasSelectedItem() {
        if (currentIsBuiltIn) return true;
        return animation != null && animation.id >= 0;
    }

    @Override
    protected int getSelectedItemId() {
        return animation != null ? animation.id : -1;
    }

    @Override
    protected void sendMovePacket(int itemId, int destCatId) {
        PacketClient.sendClient(new CategoryMoveItemPacket(EnumCategoryType.ANIMATION, itemId, destCatId));
    }

    @Override
    protected GuiScreen getWindowedVariant() {
        return new GuiNPCManageAnimations(originalNpc, false, false);
    }

    @Override
    protected void saveCurrentItem() {
        if (!currentIsBuiltIn && animation != null && animation.id >= 0 && prevItemName != null && !prevItemName.isEmpty()) {
            PacketClient.sendClient(new AnimationSavePacket(animation.writeToNBT()));
            prevItemName = animation.name;
        }
    }

    @Override
    protected void onSubGuiClosed(SubGuiInterface subgui) {
        if (animation != null && animation.id >= 0) {
            setPrevItemName(animation.name);
            if (selectedCatId >= 0 && !showingBuiltIn) requestItemsInCategory(selectedCatId);
        }
    }

    // ========== Built-in Toggle ==========

    @Override
    protected int initExtraTopBarButtons(int x, int topBtnY) {
        String toggleLabel = showingBuiltIn ? "gui.builtin" : "gui.custom";
        GuiNpcButton toggleBtn = new GuiNpcButton(60, x, topBtnY, 55, btnH, toggleLabel);
        toggleBtn.setTextColor(showingBuiltIn ? 0x55FFFF : 0xFFFFFF);
        addButton(toggleBtn);
        return x + 55 + 2;
    }

    @Override
    protected void initTopBar(int topBtnY) {
        super.initTopBar(topBtnY);

        // Disable item operations in built-in mode
        if (showingBuiltIn) {
            if (getButton(50) != null) getButton(50).enabled = false; // Add
            if (getButton(54) != null) getButton(54).enabled = false; // Move
        }
    }

    @Override
    protected void initRightPanel(int startY) {
        // Calculate layout: playback rows + edit/copy + remove rows
        int bottomRows = 2; // edit+copy, remove
        boolean hasPlayback = animation != null && !animation.frames.isEmpty() && hasSelectedItem();
        if (hasPlayback) {
            bottomRows++; // playback controls row
            if (playingAnimation) bottomRows++; // frame info row
        }
        int bottomH = bottomRows * (btnH + gap) + 14;

        previewX = rightX;
        previewY = contentY;
        previewW = rightPanelW;
        previewH = contentH - bottomH - gap;

        // Playback controls
        if (hasPlayback) {
            int playY = contentY + previewH + gap;
            String animTexture = "customnpcs:textures/gui/animation.png";
            AnimationData data = npc.display.animationData;
            int btnX = rightX + 4;

            if (!playingAnimation || (data.animation != null && data.animation.paused)) {
                String statusKey = (data.animation != null && data.animation.paused) ? "animation.paused" : "animation.stopped";
                addLabel(new GuiNpcLabel(90, statusKey, btnX, playY + 5, 0xFFFFFF));
                addButton(new GuiTexturedButton(91, "", btnX + 65, playY, 11, 20, animTexture, 18, 71));
            } else {
                addLabel(new GuiNpcLabel(90, "animation.playing", btnX, playY + 5, 0xFFFFFF));
                addButton(new GuiTexturedButton(92, "", btnX + 65, playY, 14, 20, animTexture, 0, 71));
            }
            if (playingAnimation) {
                addButton(new GuiTexturedButton(93, "", btnX + 85, playY, 14, 20, animTexture, 33, 71));
                // Frame info on its own line below playback controls
                int frameInfoY = playY + btnH + gap;
                addLabel(new GuiNpcLabel(94, "", btnX, frameInfoY + 5, 0xFFFFFF));
            }
        }

        // Edit + Copy on one row, Remove below
        int btnY = contentY + contentH - btnH * 2 - gap;
        int halfW = (rightPanelW - gap) / 2;

        GuiNpcButton editBtn = new GuiNpcButton(51, rightX, btnY, halfW, btnH, "gui.edit");
        editBtn.enabled = hasSelectedItem() && movePhase == 0 && !currentIsBuiltIn;
        addButton(editBtn);

        GuiNpcButton cloneBtn = new GuiNpcButton(52, rightX + halfW + gap, btnY, halfW, btnH, "gui.copy");
        cloneBtn.enabled = hasSelectedItem() && movePhase == 0 && !currentIsBuiltIn;
        addButton(cloneBtn);

        int removeY = btnY + btnH + gap;
        GuiNpcButton removeBtn = new GuiNpcButton(53, rightX, removeY, rightPanelW, btnH, "gui.remove");
        removeBtn.enabled = hasSelectedItem() && movePhase == 0 && !currentIsBuiltIn;
        removeBtn.setTextColor(0xFF5555);
        addButton(removeBtn);
    }

    // ========== Scroll Events Override for Built-in ==========

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0 && showingBuiltIn) {
            String selected = itemScroll.getSelected();
            if (selected != null && !selected.equals(prevItemName)) {
                PacketClient.sendClient(new BuiltInAnimationGetPacket(selected));
                prevItemName = selected;
            }
            return;
        }
        super.customScrollClicked(i, j, k, scroll);
    }

    // ========== Data ==========

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.BUILTIN_ANIMATIONS) {
            this.builtInData = data;
            if (showingBuiltIn) {
                this.itemData = data;
                itemScroll.setList(getItemSearchList());
                initGui();
            }
            return;
        }
        super.setData(list, data, type);
    }

    // ========== Animation Tick ==========

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        AnimationData data = npc.display.animationData;
        if (!data.isActive() && playingAnimation) {
            playingAnimation = false;
            initGui();
        } else if (data.isActive()) {
            long time = mc.theWorld.getTotalWorldTime();
            if (time != prevTick) {
                npc.display.animationData.increaseTime();
                GuiNpcLabel label = getLabel(94);
                if (label != null && data.animation != null) {
                    int frameIdx = data.animation.currentFrame;
                    int frameTime = data.animation.currentFrameTime;
                    int totalFrames = data.animation.frames.size();
                    Frame curFrame = (Frame) data.animation.currentFrame();
                    int duration = curFrame != null ? curFrame.getDuration() : 0;
                    label.label = (frameIdx + 1) + "/" + totalFrames + " (" + frameTime + "/" + duration + "t)";
                }
            }
            prevTick = time;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ========== Actions ==========

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        // Toggle built-in / custom
        if (id == 60) {
            showingBuiltIn = !showingBuiltIn;
            animation = new Animation();
            currentIsBuiltIn = false;
            playingAnimation = false;
            prevItemName = "";
            itemSearch = "";

            if (showingBuiltIn) {
                // Show built-in flat list
                this.itemData = builtInData;
                selectedCatId = 0; // Ensure list is visible
            } else {
                // Re-enter category mode
                this.itemData = new HashMap<>();
                selectedCatId = -1;
                requestCategoryList();
            }
            initGui();
            return;
        }

        // Playback controls
        AnimationData data = npc.display.animationData;
        if (id == 91) {
            if (!playingAnimation || !data.isActive()) {
                animation.currentFrame = 0;
                animation.currentFrameTime = 0;
                for (Frame frame : animation.frames) {
                    for (FramePart framePart : frame.frameParts.values()) {
                        framePart.prevRotations = new float[]{0, 0, 0};
                        framePart.prevPivots = new float[]{0, 0, 0};
                    }
                }
            }
            playingAnimation = true;
            data.setAnimation(this.animation);
            data.animation.paused = false;
            initGui();
            return;
        } else if (id == 92) {
            data.animation.paused = true;
            initGui();
            return;
        } else if (id == 93) {
            playingAnimation = false;
            data.animation.paused = false;
            showFirstFrame();
            initGui();
            return;
        }

        super.actionPerformed(guibutton);
    }

    // ========== Preview Rendering ==========

    @Override
    protected void drawItemPreview(int centerX, int centerY, int mouseX, int mouseY, float partialTicks) {
        if (animation == null) return;
        if (!currentIsBuiltIn && animation.id == -1) return;

        // Scale NPC with panel size
        float scale = Math.min(previewW, previewH) / 200f;
        float renderZoom = zoomed * scale;

        GL11.glColor4f(1, 1, 1, 1);
        EntityLivingBase entity = this.npc;

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 60F);
        GL11.glScalef(-renderZoom, renderZoom, renderZoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f7 = entity.rotationYawHead;
        float f5 = (float) centerX - mouseX;
        float f6 = (float) (centerY - 50) - mouseY;

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

        ClientEventHandler.renderingEntityInGUI = true;
        try {
            RenderManager.instance.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F);
        } catch (Exception ignored) {
        }
        ClientEventHandler.renderingEntityInGUI = false;

        entity.prevRenderYawOffset = entity.renderYawOffset = f2;
        entity.prevRotationYaw = entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = entity.rotationYawHead = f7;
        GL11.glPopMatrix();

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void drawItemDetails(int x, int y, int w) {
        if (animation == null) return;
        if (!currentIsBuiltIn && animation.id == -1) return;

        fontRendererObj.drawString(animation.name, x, y, 0xFFFFFF, true);
        y += 14;
        fontRendererObj.drawString(StatCollector.translateToLocal("animation.frames") + ": " + animation.frames.size(), x, y, 0xB5B5B5, false);
        y += 12;
        fontRendererObj.drawString(StatCollector.translateToLocal("stats.speed") + ": " + animation.speed, x, y, 0xB5B5B5, false);
        y += 12;
        String loopStr = animation.loop == 0 ? StatCollector.translateToLocal("gui.none") : animation.loop == 1 ? StatCollector.translateToLocal("animation.loop") : StatCollector.translateToLocal("animation.mirror");
        fontRendererObj.drawString(StatCollector.translateToLocal("animation.loop") + ": " + loopStr, x, y, 0xB5B5B5, false);
        if (currentIsBuiltIn) {
            y += 12;
            fontRendererObj.drawString(StatCollector.translateToLocal("gui.builtin.tag"), x, y, 0x55FF55, false);
        }
    }

    // ========== Cleanup ==========

    @Override
    public void onGuiClosed() {
        if (npc != null) {
            npc.display.animationData.setEnabled(false);
        }
        super.onGuiClosed();
    }

}
