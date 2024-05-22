package noppes.npcs.client.gui.mainmenu;

import cpw.mods.fml.common.Loader;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNpcManagePlayerData;
import noppes.npcs.client.gui.global.GuiNpcNaturalSpawns;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Arrays;

public class GuiNPCGlobalMainMenu extends GuiNPCInterface2{
    public GuiNPCGlobalMainMenu(EntityNPCInterface npc){
    	super(npc,5);
    }

    @Override
    public void initGui(){
    	super.initGui();
    	int y = guiTop + 10;
    	this.addButton(new GuiNpcButton(2, guiLeft + 5, y, "global.banks"));
    	this.addButton(new GuiNpcButton(3, guiLeft + 5, y += 22, "menu.factions"));
    	this.addButton(new GuiNpcButton(4, guiLeft + 5, y += 22, "dialog.dialogs"));
    	this.addButton(new GuiNpcButton(11, guiLeft + 5, y += 22, "quest.quests"));
    	this.addButton(new GuiNpcButton(12, guiLeft + 5, y += 22, "global.transport"));
    	this.addButton(new GuiNpcButton(13, guiLeft + 5, y += 22, "global.playerdata"));
    	this.addButton(new GuiNpcButton(14, guiLeft + 5, y += 22, "global.recipes"));
    	this.addButton(new GuiNpcButton(15, guiLeft + 5, y += 22, NoppesStringUtils.translate("global.naturalspawn")));
    	this.addButton(new GuiNpcButton(16, guiLeft + 5, y += 22, "global.linked"));
		y = guiTop + 10;
		this.addButton(new GuiNpcButton(18, guiLeft + 210, y, "menu.animations"));
		this.addButton(new GuiNpcButton(17, guiLeft + 210, y += 22, "menu.tags"));
        if(Loader.isModLoaded("npcdbc"))
            this.addButton(new GuiNpcButton(200, guiLeft + 210, y += 22, "global.customforms"));

        GuiScrollWindow scrollWindow = new GuiScrollWindow(this, guiLeft + 210, y+=22, 200, 150, 200);
        this.addScrollableGui(0, scrollWindow);
        int k = 10;
        scrollWindow.addButton(new GuiNpcButton(30, 0, k, 80, 20, "Something"));
        scrollWindow.addButton(new GuiNpcButton(31, 0, k += 24, 80, 20, "Something"));
        scrollWindow.addButton(new GuiNpcButton(32, 0, k += 24, 80, 20, "Something"));
        scrollWindow.addButton(new GuiNpcButton(33, 0, k += 24, 80, 20, "Something"));
        scrollWindow.addButton(new GuiNpcButton(34, 0, k += 24, 80, 20, "Something"));
        scrollWindow.addButton(new GuiNpcButton(35, 0, k += 24, 80, 20, "Something"));
        scrollWindow.addButton(new GuiNpcButton(35, 0, k += 24, 80, 20, "Something"));

        scrollWindow.addTextField(new GuiNpcTextField(40, scrollWindow, fontRendererObj, 0, k += 24, 90, 20, ""));

        GuiCustomScroll scroll = new GuiCustomScroll(scrollWindow,0);
        scroll.setList(Arrays.asList("Something", "Something2","Somethin4g","Somet5hing","Someth6ing","Somet7hing","Somet8hing","Som9ething"));
        scroll.setSize(143, 50);
        scroll.guiLeft = 0;
        scroll.guiTop = k+=22;

        scrollWindow.addScroll(scroll);

        System.out.println(k);
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

        // DBC Addon
        if (id == 200) {
            NoppesUtil.requestOpenGUI(EnumGuiType.ManageCustomForms);
        }
        System.out.println("HIT BUTTON");
	}
	@Override
	public void save() {
	}


}
