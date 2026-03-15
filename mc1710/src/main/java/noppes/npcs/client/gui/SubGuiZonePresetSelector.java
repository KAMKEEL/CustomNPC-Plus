package noppes.npcs.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.SubGuiInterface;

@SideOnly(Side.CLIENT)
public class SubGuiZonePresetSelector extends SubGuiInterface {

    public String selectedPreset = null;

    private static final String[] PRESET_NAMES = {"DEFAULT", "TOXIC", "INFERNO", "ARCANE", "ELECTRIC", "FROST"};
    private static final String[] PRESET_LABELS = {
        "ability.preset.default", "ability.preset.toxic", "ability.preset.inferno",
        "ability.preset.arcane", "ability.preset.electric", "ability.preset.frost"
    };
    private static final int[] PRESET_COLORS = {0xCCCCCC, 0x44DD44, 0xFF6611, 0xAA44FF, 0x4488FF, 0x88CCFF};

    public SubGuiZonePresetSelector() {
        xSize = 170;
        ySize = 135;
        setBackground("menubg.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        int btnW = 72;
        int btnH = 20;
        int gapY = 4;
        int col1 = guiLeft + 7;
        int col2 = col1 + btnW + 6;
        int startY = guiTop + 22;

        for (int i = 0; i < 6; i++) {
            int x = (i % 2 == 0) ? col1 : col2;
            int y = startY + (i / 2) * (btnH + gapY);
            addButton(new GuiNpcButton(i, x, y, btnW, btnH, PRESET_LABELS[i]));
        }

        int cancelY = startY + 3 * (btnH + gapY) + 4;
        addButton(new GuiNpcButton(10, guiLeft + xSize / 2 - 36, cancelY, 72, btnH, "gui.cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);
        if (btn.id >= 0 && btn.id < 6) {
            selectedPreset = PRESET_NAMES[btn.id];
            close();
        } else if (btn.id == 10) {
            close();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.applyPreset"), guiLeft + xSize / 2, guiTop + 8, 0xFFFFFF);
        for (int i = 0; i < 6; i++) {
            GuiNpcButton btn = getButton(i);
            if (btn != null && btn.visible) {
                int x = btn.xPosition + 4;
                int y = btn.yPosition + 5;
                drawRect(x, y, x + 10, y + 10, 0xFF000000 | PRESET_COLORS[i]);
            }
        }
    }
}
