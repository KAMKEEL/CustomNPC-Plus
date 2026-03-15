package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumPartyRequirements;
import noppes.npcs.controllers.data.PartyOptions;

public class SubGuiNpcPartyOptions extends SubGuiInterface implements ITextfieldListener {
    private final PartyOptions options;

    public SubGuiNpcPartyOptions(PartyOptions options) {
        this.options = options;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui() {
        super.initGui();

        int y = 0;
        //Allow party - T/F button
        addButton(new GuiNpcButton(0, guiLeft + 130, guiTop + 10 + y, 60, 20,
            new String[]{"gui.no", "gui.yes"}, options.allowParty ? 1 : 0));
        addLabel(new GuiNpcLabel(1, "party.allowParty", guiLeft + 10, guiTop + 17 + y));

        if (options.allowParty) {
            y += 23;
            //Only party - enum button
            addButton(new GuiNpcButton(24, guiLeft + 130, guiTop + 10 + y, 60, 20,
                new String[]{"gui.no", "gui.yes"}, options.onlyParty ? 1 : 0));
            addLabel(new GuiNpcLabel(24, "party.only", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            //Party requirements - enum button
            addButton(new GuiNpcButton(5, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumPartyRequirements.values(), options.partyRequirements.ordinal()));
            addLabel(new GuiNpcLabel(6, "party.partyRequirements", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            //Objective Requirement - enum button
            addButton(new GuiNpcButton(18, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumPartyObjectives.values(), options.objectiveRequirement.ordinal()));
            addLabel(new GuiNpcLabel(19, "quest.objectives", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            //Party rewards - enum button
            addButton(new GuiNpcButton(10, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumPartyExchange.values(), options.rewardControl.ordinal()));
            addLabel(new GuiNpcLabel(11, "quest.reward", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            //Complete for - enum button
            addButton(new GuiNpcButton(15, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumPartyExchange.values(), options.completeFor.ordinal()));
            addLabel(new GuiNpcLabel(16, "party.completeFor", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            //Commmand for - enum button
            addButton(new GuiNpcButton(25, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumPartyExchange.values(), options.executeCommand.ordinal()));
            addLabel(new GuiNpcLabel(25, "party.commandFor", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            //min party size - number field
            GuiNpcTextField minField = new GuiNpcTextField(21, this, guiLeft + 60, guiTop + 10 + y,
                30, 20, "" + options.minPartySize);
            minField.integersOnly = true;
            minField.setMinMaxDefault(1, Integer.MAX_VALUE, 1);
            addTextField(minField);
            addLabel(new GuiNpcLabel(21, "party.minPartySize", guiLeft + 10, guiTop + 17 + y));

            //max party size - number field
            GuiNpcTextField maxField = new GuiNpcTextField(20, this, guiLeft + 160, guiTop + 10 + y,
                30, 20, "" + options.maxPartySize);
            maxField.integersOnly = true;
            maxField.setMinMaxDefault(1, Integer.MAX_VALUE, 4);
            addTextField(maxField);
            addLabel(new GuiNpcLabel(20, "party.maxPartySize", guiLeft + 110, guiTop + 17 + y));
        }

        addButton(new GuiNpcButton(66, guiLeft + 200, guiTop + 192, 50, 20, "gui.done"));
    }

    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        switch (id) {
            case 0:
                options.allowParty = !options.allowParty;
                break;
            case 5:
                options.partyRequirements = EnumPartyRequirements.valueOf(guibutton.displayString);
                break;
            case 10:
                options.rewardControl = EnumPartyExchange.valueOf(guibutton.displayString);
                break;
            case 15:
                options.completeFor = EnumPartyExchange.valueOf(guibutton.displayString);
                break;
            case 18:
                options.objectiveRequirement = EnumPartyObjectives.valueOf(guibutton.displayString);
                break;
            case 24:
                options.onlyParty = !options.onlyParty;
                break;
            case 25:
                options.executeCommand = EnumPartyExchange.valueOf(guibutton.displayString);
                break;
            case 66:
                close();
                break;
        }
        initGui();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 20) {
            options.setMaxPartySize(textfield.getInteger());
            initGui();
        }
        if (textfield.id == 21) {
            options.setMinPartySize(textfield.getInteger());
            initGui();
        }
    }
}
