package noppes.npcs.client.gui.global;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.network.PacketClient;

import java.util.UUID;
import kamkeel.npcs.network.packets.request.ability.BuiltInAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilityRemovePacket;
import kamkeel.npcs.network.packets.request.ability.ChainedAbilitySavePacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilityRemovePacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitySavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.advanced.SubGuiAbilityTypeSelect;
import noppes.npcs.client.gui.advanced.SubGuiAbilityVariantSelect;
import noppes.npcs.client.gui.advanced.SubGuiChainedAbilityConfig;
import noppes.npcs.client.gui.advanced.SubGuiDuplicateNameConfirm;
import kamkeel.npcs.controllers.data.ability.preview.AbilityPreviewExecutor;
import kamkeel.npcs.controllers.data.ability.gui.GuiAbilityInterface;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiTexturedButton;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.IChainedAbilityConfigCallback;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Global GUI for managing saved ability presets with 3D preview.
 * <p>
 * Features:
 * - Left side: 3D preview of NPC with ability effects
 * - Right side: Ability list with search
 * - Play/Pause/Stop buttons to preview abilities
 * - Toggle between Custom and Built-in ability views
 */
public class GuiNpcManageAbilities extends GuiAbilityInterface
    implements ICustomScrollListener, ISubGuiListener, IAbilityConfigCallback,
    IChainedAbilityConfigCallback, ITextfieldListener, GuiYesNoCallback,
    IScrollData, IGuiData {

    // ── Button IDs ────────────────────────────────────────────────────────────
    private static final int BTN_REMOVE = 1;
    private static final int BTN_ADD = 2;
    private static final int BTN_TOGGLE_VIEW = 10;
    private static final int BTN_EDIT = 100;
    private static final int BTN_PREVIEW_PLAY = 91;
    private static final int BTN_PREVIEW_PAUSE = 92;
    private static final int BTN_PREVIEW_STOP = 93;
    private static final int BTN_USER_TYPE_FILTER = 11;

    // ── TextField / Label IDs ─────────────────────────────────────────────────
    private static final int TF_SEARCH = 55;
    private static final int LBL_PREVIEW_STATUS = 90;

    // ── Scroll IDs ────────────────────────────────────────────────────────────
    private static final int SCROLL_MAIN = 0;

    // ── Confirm dialog IDs ────────────────────────────────────────────────────
    private static final int CONFIRM_REMOVE_ABILITY = 1;
    private static final int CONFIRM_REMOVE_CHAIN = 2;

    // ==================== DATA ====================
    private GuiCustomScroll scroll;
    private HashMap<String, Integer> customData = new HashMap<>();
    private HashMap<String, Integer> builtInData = new HashMap<>();
    private String selected = null;
    private String search = "";
    private Ability selectedAbility = null;

    // ==================== VIEW MODE ====================
    // 0 = Custom, 1 = Built-in, 2 = Chained
    private int viewMode = 0;
    private boolean showingBuiltIn = false;
    private boolean showingChained = false;
    private boolean currentIsBuiltIn = false;

    // ==================== USER TYPE FILTER ====================
    // 0 = Both, 1 = NPC Only, 2 = Player Only
    private int userTypeFilter = 0;
    private static final String[] USER_TYPE_FILTER_LABELS = {"gui.both", "gui.npcs", "gui.players"};

    // ==================== CHAINED DATA ====================
    private HashMap<String, Integer> chainedData = new HashMap<>();
    private ChainedAbility selectedChain = null;
    private ChainedAbility pendingSaveChain = null;

    // ==================== PENDING SAVE ====================
    private Ability pendingSaveAbility = null;

    // ==================== PENDING VARIANT ====================
    private String pendingTypeId = null;

    // ==================== PREVIEW ====================
    private AbilityPreviewExecutor previewExecutor;
    private long prevTick = 0;

    public GuiNpcManageAbilities(EntityNPCInterface npc, boolean hasMenuNpc) {
        super(npc, hasMenuNpc);

        // Layout offset
        this.xOffset = -148 + 110;
        this.yOffset = -170 + 167;

        // Initialize preview executor
        previewExecutor = new AbilityPreviewExecutor();
        previewExecutor.setParentGui(this);

        // Request ability data from server (sends both custom and built-in)
        PacketClient.sendClient(new CustomAbilitiesGetPacket());

        // Enable animation data
        AnimationData data = npc.display.animationData;
        data.setEnabled(true);
    }

    @Override
    public void initGui() {
        super.initGui();

        // Fullscreen button - FIRST
        GuiNpcButton fullBtn = new GuiNpcButton(66, guiLeft + 368, guiTop + 8, 45, 20, "gui.fullscreen");
        fullBtn.setTextColor(0x55FF55);
        fullBtn.setHoverText("gui.fullscreen.tooltip");
        addButton(fullBtn);

        // Toggle button — cycles: Custom → Built-in → Chained
        String toggleLabel = showingChained ? "gui.chained" : (showingBuiltIn ? "gui.builtin" : "gui.custom");
        GuiNpcButton toggleBtn = new GuiNpcButton(BTN_TOGGLE_VIEW, guiLeft + 368, guiTop + 36, 45, 20, toggleLabel);
        if (showingChained) toggleBtn.setTextColor(0xFFFF55);
        else if (showingBuiltIn) toggleBtn.setTextColor(0x55FFFF);
        else toggleBtn.setTextColor(0xFFFFFF);
        addButton(toggleBtn);

        if (showingChained) {
            // Chained view buttons
            addButton(new GuiNpcButton(BTN_ADD, guiLeft + 368, guiTop + 60, 45, 20, "gui.add"));
            addButton(new GuiNpcButton(BTN_REMOVE, guiLeft + 368, guiTop + 84, 45, 20, "gui.remove"));
            getButton(BTN_REMOVE).setEnabled(selected != null && !selected.isEmpty() && chainedData.containsKey(selected));
            addButton(new GuiNpcButton(BTN_EDIT, guiLeft + 368, guiTop + 108, 45, 20, "gui.edit"));
            getButton(BTN_EDIT).setEnabled(selected != null && !selected.isEmpty() && selectedChain != null);
        } else if (!showingBuiltIn) {
            // Custom view buttons
            addButton(new GuiNpcButton(BTN_ADD, guiLeft + 368, guiTop + 60, 45, 20, "gui.add"));
            addButton(new GuiNpcButton(BTN_REMOVE, guiLeft + 368, guiTop + 84, 45, 20, "gui.remove"));
            getButton(BTN_REMOVE).setEnabled(selected != null && !selected.isEmpty() && customData.containsKey(selected));
            if (!currentIsBuiltIn) {
                addButton(new GuiNpcButton(BTN_EDIT, guiLeft + 368, guiTop + 108, 45, 20, "gui.edit"));
                getButton(BTN_EDIT).setEnabled(selected != null && !selected.isEmpty() && selectedAbility != null);
            }
        }

        // Scroll list (right side)
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, SCROLL_MAIN);
            scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 4;
        addScroll(scroll);

        scroll.setList(getSearchList());
        if (selected != null) {
            scroll.setSelected(selected);
        }

        // Search bar
        addTextField(new GuiNpcTextField(TF_SEARCH, this, fontRendererObj, guiLeft + 220, guiTop + 192, 143, 20, search));

        // User type filter button (only for Custom and Built-in views)
        if (!showingChained) {
            addButton(new GuiNpcButton(BTN_USER_TYPE_FILTER, guiLeft + 368, guiTop + 192, 45, 20, USER_TYPE_FILTER_LABELS[userTypeFilter]));
        }

        // Check if we have something to preview
        if (showingChained) {
            if (selectedChain == null || selected == null || selectedChain.getEntries().isEmpty()) {
                return;
            }
        } else if (selectedAbility == null || selected == null) {
            return;
        }

        // Play/Pause/Stop buttons (below model preview area)
        String animTexture = "customnpcs:textures/gui/animation.png";
        int playButtonOffsetX = 60;

        boolean isPlaying = previewExecutor.isPlaying() && !previewExecutor.isPaused();
        boolean isPaused = previewExecutor.isPaused();
        boolean isActive = previewExecutor.isActive();

        if (!isPlaying || isPaused) {
            String statusKey = isPaused ? "animation.paused" : "animation.stopped";
            addLabel(new GuiNpcLabel(LBL_PREVIEW_STATUS, statusKey, guiLeft + playButtonOffsetX, guiTop + 198));
            addButton(new GuiTexturedButton(BTN_PREVIEW_PLAY, "", guiLeft + playButtonOffsetX + 70, guiTop + 192, 11, 20, animTexture, 18, 71));
        } else {
            addLabel(new GuiNpcLabel(LBL_PREVIEW_STATUS, "animation.playing", guiLeft + playButtonOffsetX, guiTop + 198));
            addButton(new GuiTexturedButton(BTN_PREVIEW_PAUSE, "", guiLeft + playButtonOffsetX + 70, guiTop + 192, 14, 20, animTexture, 0, 71));
        }
        if (isActive) {
            addButton(new GuiTexturedButton(BTN_PREVIEW_STOP, "", guiLeft + playButtonOffsetX + 90, guiTop + 192, 14, 20, animTexture, 33, 71));
        }
    }

    private HashMap<String, Integer> getCurrentData() {
        if (showingChained) return chainedData;
        return showingBuiltIn ? builtInData : customData;
    }

    private List<String> getSearchList() {
        HashMap<String, Integer> data = getCurrentData();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String name = entry.getKey();

            // Apply search filter
            if (!search.isEmpty() && !name.toLowerCase().contains(search.toLowerCase())) {
                continue;
            }

            // Apply UserType filter (only for Custom and Built-in views)
            if (!showingChained && userTypeFilter != 0) {
                UserType ut = UserType.fromOrdinal(entry.getValue());
                if (userTypeFilter == 1 && !ut.allowsNpc()) continue;
                if (userTypeFilter == 2 && !ut.allowsPlayer()) continue;
            }

            list.add(name);
        }
        return list;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // IMPORTANT: Set NPC rotation BEFORE ticking entities
        // This ensures anchor point calculations use the correct facing direction
        npc.prevRenderYawOffset = npc.renderYawOffset = NPC_FACING_YAW;
        npc.prevRotationYaw = npc.rotationYaw = NPC_FACING_YAW;
        npc.prevRotationYawHead = npc.rotationYawHead = NPC_FACING_YAW;

        // Tick preview executor (entities will use the rotation set above)
        tickPreview();

        // Draw base (includes 3D preview via parent class)
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw status text
        if (previewExecutor.isActive()) {
            String status = previewExecutor.getStatusString();
            fontRendererObj.drawString(status, guiLeft + 12, guiTop + 175, 0xFFFFFF);
        }
    }

    /**
     * Tick the preview executor and animations.
     */
    private void tickPreview() {
        if (previewExecutor.isPlaying() && !previewExecutor.isPaused()) {
            long time = mc.theWorld != null ? mc.theWorld.getTotalWorldTime() : System.currentTimeMillis() / 50;
            if (time != prevTick) {
                // Advance animation
                npc.display.animationData.increaseTime();

                // Tick preview
                previewExecutor.tick();

                // Sync telegraph to GUI for rendering
                setPreviewTelegraph(previewExecutor.getTelegraph());

                prevTick = time;

                // Check if preview finished
                if (!previewExecutor.isActive()) {
                    initGui();
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);

        int id = guibutton.id;

        // Fullscreen
        if (id == 66) {
            previewExecutor.stop();
            mc.displayGuiScreen(new GuiAbilityDirectory(npc));
            return;
        }

        // User type filter: Both → NPC → Player → Both
        if (id == BTN_USER_TYPE_FILTER) {
            userTypeFilter = (userTypeFilter + 1) % 3;
            if (scroll != null) {
                scroll.setList(getSearchList());
            }
            initGui();
            return;
        }

        // Toggle: Custom → Built-in → Chained → Custom
        if (id == BTN_TOGGLE_VIEW) {
            if (!showingBuiltIn && !showingChained) {
                // Custom → Built-in
                showingBuiltIn = true;
                showingChained = false;
            } else if (showingBuiltIn) {
                // Built-in → Chained
                showingBuiltIn = false;
                showingChained = true;
            } else {
                // Chained → Custom
                showingChained = false;
                showingBuiltIn = false;
            }
            selected = null;
            selectedAbility = null;
            selectedChain = null;
            currentIsBuiltIn = false;
            search = "";
            previewExecutor.stop();
            if (scroll != null) {
                scroll.clear();
            }
            if (showingChained) {
                PacketClient.sendClient(new ChainedAbilitiesGetPacket());
            } else {
                PacketClient.sendClient(new CustomAbilitiesGetPacket());
            }
            initGui();
            return;
        }

        // Preview controls (shared across all views)
        if (id == BTN_PREVIEW_PLAY) {
            if (previewExecutor.isPaused()) {
                previewExecutor.play();
            } else if (showingChained && selectedChain != null) {
                previewExecutor.startChainPreview(selectedChain, npc);
            } else if (selectedAbility != null) {
                previewExecutor.startPreview(selectedAbility, npc);
            }
            initGui();
            return;
        } else if (id == BTN_PREVIEW_PAUSE) {
            previewExecutor.pause();
            initGui();
            return;
        } else if (id == BTN_PREVIEW_STOP) {
            previewExecutor.stop();
            initGui();
            return;
        }

        if (showingChained) {
            if (id == BTN_ADD) {
                // New chained ability
                ChainedAbility newChain = new ChainedAbility("New Chain");
                setSubGui(new SubGuiChainedAbilityConfig(newChain, this));
            } else if (id == BTN_REMOVE && selected != null) {
                GuiYesNo guiyesno = new GuiYesNo(this, selected, StatCollector.translateToLocal("gui.delete"), CONFIRM_REMOVE_CHAIN);
                displayGuiScreen(guiyesno);
            } else if (id == BTN_EDIT && selectedChain != null) {
                previewExecutor.stop();
                setSubGui(new SubGuiChainedAbilityConfig(selectedChain, this));
            }
            return;
        }

        if (id == BTN_ADD && !showingBuiltIn) {
            // Add — open type selection
            setSubGui(new SubGuiAbilityTypeSelect());
        } else if (id == BTN_REMOVE && !showingBuiltIn && selected != null) {
            GuiYesNo guiyesno = new GuiYesNo(this, selected, StatCollector.translateToLocal("gui.delete"), CONFIRM_REMOVE_ABILITY);
            displayGuiScreen(guiyesno);
        } else if (id == BTN_EDIT && !showingBuiltIn && !currentIsBuiltIn && selectedAbility != null) {
            // Edit
            previewExecutor.stop();
            setSubGui(selectedAbility.createConfigGui(this));
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(TF_SEARCH) != null && getTextField(TF_SEARCH).isFocused()) {
            if (!search.equals(getTextField(TF_SEARCH).getText())) {
                search = getTextField(TF_SEARCH).getText();
                scroll.resetScroll();
                scroll.setList(getSearchList());
            }
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == SCROLL_MAIN) {
            String newSelection = scroll.getSelected();
            if (newSelection != null && !newSelection.equals(selected)) {
                previewExecutor.stop();
                selected = newSelection;

                if (showingChained) {
                    PacketClient.sendClient(new ChainedAbilityGetPacket(selected));
                } else if (showingBuiltIn) {
                    currentIsBuiltIn = true;
                    PacketClient.sendClient(new BuiltInAbilityGetPacket(selected));
                } else {
                    currentIsBuiltIn = false;
                    PacketClient.sendClient(new CustomAbilityGetPacket(selected));
                }
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == SCROLL_MAIN && selection != null && !selection.isEmpty()) {
            if (showingChained && selectedChain != null) {
                setSubGui(new SubGuiChainedAbilityConfig(selectedChain, this));
            } else if (!showingBuiltIn && !showingChained && !currentIsBuiltIn && selectedAbility != null) {
                previewExecutor.stop();
                setSubGui(selectedAbility.createConfigGui(this));
            }
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.CUSTOM_ABILITIES) {
            String prevSelected = scroll != null ? scroll.getSelected() : null;
            this.customData = data;
            if (!showingBuiltIn && !showingChained && scroll != null) {
                scroll.setList(getSearchList());
                if (prevSelected != null && customData.containsKey(prevSelected)) {
                    scroll.setSelected(prevSelected);
                }
            }
            initGui();
        } else if (type == EnumScrollData.BUILTIN_ABILITIES) {
            this.builtInData = data;
            if (showingBuiltIn && scroll != null) {
                scroll.setList(getSearchList());
                if (selected != null && builtInData.containsKey(selected)) {
                    scroll.setSelected(selected);
                }
            }
            initGui();
        } else if (type == EnumScrollData.CHAINED_ABILITIES) {
            String prevSelected = scroll != null ? scroll.getSelected() : null;
            this.chainedData = data;
            if (showingChained && scroll != null) {
                scroll.setList(getSearchList());
                if (prevSelected != null && chainedData.containsKey(prevSelected)) {
                    scroll.setSelected(prevSelected);
                }
            }
            initGui();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        // Detect chained ability response (has "Entries" tag)
        if (showingChained && compound.hasKey("Entries")) {
            selectedChain = new ChainedAbility();
            selectedChain.readNBT(compound);
            selected = selectedChain.getName();
            if (scroll != null) {
                scroll.setSelected(selected);
            }
            initGui();
            return;
        }

        // Check if this is a built-in ability response
        currentIsBuiltIn = compound.hasKey("BuiltIn") && compound.getBoolean("BuiltIn");

        // Received ability data from server
        selectedAbility = AbilityController.Instance.fromNBT(compound);
        if (selectedAbility != null) {
            selected = selectedAbility.getName();
            if (scroll != null) {
                scroll.setSelected(selected);
            }
        }
        previewExecutor.stop();
        initGui();
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        if (scroll != null) {
            scroll.setSelected(selected);
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiChainedAbilityConfig) {
            handleChainConfigClosed();
            initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityVariantSelect) {
            if (handleVariantSelectClosed((SubGuiAbilityVariantSelect) subgui)) return;
        } else if (subgui instanceof SubGuiAbilityTypeSelect) {
            if (handleTypeSelectClosed((SubGuiAbilityTypeSelect) subgui)) return;
        } else if (subgui instanceof SubGuiDuplicateNameConfirm) {
            if (handleDuplicateNameClosed((SubGuiDuplicateNameConfirm) subgui)) return;
        } else if (pendingSaveAbility != null) {
            if (handlePendingSave()) return;
        }
        initGui();
    }

    private void handleChainConfigClosed() {
        if (pendingSaveChain != null) {
            PacketClient.sendClient(new ChainedAbilitySavePacket(pendingSaveChain.writeNBT(false)));
            selected = pendingSaveChain.getName();
            pendingSaveChain = null;
        }
    }

    /**
     * @return true if a SubGui was opened (caller should return early, skipping initGui)
     */
    private boolean handleVariantSelectClosed(SubGuiAbilityVariantSelect gui) {
        int idx = gui.getSelectedIndex();
        if (idx >= 0 && pendingTypeId != null) {
            Ability newAbility = AbilityController.Instance.create(pendingTypeId);
            if (newAbility != null) {
                gui.getVariants().get(idx).apply(newAbility);
                newAbility.setId(UUID.randomUUID().toString());
                pendingTypeId = null;
                openConfig(newAbility);
                return true;
            }
        }
        pendingTypeId = null;
        return false;
    }

    /**
     * @return true if a SubGui was opened (caller should return early, skipping initGui)
     */
    private boolean handleTypeSelectClosed(SubGuiAbilityTypeSelect gui) {
        String typeId = gui.getSelectedTypeId();
        if (typeId != null) {
            java.util.List<AbilityVariant> variants = AbilityController.Instance.getVariantsForType(typeId);
            if (variants.size() > 1) {
                pendingTypeId = typeId;
                setSubGui(new SubGuiAbilityVariantSelect(variants));
                return true;
            }
            Ability newAbility = AbilityController.Instance.create(typeId);
            if (newAbility != null) {
                if (variants.size() == 1) {
                    variants.get(0).apply(newAbility);
                }
                newAbility.setId(UUID.randomUUID().toString());
                openConfig(newAbility);
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if a SubGui was opened (caller should return early, skipping initGui)
     */
    private boolean handleDuplicateNameClosed(SubGuiDuplicateNameConfirm gui) {
        if (pendingSaveAbility == null) return false;

        if (gui.isConfirmed()) {
            PacketClient.sendClient(new CustomAbilitySavePacket(pendingSaveAbility.writeNBT(false)));
            pendingSaveAbility = null;
            return false;
        }

        if (gui.isBack()) {
            Ability ability = pendingSaveAbility;
            pendingSaveAbility = null;
            setSubGui(ability.createConfigGui(this));
            return true;
        }

        pendingSaveAbility = null;
        return false;
    }

    /**
     * @return true if a SubGui was opened (caller should return early, skipping initGui)
     */
    private boolean handlePendingSave() {
        if (hasDuplicateName(pendingSaveAbility)) {
            setSubGui(new SubGuiDuplicateNameConfirm());
            return true;
        }
        PacketClient.sendClient(new CustomAbilitySavePacket(pendingSaveAbility.writeNBT(false)));
        pendingSaveAbility = null;
        return false;
    }

    private void openConfig(Ability ability) {
        if (ability.isBuiltIn()) return; // Built-in abilities are immutable
        selectedAbility = ability;
        selected = ability.getName();
        previewExecutor.stop();
        setSubGui(ability.createConfigGui(this));
    }

    private boolean hasDuplicateName(Ability ability) {
        String name = ability.getName();
        if (name == null || name.isEmpty()) return false;
        if (!customData.containsKey(name)) return false;
        // Name exists — check if it belongs to a different ability (by UUID)
        String uuid = ability.getId();
        if (uuid == null || uuid.isEmpty()) return true; // New ability with conflicting name
        Ability existing = AbilityController.Instance.getCustomAbilityByName(name);
        return existing == null || !uuid.equals(existing.getId());
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result) return;

        if (id == CONFIRM_REMOVE_ABILITY && selected != null) {
            PacketClient.sendClient(new CustomAbilityRemovePacket(selected));
            scroll.clear();
            selected = null;
            selectedAbility = null;
            initGui();
        } else if (id == CONFIRM_REMOVE_CHAIN && selected != null) {
            PacketClient.sendClient(new ChainedAbilityRemovePacket(selected));
            scroll.clear();
            selected = null;
            selectedChain = null;
            initGui();
        }
    }

    @Override
    public void onAbilitySaved(Ability ability) {
        // Defer save until subGuiClosed where we can check for duplicate names
        pendingSaveAbility = ability;
    }

    @Override
    public void onChainedAbilitySaved(ChainedAbility chain) {
        pendingSaveChain = chain;
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        // Search field handling is done in keyTyped
    }

    @Override
    public void save() {
        // Nothing to save - abilities are saved individually via packets
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Stop preview when GUI closes
        previewExecutor.stop();
    }
}
