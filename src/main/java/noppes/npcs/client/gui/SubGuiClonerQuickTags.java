package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.Sys;

import java.util.*;

public class SubGuiClonerQuickTags extends SubGuiInterface implements IScrollData
{
	public GuiNpcMobSpawnerAdd parent;
	public GuiCustomScroll quickScroll = new GuiCustomScroll(this,0);
	public GuiCustomScroll selectedScroll = new GuiCustomScroll(this,5);
	private static String quickSearch = "";

    public SubGuiClonerQuickTags(GuiNpcMobSpawnerAdd par){
		this.parent = par;
		setBackground("menubg.png");
		xSize = 305;
		ySize = 220;
		closeOnEsc = true;
    }

    public void initGui(){
        super.initGui();
		quickScroll.clear();
		quickScroll.setSize(140, 166);
		quickScroll.guiLeft = guiLeft + 4;
		quickScroll.guiTop = guiTop + 19;
		quickScroll.multipleSelection = true;
		quickScroll.setSelectedList(GuiNpcMobSpawnerAdd.addTags);
        this.addScroll(quickScroll);

		addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("cloner.wandTags"), guiLeft + 7, guiTop + 7));
		addScroll(quickScroll);
		addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 4, guiTop + 190, 140, 20, quickSearch));

		addButton(new GuiNpcButton(10, guiLeft + 150, guiTop + 20, 120, 20, "gui.selectAll"));
		addButton(new GuiNpcButton(11, guiLeft + 150, guiTop + 43, 120, 20, "gui.deselectAll"));

		selectedScroll.clear();
		selectedScroll.setSize(145, 103);
		selectedScroll.guiLeft = guiLeft + 150;
		selectedScroll.guiTop = guiTop + 82;
		selectedScroll.setUnselectable();
		this.addScroll(selectedScroll);

		addLabel(new GuiNpcLabel(3, StatCollector.translateToLocal("tags.selectedTags"), guiLeft + 153, guiTop + 68));
		addScroll(selectedScroll);

		addButton(new GuiNpcButton(66, guiLeft + 240, guiTop + 190, 60, 20, "gui.done"));

		setSelected();
		refreshSelected();
    }
	protected void actionPerformed(GuiButton guibutton){
		if (guibutton.id == 10) {
			GuiNpcMobSpawnerAdd.addTags = new HashSet<>(parent.allTags);
			quickScroll.setSelectedList(GuiNpcMobSpawnerAdd.addTags);
			refreshSelected();
		}
		if (guibutton.id == 11) {
			GuiNpcMobSpawnerAdd.addTags = new HashSet<>();
			quickScroll.setSelectedList(GuiNpcMobSpawnerAdd.addTags);
			refreshSelected();
		}
		if (guibutton.id == 66) {
			close();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k)
	{
		super.mouseClicked(i, j, k);
		refreshSelected();
	}

	public void setSelected(){
		quickScroll.setList(getQuickTags());
	}

	public void refreshSelected(){
		List<String> sortedList = new ArrayList<>(new TreeSet<>(GuiNpcMobSpawnerAdd.addTags));
		selectedScroll.setList(sortedList);
	}

	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(2) != null){
			if(quickSearch.equals(getTextField(2).getText()))
				return;
			quickSearch = getTextField(2).getText().toLowerCase();
			quickScroll.setList(getQuickTags());
		}
	}

	private List<String> getQuickTags(){
		if(quickSearch.isEmpty()){
			return new ArrayList<String>(parent.allTags);
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.parent.allTags){
			if(name.toLowerCase().contains(quickSearch))
				list.add(name);
		}
		return list;
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {

	}

	@Override
	public void setSelected(String selected) {

	}
}