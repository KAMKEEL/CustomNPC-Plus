package noppes.npcs.client.gui.questtypes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestManual;

public class GuiNpcQuestTypeManual extends SubGuiInterface implements ITextfieldListener, ICustomScrollListener
{
	private GuiScreen parent;
	private GuiCustomScroll scroll;
	private QuestManual quest;
	private GuiNpcTextField lastSelected;

    public GuiNpcQuestTypeManual(EntityNPCInterface npc, Quest q, GuiScreen parent) {
    	this.npc = npc;
    	this.parent = parent;
    	title = "Quest Manual Setup";
    	
    	quest = (QuestManual) q.questInterface;

		setBackground("largebg.png");
		bgScale = 1.7F;
		bgScaleX = 1.1F;

		xSize = 300;
		ySize = 300;
		closeOnEsc = true;
	}

	public void initGui() {
		super.initGui();

		guiTop -= 20;

		int i = 0;
		addLabel(new GuiNpcLabel(0, "Type a name for each objective here and defined the amount requied to complete.", guiLeft + 4, guiTop + 20));
		for (String name : quest.objectives.keySet()) {
			this.addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 40 + i * 22, 180, 20, name));
			this.addTextField(new GuiNpcTextField(i + 12, this, fontRendererObj, guiLeft + 186, guiTop + 40 + i * 22, 24, 20, quest.objectives.get(name) + ""));
			this.getTextField(i+12).integersOnly = true;
			this.getTextField(i+12).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
			i++;
		}
		
		for(;i < 12; i++){
			this.addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 40 + i * 22, 180, 20, ""));
			this.addTextField(new GuiNpcTextField(i + 12, this, fontRendererObj, guiLeft + 186, guiTop + 40 + i * 22, 24, 20, "1"));
			this.getTextField(i+12).integersOnly = true;
			this.getTextField(i+12).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		}
        Map<?,?> data = EntityList.stringToClassMapping;
        ArrayList<String> list = new ArrayList<String>();
        for(Object name : data.keySet()){
        	Class<?> c = (Class<?>) data.get(name);
        	try {
        		if(EntityLivingBase.class.isAssignableFrom(c) && !EntityNPCInterface.class.isAssignableFrom(c) && c.getConstructor(new Class[] {World.class}) != null && !Modifier.isAbstract(c.getModifiers()))
        				list.add(name.toString());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			}
        }   
        if(scroll == null)
        	scroll = new GuiCustomScroll(this,0);
        scroll.setList(list);
        scroll.setSize(130, 198);
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 40;
        addScroll(scroll);

		guiTop += 50;
		this.addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 310, 98, 20, "gui.back"));
		this.getTextField(36).setVisible(this.getButton(1).getValue() == 2);

		guiTop -= 50;

    	scroll.visible = GuiNpcTextField.isFieldActive();
	}

	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 0) {
			close();
		}
	}
    public void mouseClicked(int i, int j, int k)
    {
    	super.mouseClicked(i, j, k);
    	scroll.visible = GuiNpcTextField.isFieldActive();
    }

	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if(guiNpcTextField.id < 12) {
            lastSelected = guiNpcTextField;
        }
        
        saveObjectives();
	}
	private void saveObjectives(){
		HashMap<String,Integer> map = new HashMap<String,Integer>();
        
		for(int i = 0; i< 12; i++){
			String name = getTextField(i).getText();
			
            if(name.isEmpty()) {
                continue;
            }

			map.put(name, getTextField(i+12).getInteger());
		}

		quest.objectives = map;
	}
	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(lastSelected == null) {
            return;
        }
		lastSelected.setText(guiCustomScroll.getSelected());
		saveObjectives();
	}
}
