package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyRequirements;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.PartyOptions;

import java.util.HashMap;
import java.util.Vector;

public class SubGuiNpcPartyOptions extends SubGuiInterface implements ITextfieldListener
{
	private final PartyOptions options;

    public SubGuiNpcPartyOptions(PartyOptions options)
    {
    	this.options = options;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();

        //Allow party - T/F button
        addButton(new GuiNpcButton(0, guiLeft + 90, guiTop + 10, 60, 20,
            new String[]{"gui.no", "gui.yes"}, options.allowParty ? 1 : 0));
        addLabel(new GuiNpcLabel(1, "party.allowParty", guiLeft + 10, guiTop + 17));

        if (options.allowParty) {
            //Party requirements - enum button
            addButton(new GuiNpcButton(5, guiLeft + 110, guiTop + 35, 60, 20,
                EnumPartyRequirements.values(), options.partyRequirements.ordinal()));
            addLabel(new GuiNpcLabel(6, "party.partyRequirements", guiLeft + 10, guiTop + 42));

            //Party rewards - enum button
            addButton(new GuiNpcButton(10, guiLeft + 110, guiTop + 60, 60, 20,
                EnumPartyExchange.values(), options.rewardControl.ordinal()));
            addLabel(new GuiNpcLabel(11, "party.partyRewards", guiLeft + 10, guiTop + 67));

            //Complete for - enum button
            addButton(new GuiNpcButton(15, guiLeft + 110, guiTop + 85, 60, 20,
                EnumPartyExchange.values(), options.completeFor.ordinal()));
            addLabel(new GuiNpcLabel(16, "party.completeFor", guiLeft + 10, guiTop + 92));

            //max party size - number field
            GuiNpcTextField textField = new GuiNpcTextField(20, this, guiLeft + 110, guiTop + 110,
                40, 20, String.valueOf(options.maxPartySize));
            textField.integersOnly = true;
            textField.setMinMaxDefault(1, Integer.MAX_VALUE, 4);
            addTextField(textField);
            addLabel(new GuiNpcLabel(20, "party.maxPartySize", guiLeft + 10, guiTop + 117));
        }

        addButton(new GuiNpcButton(66, guiLeft + 20, guiTop + 192, 90, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton)
    {
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
            case 66:
                close();
                break;
        }
        initGui();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 20) {
            options.maxPartySize = textfield.getInteger();
        }
    }
}
