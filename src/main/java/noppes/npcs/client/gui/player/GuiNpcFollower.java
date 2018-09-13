package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiNpcFollower extends GuiContainerNPCInterface  implements IGuiData{
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/follower.png");
	private EntityNPCInterface npc;
	private RoleFollower role;
	
    public GuiNpcFollower(EntityNPCInterface npc,ContainerNPCFollower container){
        super(npc, container);
        this.npc = npc;
        role = (RoleFollower) npc.roleInterface;
        closeOnEsc = true;
        
        NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
    }
    @Override
    public void initGui(){
    	super.initGui();
        buttonList.clear();
        addButton(new GuiNpcButton(4, guiLeft + 100, guiTop+ 110, 50, 20, new String[]{StatCollector.translateToLocal("follower.waiting"),StatCollector.translateToLocal("follower.following")},role.isFollowing?1:0));
        if(!role.infiniteDays)
        	addButton(new GuiNpcButton(5, guiLeft + 8, guiTop+ 30, 50, 20, StatCollector.translateToLocal("follower.hire")));
    }
    @Override
    public void actionPerformed(GuiButton guibutton){
    	super.actionPerformed(guibutton);
    	int id = guibutton.id;
        if(id == 4){
        	NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerState);
        }
        if(id == 5){
        	NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerExtend);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2){
    	fontRendererObj.drawString(StatCollector.translateToLocal("follower.health")+": " + npc.getHealth()+"/"+ npc.getMaxHealth(), 62, 70, CustomNpcResourceListener.DefaultTextColor);
    	if(!role.infiniteDays){
	       if(role.getDaysLeft() <= 1)
	    	   fontRendererObj.drawString(StatCollector.translateToLocal("follower.daysleft")+": " + StatCollector.translateToLocal("follower.lastday"), 62, 94, CustomNpcResourceListener.DefaultTextColor);
	       else
	    	   fontRendererObj.drawString(StatCollector.translateToLocal("follower.daysleft") + ": " + (role.getDaysLeft()-1), 62, 94, CustomNpcResourceListener.DefaultTextColor);   
    	}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
		int l = guiLeft;
	    int i1 = guiTop;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
		int index = 0;
		if(!role.infiniteDays){
			for(int id : role.inventory.items.keySet()){
				ItemStack itemstack = role.inventory.items.get(id);
				if(itemstack == null)
					continue;
	            int days = 1;
	            if(role.rates.containsKey(id))
	            	days = role.rates.get(id);
	            
				int yOffset = index * 20;
				
				int x = guiLeft +  68;
				int y = guiTop + yOffset + 4;
	            RenderHelper.enableGUIStandardItemLighting();
	            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	            GL11.glEnable(GL11.GL_LIGHTING);
	            itemRender.renderItemIntoGUI(fontRendererObj, mc.renderEngine, itemstack, x + 11,y);
	            itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, itemstack, x+11,y);
	            RenderHelper.disableStandardItemLighting(); 
	            GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
	
	            String daysS = days + " " + ((days == 1)?StatCollector.translateToLocal("follower.day"):StatCollector.translateToLocal("follower.days"));
	            fontRendererObj.drawString(" = "+daysS, x + 27, y + 4, CustomNpcResourceListener.DefaultTextColor);
		        //fontRenderer.drawString(quantity, x + 0 + (12-fontRenderer.getStringWidth(quantity))/2, y + 4, 0x404040);
		        
		        index++;
	    	}
		}
		this.drawNpc(33, 131);
    }
    
	@Override
	public void save() {
		
	}
	@Override
	public void setGuiData(NBTTagCompound compound) {
		npc.roleInterface.readFromNBT(compound);
		initGui();
	}
}
