package noppes.npcs.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.controllers.RecipeCarpentry;

public class ContainerManageRecipes extends Container
{
    private InventoryBasic craftingMatrix;
    public RecipeCarpentry recipe;
    public int size;
    public int width;
    private boolean init = false;
    public ContainerManageRecipes(EntityPlayer player,int size)
    {
    	this.size = size * size;
    	this.width = size;
    	craftingMatrix = new InventoryBasic("crafting", false, this.size + 1);
    	recipe = new RecipeCarpentry("");
    	addSlotToContainer(new Slot(craftingMatrix,0,87,61));

    	for(int i = 0; i < size;i++){
        	for(int j = 0; j < size;j++){
            	addSlotToContainer(new Slot(craftingMatrix, i * width + j + 1,j*18 + 8, i*18 + 35));
        	}
    	}
        
        for(int i1 = 0; i1 < 3; i1++)
        {
            for(int l1 = 0; l1 < 9; l1++)
            {
            	addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 113 + i1 * 18));
            }

        }

        for(int j1 = 0; j1 < 9; j1++)
        {
        	addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 171));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i)
    {
        return null;
    }
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
	
	public void setRecipe(RecipeCarpentry recipe) {
    	craftingMatrix.setInventorySlotContents(0,recipe.getRecipeOutput());
    	for(int i = 0; i < width;i++){
        	for(int j = 0; j < width;j++){
        		if(j >= recipe.recipeWidth)
        			craftingMatrix.setInventorySlotContents(i*width + j + 1,null);
        		else
        			craftingMatrix.setInventorySlotContents(i*width + j + 1, recipe.getCraftingItem(i * recipe.recipeWidth + j));
        	}
    	}
    	this.recipe = recipe;
	}
	public void saveRecipe(){
		int nextChar = 0;
		char[] chars = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P'};
		Map<ItemStack,Character> nameMapping = new HashMap<ItemStack,Character>();
		int firstRow = width,lastRow = 0,firstColumn = width,lastColumn = 0;
		boolean seenRow = false;
    	for(int i = 0; i < width;i++){
    		boolean seenColumn = false;
        	for(int j = 0; j < width;j++){
	        	ItemStack item = craftingMatrix.getStackInSlot(i * width + j + 1);
	        	if(item == null){
	        		continue;
	        	}else{
	        		if(!seenColumn && j < firstColumn){
	        			firstColumn = j;
	        		}
	        		if(j > lastColumn)
	        			lastColumn = j;
	        		
	        		seenColumn = true;
	        	}
	        	Character letter = null;
	        	for(ItemStack mapped : nameMapping.keySet()){
	        		if(NoppesUtilPlayer.compareItems(mapped, item, recipe.ignoreDamage, recipe.ignoreNBT))
	        			letter = nameMapping.get(mapped);
	        	}
	        	if(letter == null){
	        		letter = chars[nextChar];
	        		nextChar++;
	        		nameMapping.put(item, letter);
	        	}
        	}
        	if(seenColumn){
        		if(!seenRow){
	        		firstRow = i;
	        		lastRow = i;
	        		seenRow = true;
        		}
        		else{
        			lastRow = i;
        		}
        	}
        	
    	}    	
		ArrayList<Object> recipe = new ArrayList<Object>();

    	for(int i = 0; i < width;i++){
    		if(i < firstRow || i > lastRow)
    			continue;
    		String row = "";
        	for(int j = 0; j < width;j++){
        		if(j < firstColumn|| j > lastColumn)
        			continue;
	        	ItemStack item = craftingMatrix.getStackInSlot(i * width + j + 1);
	        	if(item == null){
	        		row += " ";
	        		continue;
	        	}

	        	for(ItemStack mapped : nameMapping.keySet()){
	        		if(NoppesUtilPlayer.compareItems(mapped, item, false, false)){
	        			row += nameMapping.get(mapped);
	        		}
	        	}
	        	
        	}
        	recipe.add(row);
    	}
		if(nameMapping.isEmpty()){
			RecipeCarpentry r = new RecipeCarpentry(this.recipe.name);
			r.copy(this.recipe);
			this.recipe = r;
			return;
		}
    	for(ItemStack mapped : nameMapping.keySet()){
    		Character letter = nameMapping.get(mapped);
    		recipe.add(letter);
    		recipe.add(mapped);
    	}
    	this.recipe = RecipeCarpentry.saveRecipe(this.recipe, craftingMatrix.getStackInSlot(0), recipe.toArray());
	}
}
