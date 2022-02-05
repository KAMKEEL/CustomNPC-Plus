package noppes.npcs.controllers;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;

public class RecipeCarpentry extends ShapedRecipes{
	public int id = -1;
	public String name = "";
    public Availability availability = new Availability();
    public boolean isGlobal = false;
    public boolean ignoreDamage = false;
    public boolean ignoreNBT = false;
    

    public RecipeCarpentry(int width, int height, ItemStack[] recipe, ItemStack result){
    	super(width, height, recipe, result);
    }
    public RecipeCarpentry(String name){
    	super(0, 0, new ItemStack[0], null);
    	this.name = name;
    }
    
    public static RecipeCarpentry read(NBTTagCompound compound){
    	RecipeCarpentry recipe = new RecipeCarpentry(compound.getInteger("Width"), compound.getInteger("Height"), 
    			NBTTags.getItemStackArray(compound.getTagList("Materials", 10)), NoppesUtilServer.readItem(compound.getCompoundTag("Item")));
		recipe.name = compound.getString("Name");
		recipe.id = compound.getInteger("ID");
		recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
		recipe.ignoreDamage = compound.getBoolean("IgnoreDamage");
		recipe.ignoreNBT = compound.getBoolean("IgnoreNBT");
		recipe.isGlobal = compound.getBoolean("Global");
		
		return recipe;
    }

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("ID", id);
		compound.setInteger("Width", recipeWidth);
		compound.setInteger("Height", recipeHeight);
		if(getRecipeOutput() != null)
			compound.setTag("Item", NoppesUtilServer.writeItem(getRecipeOutput(), new NBTTagCompound()));
		compound.setTag("Materials", NBTTags.nbtItemStackArray(recipeItems));
		compound.setTag("Availability", availability.writeToNBT(new NBTTagCompound()));
		compound.setString("Name", name);
		compound.setBoolean("Global", isGlobal);
		compound.setBoolean("IgnoreDamage", ignoreDamage);
		compound.setBoolean("IgnoreNBT", ignoreNBT);
		return compound;
	}

	@Override
	public boolean matches(InventoryCrafting par1InventoryCrafting, World world) {
        for (int i = 0; i <= 4 - this.recipeWidth; ++i){
            for (int j = 0; j <= 4 - this.recipeHeight; ++j){
                if (this.checkMatch(par1InventoryCrafting, i, j, true))
                    return true;

                if (this.checkMatch(par1InventoryCrafting, i, j, false))
                    return true;
            }
        }
        return false;
	}



    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean checkMatch(InventoryCrafting par1InventoryCrafting, int par2, int par3, boolean par4){
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                int var7 = i - par2;
                int var8 = j - par3;
                ItemStack var9 = null;

                if (var7 >= 0 && var8 >= 0 && var7 < this.recipeWidth && var8 < this.recipeHeight){
                    if (par4)
                        var9 = this.recipeItems[this.recipeWidth - var7 - 1 + var8 * this.recipeWidth];
                    else
                        var9 = this.recipeItems[var7 + var8 * this.recipeWidth];
                }

                ItemStack var10 = par1InventoryCrafting.getStackInRowAndColumn(i, j);

                if ((var10 != null || var9 != null) && !NoppesUtilPlayer.compareItems(var9, var10, ignoreDamage, ignoreNBT)){
                	return false;
                }
            }
        }

        return true;
    }

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		if(getRecipeOutput() == null)
			return null;
		return getRecipeOutput().copy();
	}

	@Override
	public int getRecipeSize() {
		return 16;
	}

    public static RecipeCarpentry saveRecipe(RecipeCarpentry recipe, ItemStack par1ItemStack, Object ... par2ArrayOfObj){
        String var3 = "";
        int var4 = 0;
        int var5 = 0;
        int var6 = 0;
        int var9;

        if (par2ArrayOfObj[var4] instanceof String[])
        {
            String[] var7 = (String[])((String[])par2ArrayOfObj[var4++]);
            String[] var8 = var7;
            var9 = var7.length;

            for (int var10 = 0; var10 < var9; ++var10)
            {
                String var11 = var8[var10];
                ++var6;
                var5 = var11.length();
                var3 = var3 + var11;
            }
        }
        else
        {
            while (par2ArrayOfObj[var4] instanceof String)
            {
                String var13 = (String)par2ArrayOfObj[var4++];
                ++var6;
                var5 = var13.length();
                var3 = var3 + var13;
            }
        }

        HashMap var14;

        for (var14 = new HashMap(); var4 < par2ArrayOfObj.length; var4 += 2)
        {
            Character var16 = (Character)par2ArrayOfObj[var4];
            ItemStack var17 = null;

            if (par2ArrayOfObj[var4 + 1] instanceof Item)
            {
                var17 = new ItemStack((Item)par2ArrayOfObj[var4 + 1]);
            }
            else if (par2ArrayOfObj[var4 + 1] instanceof Block)
            {
                var17 = new ItemStack((Block)par2ArrayOfObj[var4 + 1], 1, -1);
            }
            else if (par2ArrayOfObj[var4 + 1] instanceof ItemStack)
            {
                var17 = (ItemStack)par2ArrayOfObj[var4 + 1];
            }

            var14.put(var16, var17);
        }

        ItemStack[] var15 = new ItemStack[var5 * var6];

        for (var9 = 0; var9 < var5 * var6; ++var9)
        {
            char var18 = var3.charAt(var9);

            if (var14.containsKey(Character.valueOf(var18)))
            {
                var15[var9] = ((ItemStack)var14.get(Character.valueOf(var18))).copy();
            }
            else
            {
                var15[var9] = null;
            }
        }
        RecipeCarpentry newrecipe = new RecipeCarpentry(var5, var6, var15, par1ItemStack);    
        newrecipe.copy(recipe);
        if(var5 == 4 || var6 == 4)
        	newrecipe.isGlobal = false;
        
        
        
        return newrecipe;
    }
    public void copy(RecipeCarpentry recipe) {
		this.id = recipe.id;
		this.name = recipe.name;
		this.availability = recipe.availability;
		this.isGlobal = recipe.isGlobal;
		this.ignoreDamage = recipe.ignoreDamage;
		this.ignoreNBT = recipe.ignoreNBT;
	}
    
	public ItemStack getCraftingItem(int i){
    	if(recipeItems == null || i >= recipeItems.length)
    		return null;
    	return recipeItems[i];
    }
    public void setCraftingItem(int i, ItemStack item){
    	if(i < recipeItems.length)
    		recipeItems[i] = item;
    }
	public boolean isValid() {
		if(recipeItems.length == 0 || getRecipeOutput() == null)
			return false;
		for(ItemStack item : recipeItems){
			if(item != null)
				return true;
		}
		return false;
	}
}
