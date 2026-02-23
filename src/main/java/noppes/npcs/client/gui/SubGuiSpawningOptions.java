package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.SpawnData;

public class SubGuiSpawningOptions extends SubGuiInterface implements ITextfieldListener {
    private final SpawnData data;

    public SubGuiSpawningOptions(SpawnData data) {
        this.data = data;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui() {
        super.initGui();
        int scrollX = guiLeft + 4;
        int scrollY = guiTop + 4;
        int scrollW = xSize - 8;
        int scrollH = ySize - 34;

        GuiScrollWindow sw = new GuiScrollWindow(this, scrollX, scrollY, scrollW, scrollH, 0);
        sw.drawDefaultBackground = false;
        addScrollableGui(0, sw);

        int labelX = 4;
        int controlX = 132;
        int y = 6;

        sw.addButton(new GuiNpcButtonYesNo(10, controlX, y, data.animalSpawning));
        sw.addLabel(new GuiNpcLabel(10, "spawning.animalSpawning", labelX, y + 5));
        sw.getButton(10).setHoverText("tooltip.naturalspawns.options.animal");
        y += 22;

        sw.addButton(new GuiNpcButtonYesNo(11, controlX, y, data.monsterSpawning));
        sw.addLabel(new GuiNpcLabel(11, "spawning.monsterSpawning", labelX, y + 5));
        sw.getButton(11).setHoverText("tooltip.naturalspawns.options.monster");
        y += 22;

        sw.addButton(new GuiNpcButtonYesNo(12, controlX, y, data.liquidSpawning));
        sw.addLabel(new GuiNpcLabel(12, "spawning.liquidSpawning", labelX, y + 5));
        sw.getButton(12).setHoverText("tooltip.naturalspawns.options.liquid");
        y += 22;

        sw.addButton(new GuiNpcButtonYesNo(13, controlX, y, data.airSpawning));
        sw.addLabel(new GuiNpcLabel(13, "spawning.airSpawning", labelX, y + 5));
        sw.getButton(13).setHoverText("tooltip.naturalspawns.options.air");
        y += 22;

        sw.addTextField(new GuiNpcTextField(14, this, controlX, y, 60, 20, "" + data.spawnHeightMin));
        sw.addLabel(new GuiNpcLabel(14, "spawning.minHeight", labelX, y + 5));
        sw.getTextField(14).integersOnly = true;
        sw.getTextField(14).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        sw.getTextField(14).setHoverText("tooltip.naturalspawns.options.minHeight");
        y += 22;

        sw.addTextField(new GuiNpcTextField(15, this, controlX, y, 60, 20, "" + data.spawnHeightMax));
        sw.addLabel(new GuiNpcLabel(15, "spawning.maxHeight", labelX, y + 5));
        sw.getTextField(15).integersOnly = true;
        sw.getTextField(15).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        sw.getTextField(15).setHoverText("tooltip.naturalspawns.options.maxHeight");
        y += 22;

        sw.addTextField(new GuiNpcTextField(16, this, controlX, y, 60, 20, "" + data.maxAlive));
        sw.addLabel(new GuiNpcLabel(16, "spawning.maxAlive", labelX, y + 5));
        sw.getTextField(16).integersOnly = true;
        sw.getTextField(16).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
        sw.getTextField(16).setHoverText("tooltip.naturalspawns.options.maxAlive");
        y += 22;

        sw.addTextField(new GuiNpcTextField(17, this, controlX, y, 60, 20, "" + data.cooldownTicks));
        sw.addLabel(new GuiNpcLabel(17, "spawning.cooldownTicks", labelX, y + 5));
        sw.getTextField(17).integersOnly = true;
        sw.getTextField(17).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
        sw.getTextField(17).setHoverText("tooltip.naturalspawns.options.cooldownTicks");
        y += 22;

        sw.addTextField(new GuiNpcTextField(18, this, controlX, y, 60, 20, "" + data.attemptsPerCycle));
        sw.addLabel(new GuiNpcLabel(18, "spawning.attemptsPerCycle", labelX, y + 5));
        sw.getTextField(18).integersOnly = true;
        sw.getTextField(18).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
        sw.getTextField(18).setHoverText("tooltip.naturalspawns.options.attemptsPerCycle");
        y += 22;

        sw.addTextField(new GuiNpcTextField(19, this, controlX, y, 60, 20, "" + data.playerMinDistance));
        sw.addLabel(new GuiNpcLabel(19, "spawning.playerMinDistance", labelX, y + 5));
        sw.getTextField(19).integersOnly = true;
        sw.getTextField(19).setMinMaxDefault(0, Integer.MAX_VALUE, 24);
        sw.getTextField(19).setHoverText("tooltip.naturalspawns.options.playerMinDistance");
        y += 22;

        sw.addButton(new GuiNpcButton(20, controlX, y, 100, 20,
            new String[]{"spawning.despawn.forceNatural", "spawning.despawn.preserve", "spawning.despawn.forcePersistent"}, data.despawnMode));
        sw.addLabel(new GuiNpcLabel(20, "spawning.despawnMode", labelX, y + 5));
        sw.getButton(20).setHoverText("tooltip.naturalspawns.options.despawnMode");
        y += 22;

        sw.maxScrollY = Math.max(0, y - sw.clipHeight + 8);

        addButton(new GuiNpcButton(100, guiLeft + xSize - 64, guiTop + ySize - 24, 60, 20, "gui.done"));
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
        if (button.id == 20) {
            data.setDespawnMode(button.getValue());
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
        if (textfield.id == 16) {
            data.setMaxAlive(textfield.getInteger());
        }
        if (textfield.id == 17) {
            data.setCooldownTicks(textfield.getInteger());
        }
        if (textfield.id == 18) {
            data.setAttemptsPerCycle(textfield.getInteger());
        }
        if (textfield.id == 19) {
            data.setPlayerMinDistance(textfield.getInteger());
        }
    }
}
