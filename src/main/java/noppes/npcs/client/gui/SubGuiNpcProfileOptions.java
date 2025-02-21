package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumProfileSync;
import noppes.npcs.controllers.data.ProfileOptions;

public class SubGuiNpcProfileOptions extends SubGuiInterface
{
	private final ProfileOptions options;

    public SubGuiNpcProfileOptions(ProfileOptions options)
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

        int y = 0;
        //Allow party - T/F button
        addButton(new GuiNpcButton(0, guiLeft + 130, guiTop + 10 + y, 60, 20,
            new String[]{"gui.no", "gui.yes"}, options.enableOptions ? 1 : 0));
        addLabel(new GuiNpcLabel(1, "party.allowParty", guiLeft + 10, guiTop + 17 + y));

        if (options.enableOptions) {
            y += 23;
            addButton(new GuiNpcButton(5, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumProfileSync.values(), options.completeControl.ordinal()));
            addLabel(new GuiNpcLabel(5, "party.partyRequirements", guiLeft + 10, guiTop + 17 + y));

            y += 23;
            addButton(new GuiNpcButton(18, guiLeft + 130, guiTop + 10 + y, 60, 20,
                EnumProfileSync.values(), options.cooldownControl.ordinal()));
            addLabel(new GuiNpcLabel(18, "quest.objectives", guiLeft + 10, guiTop + 17 + y));
        }

        addButton(new GuiNpcButton(66, guiLeft + 200, guiTop + 192, 50, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        switch (id) {
            case 0:
                options.enableOptions = !options.enableOptions;
                break;
            case 5:
                options.completeControl = EnumProfileSync.valueOf(guibutton.displayString);
                break;
            case 18:
                options.cooldownControl = EnumProfileSync.valueOf(guibutton.displayString);
                break;
            case 66:
                close();
                break;
        }
        initGui();
    }
}
