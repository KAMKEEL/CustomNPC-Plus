package noppes.npcs.client.gui.global;

import kamkeel.npcs.client.renderer.TelegraphRenderer;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.SavedAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.SavedAbilityGetPacket;
import kamkeel.npcs.network.packets.request.ability.SavedAbilityRemovePacket;
import kamkeel.npcs.network.packets.request.ability.SavedAbilitySavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
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
 */
public class GuiNpcManageAbilities extends GuiAbilityInterface
        implements ICustomScrollListener, ISubGuiListener, IAbilityConfigCallback,
                   ITextfieldListener, GuiYesNoCallback, IScrollData, IGuiData {

    // ==================== DATA ====================
    private GuiCustomScroll scroll;
    private HashMap<String, Integer> abilityData = new HashMap<>();
    private String selected = null;
    private String search = "";
    private Ability selectedAbility = null;

    // ==================== PREVIEW ====================
    private AbilityPreviewExecutor previewExecutor;
    private long prevTick = 0;

    public GuiNpcManageAbilities(EntityNPCInterface npc) {
        super(npc);

        // Layout offset
        this.xOffset = -148 + 110;
        this.yOffset = -170 + 167;

        // Initialize preview executor
        previewExecutor = new AbilityPreviewExecutor();
        previewExecutor.setParentGui(this);

        // Request ability data from server
        PacketClient.sendClient(new SavedAbilitiesGetPacket());

        // Enable animation data
        AnimationData data = npc.display.animationData;
        data.setEnabled(true);
    }

    @Override
    public void initGui() {
        super.initGui();

        // Edit button (above Remove)
        addButton(new GuiNpcButton(100, guiLeft + 368, guiTop + 8, 45, 20, "gui.edit"));
        getButton(100).setEnabled(selected != null && !selected.isEmpty() && selectedAbility != null);

        // Remove button
        addButton(new GuiNpcButton(1, guiLeft + 368, guiTop + 30, 45, 20, "gui.remove"));
        getButton(1).setEnabled(selected != null && !selected.isEmpty() && abilityData.containsKey(selected));

        // Scroll list of saved abilities (right side)
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 4;
        addScroll(scroll);

        // Update scroll list
        scroll.setList(getSearchList());
        if (selected != null && abilityData.containsKey(selected)) {
            scroll.setSelected(selected);
        }

        // Search bar
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 192, 143, 20, search));

        // Show ability info only if one is selected
        if (selectedAbility == null || selected == null) {
            return;
        }

        // ID/Name label (below search bar)
        addLabel(new GuiNpcLabel(10, "ID: " + selectedAbility.getId(), guiLeft + 220, guiTop + 215));

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

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<>(abilityData.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : abilityData.keySet()) {
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

        if (id == 1 && selected != null && abilityData.containsKey(selected)) {
            // Remove - show confirmation
            GuiYesNo guiyesno = new GuiYesNo(this, selected, StatCollector.translateToLocal("gui.delete"), 1);
            displayGuiScreen(guiyesno);
        } else if (id == 100 && selectedAbility != null) {
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
                // Stop any playing preview
                previewExecutor.stop();

                selected = newSelection;
                // Request ability data from server
                PacketClient.sendClient(new SavedAbilityGetPacket(selected));
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0 && selection != null && !selection.isEmpty()) {
            // Double-click to edit
            if (selectedAbility != null) {
                previewExecutor.stop();
                setSubGui(selectedAbility.createConfigGui(this));
            }
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.ABILITIES) {
            String name = scroll != null ? scroll.getSelected() : null;
            this.abilityData = data;
            if (scroll != null) {
                scroll.setList(getSearchList());
                if (name != null && abilityData.containsKey(name)) {
                    scroll.setSelected(name);
                }
            }
            initGui();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
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
        initGui();
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result) return;

        if (id == 1 && selected != null && abilityData.containsKey(selected)) {
            // Delete ability via packet
            PacketClient.sendClient(new SavedAbilityRemovePacket(selected));
            scroll.clear();
            selected = null;
            selectedAbility = null;
            initGui();
        }
    }

    @Override
    public void onAbilitySaved(Ability ability) {
        // Save the edited ability via packet
        PacketClient.sendClient(new SavedAbilitySavePacket(ability.writeNBT()));
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
