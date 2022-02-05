package noppes.npcs.client.gui.mainmenu;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.DataStats;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMeleeProperties;
import noppes.npcs.client.gui.SubGuiNpcProjectiles;
import noppes.npcs.client.gui.SubGuiNpcRangeProperties;
import noppes.npcs.client.gui.SubGuiNpcResistanceProperties;
import noppes.npcs.client.gui.SubGuiNpcRespawn;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcStats extends GuiNPCInterface2 implements ITextfieldListener, IGuiData{
	private DataStats stats;
	public GuiNpcStats(EntityNPCInterface npc) {
		super(npc,2);
		stats = npc.stats;
    	Client.sendData(EnumPacketServer.MainmenuStatsGet);
	}

	@Override
    public void initGui(){
        super.initGui();
        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0,"stats.health", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(0,this, guiLeft + 85, y, 50, 18, stats.maxHealth+""));
        getTextField(0).numbersOnly = true;
        getTextField(0).setMinMaxDefault(1, Integer.MAX_VALUE, 20);
        addLabel(new GuiNpcLabel(1,"stats.aggro", guiLeft + 140, y + 5));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 220, y, 50, 18, stats.aggroRange+""));
        getTextField(1).numbersOnly = true;
        getTextField(1).setMinMaxDefault(1, 64, 2);
        addLabel(new GuiNpcLabel(34,"stats.creaturetype", guiLeft + 275, y + 5));
    	addButton(new GuiNpcButton(8,guiLeft + 355, y, 56, 20, new String[]{"stats.normal","stats.undead","stats.arthropod"} ,stats.creatureType.ordinal()));

    	addButton(new GuiNpcButton(0,guiLeft + 82, y+=22, 56, 20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(2,"stats.respawn", guiLeft + 5, y + 5));

    	
    	this.addButton(new GuiNpcButton(2, guiLeft + 82, y+=22, 56, 20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(5,"stats.meleeproperties", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(3, guiLeft + 82, y+=22, 56, 20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(6,"stats.rangedproperties", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(9, guiLeft + 217, y, 56, 20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(7,"stats.projectileproperties", guiLeft + 140, y + 5));
    	
    	this.addButton(new GuiNpcButton(15, guiLeft + 82, y+=34, 56, 20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(15,"potion.resistance", guiLeft + 5, y + 5));

    	    	
    	addButton(new GuiNpcButton(4,guiLeft + 82, y+=34, 56, 20, new String[]{"gui.no","gui.yes"} ,npc.isImmuneToFire()? 1:0));
    	addLabel(new GuiNpcLabel(10,"stats.fireimmune", guiLeft + 5, y + 5));
    	addButton(new GuiNpcButton(5,guiLeft + 217, y, 56, 20, new String[]{"gui.no","gui.yes"} ,stats.canDrown? 1:0));
    	addLabel(new GuiNpcLabel(11,"stats.candrown", guiLeft + 140, y + 5));
    	addTextField(new GuiNpcTextField(14, this, guiLeft + 355, y, 56, 20, stats.healthRegen + "").setNumbersOnly());
    	addLabel(new GuiNpcLabel(14,"stats.regenhealth", guiLeft + 275, y + 5));
    	
    	addTextField(new GuiNpcTextField(16, this, guiLeft + 355, y+=22, 56, 20, stats.combatRegen + "").setNumbersOnly());
    	addLabel(new GuiNpcLabel(16,"stats.combatregen", guiLeft + 275, y + 5));
    	addButton(new GuiNpcButton(6,guiLeft + 82, y, 56, 20, new String[]{"gui.no","gui.yes"} ,stats.burnInSun? 1:0));
    	addLabel(new GuiNpcLabel(12,"stats.burninsun", guiLeft + 5, y + 5));
    	addButton(new GuiNpcButton(7,guiLeft + 217, y, 56, 20, new String[]{"gui.no","gui.yes"} ,stats.noFallDamage? 1:0));
    	addLabel(new GuiNpcLabel(13,"stats.nofalldamage", guiLeft + 140, y + 5));

    	addButton(new GuiNpcButtonYesNo(17, guiLeft + 82, y+=22, 56, 20, stats.potionImmune));
    	addLabel(new GuiNpcLabel(17,"stats.potionImmune", guiLeft + 5, y + 5));
    	addButton(new GuiNpcButtonYesNo(18, guiLeft + 217, y, 56, 20, stats.attackInvisible));
    	addLabel(new GuiNpcLabel(18,"stats.attackInvisible", guiLeft + 140, y + 5));
    }	
    
	@Override
	public void unFocused(GuiNpcTextField textfield){
		if(textfield.id == 0){
			stats.maxHealth = textfield.getInteger();
			npc.heal(stats.maxHealth);
		}
		else if(textfield.id == 1){
			stats.aggroRange = textfield.getInteger();
		}
		else if(textfield.id == 14){
			stats.healthRegen = textfield.getInteger();
		}
		else if(textfield.id == 16){
			stats.combatRegen = textfield.getInteger();
		}
	}
	
    @Override
	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0){
			setSubGui(new SubGuiNpcRespawn(this.stats));
		}
		else if (button.id == 2){
			setSubGui(new SubGuiNpcMeleeProperties(this.stats));
		}
		else if (button.id == 3){
			setSubGui(new SubGuiNpcRangeProperties(this.stats));
		}
		else if(button.id == 4){
			npc.setImmuneToFire(button.getValue() == 1);
		}
		else if(button.id == 5){
			stats.canDrown = button.getValue() == 1;
		}
		else if(button.id == 6){
			stats.burnInSun = button.getValue() == 1;
		}
		else if(button.id == 7){
			stats.noFallDamage = button.getValue() == 1;
		}
		else if (button.id == 8) {
			stats.creatureType = EnumCreatureAttribute.values()[button.getValue()];
		}
		else if (button.id == 9) {
			setSubGui(new SubGuiNpcProjectiles(this.stats));
		}
		else if (button.id == 15) {
			setSubGui(new SubGuiNpcResistanceProperties(this.stats.resistances));
		}
		else if (button.id == 17) {
			stats.potionImmune = ((GuiNpcButtonYesNo)guibutton).getBoolean();
		}
		else if (button.id == 18) {
			stats.potionImmune = ((GuiNpcButtonYesNo)guibutton).getBoolean();
		}
    }
	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuStatsSave, stats.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		stats.readToNBT(compound);
		initGui();
	}
}
