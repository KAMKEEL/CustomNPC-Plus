package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.SpawnData;

public class SubGuiSpawningOptions extends SubGuiInterface implements ITextfieldListener {
    private SpawnData data;

    public SubGuiSpawningOptions(SpawnData data){
        this.data = data;
        setBackground("menubg.png");
        xSize = 216;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui() {
        super.initGui();
        int y = guiTop - 15;

        addButton(new GuiNpcButtonYesNo(10, guiLeft + 120, y += 22, data.animalSpawning));
        addLabel(new GuiNpcLabel(10, "spawning.animalSpawning", guiLeft + 4, y + 5));

        addButton(new GuiNpcButtonYesNo(11, guiLeft + 120, y += 22, data.monsterSpawning));
        addLabel(new GuiNpcLabel(11, "spawning.monsterSpawning", guiLeft + 4, y + 5));

        addButton(new GuiNpcButtonYesNo(12, guiLeft + 120, y += 22, data.liquidSpawning));
        addLabel(new GuiNpcLabel(12, "spawning.liquidSpawning", guiLeft + 4, y + 5));

        addButton(new GuiNpcButtonYesNo(13, guiLeft + 120, y += 22, data.airSpawning));
        addLabel(new GuiNpcLabel(13, "spawning.airSpawning", guiLeft + 4, y + 5));

        addTextField(new GuiNpcTextField(14, this, guiLeft + 120, y += 22, 40, 20, String.valueOf(data.spawnHeightMin)));
        addLabel(new GuiNpcLabel(14, "spawning.minHeight", guiLeft + 4, y + 5));
        addTextField(new GuiNpcTextField(15, this, guiLeft + 120, y += 22, 40, 20, String.valueOf(data.spawnHeightMax)));
        addLabel(new GuiNpcLabel(15, "spawning.maxHeight", guiLeft + 4, y + 5));
        getTextField(14).integersOnly = true;
        getTextField(14).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        getTextField(15).integersOnly = true;
        getTextField(15).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

        addButton(new GuiNpcButton(100, guiLeft + 154, guiTop + 192, 60, 20, "gui.done"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;

        if (button.id == 10) {
            data.animalSpawning = button.getValue() == 1;
        }
        if (button.id == 11) {
            data.monsterSpawning = button.getValue() == 1;
        }
        if (button.id == 12) {
            data.liquidSpawning = button.getValue() == 1;
        }
        if (button.id == 13) {
            data.airSpawning = button.getValue() == 1;
        }
        if (button.id == 100) {
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 14) {
            data.spawnHeightMin = textfield.getInteger();
        }
        if (textfield.id == 15) {
            data.spawnHeightMax = textfield.getInteger();
        }
    }
}
