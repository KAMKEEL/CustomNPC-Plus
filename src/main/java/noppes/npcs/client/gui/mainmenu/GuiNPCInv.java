package noppes.npcs.client.gui.mainmenu;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class GuiNPCInv extends GuiContainerNPCInterface2 implements IGuiData, ITextfieldListener
{
	private HashMap<Integer,Double> chances = new HashMap<Integer, Double>();
	private ContainerNPCInv container;
	private ResourceLocation slot;
    private int inventoryTab = 0;

    public GuiNPCInv(EntityNPCInterface npc,ContainerNPCInv container)
    {
        super(npc,container,3);
        this.setBackground("npcinv.png");
        this.container = container;
        ySize = 200;
        slot = getResource("slot.png");
        Client.sendData(EnumPacketServer.MainmenuInvGet);
    }
    
    public void initGui()
    {
        super.initGui();
        addLabel(new GuiNpcLabel(0,"inv.minExp", guiLeft + 118, guiTop + 18));
        addTextField(new GuiNpcTextField(0,this, fontRendererObj, guiLeft + 108, guiTop + 29, 60, 20, npc.inventory.minExp + ""));
        getTextField(0).integersOnly = true;
        
        addLabel(new GuiNpcLabel(1,"inv.maxExp", guiLeft + 118, guiTop + 52));
        addTextField(new GuiNpcTextField(1,this, fontRendererObj, guiLeft + 108, guiTop + 63, 60, 20, npc.inventory.maxExp + ""));
        getTextField(1).integersOnly = true;

        getTextField(0).setMinMaxDefault(0, getTextField(1).getInteger(), 0);
        getTextField(1).setMinMaxDefault(getTextField(0).getInteger(), Short.MAX_VALUE, 0);

        addButton(new GuiNpcButton(10, guiLeft + 88, guiTop + 88, 80, 20, new String[]{"stats.normal", "inv.auto"}, npc.inventory.lootMode));

        addLabel(new GuiNpcLabel(2,"inv.npcInventory", guiLeft + 191, guiTop + 5));
        addLabel(new GuiNpcLabel(3,"inv.inventory", guiLeft + 8, guiTop + 101));

        addLabel(new GuiNpcLabel(4,"Tab", guiLeft + 381, guiTop + 5));
        addButton(new GuiNpcButton(11, guiLeft + 375, guiTop + 13, 30, 20, "1"));
        addButton(new GuiNpcButton(12, guiLeft + 375, guiTop + 34, 30, 20, "2"));

        for(int c = 0; c < 4; c++) {
            for (int r = 0; r < 9; r++) {
                double chance = 100;
                if (npc.inventory.dropchance.containsKey(r + c*9)) {
                    chance = npc.inventory.dropchance.get(r + c*9);
                }
                if (chance <= 0 || chance > 100)
                    chance = 100;
                chances.put(r + c*9, chance);

                if(Math.floor((float)c/2) != this.inventoryTab) {
                    container.getSlot(r + c*9 + 7).xDisplayPosition = 10000;
                    container.getSlot(r + c*9 + 7).yDisplayPosition = 10000;
                    continue;
                } else {
                    container.getSlot(r + c*9 + 7).xDisplayPosition = 191 + (c-this.inventoryTab*2) * 90;
                    container.getSlot(r + c*9 + 7).yDisplayPosition = 16 + r * 21;
                }

                GuiNpcTextField textField = new GuiNpcTextField(2 + r + c*9, this, fontRendererObj, guiLeft + 210 + (c-this.inventoryTab*2)*90, guiTop + 14 + r * 21, 60, 18, chance + "");
                addLabel(new GuiNpcLabel(c*9 + r + 5,"%", guiLeft + 272 + (c-this.inventoryTab*2)*90, guiTop + 16 + r * 21));
                textField.doublesOnly = true;
                textField.setMinMaxDefaultDouble(0, 100, 100);
                addTextField(textField);
            }
        }
    }

    private void drawSlot(int x, int y)
    {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND); // Forge: Blending needs to be enabled for this.
        this.mc.getTextureManager().bindTexture(slot);
        this.drawTexturedModalRect(x, y, 0,0,18, 18);
        GL11.glDisable(GL11.GL_BLEND); // Forge: And clean that up
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
	protected void actionPerformed(GuiButton guibutton) {
    	if(guibutton.id == 10){
    		npc.inventory.lootMode = ((GuiNpcButton)guibutton).getValue();
    	}
        if(guibutton.id == 11 || guibutton.id == 12){
            this.inventoryTab = Integer.parseInt(guibutton.displayString)-1;
            initGui();
        }
    }

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
    	super.drawGuiContainerBackgroundLayer(f, i, j);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(slot);

    	for(int id = 4; id <= 6;id++){
        	Slot slot = container.getSlot(id);
        	if(slot.getHasStack())
        		drawTexturedModalRect(guiLeft + slot.xDisplayPosition - 1, guiTop + slot.yDisplayPosition -1, 0, 0, 18, 18);
    	}

        for(int c = 0; c < 2; c++) {
            for (int r = 0; r < 9; r++) {
                this.drawSlot(guiLeft + 190 + c*90, guiTop + 15 + r * 21);
            }
        }
    }
    public void drawScreen(int i, int j, float f)
    {
    	int showname = npc.display.showName;
    	npc.display.showName = 1;
       	int l = guiLeft + 20; //(width/2)-180;
    	int i1 = (height/2) - 145;
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glEnable(2903 /*GL_COLOR_MATERIAL*/);
        GL11.glPushMatrix();
        GL11.glTranslatef(l + 33, i1 + 131, 50F);
        float f1 = (50F * 3) / npc.display.modelSize;
        GL11.glScalef(-f1, f1, f1);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        float f2 = npc.renderYawOffset;
        float f3 = npc.rotationYaw;
        float f4 = npc.rotationPitch;
        float f7 = npc.rotationYawHead;
        float f5 = (float)(l + 33) - i;
        float f6 = (float)((i1 + 131) - 50) - j;
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float)Math.atan(f6 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
        npc.renderYawOffset = (float)Math.atan(f5 / 40F) * 20F;
        npc.rotationYaw = (float)Math.atan(f5 / 40F) * 40F;
        npc.rotationPitch = -(float)Math.atan(f6 / 40F) * 20F;
        npc.rotationYawHead = npc.rotationYaw;
        //npc.updateCloak();
        GL11.glTranslatef(0.0F, npc.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180F;
        RenderManager.instance.renderEntityWithPosYaw(npc, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        npc.renderYawOffset = f2;
        npc.rotationYaw = f3;
        npc.rotationPitch = f4;
        npc.rotationYawHead = f7;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	npc.display.showName = showname;
    	
        super.drawScreen(i, j, f);
    }
    
	@Override
	public void save() {
        npc.inventory.dropchance = chances;
    	npc.inventory.minExp = getTextField(0).getInteger();
    	npc.inventory.maxExp = getTextField(1).getInteger();
    	Client.sendData(EnumPacketServer.MainmenuInvSave, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void setGuiData(NBTTagCompound compound) {
		npc.inventory.readEntityFromNBT(compound);
		initGui();
	}

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if(textfield.id >= 2){
            chances.put(textfield.id-2, Double.parseDouble(textfield.getText()));
        }
        getTextField(0).setMinMaxDefault(0, getTextField(1).getInteger(), 0);
        getTextField(1).setMinMaxDefault(getTextField(0).getInteger(), Short.MAX_VALUE, 0);
        this.save();
    }
}
