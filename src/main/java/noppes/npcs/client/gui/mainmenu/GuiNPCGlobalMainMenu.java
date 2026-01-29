package noppes.npcs.client.gui.mainmenu;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNpcManagePlayerData;
import noppes.npcs.client.gui.global.GuiNpcNaturalSpawns;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcSquareButton;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class GuiNPCGlobalMainMenu extends GuiNPCInterface2 {

    // Store all composite buttons keyed by their IDs.
    private final Map<Integer, GuiNpcSquareButton> buttonMap = new HashMap<>();

    private GuiNpcSquareButton bankButton;
    private GuiNpcSquareButton factionButton;
    private GuiNpcSquareButton dialogButton;
    private GuiNpcSquareButton questButton;
    private GuiNpcSquareButton transportButton;
    private GuiNpcSquareButton playerdataButton;
    private GuiNpcSquareButton recipeButton;
    private GuiNpcSquareButton spawnButton;
    private GuiNpcSquareButton linkedButton;
    private GuiNpcSquareButton animationButton;
    private GuiNpcSquareButton tagButton;
    private GuiNpcSquareButton effectsButton;
    private GuiNpcSquareButton magicsButton;
    private GuiNpcSquareButton abilitiesButton;
    private GuiNpcSquareButton auctionsButton;

    public GuiNPCGlobalMainMenu(EntityNPCInterface npc) {
        super(npc, 5);
    }

    /**
     * Register a new composite button.
     */
    public void registerButton(GuiNpcSquareButton button) {
        buttonMap.put(button.id, button);
    }

    @Override
    public void initGui() {
        super.initGui();

        // Create and register composite buttons.
        registerButton(bankButton = new GuiNpcSquareButton(2, 0, 0, 20, "global.banks", 0xFF333333));
        bankButton.setIconPos(24, 24, 72, 32).setIconTexture(specialIcons);

        registerButton(factionButton = new GuiNpcSquareButton(3, 0, 0, 20, "menu.factions", 0xFF333333));
        factionButton.setIconPos(24, 24, 0, 32).setIconTexture(specialIcons);

        registerButton(dialogButton = new GuiNpcSquareButton(4, 0, 0, 20, "dialog.dialogs", 0xFF333333));
        dialogButton.setIconPos(24, 24, 24, 32).setIconTexture(specialIcons);

        registerButton(questButton = new GuiNpcSquareButton(11, 0, 0, 20, "quest.quests", 0xFF333333));
        questButton.setIconPos(24, 24, 0, 56).setIconTexture(specialIcons);

        registerButton(transportButton = new GuiNpcSquareButton(12, 0, 0, 20, "global.transport", 0xFF333333));
        transportButton.setIconPos(24, 24, 96, 32).setIconTexture(specialIcons);

        registerButton(playerdataButton = new GuiNpcSquareButton(13, 0, 0, 20, "global.playerdata", 0xFF333333));
        playerdataButton.setIconPos(24, 24, 216, 32).setIconTexture(specialIcons);

        registerButton(recipeButton = new GuiNpcSquareButton(14, 0, 0, 20, "global.recipes", 0xFF333333));
        recipeButton.setIconPos(24, 24, 48, 32).setIconTexture(specialIcons);

        registerButton(spawnButton = new GuiNpcSquareButton(15, 0, 0, 20, NoppesStringUtils.translate("global.naturalspawn"), 0xFF333333));
        spawnButton.setIconPos(24, 24, 144, 32).setIconTexture(specialIcons);

        registerButton(linkedButton = new GuiNpcSquareButton(16, 0, 0, 20, "global.linked", 0xFF333333));
        linkedButton.setIconPos(24, 24, 168, 32).setIconTexture(specialIcons);

        registerButton(animationButton = new GuiNpcSquareButton(18, 0, 0, 20, "menu.animations", 0xFF333333));
        animationButton.setIconPos(24, 24, 120, 32).setIconTexture(specialIcons);

        registerButton(tagButton = new GuiNpcSquareButton(17, 0, 0, 20, "menu.tags", 0xFF333333));
        tagButton.setIconPos(24, 24, 192, 32).setIconTexture(specialIcons);

        registerButton(effectsButton = new GuiNpcSquareButton(19, 0, 0, 20, "global.customeffects", 0xFF333333));
        effectsButton.setIconPos(24, 24, 24, 56).setIconTexture(specialIcons);

        registerButton(magicsButton = new GuiNpcSquareButton(20, 0, 0, 20, "global.magic", 0xFF333333));
        magicsButton.setIconPos(24, 24, 48, 56).setIconTexture(specialIcons);

        registerButton(abilitiesButton = new GuiNpcSquareButton(21, 0, 0, 20, "global.abilities", 0xFF333333));
        abilitiesButton.setIconPos(24, 24, 72, 56).setIconTexture(specialIcons);

        registerButton(auctionsButton = new GuiNpcSquareButton(22, 0, 0, 20, "global.auction", 0xFF333333));
        auctionsButton.setIconPos(24, 24, 96, 56).setIconTexture(specialIcons);

        // Layout composite buttons optimally.
        layoutButtons();
        // Add all composite buttons to the GUI.
        for (GuiNpcButton btn : buttonMap.values()) {
            this.addButton(btn);
        }
    }

    /**
     * Compute the optimal layout by iterating over candidate row counts from 1 to total.
     * For each candidate:
     * - Let c = ceil(total / r)
     * - candidateHoriz = (availWidth - (c+1)*padding) / c
     * - candidateVert  = (availHeight - (r+1)*padding) / r
     * - candidate = min(candidateHoriz, candidateVert)
     * - usedWidth = candidate * c + (c+1)*padding
     * - usedHeight = candidate * r + (r+1)*padding
     * - ratio = (usedWidth * usedHeight) / (availWidth * availHeight)
     * <p>
     * We choose the candidate (r, candidate) that maximizes this ratio.
     *
     * @param total   Total number of buttons.
     * @param padding Padding in pixels.
     * @return An array {optimalRows, candidateSize}.
     */
    private int[] computeOptimalLayout(int total, int padding) {
        int availWidth = xSize - 2 * padding;
        int availHeight = ySize - 2 * padding;
        double bestRatio = -1;
        int bestRows = 1;
        int bestCandidate = 0;
        for (int r = 1; r <= total; r++) {
            int cols = (int) Math.ceil(total / (double) r);
            int candidateHoriz = (availWidth - (cols + 1) * padding) / cols;
            int candidateVert = (availHeight - (r + 1) * padding) / r;
            int candidate = Math.min(candidateHoriz, candidateVert);
            if (candidate <= 0)
                continue;
            int usedWidth = candidate * cols + (cols + 1) * padding;
            int usedHeight = candidate * r + (r + 1) * padding;
            double ratio = (double) (usedWidth * usedHeight) / (availWidth * availHeight);
            if (ratio > bestRatio || (Math.abs(ratio - bestRatio) < 0.0001 && candidate > bestCandidate)) {
                bestRatio = ratio;
                bestCandidate = candidate;
                bestRows = r;
            }
        }
        if (bestCandidate <= 0) {
            bestCandidate = availHeight - 2 * padding;
            bestRows = 1;
        }
        return new int[]{bestRows, bestCandidate};
    }

    /**
     * Layout composite buttons using the optimal configuration.
     */
    private void layoutButtons() {
        List<GuiNpcSquareButton> buttons = new ArrayList<>(buttonMap.values());
        Collections.sort(buttons, new Comparator<GuiNpcSquareButton>() {
            @Override
            public int compare(GuiNpcSquareButton b1, GuiNpcSquareButton b2) {
                return Integer.compare(b1.id, b2.id);
            }
        });
        int total = buttons.size();
        if (total == 0)
            return;
        int padding = 2;
        int[] optimal = computeOptimalLayout(total, padding);
        int rows = optimal[0];
        int buttonSize = optimal[1]; // composite button is square with side "buttonSize"

        // Distribute buttons evenly among the rows.
        int base = total / rows;
        int extra = total % rows;
        int[] rowCounts = new int[rows];
        for (int i = 0; i < rows; i++) {
            rowCounts[i] = base + (i < extra ? 1 : 0);
        }
        int totalHeight = rows * buttonSize + (rows + 1) * padding;
        int startY = guiTop + (ySize - totalHeight) / 2;
        int index = 0;
        for (int row = 0; row < rows; row++) {
            int count = rowCounts[row];
            int rowWidth = count * buttonSize + (count + 1) * padding;
            int rowStartX = guiLeft + (xSize - rowWidth) / 2;
            int yPos = startY + padding + row * (buttonSize + padding);
            for (int col = 0; col < count; col++) {
                if (index >= total)
                    break;
                GuiNpcSquareButton btn = buttons.get(index);
                btn.updatePositionAndSize(rowStartX + padding + col * (buttonSize + padding), yPos, buttonSize);
                index++;
            }
        }
    }


    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 11) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageQuests);
        }
        if (id == 2) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageBanks);
        }
        if (id == 3) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageFactions);
        }
        if (id == 17) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageTags);
        }
        if (id == 18) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageAnimations);
        }
        if (id == 4) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageDialogs);
        }
        if (id == 12) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport);
        }
        if (id == 13) {
            NoppesUtil.openGUI(player, new GuiNpcManagePlayerData(npc));
        }
        if (id == 14) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 4, 0, 0);
        }
        if (id == 15) {
            NoppesUtil.openGUI(player, new GuiNpcNaturalSpawns(npc));
        }
        if (id == 16) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageLinked);
        }
        if (id == 20) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageMagic);
        }
        if (id == 19) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageEffects);
        }
        if (id == 21) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageAbilities);
        }
        if (id == 22) {
            // NoppesUtil.requestOpenGUI(EnumGuiType.ManageAuction);
        }
    }

    @Override
    public void save() {
        // No saving logic needed.
    }
}
