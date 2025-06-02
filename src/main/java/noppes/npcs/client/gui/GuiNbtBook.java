package noppes.npcs.client.gui;

import kamkeel.npcs.network.packets.request.nbtbook.NbtBookPacket;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import org.lwjgl.opengl.GL11;

public class GuiNbtBook extends GuiNPCInterface implements IGuiData{

	private int x, y, z;
	private TileEntity tile;
	private Block block;
	private ItemStack blockStack;

	private int entityId;
	private Entity entity;

	private NBTTagCompound originalCompound;
	private NBTTagCompound compound;

	private String faultyText = null;
	private String errorMessage = null;

    public GuiNbtBook(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
	}

	@Override
	public void initGui(){
    	super.initGui();
    	if(block != null) {
    		addLabel(new GuiNpcLabel(11, "x: " + x + ", y: " + y + ", z: " + z, guiLeft + 60, guiTop + 6));
    		addLabel(new GuiNpcLabel(12, "id: " + Block.blockRegistry.getNameForObject(block), guiLeft + 60, guiTop + 16));
    	}
    	if(entity != null) {
    		addLabel(new GuiNpcLabel(12, "id: " + entity.getClass().getSimpleName(), guiLeft + 60, guiTop + 6));
    	}

        addButton(new GuiNpcButton(0, guiLeft + 38, guiTop + 144, 180,20, "nbt.edit"));
        getButton(0).enabled = compound != null && !compound.hasNoTags();

        addLabel(new GuiNpcLabel(0, "", guiLeft + 4, guiTop + 167));
        addLabel(new GuiNpcLabel(1, "", guiLeft + 4, guiTop + 177));

        addButton(new GuiNpcButton(66, guiLeft + 128, guiTop + 190,120,20, "gui.close"));
        addButton(new GuiNpcButton(67, guiLeft + 4, guiTop + 190,120,20, "gui.save"));

        if(errorMessage != null) {
        	getButton(67).enabled = false;
    		int i = errorMessage.indexOf(" at: ");
    		if(i > 0) {
                getLabel(0).label = errorMessage.substring(0, i);
                getLabel(1).label = errorMessage.substring(i);
    		}
    		else {
                getLabel(0).label = errorMessage;
    		}
        }
        if(getButton(67).enabled && originalCompound != null) {
        	getButton(67).enabled = !originalCompound.equals(compound);
        }
    }

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if(id == 0) {
			if(faultyText != null) {
				setSubGui(new SubGuiNpcTextArea(compound.toString(), faultyText).enableHighlighting());
			}
			else {
				setSubGui(new SubGuiNpcTextArea(compound.toString()).enableHighlighting());
			}
		}
		if(id == 67) {
            getLabel(0).label = "Saved";
			if(compound.equals(originalCompound))
				return;
			if(tile == null) {
                NbtBookPacket.SaveEntity(entityId, compound);
				return;
			}
			else {
                NbtBookPacket.SaveBlock(x, y, z, compound);
			}
			originalCompound = (NBTTagCompound) compound.copy();
        	getButton(67).enabled = false;
		}
		if(id == 66) {
			close();
		}
	}

	@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    	super.drawScreen(mouseX, mouseY, partialTicks);
    	if(hasSubGui())
    		return;

    	if(block != null) {
    		GL11.glPopMatrix();
    		GL11.glTranslatef(guiLeft + 4, guiTop + 4, 0);
    		GL11.glScalef(3, 3, 3);
            RenderHelper.enableGUIStandardItemLighting();
            if(blockStack != null){
                itemRender.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), blockStack,0, 0);
                itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), blockStack, 0, 0);
            }
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
    	}

    	if(entity instanceof EntityLivingBase) {
    		drawNpc((EntityLivingBase)entity, 20, 80, 1, 0);
    	}
    }

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);

		if(gui instanceof SubGuiNpcTextArea){
			try {
				compound = (NBTTagCompound) JsonToNBT.func_150315_a(((SubGuiNpcTextArea)gui).text);
				errorMessage = faultyText = null;
			} catch (NBTException e) {
				errorMessage = e.getLocalizedMessage();
				faultyText = ((SubGuiNpcTextArea)gui).text;
			}
	        initGui();
		}
	}

	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(compound.hasKey("EntityId")) {
			entityId = compound.getInteger("EntityId");
			entity = player.worldObj.getEntityByID(entityId);
		}
		else {
			tile = player.worldObj.getTileEntity(x, y, z);
			block = player.worldObj.getBlock(x, y, z);
            if(block != null && Item.getItemFromBlock(block) != null){
                blockStack = new ItemStack(Item.getItemFromBlock(block), 1, player.worldObj.getBlockMetadata(x, y, z));
            }
		}

		originalCompound = compound.getCompoundTag("Data");
		this.compound = (NBTTagCompound) originalCompound.copy();
		initGui();
	}
}
