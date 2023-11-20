package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNpcManageRecipes extends GuiContainerNPCInterface2 implements IScrollData, IGuiData, ICustomScrollListener,ITextfieldListener{
    private GuiCustomScroll scroll;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private ContainerManageRecipes container;
	private String selected = null;
	private ResourceLocation slot;
	private String search = "";
	
    public GuiNpcManageRecipes(EntityNPCInterface npc,ContainerManageRecipes container){
    	super(npc,container);
    	this.container = container;
    	drawDefaultBackground = false;
    	Client.sendData(EnumPacketServer.RecipesGet,container.width);
        setBackground("inventorymenu.png");
        slot = getResource("slot.png");
        ySize = 200;
    }

	@Override
    public void initGui(){
        super.initGui();
        
        if(scroll == null)
        	scroll = new GuiCustomScroll(this,0,0);
        scroll.setSize(130, 180);
        scroll.guiLeft = guiLeft + 172;
        scroll.guiTop = guiTop + 8;
        addScroll(scroll);

		this.addButton(new GuiNpcButton(0,guiLeft + 306, guiTop + 10, 84, 20, "menu.global"));
		this.addButton(new GuiNpcButton(1,guiLeft + 306, guiTop + 32, 84, 20, "tile.npcCarpentyBench.name"));
		this.getButton(0).setEnabled(container.width == 4);
		this.getButton(1).setEnabled(container.width == 3);

		this.addButton(new GuiNpcButton(3,guiLeft + 306, guiTop + 60, 84, 20, "gui.add"));
		this.addButton(new GuiNpcButton(4,guiLeft + 306, guiTop + 82, 84, 20, "gui.remove"));

    	this.addLabel(new GuiNpcLabel(0, "gui.ignoreDamage", guiLeft + 86, guiTop + 32));
    	this.addButton(new GuiNpcButtonYesNo(5,guiLeft + 114, guiTop + 40, 50, 20, container.recipe.ignoreDamage));
    	
    	this.addLabel(new GuiNpcLabel(1, "gui.ignoreNBT", guiLeft + 86, guiTop + 82));
    	this.addButton(new GuiNpcButtonYesNo(6,guiLeft + 114, guiTop + 90, 50, 20, container.recipe.ignoreNBT));

		this.addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 8, guiTop + 8, 160, 20, container.recipe.name));
		this.getTextField(0).enabled = false;
		this.getButton(5).setEnabled(false);
		this.getButton(6).setEnabled(false);

		this.addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 172, guiTop + 8 + 3 + 180, 130, 20, search));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0){
			getTextField(55).setText("");
			search = "";
			scroll.clear();
        	save();
        	NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes,3,0,0);
        }
        if(button.id == 1){
			getTextField(55).setText("");
			search = "";
			scroll.clear();
        	save();
        	NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes,4,0,0);
        }
        if(button.id == 3){
        	save();
        	scroll.clear();
        	String name = "New";
        	while(data.containsKey(name))
        		name += "_";
        	RecipeCarpentry recipe = new RecipeCarpentry(name);
        	recipe.isGlobal = container.width == 3;
        	Client.sendData(EnumPacketServer.RecipeSave,recipe.writeNBT());
        }
        if(button.id == 4){
        	if(data.containsKey(scroll.getSelected())){
        		Client.sendData(EnumPacketServer.RecipeRemove, data.get(scroll.getSelected()));
        		scroll.clear();
        	}
        }
        if(button.id == 5){
        	container.recipe.ignoreDamage = button.getValue() == 1;
        }
        if(button.id == 6){
        	container.recipe.ignoreNBT = button.getValue() == 1;
        }
    }

	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(55) != null){
			if(getTextField(55).isFocused()){
				if(search.equals(getTextField(55).getText()))
					return;
				search = getTextField(55).getText().toLowerCase();
				scroll.setList(getSearchList());
			}
		}
	}

	private List<String> getSearchList(){
		if(search.isEmpty()){
			return new ArrayList<String>(this.data.keySet());
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.data.keySet()){
			if(name.toLowerCase().contains(search))
				list.add(name);
		}
		return list;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		RecipeCarpentry recipe = RecipeCarpentry.read(compound);
		getTextField(0).setText(recipe.name);
		container.setRecipe(recipe);
		this.getTextField(0).enabled = true;
    	this.getButton(5).setEnabled(true);
    	this.getButton(5).setDisplay(recipe.ignoreDamage?1:0);
    	this.getButton(6).setEnabled(true);
    	this.getButton(6).setDisplay(recipe.ignoreNBT?1:0);
		setSelected(recipe.name);
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y){
    	super.drawGuiContainerBackgroundLayer(f, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(slot);
    	
    	for(int i = 0; i < container.width;i++){
        	for(int j = 0; j < container.width;j++){
        		drawTexturedModalRect(guiLeft + i*18 + 7, guiTop + j*18 + 34, 0, 0, 18, 18);
        	}
    	}
		drawTexturedModalRect(guiLeft + 86, guiTop + 60, 0, 0, 18, 18);
    }
	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = scroll.getSelected();
		this.data = data;
		scroll.setList(getSearchList());

		this.getTextField(0).enabled = name != null;
		this.getButton(5).setEnabled(name != null);
		
		if(name != null)
			scroll.setSelected(name);
	}
	
	@Override
	public void setSelected(String selected) {
		this.selected = selected;
		scroll.setSelected(selected);
	}

	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		save();
		selected = scroll.getSelected();
		Client.sendData(EnumPacketServer.RecipeGet, data.get(selected));
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if(selected != null && data.containsKey(selected)){
			container.saveRecipe();
			Client.sendData(EnumPacketServer.RecipeSave, container.recipe.writeNBT());
		}
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if(guiNpcTextField.id == 0){
			String name = guiNpcTextField.getText();
			if(!name.isEmpty() && !data.containsKey(name)){
				String old = container.recipe.name;
				data.remove(container.recipe.name);
				container.recipe.name = name;
				data.put(container.recipe.name, container.recipe.id);
				selected = name;
				scroll.replace(old,container.recipe.name);
			}
		}
	}
}
