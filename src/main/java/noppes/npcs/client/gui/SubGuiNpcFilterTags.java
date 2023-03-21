package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.*;

public class SubGuiNpcFilterTags extends SubGuiInterface
{
	ArrayList<String> allTags = new ArrayList<>();
	HashSet<String> filter;
	public GuiCustomScroll filterScroll;
	public boolean showNoTags;

    public SubGuiNpcFilterTags(){
		setBackground("menubg.png");
		xSize = 305;
		ySize = 200;
		closeOnEsc = true;
		this.allTags.addAll(GuiNpcMobSpawner.allTags);
		this.filter = GuiNpcMobSpawner.filter;
		this.showNoTags = GuiNpcMobSpawner.showNoTags;
    }

    public void initGui(){
        super.initGui();
        if(filterScroll == null){
        	filterScroll = new GuiCustomScroll(this,0);
        	filterScroll.setSize(140, 180);
        }
        filterScroll.guiLeft = guiLeft + 4;
        filterScroll.guiTop = guiTop + 14;
		filterScroll.setList(this.allTags);
		filterScroll.multipleSelection = true;
		filterScroll.setSelectedList(this.filter);
        this.addScroll(filterScroll);

		addButton(new GuiNpcButton(10, guiLeft + 150, guiTop + 20, 120, 20, "gui.selectAll"));
		addButton(new GuiNpcButton(11, guiLeft + 150, guiTop + 43, 120, 20, "gui.deselectAll"));
		addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("tags.taglessEntries") + ":", guiLeft + 150, guiTop + 72));
		addButton(new GuiNpcButton(12, guiLeft + 240, guiTop + 65, new String[]{"display.show", "display.hide"}, showNoTags ? 0 : 1));
		getButton(12).width = 60;
		getButton(12).height = 20;

		addButton(new GuiNpcButton(66, guiLeft + 240, guiTop + 175, 60, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton){
		if (guibutton.id == 10) {
			HashSet<String> hashSet = new HashSet<>(this.allTags);
			filterScroll.setSelectedList(hashSet);
		}
		if (guibutton.id == 11) {
			filterScroll.setSelectedList(new HashSet<>());
		}
		if (guibutton.id == 12) {
			showNoTags = !showNoTags;
		}
		if (guibutton.id == 66) {
			close();
		}
	}
}
