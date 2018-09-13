package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiNPCTrader extends GuiContainerNPCInterface{
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/trader.png");
	private final ResourceLocation slot = new ResourceLocation("customnpcs","textures/gui/slot.png");
	private RoleTrader role;
	private ContainerNPCTrader container;
	
    public GuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader container){
        super(npc, container);
        this.container = container;
        role = (RoleTrader) npc.roleInterface;
        closeOnEsc = true;
        ySize = 224;
        xSize = 223;
        this.title = "role.trader";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
        this.drawWorldBackground(0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        mc.renderEngine.bindTexture(slot);
		for(int slot = 0; slot < 18; slot++){
			int x = guiLeft + slot%3 * 72 + 10;
			int y = guiTop + slot/3 * 21 + 6;
			
			ItemStack item = role.inventoryCurrency.items.get(slot);
			ItemStack item2 = role.inventoryCurrency.items.get(slot + 18);
			if(item == null){
				item = item2;
				item2 = null;
			}
			if(NoppesUtilPlayer.compareItems(item, item2, false, false)){
				item = item.copy();
				item.stackSize += item2.stackSize;
				item2 = null;
			}

			ItemStack sold = role.inventorySold.items.get(slot);
	        GL11.glColor4f(1, 1, 1, 1);
	        mc.renderEngine.bindTexture(this.slot);
	        drawTexturedModalRect(x + 42, y, 0, 0, 18, 18);
			if(item != null && sold != null){
		        RenderHelper.enableGUIStandardItemLighting();
		        
	            if(item2 != null){
	            	itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item2, x, y + 1);
		        	itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item2, x, y + 1);
	            }
		        GL11.glColor4f(1, 1, 1, 1);
		        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item, x + 18, y + 1);
		        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item, x + 18, y + 1);
	            RenderHelper.disableStandardItemLighting(); 
	
	            fontRendererObj.drawString("=", x + 36, y + 5, CustomNpcResourceListener.DefaultTextColor);

			}
    	}
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
    	super.drawGuiContainerBackgroundLayer(f, i, j);
    }
    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2){
		for(int slot = 0; slot < 18; slot++){
			int x = slot%3 * 72 + 10;
			int y = slot/3 * 21 + 6;
			
			ItemStack item = role.inventoryCurrency.items.get(slot);
			ItemStack item2 = role.inventoryCurrency.items.get(slot + 18);
			if(item == null){
				item = item2;
				item2 = null;
			}
			if(NoppesUtilPlayer.compareItems(item, item2, role.ignoreDamage, role.ignoreNBT)){
				item = item.copy();
				item.stackSize += item2.stackSize;
				item2 = null;
			}
			ItemStack sold = role.inventorySold.items.get(slot);
			if(item == null || sold == null)
				continue;

			if(this.func_146978_c(x + 43, y + 1, 16, 16, par1, par2)){
				if(!container.canBuy(slot, player)){
		        	GL11.glTranslatef(0, 0, 300);
					if(item != null && !NoppesUtilPlayer.compareItems(player, item, role.ignoreDamage, role.ignoreNBT))
						this.drawGradientRect(x + 17, y, x + 35, y + 18, 0x70771010, 0x70771010);
					if(item2 != null && !NoppesUtilPlayer.compareItems(player, item2, role.ignoreDamage, role.ignoreNBT))
						this.drawGradientRect(x - 1, y, x + 17, y + 18, 0x70771010, 0x70771010);
					
		        	String title = StatCollector.translateToLocal("trader.insufficient");
					this.fontRendererObj.drawString(title, (xSize - fontRendererObj.getStringWidth(title))/2, 131, 0xDD0000);
		        	GL11.glTranslatef(0, 0, -300);
				}
				else{
		        	String title = StatCollector.translateToLocal("trader.sufficient");
					this.fontRendererObj.drawString(title, (xSize - fontRendererObj.getStringWidth(title))/2, 131, 0x00DD00);
				}
			}

            if (this.func_146978_c(x, y, 16, 16, par1, par2) && item2 != null){
                this.renderToolTip(item2, par1 - guiLeft, par2 - guiTop);
            }
            if (this.func_146978_c(x + 18, y, 16, 16, par1, par2)){
                this.renderToolTip(item, par1 - guiLeft, par2 - guiTop);
            }
    	}
    }
    
	@Override
	public void save() {
	}
}
