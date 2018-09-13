package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.controllers.RecipeCarpentry;
import noppes.npcs.controllers.RecipeController;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRecipes extends GuiNPCInterface
{
	private static final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/slot.png");
    private int page = 0;
    private boolean npcRecipes = true;
    private GuiNpcLabel label;
    private GuiNpcButton left, right;
    private List<IRecipe> recipes = new ArrayList<IRecipe>();

    public GuiRecipes(){
        this.ySize = 182;
        this.xSize = 256;
        setBackground("recipes.png");
        this.closeOnEsc = true;
		recipes.addAll(RecipeController.instance.anvilRecipes.values());
    }
    @Override
    public void initGui(){
    	super.initGui();

    	addLabel(new GuiNpcLabel(0, "Recipe List", guiLeft + 5, guiTop + 5));
    	addLabel(label = new GuiNpcLabel(1, "", guiLeft + 5, guiTop + 168));

        addButton(this.left = new GuiButtonNextPage(1, guiLeft + 150, guiTop + 164, true));
        addButton(this.right = new GuiButtonNextPage(2, guiLeft + 80, guiTop + 164, false));
        
        updateButton();
    }
    private void updateButton(){
    	right.visible = right.enabled = page > 0;
    	left.visible = left.enabled = page + 1 < MathHelper.ceiling_float_int(recipes.size() / 4f);
    }
    
    protected void actionPerformed(GuiButton button){
    	if(!button.enabled)
    		return;

    	if(button == right)
    		page--;
    	if(button == left)
    		page++;
        updateButton();
    }
    
    @Override
    public void drawScreen(int xMouse, int yMouse, float f){
    	super.drawScreen(xMouse, yMouse, f);
    	mc.renderEngine.bindTexture(resource);
		
		label.label = page + 1 + "/" + MathHelper.ceiling_float_int(recipes.size() / 4f);
		label.x = guiLeft + (256 - Minecraft.getMinecraft().fontRenderer.getStringWidth(label.label)) / 2;
		for(int i = 0; i < 4; i++){
			int index = i + page * 4;
			if(index >= recipes.size())
				break;
			IRecipe irecipe = recipes.get(index);
			if(irecipe.getRecipeOutput() == null)
				continue;
			int x = guiLeft + 5 + i / 2 * 126;
			int y = guiTop + 15 + i % 2 * 76;
			drawItem(irecipe.getRecipeOutput(), x + 98, y + 28, xMouse, yMouse);
			if(irecipe instanceof RecipeCarpentry){
				RecipeCarpentry recipe = (RecipeCarpentry) irecipe;
				x += (72 - recipe.recipeWidth * 18) / 2;
				y += (72 - recipe.recipeHeight * 18) / 2;
				for(int j = 0; j < recipe.recipeWidth; j++){
					for(int k = 0; k < recipe.recipeHeight; k++){
				    	mc.renderEngine.bindTexture(resource);
				        GL11.glColor4f(1, 1, 1, 1);
				        drawTexturedModalRect(x + j * 18, y + k * 18, 0, 0, 18, 18);
				        ItemStack item = recipe.getCraftingItem(j + k * recipe.recipeWidth);
				        if(item == null)
				        	continue;
				        drawItem(item, x + j * 18 + 1, y + k * 18 + 1, xMouse, yMouse);
					}
				}
			}
		}
		for(int i = 0; i < 4; i++){
			int index = i + page * 4;
			if(index >= recipes.size())
				break;
			IRecipe irecipe = recipes.get(index);
			if(irecipe instanceof RecipeCarpentry){
				RecipeCarpentry recipe = (RecipeCarpentry) irecipe;
				if(recipe.getRecipeOutput() == null)
					continue;
				int x = guiLeft + 5 + i / 2 * 126;
				int y = guiTop + 15 + i % 2 * 76;
				drawOverlay(recipe.getRecipeOutput(), x + 98, y + 22, xMouse, yMouse);
				x += (72 - recipe.recipeWidth * 18) / 2;
				y += (72 - recipe.recipeHeight * 18) / 2;
				for(int j = 0; j < recipe.recipeWidth; j++){
					for(int k = 0; k < recipe.recipeHeight; k++){
				        ItemStack item = recipe.getCraftingItem(j + k * recipe.recipeWidth);
				        if(item == null)
				        	continue;
				        drawOverlay(item, x + j * 18 + 1, y + k * 18 + 1, xMouse, yMouse);
					}
				}
			}
		}
    }

    private void drawItem(ItemStack item, int x, int y, int xMouse, int yMouse){
    	GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.zLevel = 100.0F;
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item, x, y);
        itemRender.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting(); 
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    	GL11.glPopMatrix();
    }
    
    private void drawOverlay(ItemStack item, int x, int y, int xMouse, int yMouse){
        if (this.func_146978_c(x - guiLeft, y - guiTop, 16, 16, xMouse, yMouse)){
            this.renderToolTip(item, xMouse, yMouse);
        }
    }
    protected boolean func_146978_c(int p_146978_1_, int p_146978_2_, int p_146978_3_, int p_146978_4_, int p_146978_5_, int p_146978_6_)
    {
        int k1 = this.guiLeft;
        int l1 = this.guiTop;
        p_146978_5_ -= k1;
        p_146978_6_ -= l1;
        return p_146978_5_ >= p_146978_1_ - 1 && p_146978_5_ < p_146978_1_ + p_146978_3_ + 1 && p_146978_6_ >= p_146978_2_ - 1 && p_146978_6_ < p_146978_2_ + p_146978_4_ + 1;
    }

	@Override
	public void save() {
	}
}