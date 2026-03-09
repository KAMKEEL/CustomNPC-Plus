package noppes.npcs.client.gui.global;

import kamkeel.npcs.client.renderer.TelegraphRenderer;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.ability.preview.AbilityPreviewExecutor;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.BuiltInAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilityRemovePacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilitySavePacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilityRemovePacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitySavePacket;
import kamkeel.npcs.network.packets.request.category.AbilityCategoryMovePacket;
import kamkeel.npcs.network.packets.request.category.CategoryItemsRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryListRequestPacket;
import kamkeel.npcs.network.packets.request.category.CategoryRemovePacket;
import kamkeel.npcs.network.packets.request.category.CategorySavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.advanced.SubGuiAbilityTypeSelect;
import noppes.npcs.client.gui.advanced.SubGuiAbilityVariantSelect;
import noppes.npcs.client.gui.advanced.SubGuiChainedAbilityConfig;
import noppes.npcs.client.gui.advanced.SubGuiDuplicateNameConfirm;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiDirectoryCategorized;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiTexturedButton;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.IChainedAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumCategoryType;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Category;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class GuiAbilityDirectory extends GuiDirectoryCategorized
    implements IAbilityConfigCallback, IChainedAbilityConfigCallback {

    private EntityNPCInterface npc;
    private EntityNPCInterface originalNpc;

    // View mode: 0=Built-In, 1=Custom, 2=Chained
    private int viewMode = 1;
    private boolean currentIsBuiltIn = false;

    // Data caches
    private HashMap<String, Integer> builtInData = new HashMap<>();
    private Ability selectedAbility = null;
    private ChainedAbility selectedChain = null;

    // User type filter: 0=Both, 1=NPC Only, 2=Player Only
    private int userTypeFilter = 0;
    private static final String[] FILTER_LABELS = {"gui.both", "gui.npcs", "gui.players"};

    // Pending save state
    private Ability pendingSaveAbility = null;
    private ChainedAbility pendingSaveChain = null;
    private String pendingTypeId = null;

    // Preview playback
    private AbilityPreviewExecutor previewExecutor;
    private long prevTick = 0;
    private double npcStartX, npcStartY, npcStartZ;
    private boolean trackingMovement = false;

    // Fixed camera settings (matching GuiAbilityInterface)
    private static final float NPC_FACING_YAW = 310f;
    private static final float CAMERA_PITCH = 5f;

    public GuiAbilityDirectory(EntityNPCInterface npc) {
        super();
        this.originalNpc = npc;
        this.npc = createFakeNPC(npc);
        rightPanelPercent = 0.38f;
        minRightPanelW = 200;
        zoomed = 50;

        // Initialize preview executor
        previewExecutor = new AbilityPreviewExecutor();

        // Re-request categories — viewMode was 0 during super() call
        requestCategoryList();
        PacketClient.sendClient(new CustomAbilitiesGetPacket());
    }

    private static EntityNPCInterface createFakeNPC(EntityNPCInterface original) {
        EntityCustomNpc fake = new EntityCustomNpc(Minecraft.getMinecraft().theWorld);
        fake.display.readToNBT(original.display.writeToNBT(new NBTTagCompound()));
        fake.display.name = "ability preview";
        fake.height = original.height;
        fake.width = original.width;
        fake.display.animationData.setEnabled(true);
        return fake;
    }

    private boolean isBuiltInMode() { return viewMode == 0; }
    private boolean isCustomMode() { return viewMode == 1; }
    private boolean isChainedMode() { return viewMode == 2; }

    private int getCategoryType() {
        return isChainedMode() ? EnumCategoryType.CHAINED_ABILITY : EnumCategoryType.ABILITY;
    }

    // ========== Layout Overrides ==========

    @Override
    protected boolean hasCategories() { return !isBuiltInMode(); }

    @Override
    protected void computeLayout() {
        if (isBuiltInMode()) {
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
        if (isBuiltInMode()) {
            // No left panel border
            if (rightPanelW > 0) {
                GuiUtil.drawRectD(rightX - 1, contentY - 1, rightX + rightPanelW + 1, originY + usableH + 1, panelBorder);
            }
        } else {
            super.drawPanels();
        }
    }

    @Override
    protected void initLeftPanel() {
        if (isBuiltInMode()) return;
        super.initLeftPanel();
    }

    // ========== Title ==========

    @Override
    protected String getTitle() {
        if (isBuiltInMode()) return "Abilities (Built-in)";
        if (isChainedMode()) return "Chained Abilities";
        return "Abilities";
    }

    // ========== Top Bar ==========

    @Override
    protected int initExtraTopBarButtons(int x, int topBtnY) {
        int topBtnW = 55;

        // View mode toggle: Built-In / Custom / Chained
        String[] labels = {"gui.builtin", "gui.custom", "gui.chained"};
        int[] colors = {0x55FFFF, 0xFFFFFF, 0xFFFF55};
        GuiNpcButton viewBtn = new GuiNpcButton(60, x, topBtnY, topBtnW, btnH, labels[viewMode]);
        viewBtn.setTextColor(colors[viewMode]);
        addButton(viewBtn);
        x += topBtnW + 2;

        // User type filter (only for Custom and Built-in, not Chained)
        if (!isChainedMode()) {
            addButton(new GuiNpcButton(61, x, topBtnY, topBtnW, btnH, FILTER_LABELS[userTypeFilter]));
            x += topBtnW + 2;
        }

        return x;
    }

    @Override
    protected void initTopBar(int topBtnY) {
        super.initTopBar(topBtnY);

        // Disable category-dependent buttons in built-in mode
        if (isBuiltInMode()) {
            if (getButton(50) != null) getButton(50).enabled = false; // Add
            if (getButton(54) != null) getButton(54).enabled = false; // Move
        }
    }

    // ========== Category Requests ==========

    @Override
    protected void requestCategoryList() {
        if (isBuiltInMode()) return;
        PacketClient.sendClient(new CategoryListRequestPacket(getCategoryType()));
    }

    @Override
    protected void requestItemsInCategory(int catId) {
        if (isBuiltInMode()) return;
        PacketClient.sendClient(new CategoryItemsRequestPacket(getCategoryType(), catId));
    }

    @Override
    protected void requestItemData(int itemId) {
        // Not used — we override customScrollClicked for name-based requests
    }

    @Override
    protected void onSaveCategory(Category cat) {
        PacketClient.sendClient(new CategorySavePacket(getCategoryType(), cat.writeNBT(new NBTTagCompound())));
    }

    @Override
    protected void onRemoveCategory(int catId) {
        PacketClient.sendClient(new CategoryRemovePacket(getCategoryType(), catId));
    }

    @Override
    protected void onAddItem(int catId) {
        if (isBuiltInMode()) return;
        if (isChainedMode()) {
            ChainedAbility newChain = new ChainedAbility("New Chain");
            setSubGui(new SubGuiChainedAbilityConfig(newChain, this));
        } else {
            setSubGui(new SubGuiAbilityTypeSelect());
        }
    }

    @Override
    protected void onRemoveItem(int itemId) {
        // Handled via confirmClicked with name-based packets
    }

    @Override
    protected void onEditItem() {
        stopPreviewPlayback();
        if (isChainedMode() && selectedChain != null) {
            setSubGui(new SubGuiChainedAbilityConfig(selectedChain, this));
        } else if (!currentIsBuiltIn && selectedAbility != null) {
            setSubGui(selectedAbility.createConfigGui(this));
        }
    }

    @Override
    protected void onCloneItem() {
        // Abilities don't support cloning through this mechanism
    }

    @Override
    protected void onItemReceived(NBTTagCompound compound) {
        if (isChainedMode() && compound.hasKey("Entries")) {
            selectedChain = new ChainedAbility();
            selectedChain.readNBT(compound);
            setPrevItemName(selectedChain.getName());
        } else {
            currentIsBuiltIn = compound.hasKey("BuiltIn") && compound.getBoolean("BuiltIn");
            selectedAbility = AbilityController.Instance.fromNBT(compound);
            if (selectedAbility != null) {
                setPrevItemName(selectedAbility.getName());
            }
        }
    }

    @Override
    protected boolean hasSelectedItem() {
        if (isChainedMode()) return selectedChain != null;
        if (currentIsBuiltIn) return selectedAbility != null;
        return selectedAbility != null;
    }

    @Override
    protected int getSelectedItemId() {
        return -1; // Abilities use names, not IDs
    }

    @Override
    protected void sendMovePacket(int itemId, int destCatId) {
        // Not used — we override executeMoveItems for name-based moves
    }

    @Override
    protected void onMoveNewItem(int catId) {
        String name = prevItemName;
        if (name != null && !name.isEmpty()) {
            PacketClient.sendClient(new AbilityCategoryMovePacket(getCategoryType(), name, catId));
            if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
        }
    }

    @Override
    protected GuiScreen getWindowedVariant() {
        return new GuiNpcManageAbilities(originalNpc, false);
    }

    @Override
    protected void saveCurrentItem() {
        // Abilities are saved via SubGui callbacks, not on close
    }

    // ========== Move Override (name-based) ==========

    @Override
    protected void executeMoveItems(int destCatId) {
        for (String name : moveSelection) {
            PacketClient.sendClient(new AbilityCategoryMovePacket(getCategoryType(), name, destCatId));
        }
        movePhase = 0;
        moveSelection.clear();
        if (selectedCatId >= 0) {
            requestItemsInCategory(selectedCatId);
        }
        initGui();
    }

    // ========== Scroll Events (name-based) ==========

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            if (isBuiltInMode()) {
                // Built-in: name-based request
                String selected = itemScroll.getSelected();
                if (selected != null && !selected.equals(prevItemName)) {
                    stopPreviewPlayback();
                    currentIsBuiltIn = true;
                    PacketClient.sendClient(new BuiltInAbilityGetPacket(selected));
                    prevItemName = selected;
                }
                return;
            }

            if (movePhase == 1) {
                // Move phase 1: selecting items
                if (moveSelection.size() > MAX_MOVE_ITEMS) {
                    java.util.HashSet<String> trimmed = new java.util.HashSet<>();
                    int count = 0;
                    for (String s : moveSelection) {
                        if (count++ >= MAX_MOVE_ITEMS) break;
                        trimmed.add(s);
                    }
                    moveSelection.clear();
                    moveSelection.addAll(trimmed);
                    itemScroll.setSelectedList(moveSelection);
                }
                if (getButton(55) != null) {
                    getButton(55).enabled = !moveSelection.isEmpty();
                }
                return;
            }

            // Normal click: name-based request
            String selected = itemScroll.getSelected();
            if (selected != null && !selected.equals(prevItemName)) {
                stopPreviewPlayback();
                prevItemName = selected;
                currentIsBuiltIn = false;
                if (isChainedMode()) {
                    PacketClient.sendClient(new ChainedAbilityGetPacket(selected));
                } else {
                    PacketClient.sendClient(new CustomAbilityGetPacket(selected));
                }
            }
            return;
        }

        // Category scroll — delegate to parent
        super.customScrollClicked(i, j, k, scroll);
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0 && hasSelectedItem() && movePhase == 0) {
            onEditItem();
        }
    }

    // ========== Search with User Type Filtering ==========

    @Override
    protected List<String> getItemSearchList() {
        if (selectedCatId < 0 && !isBuiltInMode()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        HashMap<String, Integer> data = isBuiltInMode() ? builtInData : itemData;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String name = entry.getKey();
            if (!itemSearch.isEmpty() && !name.toLowerCase().contains(itemSearch)) continue;
            if (!isChainedMode() && userTypeFilter != 0) {
                UserType ut = UserType.fromOrdinal(entry.getValue());
                if (userTypeFilter == 1 && !ut.allowsNpc()) continue;
                if (userTypeFilter == 2 && !ut.allowsPlayer()) continue;
            }
            list.add(name);
        }
        return list;
    }

    // ========== Data from Server ==========

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.BUILTIN_ABILITIES) {
            builtInData = data;
            if (isBuiltInMode()) {
                itemScroll.setList(getItemSearchList());
                initGui();
            }
            return;
        }
        if (type == EnumScrollData.CUSTOM_ABILITIES) {
            // Received flat list — used only for built-in data alongside it
            // Category mode uses CATEGORY_GROUP instead
            return;
        }
        if (type == EnumScrollData.CHAINED_ABILITIES) {
            // Flat list — not used in category mode
            return;
        }
        super.setData(list, data, type);
    }

    // ========== Actions ==========

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        // View mode toggle
        if (id == 60) {
            viewMode = (viewMode + 1) % 3;
            stopPreviewPlayback();
            clearSelection();
            if (isBuiltInMode()) {
                selectedCatId = 0;
                PacketClient.sendClient(new CustomAbilitiesGetPacket());
            } else {
                selectedCatId = -1;
                requestCategoryList();
            }
            initGui();
            return;
        }

        // User type filter
        if (id == 61) {
            userTypeFilter = (userTypeFilter + 1) % 3;
            itemScroll.setList(getItemSearchList());
            initGui();
            return;
        }

        // Preview controls
        if (id == 91) { // Play
            if (previewExecutor.isPaused()) {
                previewExecutor.play();
            } else {
                startPreviewPlayback();
            }
            initGui();
            return;
        } else if (id == 92) { // Pause
            previewExecutor.pause();
            initGui();
            return;
        } else if (id == 93) { // Stop
            stopPreviewPlayback();
            initGui();
            return;
        }

        // Remove — need custom handling for name-based remove
        if (id == 53 && prevItemName != null && !prevItemName.isEmpty() && movePhase == 0) {
            int confirmId = isChainedMode() ? 3 : 2;
            GuiYesNo guiyesno = new GuiYesNo(this, prevItemName, StatCollector.translateToLocal("gui.delete"), confirmId);
            displayGuiScreen(guiyesno);
            return;
        }

        super.actionPerformed(guibutton);
    }

    private void clearSelection() {
        prevItemName = "";
        selectedAbility = null;
        selectedChain = null;
        currentIsBuiltIn = false;
        itemSearch = "";
        movePhase = 0;
        moveSelection.clear();
        trackingMovement = false;
    }

    // ========== Confirm Dialog ==========

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result) return;

        // Remove custom ability
        if (id == 2 && prevItemName != null && !prevItemName.isEmpty()) {
            PacketClient.sendClient(new CustomAbilityRemovePacket(prevItemName));
            selectedAbility = null;
            prevItemName = "";
            if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
            initGui();
            return;
        }

        // Remove chained ability
        if (id == 3 && prevItemName != null && !prevItemName.isEmpty()) {
            PacketClient.sendClient(new ChainedAbilityRemovePacket(prevItemName));
            selectedChain = null;
            prevItemName = "";
            if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
            initGui();
            return;
        }

        // Category remove (id == 5) handled by parent
        super.confirmClicked(result, id);
    }

    // ========== SubGui Callbacks ==========

    @Override
    protected void onSubGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiChainedAbilityConfig) {
            if (pendingSaveChain != null) {
                PacketClient.sendClient(new ChainedAbilitySavePacket(pendingSaveChain.writeNBT(false)));
                prevItemName = pendingSaveChain.getName();
                pendingSaveChain = null;
                if (selectedCatId >= 0 && isChainedMode()) requestItemsInCategory(selectedCatId);
            }
            initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityVariantSelect) {
            SubGuiAbilityVariantSelect gui = (SubGuiAbilityVariantSelect) subgui;
            int idx = gui.getSelectedIndex();
            if (idx >= 0 && pendingTypeId != null) {
                Ability newAbility = AbilityController.Instance.create(pendingTypeId);
                if (newAbility != null) {
                    gui.getVariants().get(idx).apply(newAbility);
                    newAbility.setId(UUID.randomUUID().toString());
                    pendingTypeId = null;
                    selectedAbility = newAbility;
                    prevItemName = newAbility.getName();
                    setSubGui(newAbility.createConfigGui(this));
                    return;
                }
            }
            pendingTypeId = null;
        } else if (subgui instanceof SubGuiAbilityTypeSelect) {
            String typeId = ((SubGuiAbilityTypeSelect) subgui).getSelectedTypeId();
            if (typeId != null) {
                List<AbilityVariant> variants = AbilityController.Instance.getVariantsForType(typeId);
                if (variants.size() > 1) {
                    pendingTypeId = typeId;
                    setSubGui(new SubGuiAbilityVariantSelect(variants));
                    return;
                }
                Ability newAbility = AbilityController.Instance.create(typeId);
                if (newAbility != null) {
                    if (variants.size() == 1) variants.get(0).apply(newAbility);
                    newAbility.setId(UUID.randomUUID().toString());
                    selectedAbility = newAbility;
                    prevItemName = newAbility.getName();
                    setSubGui(newAbility.createConfigGui(this));
                    return;
                }
            }
        } else if (subgui instanceof SubGuiDuplicateNameConfirm) {
            SubGuiDuplicateNameConfirm gui = (SubGuiDuplicateNameConfirm) subgui;
            if (pendingSaveAbility != null) {
                if (gui.isConfirmed()) {
                    PacketClient.sendClient(new CustomAbilitySavePacket(pendingSaveAbility.writeNBT(false)));
                    prevItemName = pendingSaveAbility.getName();
                    pendingSaveAbility = null;
                    if (selectedCatId >= 0 && isCustomMode()) requestItemsInCategory(selectedCatId);
                } else if (gui.isBack()) {
                    Ability ability = pendingSaveAbility;
                    pendingSaveAbility = null;
                    setSubGui(ability.createConfigGui(this));
                    return;
                } else {
                    pendingSaveAbility = null;
                }
            }
        } else if (pendingSaveAbility != null) {
            if (hasDuplicateName(pendingSaveAbility)) {
                setSubGui(new SubGuiDuplicateNameConfirm());
                return;
            }
            PacketClient.sendClient(new CustomAbilitySavePacket(pendingSaveAbility.writeNBT(false)));
            prevItemName = pendingSaveAbility.getName();
            pendingSaveAbility = null;
            if (selectedCatId >= 0 && isCustomMode()) requestItemsInCategory(selectedCatId);
        }
        initGui();
    }

    private boolean hasDuplicateName(Ability ability) {
        String name = ability.getName();
        if (name == null || name.isEmpty()) return false;
        if (!itemData.containsKey(name)) return false;
        String uuid = ability.getId();
        if (uuid == null || uuid.isEmpty()) return true;
        Ability existing = AbilityController.Instance.getCustomAbilityByName(name);
        return existing == null || !uuid.equals(existing.getId());
    }

    @Override
    public void onAbilitySaved(Ability ability) {
        pendingSaveAbility = ability;
    }

    @Override
    public void onChainedAbilitySaved(ChainedAbility chain) {
        pendingSaveChain = chain;
    }

    // ========== Right Panel ==========

    @Override
    protected void initRightPanel(int startY) {
        // Calculate layout: playback row + edit + remove rows
        int bottomRows = 2; // edit, remove
        boolean hasPlayback = hasSelectedItem();
        if (hasPlayback) bottomRows++; // playback row
        int bottomH = bottomRows * (btnH + gap) + 14;

        previewX = rightX;
        previewY = contentY;
        previewW = rightPanelW;
        previewH = contentH - bottomH - gap;

        // Playback controls
        if (hasPlayback) {
            int playY = contentY + previewH + gap;
            String animTexture = "customnpcs:textures/gui/animation.png";
            int btnX = rightX + 4;

            boolean isPlaying = previewExecutor.isPlaying() && !previewExecutor.isPaused();
            boolean isPaused = previewExecutor.isPaused();
            boolean isActive = previewExecutor.isActive();

            if (!isPlaying || isPaused) {
                String statusKey = isPaused ? "animation.paused" : "animation.stopped";
                addLabel(new GuiNpcLabel(90, statusKey, btnX, playY + 5));
                addButton(new GuiTexturedButton(91, "", btnX + 65, playY, 11, 20, animTexture, 18, 71));
            } else {
                addLabel(new GuiNpcLabel(90, "animation.playing", btnX, playY + 5));
                addButton(new GuiTexturedButton(92, "", btnX + 65, playY, 14, 20, animTexture, 0, 71));
            }
            if (isActive) {
                addButton(new GuiTexturedButton(93, "", btnX + 85, playY, 14, 20, animTexture, 33, 71));
            }
        }

        // Edit + Remove at bottom
        int editY = contentY + contentH - btnH * 2 - gap;
        GuiNpcButton editBtn = new GuiNpcButton(51, rightX, editY, rightPanelW, btnH, "gui.edit");
        editBtn.enabled = hasSelectedItem() && movePhase == 0 && !currentIsBuiltIn;
        addButton(editBtn);

        int removeY = editY + btnH + gap;
        GuiNpcButton removeBtn = new GuiNpcButton(53, rightX, removeY, rightPanelW, btnH, "gui.remove");
        removeBtn.enabled = hasSelectedItem() && movePhase == 0 && !currentIsBuiltIn;
        removeBtn.setTextColor(0xFF5555);
        addButton(removeBtn);
    }

    // ========== Preview Rendering ==========

    @Override
    protected void drawItemPreview(int centerX, int centerY, int mouseX, int mouseY, float partialTicks) {
        if (selectedAbility == null && selectedChain == null) return;

        boolean previewing = previewExecutor.isActive();

        // When preview is active, use fixed camera like GuiAbilityInterface
        // Otherwise use the standard mouse-follow rotation
        if (previewing) {
            drawAbilityPreview(partialTicks);
        } else {
            drawStaticNpcPreview(centerX, centerY, mouseX, mouseY);
        }
    }

    /**
     * Renders the full ability preview with fixed camera, preview entities, and telegraph.
     * Matches GuiAbilityInterface's rendering pipeline.
     */
    private void drawAbilityPreview(float partialTicks) {
        // Set NPC facing direction
        npc.prevRenderYawOffset = npc.renderYawOffset = NPC_FACING_YAW;
        npc.prevRotationYaw = npc.rotationYaw = NPC_FACING_YAW;
        npc.prevRotationYawHead = npc.rotationYawHead = NPC_FACING_YAW;
        npc.rotationPitch = 0;

        // Calculate NPC position delta for movement rendering
        double npcDeltaX = 0, npcDeltaY = 0, npcDeltaZ = 0;
        if (trackingMovement) {
            double interpX = npc.prevPosX + (npc.posX - npc.prevPosX) * partialTicks;
            double interpY = npc.prevPosY + (npc.posY - npc.prevPosY) * partialTicks;
            double interpZ = npc.prevPosZ + (npc.posZ - npc.prevPosZ) * partialTicks;
            npcDeltaX = interpX - npcStartX;
            npcDeltaY = interpY - npcStartY;
            npcDeltaZ = interpZ - npcStartZ;
        }

        // NPC render position: left-center of preview area
        int npcScreenX = previewX + (int)(previewW * 0.33f);
        int npcScreenY = previewY + (int)(previewH * 0.80f);

        // Scale camera zoom based on panel size
        float scale = Math.min(previewW, previewH) / 200f;
        float renderZoom = zoomed * scale;

        // Enable scissor to clip rendering to preview area
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        setScissorClip(previewX, previewY, previewW, previewH);

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();

        GL11.glTranslatef(npcScreenX, npcScreenY, 500F);
        GL11.glScalef(-renderZoom, renderZoom, renderZoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(CAMERA_PITCH, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);

        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);

        GL11.glTranslatef(0.0F, npc.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180F;
        ClientEventHandler.renderingEntityInGUI = true;

        // Render NPC at its position delta
        try {
            RenderManager.instance.renderEntityWithPosYaw(npc, npcDeltaX, npcDeltaY, npcDeltaZ, 0.0F, partialTicks);
        } catch (Exception ignored) {}

        // Render preview entities
        renderPreviewEntities(partialTicks);

        // Render telegraph
        renderPreviewTelegraph(partialTicks);

        ClientEventHandler.renderingEntityInGUI = false;
        GL11.glPopMatrix();

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Standard static NPC preview with mouse-follow rotation (when not previewing an ability).
     */
    private void drawStaticNpcPreview(int centerX, int centerY, int mouseX, int mouseY) {
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

        try {
            RenderManager.instance.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F);
        } catch (Exception ignored) {}

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
    }

    private void renderPreviewEntities(float partialTicks) {
        List<Entity> entities = previewExecutor.getPreviewEntities();
        if (entities.isEmpty()) return;

        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        double refX = trackingMovement ? npcStartX : npc.posX;
        double refY = trackingMovement ? npcStartY : npc.posY;
        double refZ = trackingMovement ? npcStartZ : npc.posZ;

        for (Entity entity : entities) {
            if (entity == null || entity.isDead) continue;
            double offsetX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks - refX;
            double offsetY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks - refY - npc.yOffset;
            double offsetZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks - refZ;

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(GL11.GL_LIGHTING);
            try {
                RenderManager.instance.renderEntityWithPosYaw(entity, offsetX, offsetY, offsetZ, entity.rotationYaw, partialTicks);
            } catch (Exception ignored) {}
        }

        if (lightingWasEnabled) GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderPreviewTelegraph(float partialTicks) {
        TelegraphInstance telegraph = previewExecutor.getTelegraph();
        if (telegraph == null || TelegraphRenderer.Instance == null) return;

        double refX = trackingMovement ? npcStartX : npc.posX;
        double refY = trackingMovement ? npcStartY : npc.posY;
        double refZ = trackingMovement ? npcStartZ : npc.posZ;

        double offsetX = telegraph.getInterpolatedX(partialTicks) - refX;
        double offsetY = telegraph.getInterpolatedY(partialTicks) - refY - npc.yOffset;
        double offsetZ = telegraph.getInterpolatedZ(partialTicks) - refZ;

        TelegraphRenderer.Instance.renderTelegraphInGUI(telegraph, offsetX, offsetY, offsetZ, 1.0f, partialTicks);
    }

    private void setScissorClip(int x, int y, int w, int h) {
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();
        int scaledY = mc.displayHeight - (y + h) * scale;
        GL11.glScissor(x * scale, scaledY, w * scale, h * scale);
    }

    // ========== Preview Tick ==========

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Set NPC rotation BEFORE ticking for correct anchor points
        if (previewExecutor.isActive()) {
            npc.prevRenderYawOffset = npc.renderYawOffset = NPC_FACING_YAW;
            npc.prevRotationYaw = npc.rotationYaw = NPC_FACING_YAW;
            npc.prevRotationYawHead = npc.rotationYawHead = NPC_FACING_YAW;
        }

        tickPreview();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void tickPreview() {
        if (previewExecutor.isPlaying() && !previewExecutor.isPaused()) {
            long time = mc.theWorld != null ? mc.theWorld.getTotalWorldTime() : System.currentTimeMillis() / 50;
            if (time != prevTick) {
                npc.display.animationData.increaseTime();
                previewExecutor.tick();
                prevTick = time;

                if (!previewExecutor.isActive()) {
                    trackingMovement = false;
                    initGui();
                }
            }
        }
    }

    private void startPreviewPlayback() {
        // Save NPC start position for movement tracking
        npcStartX = npc.posX;
        npcStartY = npc.posY;
        npcStartZ = npc.posZ;
        trackingMovement = true;

        if (isChainedMode() && selectedChain != null) {
            previewExecutor.startChainPreview(selectedChain, npc);
        } else if (selectedAbility != null) {
            previewExecutor.startPreview(selectedAbility, npc);
        }
    }

    private void stopPreviewPlayback() {
        previewExecutor.stop();
        trackingMovement = false;
        // Reset NPC animation
        AnimationData data = npc.display.animationData;
        data.setAnimation(new Animation());
    }

    // ========== Preview Status Drawing ==========

    @Override
    protected void drawOverlay(int mouseX, int mouseY, float partialTicks) {
        super.drawOverlay(mouseX, mouseY, partialTicks);

        // Draw preview status text inside the preview area
        if (previewExecutor.isActive() && !hasSubGui()) {
            String status = previewExecutor.getStatusString();
            fontRendererObj.drawString(status, previewX + 4, previewY + previewH - 12, 0xFFFFFF, true);
        }
    }

    @Override
    protected void drawItemDetails(int x, int y, int w) {
        if (isChainedMode() && selectedChain != null) {
            fontRendererObj.drawString(selectedChain.getDisplayName(), x, y, 0xFFFFFF, true);
            y += 14;
            fontRendererObj.drawString("Entries: " + selectedChain.getEntries().size(), x, y, 0xB5B5B5, false);
            y += 12;
            fontRendererObj.drawString("Cooldown: " + selectedChain.getCooldownTicks() + "t", x, y, 0xB5B5B5, false);
            y += 12;
            UserType ut = selectedChain.getAllowedBy();
            fontRendererObj.drawString("Allowed: " + ut.name(), x, y, 0xB5B5B5, false);
        } else if (selectedAbility != null) {
            fontRendererObj.drawString(selectedAbility.getDisplayName(), x, y, 0xFFFFFF, true);
            y += 14;
            String typeId = selectedAbility.getTypeId();
            String typeName = StatCollector.translateToLocal(typeId);
            fontRendererObj.drawString("Type: " + typeName, x, y, 0xFFAE0D, false);
            y += 12;
            fontRendererObj.drawString("Cooldown: " + selectedAbility.getCooldownTicks() + "t", x, y, 0xB5B5B5, false);
            y += 12;
            fontRendererObj.drawString("Windup: " + selectedAbility.getWindUpTicks() + "t", x, y, 0xB5B5B5, false);
            y += 12;
            UserType ut = selectedAbility.getAllowedBy();
            fontRendererObj.drawString("Allowed: " + ut.name(), x, y, 0xB5B5B5, false);
            if (selectedAbility.hasDamage()) {
                y += 12;
                fontRendererObj.drawString("Damage: " + selectedAbility.getDisplayDamage(), x, y, 0xFF5555, false);
            }
            if (currentIsBuiltIn) {
                y += 12;
                fontRendererObj.drawString("(Built-in)", x, y, 0x55FF55, false);
            }
        }
    }

    // ========== Cleanup ==========

    @Override
    public void onGuiClosed() {
        stopPreviewPlayback();
        if (npc != null) {
            npc.display.animationData.setEnabled(false);
        }
        super.onGuiClosed();
    }

    @Override
    public void save() {}
}
