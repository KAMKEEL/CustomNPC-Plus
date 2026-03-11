package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;

/**
 * Choice dialog for adding an entry to a chain.
 * Options: By Reference, Create New, Built-in, From NPC Slots (optional).
 */
public class SubGuiChainedEntrySource extends SubGuiSimpleChoice {

    public static final int SOURCE_NONE = -1;
    public static final int SOURCE_NPC_SLOTS = 0;
    public static final int SOURCE_LOAD_PRESET = 1;
    public static final int SOURCE_CREATE_NEW = 2;
    public static final int SOURCE_BUILT_IN = 3;

    private final boolean showNpcOption;

    public SubGuiChainedEntrySource() {
        this(true);
    }

    public SubGuiChainedEntrySource(boolean showNpcOption) {
        this.showNpcOption = showNpcOption;
        setBackground("menubg.png");
        xSize = 200;
        ySize = showNpcOption ? 132 : 110;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0, "ability.entrySource", guiLeft + 10, y));

        y += 20;
        addButton(new GuiNpcButton(SOURCE_LOAD_PRESET, guiLeft + 10, y, 180, 20, "ability.byReference"));

        y += 22;
        addButton(new GuiNpcButton(SOURCE_CREATE_NEW, guiLeft + 10, y, 180, 20, "ability.createNew"));

        y += 22;
        addButton(new GuiNpcButton(SOURCE_BUILT_IN, guiLeft + 10, y, 180, 20, "gui.builtin"));

        if (showNpcOption) {
            y += 22;
            addButton(new GuiNpcButton(SOURCE_NPC_SLOTS, guiLeft + 10, y, 180, 20, "ability.fromNpc"));
        }
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id >= 0 && id <= 3) {
            setResult(id);
            close();
        }
    }
}
