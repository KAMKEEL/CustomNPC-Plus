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
 * Requests animation data from the server via packets.
 */
public class GuiAnimationSelection extends SubGuiInterface implements ICustomScrollListener, IScrollData {

    private GuiCustomScroll scrollAnimations;
    private HashMap<String, Integer> animationData = new HashMap<String, Integer>();

    private int initialAnimationId;
    public int selectedAnimationId = -1;
    private String selectedName = null;

    private String search = "";

    public GuiAnimationSelection(int animationId) {
        drawDefaultBackground = false;
        title = "";
        setBackground("menubg.png");
        xSize = 220;
        ySize = 226;

        this.initialAnimationId = animationId;
        this.selectedAnimationId = animationId;

        // Request animation data from server
        PacketClient.sendClient(new AnimationsGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        this.addLabel(new GuiNpcLabel(0, "gui.animations", guiLeft + 8, guiTop + 4));

        this.addButton(new GuiNpcButton(1, guiLeft + 4, guiTop + ySize - 35, 50, 20, "gui.clear"));
        this.addButton(new GuiNpcButton(2, guiLeft + xSize - 56, guiTop + ySize - 35, 50, 20, "gui.done"));
        this.addButton(new GuiNpcButton(3, guiLeft + xSize - 108, guiTop + ySize - 35, 50, 20, "gui.cancel"));

        if (scrollAnimations == null) {
            scrollAnimations = new GuiCustomScroll(this, 0, 0);
            scrollAnimations.setSize(212, 163);
        }
        scrollAnimations.setList(getSearchList());
        if (selectedName != null) {
            scrollAnimations.setSelected(selectedName);
        }
        scrollAnimations.guiLeft = guiLeft + 4;
        scrollAnimations.guiTop = guiTop + 14;
        this.addScroll(scrollAnimations);

        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 4, guiTop + 179, 212, 20, search));
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        this.animationData = data;

        // Find the name for our initial animation ID
        if (initialAnimationId >= 0) {
            for (String name : data.keySet()) {
                if (data.get(name) == initialAnimationId) {
                    selectedName = name;
                    break;
                }
            }
        }

        if (scrollAnimations != null) {
            scrollAnimations.setList(getSearchList());
            if (selectedName != null) {
                scrollAnimations.setSelected(selectedName);
            }
        }
    }

    @Override
    public void setSelected(String selected) {
        this.selectedName = selected;
        if (scrollAnimations != null) {
            scrollAnimations.setSelected(selected);
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollAnimations.resetScroll();
                scrollAnimations.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<String>(animationData.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : animationData.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            String selected = scrollAnimations.getSelected();
            if (selected != null && animationData.containsKey(selected)) {
                selectedName = selected;
                selectedAnimationId = animationData.get(selected);
            }
        }
        initGui();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (selectedAnimationId < 0)
            return;
        close();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 1) {
            // Clear selection
            selectedName = null;
            selectedAnimationId = -1;
            close();
        }
        if (id == 2) {
            // Done - keep selection
            close();
        }
        if (id == 3) {
            // Cancel - restore initial value
            selectedAnimationId = initialAnimationId;
            close();
        }
    }

    /**
     * Get the name of the selected animation (for display purposes).
     */
    public String getSelectedName() {
        return selectedName;
    }
}
