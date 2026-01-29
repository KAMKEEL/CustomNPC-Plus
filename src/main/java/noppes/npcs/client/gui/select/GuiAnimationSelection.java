package noppes.npcs.client.gui.select;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.animation.AnimationsGetPacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Animation selection dialog for choosing global animations.
 * Toggle between Built-in animations (by name) and Custom animations (by ID).
 */
public class GuiAnimationSelection extends SubGuiInterface implements ICustomScrollListener, IScrollData {

    private GuiCustomScroll scrollAnimations;

    // Built-in animations (name only, no ID)
    private HashMap<String, Integer> builtInData = new HashMap<>();
    // Custom/user animations (name -> ID)
    private HashMap<String, Integer> customData = new HashMap<>();

    // View mode: true = built-in, false = custom
    private boolean showingBuiltIn = true;

    // Initial values passed in
    private int initialAnimationId;
    private String initialAnimationName;

    // Selected values to return
    public int selectedAnimationId = -1;
    public String selectedBuiltInName = "";

    private String selectedCustomName = null;
    private String search = "";

    /**
     * Constructor for selecting by ID (user animations).
     */
    public GuiAnimationSelection(int animationId) {
        this(animationId, "");
    }

    /**
     * Constructor for selecting by name (built-in) or ID (user).
     */
    public GuiAnimationSelection(int animationId, String animationName) {
        drawDefaultBackground = false;
        title = "";
        setBackground("menubg.png");
        xSize = 220;
        ySize = 226;

        this.initialAnimationId = animationId;
        this.initialAnimationName = animationName != null ? animationName : "";
        this.selectedAnimationId = animationId;
        this.selectedBuiltInName = this.initialAnimationName;

        // Start showing built-in if we have a built-in name, otherwise custom
        this.showingBuiltIn = !this.initialAnimationName.isEmpty();

        // Request animation data from server
        PacketClient.sendClient(new AnimationsGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 4;

        // Toggle button for switching views
        String viewLabel = showingBuiltIn ? "gui.builtinAnimations" : "gui.customAnimations";
        this.addButton(new GuiNpcButton(10, guiLeft + 4, y, 212, 20, viewLabel));
        y += 24;

        // Animation scroll list
        if (scrollAnimations == null) {
            scrollAnimations = new GuiCustomScroll(this, 0, 0);
            scrollAnimations.setSize(212, 130);
        }
        scrollAnimations.setList(getSearchList());

        // Set selection based on current mode
        if (showingBuiltIn && !selectedBuiltInName.isEmpty()) {
            scrollAnimations.setSelected(selectedBuiltInName);
        } else if (!showingBuiltIn && selectedCustomName != null) {
            scrollAnimations.setSelected(selectedCustomName);
        }

        scrollAnimations.guiLeft = guiLeft + 4;
        scrollAnimations.guiTop = y;
        this.addScroll(scrollAnimations);
        y += 134;

        // Search textbox
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 4, y, 212, 20, search));
        y += 24;

        // Buttons
        this.addButton(new GuiNpcButton(1, guiLeft + 4, y, 50, 20, "gui.clear"));
        this.addButton(new GuiNpcButton(3, guiLeft + xSize - 108, y, 50, 20, "gui.cancel"));
        this.addButton(new GuiNpcButton(2, guiLeft + xSize - 56, y, 50, 20, "gui.done"));
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.BUILTIN_ANIMATIONS) {
            this.builtInData = data;

            // Check if initial name matches a built-in animation
            if (!initialAnimationName.isEmpty()) {
                for (String name : builtInData.keySet()) {
                    if (name.equalsIgnoreCase(initialAnimationName)) {
                        selectedBuiltInName = name;
                        selectedAnimationId = -1;
                        selectedCustomName = null;
                        showingBuiltIn = true;
                        break;
                    }
                }
            }
        } else if (type == EnumScrollData.ANIMATIONS) {
            this.customData = data;

            // Find the name for our initial animation ID (if not using built-in)
            if (selectedBuiltInName.isEmpty() && initialAnimationId >= 0) {
                for (String name : data.keySet()) {
                    if (data.get(name) == initialAnimationId) {
                        selectedCustomName = name;
                        showingBuiltIn = false;
                        break;
                    }
                }
            }
        }

        if (scrollAnimations != null) {
            scrollAnimations.setList(getSearchList());
            if (showingBuiltIn && !selectedBuiltInName.isEmpty()) {
                scrollAnimations.setSelected(selectedBuiltInName);
            } else if (!showingBuiltIn && selectedCustomName != null) {
                scrollAnimations.setSelected(selectedCustomName);
            }
        }
    }

    @Override
    public void setSelected(String selected) {
        // Not used in this implementation
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                if (scrollAnimations != null) {
                    scrollAnimations.resetScroll();
                    scrollAnimations.setList(getSearchList());
                }
            }
        }
    }

    private List<String> getSearchList() {
        HashMap<String, Integer> sourceData = showingBuiltIn ? builtInData : customData;

        if (search.isEmpty()) {
            return new ArrayList<>(sourceData.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : sourceData.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        String selected = scrollAnimations.getSelected();
        if (selected == null) return;

        if (showingBuiltIn) {
            if (builtInData.containsKey(selected)) {
                selectedBuiltInName = selected;
                selectedAnimationId = -1;
                selectedCustomName = null;
            }
        } else {
            if (customData.containsKey(selected)) {
                selectedCustomName = selected;
                selectedAnimationId = customData.get(selected);
                selectedBuiltInName = "";
            }
        }
        initGui();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        // Double-click to confirm selection
        if (!selectedBuiltInName.isEmpty() || selectedAnimationId >= 0) {
            close();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        // Toggle view button
        if (id == 10) {
            showingBuiltIn = !showingBuiltIn;
            search = "";
            if (scrollAnimations != null) {
                scrollAnimations.resetScroll();
            }
            initGui();
        }
        // Clear
        else if (id == 1) {
            selectedBuiltInName = "";
            selectedAnimationId = -1;
            selectedCustomName = null;
            close();
        }
        // Done
        else if (id == 2) {
            close();
        }
        // Cancel
        else if (id == 3) {
            selectedAnimationId = initialAnimationId;
            selectedBuiltInName = initialAnimationName;
            close();
        }
    }

    /**
     * Get the name of the selected animation (for display purposes).
     */
    public String getSelectedName() {
        if (!selectedBuiltInName.isEmpty()) {
            return selectedBuiltInName;
        }
        return selectedCustomName;
    }

    /**
     * Check if a built-in animation was selected.
     */
    public boolean isBuiltInSelected() {
        return !selectedBuiltInName.isEmpty();
    }
}
