package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.DataStats;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumPotionType;
import noppes.npcs.constants.EnumParticleType;

public class SubGuiNpcProjectiles extends SubGuiInterface implements ITextfieldListener
{
	private DataStats stats;
	private String[] potionNames = new String[]{"gui.none", "tile.fire.name", "potion.poison", "potion.hunger", "potion.weakness", "potion.moveSlowdown", "potion.confusion", "potion.blindness", "potion.wither"};
	private String[] trailNames = new String[]{"gui.none", "Smoke", "Portal", "Redstone", "Lightning", "LargeSmoke", "Magic", "Enchant"};
    public SubGuiNpcProjectiles(DataStats stats)
    {
    	this.stats = stats;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();
        addLabel(new GuiNpcLabel(1,"enchantment.arrowDamage", guiLeft + 5, guiTop + 15));
        addTextField(new GuiNpcTextField(1,this, fontRendererObj, guiLeft + 45, guiTop + 10, 50, 18, stats.pDamage+""));
        getTextField(1).numbersOnly = true;
        getTextField(1).setMinMaxDefault(0, 9999, 5);
        addLabel(new GuiNpcLabel(2,"enchantment.arrowKnockback", guiLeft + 110, guiTop + 15));
        addTextField(new GuiNpcTextField(2,this, fontRendererObj, guiLeft + 150, guiTop + 10, 50, 18, stats.pImpact+""));
        getTextField(2).numbersOnly = true;
        getTextField(2).setMinMaxDefault(0, 3, 0);
        addLabel(new GuiNpcLabel(3,"stats.size", guiLeft + 5, guiTop + 45));
        addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 45, guiTop + 40, 50, 18, stats.pSize+""));
        getTextField(3).numbersOnly = true;
        getTextField(3).setMinMaxDefault(1, 10, 10);
        addLabel(new GuiNpcLabel(4,"stats.speed", guiLeft + 5, guiTop + 75));
        addTextField(new GuiNpcTextField(4,this, fontRendererObj, guiLeft + 45, guiTop + 70, 50, 18, stats.pSpeed+""));
        getTextField(4).numbersOnly = true;
        getTextField(4).setMinMaxDefault(1, 50, 10);
        
        addLabel(new GuiNpcLabel(5,"stats.hasgravity", guiLeft + 5, guiTop + 105));
    	addButton(new GuiNpcButton(0, guiLeft + 60, guiTop + 100, 60, 20, new String[]{"gui.no", "gui.yes"} ,stats.pPhysics ? 1:0));
    	if(!stats.pPhysics) {
        	addButton(new GuiNpcButton(1, guiLeft + 140, guiTop + 100, 60, 20, new String[]{"gui.constant", "gui.accelerate"} ,stats.pXlr8 ? 1:0));
    	}
    	addLabel(new GuiNpcLabel(6,"stats.explosive", guiLeft + 5, guiTop + 135));
    	addButton(new GuiNpcButton(2, guiLeft + 60, guiTop + 130, 60, 20, new String[]{"gui.no", "gui.yes"} ,stats.pExplode ? 1:0));
    	if(stats.pExplode) {
        	addButton(new GuiNpcButton(3, guiLeft + 140, guiTop + 130, 60, 20, new String[]{"gui.none", "gui.small", "gui.medium", "gui.large"} ,stats.pArea));
    	}
    	addLabel(new GuiNpcLabel(7,"stats.rangedeffect", guiLeft + 5, guiTop + 165));
    	addButton(new GuiNpcButton(4,guiLeft + 60, guiTop + 160, 60, 20, potionNames, stats.pEffect.ordinal()));
    	if(stats.pEffect != EnumPotionType.None) {
    		addTextField(new GuiNpcTextField(5,this, fontRendererObj, guiLeft + 140, guiTop + 160, 60, 18, stats.pDur + ""));
    		getTextField(5).numbersOnly = true;
    		getTextField(5).setMinMaxDefault(1, 99999, 5);
    		if(stats.pEffect != EnumPotionType.Fire) {
    			addButton(new GuiNpcButton(10, guiLeft + 210, guiTop + 160, 40, 20, new String[]{"stats.regular", "stats.amplified"} ,stats.pEffAmp));
    		}
    	}
    	
    	addLabel(new GuiNpcLabel(8,"stats.trail", guiLeft + 5, guiTop + 195));
    	addButton(new GuiNpcButton(5, guiLeft + 60, guiTop + 190, 60, 20, trailNames, stats.pTrail.ordinal()));
    	
    	addButton(new GuiNpcButton(7, guiLeft + 220, guiTop + 10, 30, 20, new String[]{"2D", "3D"} ,stats.pRender3D ? 1:0));
    	if (stats.pRender3D) {
    		addLabel(new GuiNpcLabel(10,"stats.spin", guiLeft + 160, guiTop + 45));
    		addButton(new GuiNpcButton(8, guiLeft + 220, guiTop + 40, 30, 20, new String[]{"gui.no", "gui.yes"} ,stats.pSpin ? 1:0));
    		addLabel(new GuiNpcLabel(11,"stats.stick", guiLeft + 160, guiTop + 75));
    		addButton(new GuiNpcButton(9, guiLeft + 220, guiTop + 70, 30, 20, new String[]{"gui.no", "gui.yes"} ,stats.pStick ? 1:0));
    	}
    	addButton(new GuiNpcButton(6, guiLeft + 140, guiTop + 190, 60, 20, new String[]{"stats.noglow", "stats.glows"} ,stats.pGlows ? 1:0));
    	addButton(new GuiNpcButton(66, guiLeft + 210, guiTop + 190, 40, 20, "gui.done"));
    }

	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 1){
			stats.pDamage = textfield.getInteger();
		}
		else if(textfield.id == 2){
			stats.pImpact = textfield.getInteger();
		}
		else if(textfield.id == 3){
			stats.pSize = textfield.getInteger();
		}
		else if(textfield.id == 4){
			stats.pSpeed = textfield.getInteger();
		}
		else if(textfield.id == 5){
			stats.pDur = textfield.getInteger();
		}
	}
    
	protected void actionPerformed(GuiButton guibutton)
    {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if(button.id == 0){
			stats.pPhysics = (button.getValue() == 1);
			initGui();
        }
		if(button.id == 1){
			stats.pXlr8 = (button.getValue() == 1);
        }
		if(button.id == 2){
			stats.pExplode = (button.getValue() == 1);
			initGui();
        }
		if(button.id == 3){
			stats.pArea = button.getValue();
        }
		if(button.id == 4){
			stats.pEffect = EnumPotionType.values()[button.getValue()];
			initGui();
        }
		if(button.id == 5){
			stats.pTrail = EnumParticleType.values()[button.getValue()];
        }
		if(button.id == 6){
			stats.pGlows = (button.getValue() == 1);
        }
		if(button.id == 7){
			stats.pRender3D = (button.getValue() == 1);
			initGui();
        }
		if(button.id == 8){
			stats.pSpin = (button.getValue() == 1);
        }
		if(button.id == 9){
			stats.pStick = (button.getValue() == 1);
        }
		if(button.id == 10){
			stats.pEffAmp = button.getValue();
        }
        if(button.id == 66)
        {
        	close();
        }
    }
}
