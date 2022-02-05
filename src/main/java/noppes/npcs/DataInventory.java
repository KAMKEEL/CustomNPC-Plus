package noppes.npcs;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class DataInventory implements IInventory{
	public HashMap<Integer,ItemStack> items = new HashMap<Integer,ItemStack>();
	public HashMap<Integer,Integer> dropchance = new HashMap<Integer,Integer>();
	public HashMap<Integer, ItemStack> weapons = new HashMap<Integer, ItemStack>();
	public HashMap<Integer, ItemStack> armor = new HashMap<Integer, ItemStack>();
		
	public int minExp = 0;
	public int maxExp = 0;
	
	public int lootMode = 0;
	
	private EntityNPCInterface npc;
	
	public DataInventory(EntityNPCInterface npc){
		this.npc = npc;
	}
	public NBTTagCompound writeEntityToNBT(NBTTagCompound nbttagcompound){
		nbttagcompound.setInteger("MinExp", minExp);
		nbttagcompound.setInteger("MaxExp", maxExp);
		nbttagcompound.setTag("NpcInv", NBTTags.nbtItemStackList(items));
		nbttagcompound.setTag("Armor", NBTTags.nbtItemStackList(getArmor()));
		nbttagcompound.setTag("Weapons", NBTTags.nbtItemStackList(getWeapons()));
		nbttagcompound.setTag("DropChance", NBTTags.nbtIntegerIntegerMap(dropchance));
		nbttagcompound.setInteger("LootMode", lootMode);
		
		return nbttagcompound;
	}
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		minExp = nbttagcompound.getInteger("MinExp");
		maxExp = nbttagcompound.getInteger("MaxExp");
		items = NBTTags.getItemStackList(nbttagcompound.getTagList("NpcInv", 10));
		setArmor(NBTTags.getItemStackList(nbttagcompound.getTagList("Armor", 10)));
		setWeapons(NBTTags.getItemStackList(nbttagcompound.getTagList("Weapons", 10)));
		dropchance = NBTTags.getIntegerIntegerMap(nbttagcompound.getTagList("DropChance", 10));
		lootMode = nbttagcompound.getInteger("LootMode");
	}
	public HashMap<Integer, ItemStack> getWeapons() {
		return weapons;
	}
	public void setWeapons(HashMap<Integer, ItemStack> list) {
		weapons = list;
	}
	public HashMap<Integer, ItemStack> getArmor() {
		return armor;
	}
	public void setArmor(HashMap<Integer, ItemStack> list) {
		armor = list;
	}
	public ItemStack getWeapon(){
		return weapons.get(0);
	}
	public void setWeapon(ItemStack item){
		weapons.put(0, item);
	}
	public ItemStack getProjectile(){
		return weapons.get(1);
	}
	public void setProjectile(ItemStack item){
		weapons.put(1, item);
	}
	public ItemStack getOffHand(){
		return weapons.get(2);
	}
	public void setOffHand(ItemStack item){
		weapons.put(2, item);
	}

	public void dropStuff(Entity entity, DamageSource damagesource) {
		ArrayList<EntityItem> list = new ArrayList<EntityItem>();
		for (int i : items.keySet()) {
			ItemStack item = items.get(i);
			if(item == null)
				continue;
			int dchance = 100;
			if(dropchance.containsKey(i))
				dchance = dropchance.get(i);
			int chance = npc.worldObj.rand.nextInt(100) + dchance;
			if(chance >= 100){
				EntityItem e = getEntityItem(item.copy());
				if(e != null)
					list.add(e);
			}
		}
		
		int enchant = 0;
        if (damagesource.getEntity() instanceof EntityPlayer){
        	enchant = EnchantmentHelper.getLootingModifier((EntityLivingBase)damagesource.getEntity());
        }
        
        if (!net.minecraftforge.common.ForgeHooks.onLivingDrops(npc, damagesource, list, enchant, true, 0)){
            for (EntityItem item : list){
            	if(lootMode == 1 && entity instanceof EntityPlayer){
            		EntityPlayer player = (EntityPlayer)entity;
            		item.delayBeforeCanPickup = 2;
            		npc.worldObj.spawnEntityInWorld(item);
            		ItemStack stack = item.getEntityItem();
            		int i = stack.stackSize;

            		if (player.inventory.addItemStackToInventory(stack)) {
            			npc.worldObj.playSoundAtEntity(item,
            					"random.pop",
            					0.2F,
            					((npc.getRNG().nextFloat() - npc.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            			player.onItemPickup(item, i);

            			if (stack.stackSize <= 0) {
            				item.setDead();
            			}
            		}
            	}
            	else
            		npc.worldObj.spawnEntityInWorld(item);
            }
        }
		
		int var1 = minExp;
		if (maxExp - minExp > 0)
			var1 += npc.worldObj.rand.nextInt(maxExp - minExp);

        while (var1 > 0){
            int var2 = EntityXPOrb.getXPSplit(var1);
            var1 -= var2;
            if(lootMode == 1 && entity instanceof EntityPlayer){
                npc.worldObj.spawnEntityInWorld(new EntityXPOrb(entity.worldObj, entity.posX, entity.posY, entity.posZ, var2));
            }
            else{
                npc.worldObj.spawnEntityInWorld(new EntityXPOrb(npc.worldObj, npc.posX, npc.posY, npc.posZ, var2));
            }
        }
		
	}
	
	public EntityItem getEntityItem(ItemStack itemstack) {
		if (itemstack == null) {
			return null;
		}
		EntityItem entityitem = new EntityItem(npc.worldObj, npc.posX,
				(npc.posY - 0.30000001192092896D) + (double) npc.getEyeHeight(), npc.posZ,
				itemstack);
		entityitem.delayBeforeCanPickup = 40;

		float f2 = npc.getRNG().nextFloat() * 0.5F;
		float f4 = npc.getRNG().nextFloat() * 3.141593F * 2.0F;
		entityitem.motionX = -MathHelper.sin(f4) * f2;
		entityitem.motionZ = MathHelper.cos(f4) * f2;
		entityitem.motionY = 0.20000000298023224D;

		return entityitem;
	}
	
	public ItemStack armorItemInSlot(int i) {
		return getArmor().get(i);
	}
	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 15;
	}
	@Override
	public ItemStack getStackInSlot(int i) {
		if(i < 4)
			return armorItemInSlot(i);
		else if(i < 7)
			return getWeapons().get(i-4);
		else
			return items.get(i-7);
	}
	@Override
	public ItemStack decrStackSize(int par1, int par2) {
		int i =0;
        HashMap<Integer,ItemStack> var3;

        if (par1 >= 7)
        {
        	var3 = items;
            par1 -= 7;
        }
        else if (par1 >= 4)
        {
        	var3 = getWeapons();
            par1 -= 4;
            i = 1;
        }
        else{
        	var3 = getArmor();
            i = 2;
        }
        
        ItemStack var4 = null;
        if (var3.get(par1) != null)
        {

            if (var3.get(par1).stackSize <= par2)
            {
                var4 = var3.get(par1);
                var3.put(par1,null);
            }
            else
            {
                var4 = var3.get(par1).splitStack(par2);

                if (var3.get(par1).stackSize == 0)
                {
                    var3.put(par1,null);
                }
            }
        }
        if(i == 1)
        	setWeapons(var3);
        if(i == 2)
        	setArmor(var3);
        return var4;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		int i = 0;
        HashMap<Integer,ItemStack> var2;;

        if (par1 >= 7)
        {
        	var2 = items;
            par1 -= 7;
        }
        else if (par1 >= 4)
        {
        	var2 = getWeapons();
            par1 -= 4;
            i = 1;
        }
        else{
        	var2 = getArmor();
            i = 2;
        }

        if (var2.get(par1) != null)
        {
            ItemStack var3 = var2.get(par1);
            var2.put(par1,null);
            if(i == 1)
            	setWeapons(var2);
            if(i == 2)
            	setArmor(var2);
            return var3;
        }
        else
        {
            return null;
        }
	}
	@Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
		int i = 0;
        HashMap<Integer,ItemStack> var3;

        if (par1 >= 7)
        {
        	var3 = items;
            par1 -= 7;
        }
        else if (par1 >= 4)
        {
        	var3 = getWeapons();
            par1 -= 4;
            i = 1;
        }
        else{
        	var3 = getArmor();
            i = 2;
        }

        var3.put(par1,par2ItemStack);
        if(i == 1)
        	setWeapons(var3);
        if(i == 2)
        	setArmor(var3);
    }
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}
	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
	@Override
	public String getInventoryName() {
		return "NPC Inventory";
	}
	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}
	@Override
	public void markDirty() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void openInventory() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub
		
	}
}
