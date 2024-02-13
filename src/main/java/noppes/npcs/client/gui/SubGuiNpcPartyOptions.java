package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.PartyOptions;

import java.util.HashMap;
import java.util.Vector;

public class SubGuiNpcPartyOptions extends SubGuiInterface
{
	private final PartyOptions options;
	private HashMap<String,Integer> data = new HashMap<>();

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

        addButton(new GuiNpcButton(66, guiLeft + 20, guiTop + 192, 90, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;

        if(id == 66)
        {
        	close();
        }
    }
}
