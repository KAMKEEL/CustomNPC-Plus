package noppes.npcs.client.gui.global;

import kamkeel.npcs.client.renderer.TelegraphRenderer;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.BuiltInAbilityGetPacket;
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
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.SubGuiAbilityTypeSelect;
import noppes.npcs.client.gui.advanced.SubGuiAbilityVariantSelect;
import noppes.npcs.client.gui.advanced.SubGuiDuplicateNameConfirm;
import noppes.npcs.client.gui.util.AbilityPreviewExecutor;
import noppes.npcs.client.gui.util.GuiAbilityInterface;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiTexturedButton;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
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
import java.util.Vector;

/**
 * Global GUI for managing saved ability presets with 3D preview.
 *
 * Features:
 * - Left side: 3D preview of NPC with ability effects
 * - Right side: Ability list with search
 * - Play/Pause/Stop buttons to preview abilities
 * - Toggle between Custom and Built-in ability views
 */
public class GuiNpcManageAbilities extends GuiAbilityInterface
        implements ICustomScrollListener, ISubGuiListener, IAbilityConfigCallback,
                   ITextfieldListener, GuiYesNoCallback, IScrollData, IGuiData {

    // ==================== DATA ====================
    private GuiCustomScroll scroll;
    private HashMap<String, Integer> customData = new HashMap<>();
    private HashMap<String, Integer> builtInData = new HashMap<>();
    private String selected = null;
    private String search = "";
    private Ability selectedAbility = null;

    // ==================== BUILT-IN TOGGLE ====================
    private boolean showingBuiltIn = false;
    private boolean currentIsBuiltIn = false;

    // ==================== PENDING SAVE ====================
    private Ability pendingSaveAbility = null;
    private boolean pendingNewCreation = false;

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

        // Toggle button — added FIRST to maintain stable buttonList index
        String toggleLabel = showingBuiltIn ? "gui.builtin" : "gui.custom";
        addButton(new GuiNpcButton(10, guiLeft + 368, guiTop + 52, 45, 20, toggleLabel));

        if (!showingBuiltIn) {
            // Add button — only for custom view
            addButton(new GuiNpcButton(2, guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));

            // Remove button — only for custom view
            addButton(new GuiNpcButton(1, guiLeft + 368, guiTop + 30, 45, 20, "gui.remove"));
            getButton(1).setEnabled(selected != null && !selected.isEmpty() && customData.containsKey(selected));
        }

        // Edit button — hidden when viewing built-in
        if (!showingBuiltIn && !currentIsBuiltIn) {
            addButton(new GuiNpcButton(100, guiLeft + 368, guiTop + 74, 45, 20, "gui.edit"));
            getButton(100).setEnabled(selected != null && !selected.isEmpty() && selectedAbility != null);
        }

        // Scroll list of abilities (right side)
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 4;
        addScroll(scroll);

        // Update scroll list
        scroll.setList(getSearchList());
        if (selected != null) {
            scroll.setSelected(selected);
        }

        // Search bar
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 192, 143, 20, search));

        // Show ability info only if one is selected
        if (selectedAbility == null || selected == null) {
            return;
        }

        // Play/Pause/Stop buttons (below model preview area)
        String animTexture = "customnpcs:textures/gui/animation.png";
        int playButtonOffsetX = 60;

        boolean isPlaying = previewExecutor.isPlaying() && !previewExecutor.isPaused();
        boolean isPaused = previewExecutor.isPaused();
        boolean isActive = previewExecutor.isActive();

        if (!isPlaying || isPaused) {
            // Show Play button
            String statusKey = isPaused ? "animation.paused" : "animation.stopped";
            addLabel(new GuiNpcLabel(90, statusKey, guiLeft + playButtonOffsetX, guiTop + 198));
            addButton(new GuiTexturedButton(91, "", guiLeft + playButtonOffsetX + 70, guiTop + 192, 11, 20, animTexture, 18, 71));
        } else {
            // Show Pause button
            addLabel(new GuiNpcLabel(90, "animation.playing", guiLeft + playButtonOffsetX, guiTop + 198));
            addButton(new GuiTexturedButton(92, "", guiLeft + playButtonOffsetX + 70, guiTop + 192, 14, 20, animTexture, 0, 71));
        }
        if (isActive) {
            // Show Stop button
            addButton(new GuiTexturedButton(93, "", guiLeft + playButtonOffsetX + 90, guiTop + 192, 14, 20, animTexture, 33, 71));
        }
    }

    private HashMap<String, Integer> getCurrentData() {
        return showingBuiltIn ? builtInData : customData;
    }

    private List<String> getSearchList() {
        HashMap<String, Integer> data = getCurrentData();
        if (search.isEmpty()) {
            return new ArrayList<>(data.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : data.keySet()) {
            if (name.toLowerCase().contains(search.toLowerCase())) {
                list.add(name);
            }
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

        // Toggle between custom and built-in views
        if (id == 10) {
            showingBuiltIn = !showingBuiltIn;
            selected = null;
            selectedAbility = null;
            currentIsBuiltIn = false;
            search = "";
            previewExecutor.stop();
            if (scroll != null) {
                scroll.clear();
            }
            PacketClient.sendClient(new CustomAbilitiesGetPacket());
            initGui();
            return;
        }

        if (id == 2 && !showingBuiltIn) {
            // Add — open type selection
            setSubGui(new SubGuiAbilityTypeSelect());
        } else if (id == 1 && !showingBuiltIn && selected != null) {
            GuiYesNo guiyesno = new GuiYesNo(this, selected, StatCollector.translateToLocal("gui.delete"), 1);
            displayGuiScreen(guiyesno);
        } else if (id == 100 && !showingBuiltIn && !currentIsBuiltIn && selectedAbility != null) {
            // Edit
            previewExecutor.stop();
            setSubGui(selectedAbility.createConfigGui(this));
        } else if (id == 91) {
            // Play button
            if (selectedAbility != null) {
                if (previewExecutor.isPaused()) {
                    previewExecutor.play();
                } else {
                    previewExecutor.startPreview(selectedAbility, npc);
                }
                initGui();
            }
        } else if (id == 92) {
            // Pause button
            previewExecutor.pause();
            initGui();
        } else if (id == 93) {
            // Stop button
            previewExecutor.stop();
            initGui();
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null && getTextField(55).isFocused()) {
            if (!search.equals(getTextField(55).getText())) {
                search = getTextField(55).getText();
                scroll.resetScroll();
                scroll.setList(getSearchList());
            }
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            String newSelection = scroll.getSelected();
            if (newSelection != null && !newSelection.equals(selected)) {
                previewExecutor.stop();
                selected = newSelection;

                if (showingBuiltIn) {
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
        if (scroll.id == 0 && selection != null && !selection.isEmpty()) {
            // Double-click to edit — only for custom, non-built-in abilities
            if (!showingBuiltIn && !currentIsBuiltIn && selectedAbility != null) {
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
            if (!showingBuiltIn && scroll != null) {
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
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
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
        if (subgui instanceof SubGuiAbilityVariantSelect) {
            SubGuiAbilityVariantSelect variantGui = (SubGuiAbilityVariantSelect) subgui;
            int idx = variantGui.getSelectedIndex();
            if (idx >= 0 && pendingTypeId != null) {
                Ability newAbility = AbilityController.Instance.create(pendingTypeId);
                if (newAbility != null) {
                    variantGui.getVariants().get(idx).apply(newAbility);
                    if (hasDuplicateName(newAbility)) {
                        pendingSaveAbility = newAbility;
                        pendingNewCreation = true;
                        pendingTypeId = null;
                        setSubGui(new SubGuiDuplicateNameConfirm());
                        return;
                    }
                    pendingTypeId = null;
                    openConfig(newAbility);
                    return;
                }
            }
            pendingTypeId = null;
        } else if (subgui instanceof SubGuiAbilityTypeSelect) {
            String typeId = ((SubGuiAbilityTypeSelect) subgui).getSelectedTypeId();
            if (typeId != null) {
                java.util.List<AbilityVariant> variants = AbilityController.Instance.getVariantsForType(typeId);
                if (variants.size() > 1) {
                    pendingTypeId = typeId;
                    setSubGui(new SubGuiAbilityVariantSelect(variants));
                    return;
                }
                Ability newAbility = AbilityController.Instance.create(typeId);
                if (newAbility != null) {
                    if (variants.size() == 1) {
                        variants.get(0).apply(newAbility);
                    }
                    if (hasDuplicateName(newAbility)) {
                        pendingSaveAbility = newAbility;
                        pendingNewCreation = true;
                        setSubGui(new SubGuiDuplicateNameConfirm());
                        return;
                    }
                    openConfig(newAbility);
                    return;
                }
            }
        } else if (subgui instanceof SubGuiDuplicateNameConfirm) {
            SubGuiDuplicateNameConfirm confirm = (SubGuiDuplicateNameConfirm) subgui;
            if (confirm.isConfirmed() && pendingSaveAbility != null) {
                if (pendingNewCreation) {
                    openConfig(pendingSaveAbility);
                    pendingSaveAbility = null;
                    pendingNewCreation = false;
                    return;
                } else {
                    PacketClient.sendClient(new CustomAbilitySavePacket(pendingSaveAbility.writeNBT()));
                    PacketClient.sendClient(new CustomAbilitiesGetPacket());
                    pendingSaveAbility = null;
                }
            } else if (pendingSaveAbility != null) {
                if (pendingNewCreation) {
                    // Cancel new creation - discard
                    pendingSaveAbility = null;
                    pendingNewCreation = false;
                } else {
                    // Cancel edit save - re-open config GUI to change name
                    Ability ability = pendingSaveAbility;
                    pendingSaveAbility = null;
                    setSubGui(ability.createConfigGui(this));
                    return;
                }
            }
        } else if (pendingSaveAbility != null) {
            // Config GUI closed with a pending save (set by onAbilitySaved)
            if (hasDuplicateName(pendingSaveAbility)) {
                pendingNewCreation = false;
                setSubGui(new SubGuiDuplicateNameConfirm());
                return;
            }
            PacketClient.sendClient(new CustomAbilitySavePacket(pendingSaveAbility.writeNBT()));
            PacketClient.sendClient(new CustomAbilitiesGetPacket());
            pendingSaveAbility = null;
        }
        initGui();
    }

    private void openConfig(Ability ability) {
        selectedAbility = ability;
        selected = ability.getName();
        previewExecutor.stop();
        setSubGui(ability.createConfigGui(this));
    }

    private boolean hasDuplicateName(Ability ability) {
        String name = ability.getName();
        if (name == null || name.isEmpty()) return false;
        String oldName = ability.getId();
        // A duplicate exists if the name is already in use and it's not the same ability being edited
        return customData.containsKey(name) && !name.equals(oldName);
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result) return;

        if (id == 1 && selected != null) {
            PacketClient.sendClient(new CustomAbilityRemovePacket(selected));
            scroll.clear();
            selected = null;
            selectedAbility = null;
            initGui();
        }
    }

    @Override
    public void onAbilitySaved(Ability ability) {
        // Defer save until subGuiClosed where we can check for duplicate names
        pendingSaveAbility = ability;
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
