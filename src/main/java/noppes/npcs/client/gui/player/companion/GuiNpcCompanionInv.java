package noppes.npcs.client.gui.player.companion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiMenuTopIconButton;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

import org.lwjgl.opengl.GL11;

public class GuiNpcCompanionInv extends GuiContainerNPCInterface{
	private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/companioninv.png");
	private final ResourceLocation slot = new ResourceLocation("customnpcs", "textures/gui/slot.png");
	private EntityNPCInterface npc;
	private RoleCompanion role;

	public GuiNpcCompanionInv(EntityNPCInterface npc,
			ContainerNPCCompanion container) {
		super(npc, container);
		this.npc = npc;
		role = (RoleCompanion) npc.roleInterface;
		closeOnEsc = true;
		xSize = 171;
		ySize = 166;
	}

	@Override
	public void initGui() {
		super.initGui();
		GuiNpcCompanionStats.addTopMenu(role, this, 3);
	
	}

	@Override
	public void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		int id = guibutton.id;
		if(id == 1){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.Companion);
		}
		if(id == 2){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.CompanionTalent);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int xMouse, int yMouse) {
        this.drawWorldBackground(0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(resource);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		mc.renderEngine.bindTexture(slot);
		if(role.getTalentLevel(EnumCompanionTalent.ARMOR) > 0){
			for(int i = 0; i < 4; i++){
				drawTexturedModalRect(guiLeft + 5, guiTop + 7 + i * 18 , 0, 0, 18, 18);
			}
		}
		if(role.getTalentLevel(EnumCompanionTalent.SWORD) > 0){
			drawTexturedModalRect(guiLeft + 78, guiTop + 16, 0, npc.inventory.weapons.get(0) == null?18:0, 18, 18);
		}
		if(role.getTalentLevel(EnumCompanionTalent.RANGED) > 0){
			
		}
		if(role.talents.containsKey(EnumCompanionTalent.INVENTORY)){
			int size = (role.getTalentLevel(EnumCompanionTalent.INVENTORY) + 1) * 2;
			for(int i = 0; i < size; i++){
				drawTexturedModalRect(guiLeft + 113 + i % 3 * 18, guiTop + 7 + i / 3 * 18 , 0, 0, 18, 18);
			}
		}

		super.drawNpc(52, 70);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
	}

	@Override
	public void save() {
		
	}
}
