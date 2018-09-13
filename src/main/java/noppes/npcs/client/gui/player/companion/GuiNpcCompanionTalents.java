package noppes.npcs.client.gui.player.companion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiNpcCompanionTalents extends GuiNPCInterface{
	private RoleCompanion role;
	private Map<Integer,GuiTalent> talents = new HashMap<Integer,GuiTalent>();	
	private GuiNpcButton selected;

	public GuiNpcCompanionTalents(EntityNPCInterface npc) {
		super(npc);
		role = (RoleCompanion) npc.roleInterface;
		closeOnEsc = true;
		setBackground("companion_empty.png");
		xSize = 171;
		ySize = 166;
	}

	@Override
	public void initGui() {
		super.initGui();
    	talents.clear();
		int y = guiTop + 12;
		
		addLabel(new GuiNpcLabel(0, NoppesStringUtils.translate("quest.exp", ": "), guiLeft + 4, guiTop + 10));

		GuiNpcCompanionStats.addTopMenu(role, this, 2);
		int i = 0;
		for(EnumCompanionTalent e : role.talents.keySet()){
			addTalent(i++, e);
		}
	}
	
	private void addTalent(int i, EnumCompanionTalent talent){
		int y = guiTop + 28 + i/2 * 26;
		int x = guiLeft + 4 + i % 2 * 84;
		talents.put(i, new GuiTalent(role, talent, x, y));
		if(role.getTalentLevel(talent) < 5){
			addButton(new GuiNpcButton(i + 10, x + 26, y, 14, 14, "+"));
			y += 8;
		}
		addLabel(new GuiNpcLabel(i, role.talents.get(talent) + "/" + role.getNextLevel(talent), x + 26, y + 8));
	}

	@Override
	public void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		int id = guibutton.id;
		if(id == 1){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.Companion);
		}
		if(id == 3){
			NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv);
		}
		if(id >= 10){
			selected = (GuiNpcButton) guibutton;
			lastPressedTime = startPressedTime = mc.theWorld.getWorldTime();
			addExperience(1);
		}
	}
	
	private void addExperience(int exp){
		EnumCompanionTalent talent = talents.get(selected.id - 10).talent;
		if(!role.canAddExp(-exp) && role.currentExp <= 0)
			return;
		if(exp > role.currentExp)
			exp = role.currentExp;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionTalentExp, talent.ordinal(), exp);
		role.talents.put(talent, role.talents.get(talent) + exp);
		role.addExp(-exp);
		getLabel(selected.id - 10).label = role.talents.get(talent) + "/" + role.getNextLevel(talent);
	}
	
	private long lastPressedTime = 0;
	private long startPressedTime = 0;
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if(selected != null && mc.theWorld.getWorldTime() - startPressedTime > 4 && lastPressedTime < mc.theWorld.getWorldTime() && mc.theWorld.getWorldTime() % 4 == 0){
			if(selected.mousePressed(mc, i, j) && Mouse.isButtonDown(0)){
				lastPressedTime = mc.theWorld.getWorldTime();
				if(lastPressedTime - startPressedTime < 20)
					addExperience(1);
				else if(lastPressedTime - startPressedTime < 40)
					addExperience(2);
				else if(lastPressedTime - startPressedTime < 60)
					addExperience(4);
				else if(lastPressedTime - startPressedTime < 90)
					addExperience(8);
				else if(lastPressedTime - startPressedTime < 140)
					addExperience(14);
				else
					addExperience(28);
			}
			else{
				lastPressedTime = 0;
				selected = null;
			}
		}
		
        mc.getTextureManager().bindTexture(Gui.icons);
        this.drawTexturedModalRect(guiLeft + 4, guiTop + 20, 10, 64, 162, 5);

        if (role.currentExp > 0){
        	float v = 1f * role.currentExp / role.getMaxExp();
        	if(v > 1)
        		v = 1;
            this.drawTexturedModalRect(guiLeft + 4, guiTop + 20, 10, 69, (int)(v * 162), 5);
        }
        String s = role.currentExp + "\\" + role.getMaxExp();
        mc.fontRenderer.drawString(s, guiLeft + xSize / 2 - mc.fontRenderer.getStringWidth(s) / 2, guiTop + 10, CustomNpcResourceListener.DefaultTextColor);

		for(GuiTalent talent : talents.values()){
			talent.drawScreen(i, j, f);
		}
	}

	@Override
	public void save() {
		
	}
	
	public static class GuiTalent extends GuiScreen{
		private EnumCompanionTalent talent;
		private int x, y;
		private RoleCompanion role;
		private static final ResourceLocation resource = new ResourceLocation("customnpcs:textures/gui/talent.png");
		public GuiTalent(RoleCompanion role, EnumCompanionTalent talent, int x, int y){
			this.talent = talent;
			this.x = x;
			this.y = y;
			this.role = role;
		}
		
		@Override
		public void drawScreen(int i, int j, float f) {
	        Minecraft mc = Minecraft.getMinecraft();
	        mc.getTextureManager().bindTexture(resource);
	        
			ItemStack item = talent.item;
			if(item.getItem() == null)
				item = new ItemStack(Blocks.dirt);
	        GL11.glPushMatrix();
	        GL11.glColor3f(1F, 1F, 1F);
	        GL11.glEnable(GL11.GL_BLEND);
	        boolean hover = x < i && x + 24 > i && y < j && y + 24 > j;
	        this.drawTexturedModalRect(x, y, 0, hover?24:0, 24, 24);
	        this.zLevel = 100.0F;
	        itemRender.zLevel = 100.0F;
	        GL11.glEnable(GL11.GL_LIGHTING);
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        RenderHelper.enableGUIStandardItemLighting();
	        itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, x + 4, y + 4);
	        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, x + 4, y + 4);
	        RenderHelper.disableStandardItemLighting(); 
	        GL11.glDisable(GL11.GL_LIGHTING);
	        GL11.glTranslatef(0, 0, 200);
            this.drawCenteredString(mc.fontRenderer, role.getTalentLevel(talent) + "", x + 20, y + 16, 0xFFFFFF);
	        itemRender.zLevel = 0.0F;
	        this.zLevel = 0.0F;
	        GL11.glPopMatrix();
		}
	}
}
