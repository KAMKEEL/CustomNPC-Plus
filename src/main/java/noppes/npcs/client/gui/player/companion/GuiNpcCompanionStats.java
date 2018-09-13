package noppes.npcs.client.gui.player.companion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiMenuTopIconButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

public class GuiNpcCompanionStats extends GuiNPCInterface implements IGuiData {
	private RoleCompanion role;
	private boolean isEating = false;

	public GuiNpcCompanionStats(EntityNPCInterface npc) {
		super(npc);
		role = (RoleCompanion) npc.roleInterface;
		closeOnEsc = true;
		setBackground("companion.png");
		xSize = 171;
		ySize = 166;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 10;
		addLabel(new GuiNpcLabel(0, NoppesStringUtils.translate("gui.name", ": ",npc.display.name), guiLeft + 4, y));
		addLabel(new GuiNpcLabel(1, NoppesStringUtils.translate("companion.owner", ": ",role.ownerName), guiLeft + 4, y+=12));
		addLabel(new GuiNpcLabel(2, NoppesStringUtils.translate("companion.age", ": ", role.ticksActive / 18000 + " (", role.stage.name, ")" ), guiLeft + 4, y+=12));
		addLabel(new GuiNpcLabel(3, NoppesStringUtils.translate("companion.strength", ": ", npc.stats.getAttackStrength()), guiLeft + 4, y+=12));
		addLabel(new GuiNpcLabel(4, NoppesStringUtils.translate("companion.level", ": ", role.getTotalLevel()), guiLeft + 4, y+=12));
		addLabel(new GuiNpcLabel(5, NoppesStringUtils.translate("job.name", ": ", "gui.none"), guiLeft + 4, y+=12));
		
		addTopMenu(role, this, 1);
	
	}
	
	public static void addTopMenu(RoleCompanion role, GuiScreen screen, int active){
		if(screen instanceof GuiNPCInterface){
			GuiNPCInterface gui = (GuiNPCInterface) screen;
			GuiMenuTopIconButton button;
			gui.addTopButton(button = new GuiMenuTopIconButton(1, gui.guiLeft + 4, gui.guiTop - 27, "menu.stats", new ItemStack(CustomItems.letter)));
			gui.addTopButton(button = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(CustomItems.spellHoly)));
			if(role.hasInv())
				gui.addTopButton(button = new GuiMenuTopIconButton(3, button, "inv.inventory", new ItemStack(CustomItems.bag)));
			if(role.job != EnumCompanionJobs.NONE)
				gui.addTopButton(new GuiMenuTopIconButton(4, button, "job.name", new ItemStack(CustomItems.bag)));
			gui.getTopButton(active).active = true;
		}
		if(screen instanceof GuiContainerNPCInterface){
			GuiContainerNPCInterface gui = (GuiContainerNPCInterface) screen;
			GuiMenuTopIconButton button;
			gui.addTopButton(button = new GuiMenuTopIconButton(1, gui.guiLeft + 4, gui.guiTop - 27, "menu.stats", new ItemStack(CustomItems.letter)));
			gui.addTopButton(button = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(CustomItems.spellHoly)));
			if(role.hasInv())
				gui.addTopButton(button = new GuiMenuTopIconButton(3, button, "inv.inventory", new ItemStack(CustomItems.bag)));
			if(role.job != EnumCompanionJobs.NONE)
				gui.addTopButton(new GuiMenuTopIconButton(4, button, "job.name", new ItemStack(CustomItems.bag)));
			gui.getTopButton(active).active = true;
		}
	}

	@Override
	public void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		int id = guibutton.id;
		if(id == 2){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.CompanionTalent);
		}
		if(id == 3){
			NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if(isEating && !role.isEating()){
			NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
		}
		
		isEating = role.isEating();
		super.drawNpc(34, 150);
		int y = drawHealth(guiTop + 88);
	}
	
	private int drawHealth(int y){
		this.mc.getTextureManager().bindTexture(icons);

		int max = role.getTotalArmorValue();
		if(role.talents.containsKey(EnumCompanionTalent.ARMOR) || max > 0){
	        for (int i = 0; i < 10; ++i){
	            int x = guiLeft + 66 + i * 10;
	
	            if (i * 2 + 1 < max){
	                this.drawTexturedModalRect(x, y, 34, 9, 9, 9);
	            }
	
	            if (i * 2 + 1 == max){
	                this.drawTexturedModalRect(x, y, 25, 9, 9, 9);
	            }
	
	            if (i * 2 + 1 > max){
	                this.drawTexturedModalRect(x, y, 16, 9, 9, 9);
	            }
	            
	        }
	        y += 10;
		}
		
		max = MathHelper.ceiling_float_int(npc.getMaxHealth());
        int k = (int)npc.getHealth();
        float scale = 1;
        if(max > 40){
        	scale = max / 40f;
        	k = (int) (k / scale);
        	max = 40;
        }
        for(int i = 0; i < max; i++){
        	int x = guiLeft + 66 + i % 20 * 5;
        	int offset = i / 20 * 10;
            this.drawTexturedModalRect(x, y + offset, 52 + i % 2 * 5, 9,  i % 2 == 1?4:5, 9);
            if(k > i)
                this.drawTexturedModalRect(x, y + offset, 52 + i % 2 * 5, 0, i % 2 == 1?4:5, 9);
        }
        
        k = role.foodstats.getFoodLevel();
        y += 10;
        if(max > 20)
            y += 10;
        
        for(int i = 0; i < 20; i++){
        	int x = guiLeft + 66 + i % 20 * 5;
            this.drawTexturedModalRect(x, y, 16 + i % 2 * 5, 27, i % 2 == 1?4:5, 9);
            if(k > i)
                this.drawTexturedModalRect(x, y, 52 + i % 2 * 5, 27, i % 2 == 1?4:5, 9);
        }
        return y;
	}

	@Override
	public void save() {
		
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		role.readFromNBT(compound);
	}
}
