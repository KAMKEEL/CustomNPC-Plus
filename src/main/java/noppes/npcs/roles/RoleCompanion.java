package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.companion.CompanionFarmer;
import noppes.npcs.roles.companion.CompanionFoodStats;
import noppes.npcs.roles.companion.CompanionGuard;
import noppes.npcs.roles.companion.CompanionJobInterface;
import noppes.npcs.roles.companion.CompanionTrader;

public class RoleCompanion extends RoleInterface {
	public NpcMiscInventory inventory;
	public String uuid = "";
	public String ownerName = "";
	public Map<EnumCompanionTalent, Integer> talents = new TreeMap<EnumCompanionTalent, Integer>();

	public boolean canAge = true;
	public long ticksActive = 0;
	public EnumCompanionStage stage = EnumCompanionStage.FULLGROWN;
	
	public EntityPlayer owner = null;
	public int companionID;
	
	public EnumCompanionJobs job = EnumCompanionJobs.NONE;
	public CompanionJobInterface jobInterface = null;
	
	public boolean hasInv = true;	
	public boolean defendOwner = true;
	
	public CompanionFoodStats foodstats = new CompanionFoodStats();
	private int eatingTicks = 20;
	private ItemStack eating = null;
	private int eatingDelay = 00;
	
	public int currentExp = 0;
	
	public RoleCompanion(EntityNPCInterface npc) {
		super(npc);
		inventory = new NpcMiscInventory(12);
	}

	@Override
	public boolean aiShouldExecute() {
		EntityPlayer prev = owner;
		owner = getOwner();
		if(jobInterface != null && jobInterface.isSelfSufficient())
			return true;

		if(owner == null && !uuid.isEmpty()){
			npc.isDead = true;
		}
		else if(prev != owner && owner != null){
			ownerName = owner.getDisplayName();
			PlayerData data = PlayerDataController.instance.getPlayerData(owner);
			if(data.companionID != companionID){
				npc.isDead = true;
			}
		}
		return owner != null;
	}

	@Override
	public void aiUpdateTask() {
		if(owner != null && (jobInterface == null || !jobInterface.isSelfSufficient()))
			foodstats.onUpdate(npc);
		if(foodstats.getFoodLevel() >= 18){
			npc.stats.healthRegen = 0;
			npc.stats.combatRegen = 0;
		}
		if(foodstats.needFood() && isSitting()){
    		if(eatingDelay > 0){
    			eatingDelay--;
    			return;
    		}
    		
			ItemStack prev = eating;
			eating = getFood();
			
			if(prev != null && eating == null)
				npc.setRoleDataWatcher("");
    		
			if(prev == null && eating != null){
				npc.setRoleDataWatcher("eating");
				eatingTicks = 20;
			}
			
			if(isEating()){
				doEating();
			}
			
		}
		else if(eating != null && !isSitting()){
			eating = null;
			eatingDelay = 20;
			npc.setRoleDataWatcher("");
		}
		
		ticksActive++;
		if(canAge && stage != EnumCompanionStage.FULLGROWN){
			if(stage == EnumCompanionStage.BABY && ticksActive > EnumCompanionStage.CHILD.matureAge){
				matureTo(EnumCompanionStage.CHILD);
			}
			else if(stage == EnumCompanionStage.CHILD && ticksActive > EnumCompanionStage.TEEN.matureAge){
				matureTo(EnumCompanionStage.TEEN);
			}
			else if(stage == EnumCompanionStage.TEEN && ticksActive > EnumCompanionStage.ADULT.matureAge){
				matureTo(EnumCompanionStage.ADULT);
			}
			else if(stage == EnumCompanionStage.ADULT && ticksActive > EnumCompanionStage.FULLGROWN.matureAge){
				matureTo(EnumCompanionStage.FULLGROWN);
			}
		}
	}

	public void clientUpdate() {
		if(npc.getRoleDataWatcher().equals("eating")){
			eating = getFood();
			
			if(isEating()){
				doEating();
			}
		}
		else if(eating != null){
			eating = null;
		}
		
	}
	private void doEating(){
    	if(npc.worldObj.isRemote){
    		Random rand = npc.getRNG();
            for (int j = 0; j < 2; ++j){
                Vec3 vec3 = Vec3.createVectorHelper(((double)rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                vec3.rotateAroundX(-npc.rotationPitch * (float)Math.PI / 180.0F);
                vec3.rotateAroundY(-npc.renderYawOffset * (float)Math.PI / 180.0F);
                Vec3 vec31 = Vec3.createVectorHelper(((double)rand.nextFloat() - 0.5D) * 0.3D, (double)(-rand.nextFloat()) * 0.6D - 0.3D, npc.width / 2 + 0.1);
                vec31.rotateAroundX(-npc.rotationPitch * (float)Math.PI / 180.0F);
                vec31.rotateAroundY(-npc.renderYawOffset * (float)Math.PI / 180.0F);
                vec31 = vec31.addVector(npc.posX, npc.posY + (double)npc.height + 0.1, npc.posZ);
                String s = "iconcrack_" + Item.getIdFromItem(eating.getItem());

                if (eating.getHasSubtypes()){
                    s = s + "_" + eating.getItemDamage();
                }

                npc.worldObj.spawnParticle(s, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.0D, vec3.zCoord);
            }
    	}
    	else{    		
			eatingTicks--;
	
			if(eatingTicks <= 0){
				if(inventory.decrStackSize(eating, 1)){
					ItemFood food = (ItemFood) eating.getItem();
					foodstats.onFoodEaten(food, eating);
					npc.playSound("random.burp", 0.5F, npc.getRNG().nextFloat() * 0.1F + 0.9F);
				}
				eatingDelay = 20;
				npc.setRoleDataWatcher("");
				eating = null;
			}
			else if(eatingTicks > 3 && eatingTicks % 2 == 0){
				Random rand = npc.getRNG();
	    		npc.playSound("random.eat", 0.5F + 0.5F * rand.nextInt(2), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
			}
    	}
	}
	
	public void matureTo(EnumCompanionStage stage){
		this.stage = stage;
		EntityCustomNpc npc = (EntityCustomNpc) this.npc;
		npc.ai.animationType = stage.animation;
		if(stage == EnumCompanionStage.BABY){
			npc.modelData.arms.setScale(0.5f, 0.5f, 0.5f);
			npc.modelData.legs.setScale(0.5f, 0.5f, 0.5f);
			npc.modelData.body.setScale(0.5f, 0.5f, 0.5f);
			npc.modelData.head.setScale(0.7f, 0.7f, 0.7f);
			
			npc.ai.onAttack = 1;
			npc.ai.setWalkingSpeed(3);
			if(!talents.containsKey(EnumCompanionTalent.INVENTORY))
				talents.put(EnumCompanionTalent.INVENTORY, 0);
		}
		if(stage == EnumCompanionStage.CHILD){
			npc.modelData.arms.setScale(0.6f, 0.6f, 0.6f);
			npc.modelData.legs.setScale(0.6f, 0.6f, 0.6f);
			npc.modelData.body.setScale(0.6f, 0.6f, 0.6f);
			npc.modelData.head.setScale(0.8f, 0.8f, 0.8f);

			npc.ai.onAttack = 0;
			npc.ai.setWalkingSpeed(4);
			if(!talents.containsKey(EnumCompanionTalent.SWORD))
				talents.put(EnumCompanionTalent.SWORD, 0);
		}
		if(stage == EnumCompanionStage.TEEN){
			npc.modelData.arms.setScale(0.8f, 0.8f, 0.8f);
			npc.modelData.legs.setScale(0.8f, 0.8f, 0.8f);
			npc.modelData.body.setScale(0.8f, 0.8f, 0.8f);
			npc.modelData.head.setScale(0.9f, 0.9f, 0.9f);

			npc.ai.onAttack = 0;
			npc.ai.setWalkingSpeed(5);
			if(!talents.containsKey(EnumCompanionTalent.ARMOR))
				talents.put(EnumCompanionTalent.ARMOR, 0);			
		}
		if(stage == EnumCompanionStage.ADULT || stage == EnumCompanionStage.FULLGROWN){
			npc.modelData.arms.setScale(1f, 1f, 1f);
			npc.modelData.legs.setScale(1f, 1f, 1f);
			npc.modelData.body.setScale(1f, 1f, 1f);
			npc.modelData.head.setScale(1f, 1f, 1f);

			npc.ai.onAttack = 0;
			npc.ai.setWalkingSpeed(5);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("CompanionInventory", inventory.getToNBT());
		compound.setString("CompanionOwner", uuid);
		compound.setString("CompanionOwnerName", ownerName);
		compound.setInteger("CompanionID", companionID);

		compound.setInteger("CompanionStage", stage.ordinal());
		compound.setInteger("CompanionExp", currentExp);
		compound.setBoolean("CompanionCanAge", canAge);
		compound.setLong("CompanionAge", ticksActive);

		compound.setBoolean("CompanionHasInv", hasInv);
		compound.setBoolean("CompanionDefendOwner", defendOwner);
		
		foodstats.writeNBT(compound);
		
		compound.setInteger("CompanionJob", job.ordinal());
		if(jobInterface != null)
			compound.setTag("CompanionJobData", jobInterface.getNBT());
		
		NBTTagList list = new NBTTagList();
		for(EnumCompanionTalent talent : talents.keySet()){
			NBTTagCompound c = new NBTTagCompound();
			c.setInteger("Talent", talent.ordinal());
			c.setInteger("Exp", talents.get(talent));
			list.appendTag(c);
		}
		compound.setTag("CompanionTalents", list);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		inventory.setFromNBT(compound.getCompoundTag("CompanionInventory"));
		uuid = compound.getString("CompanionOwner");
		ownerName = compound.getString("CompanionOwnerName");
		companionID = compound.getInteger("CompanionID");

		stage = EnumCompanionStage.values()[compound.getInteger("CompanionStage")];
		currentExp = compound.getInteger("CompanionExp");
		canAge = compound.getBoolean("CompanionCanAge");
		ticksActive = compound.getLong("CompanionAge");
		
		hasInv = compound.getBoolean("CompanionHasInv");
		defendOwner = compound.getBoolean("CompanionDefendOwner");
		
		foodstats.readNBT(compound);
		
		NBTTagList list = compound.getTagList("CompanionTalents", 10);
		Map<EnumCompanionTalent, Integer> talents = new TreeMap<EnumCompanionTalent, Integer>();
		for(int i = 0; i < list.tagCount(); i++){
			NBTTagCompound c = list.getCompoundTagAt(i);
			EnumCompanionTalent talent = EnumCompanionTalent.values()[c.getInteger("Talent")];
			talents.put(talent, c.getInteger("Exp"));
		}
		this.talents = talents;
		
		setJob(compound.getInteger("CompanionJob"));
		if(jobInterface != null)
			jobInterface.setNBT(compound.getCompoundTag("CompanionJobData"));
		setStats();
	}

	private void setJob(int i) {
		job = EnumCompanionJobs.values()[i];
		if(job == EnumCompanionJobs.SHOP)
			jobInterface = new CompanionTrader();
		else if(job == EnumCompanionJobs.FARMER)
			jobInterface = new CompanionFarmer();
		else if(job == EnumCompanionJobs.GUARD)
			jobInterface = new CompanionGuard();
		else
			jobInterface = null;
		
		if(jobInterface != null)
			jobInterface.npc = npc;
	}

	@Override
	public void interact(EntityPlayer player) {
		if(player != null && job == EnumCompanionJobs.SHOP)
			((CompanionTrader)jobInterface).interact(player);
		if(player != owner || !npc.isEntityAlive() || npc.isAttacking())
			return;
		if(player.isSneaking()){
			openGui(player);
		}
		else{
			setSitting(!isSitting());
		}
	}
	
	public int getTotalLevel(){
		int level = 0;
		for(EnumCompanionTalent talent : talents.keySet())
			level += this.getTalentLevel(talent);
		return level;
	}
	
	public int getMaxExp(){
		return 500 + getTotalLevel() * 200;
	}
	
	public void addExp(int exp){
		if(canAddExp(exp))
			this.currentExp += exp;
	}
	
	public boolean canAddExp(int exp){
		int newExp = this.currentExp + exp;
		return newExp >= 0 && newExp < getMaxExp();
	}
	
	public void gainExp(int chance){
		if(npc.getRNG().nextInt(chance) == 0)
			addExp(1);
	}
	
	private void openGui(EntityPlayer player){
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.Companion, npc);
	}
	
	public EntityPlayer getOwner(){
		if(uuid == null || uuid.isEmpty())
			return null;
		try{
	        UUID id = UUID.fromString(uuid);
	        if(id != null){
	        	return NoppesUtilServer.getPlayer(id);
	        }
		}
		catch(IllegalArgumentException ex){
			
		}
    	return null;
	}


	public void setOwner(EntityPlayer player) {
		uuid = player.getUniqueID().toString();
	}


	public boolean hasTalent(EnumCompanionTalent talent) {
		return getTalentLevel(talent) > 0;
	}
	
	public int getTalentLevel(EnumCompanionTalent talent){
		if(!talents.containsKey(talent))
			return 0;
		
		int exp = talents.get(talent);
		if(exp >= 5000)
			return 5;
		if(exp >= 3000)
			return 4;
		if(exp >= 1700)
			return 3;
		if(exp >= 1000)
			return 2;
		if(exp >= 400)
			return 1;
		return 0;
	}

	public Integer getNextLevel(EnumCompanionTalent talent) {
		if(!talents.containsKey(talent))
			return 0;
		int exp = talents.get(talent);
		if(exp < 400)
			return 400;
		if(exp < 1000)
			return 700;
		if(exp < 1700)
			return 1700;
		if(exp < 3000)
			return 3000;		
		return 5000;
	}
	
	public void levelSword(){
		if(!talents.containsKey(EnumCompanionTalent.SWORD))
			return;
	}
	public void levelTalent(EnumCompanionTalent talent, int exp){
		if(!talents.containsKey(EnumCompanionTalent.SWORD))
			return;
		talents.put(talent, exp + talents.get(talent));
	}

	public int getExp(EnumCompanionTalent talent) {
		if(talents.containsKey(talent))
			return talents.get(talent);
		return -1;
	}

	public void setExp(EnumCompanionTalent talent, int exp) {
		talents.put(talent, exp);
	}
	
	private boolean isWeapon(ItemStack item) {
		if(item == null || item.getItem() == null)
			return false;
		return item.getItem() instanceof ItemSword || 
				item.getItem() instanceof ItemBow ||
				item.getItem() == Item.getItemFromBlock(Blocks.cobblestone);
	}

	public boolean canWearWeapon(ItemStack item){
		if(item == null || item.getItem() == null)
			return false;

		if(item.getItem() instanceof ItemSword)
			return canWearSword(item);

		if(item.getItem() instanceof ItemBow)
			return this.getTalentLevel(EnumCompanionTalent.RANGED) > 2;
			
		if(item.getItem() == Item.getItemFromBlock(Blocks.cobblestone))
			return this.getTalentLevel(EnumCompanionTalent.RANGED) > 1;
			
		return false;
	}
	
	public boolean canWearArmor(ItemStack item){
		int level = getTalentLevel(EnumCompanionTalent.ARMOR);
		if(item == null || !(item.getItem() instanceof ItemArmor) || level <= 0)
			return false;
			
		if(level >= 5)
			return true;
		
		ItemArmor armor = (ItemArmor) item.getItem();
		int reduction = ObfuscationReflectionHelper.getPrivateValue(ArmorMaterial.class, armor.getArmorMaterial(), 5);
		if(reduction <= 5 && level >= 1)
			return true;
		if(reduction <= 7 && level >= 2)
			return true;
		if(reduction <= 15 && level >= 3)
			return true;
		if(reduction <= 33 && level >= 4)
			return true;
		return false;
	}	
	public boolean canWearSword(ItemStack item){
		int level = getTalentLevel(EnumCompanionTalent.SWORD);
		if(item == null || !(item.getItem() instanceof ItemSword) || level <= 0)
			return false;
		if(level >= 5)
			return true;
		return getSwordDamage(item) - level < 4;
	}
	
	private double getSwordDamage(ItemStack item){
		if(item == null || !(item.getItem() instanceof ItemSword))
			return 0;
		HashMultimap map = (HashMultimap) item.getAttributeModifiers();
        Iterator iterator = map.entries().iterator();
        while (iterator.hasNext())
        {
            Entry entry = (Entry)iterator.next();
            if(entry.getKey().equals(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())){
                AttributeModifier mod = (AttributeModifier) entry.getValue();
                return mod.getAmount();
            }
        }
		return 0;
	}
	
	public void setStats(){		
		ItemStack weapon = npc.inventory.getWeapon();
		npc.stats.setAttackStrength((int) (1 + getSwordDamage(weapon)));
		npc.stats.healthRegen = 0;
		npc.stats.combatRegen = 0;
		int ranged = getTalentLevel(EnumCompanionTalent.RANGED);
		if(ranged > 0 && weapon != null){
			if(ranged > 0 && weapon.getItem() == Item.getItemFromBlock(Blocks.cobblestone)){
				npc.inventory.setProjectile(weapon);
			}
			if(ranged > 0 && weapon.getItem() instanceof ItemBow){
				npc.inventory.setProjectile(new ItemStack(Items.arrow));
			}
		}
		
		inventory.setSize(2 + getTalentLevel(EnumCompanionTalent.INVENTORY) * 2);
	}
	
	public void setSelfsuficient(boolean bo){
		if(owner == null || jobInterface != null && bo == jobInterface.isSelfSufficient())
			return;
		PlayerData data = PlayerDataController.instance.getPlayerData(owner);
		if(!bo && data.hasCompanion())
			return;
		data.setCompanion(bo?null:npc);
		if(job == EnumCompanionJobs.GUARD)
			((CompanionGuard)jobInterface).isStanding = bo;
		else if(job == EnumCompanionJobs.FARMER)
			((CompanionFarmer)jobInterface).isStanding = bo;
		
	}
	
	public void setSitting(boolean sit){
		if(sit){
			npc.ai.animationType = EnumAnimation.SITTING;
			npc.ai.onAttack = 3;
			npc.ai.startPos = new int[]{MathHelper.floor_double(npc.posX), 
					MathHelper.floor_double(npc.posY), MathHelper.floor_double(npc.posZ)};
			npc.getNavigator().clearPathEntity();
			npc.setPositionAndUpdate(npc.ai.startPos[0] + 0.5, npc.posY, npc.ai.startPos[2] + 0.5);
		}
		else{
			npc.ai.animationType = stage.animation;
			npc.ai.onAttack = 0;
		}
		npc.setResponse();
	}
	
	public boolean isSitting(){
		return npc.ai.animationType == EnumAnimation.SITTING;
	}

	public float applyArmorCalculations(DamageSource source, float damage) {
		if(!hasInv || getTalentLevel(EnumCompanionTalent.ARMOR) <=0)
			return damage;
        if (!source.isUnblockable()){
        	damageArmor(damage);
            int i = 25 - getTotalArmorValue();
            float f1 = damage * (float)i;
            damage = f1 / 25.0F;
        }
		return damage;
	}
	
    private void damageArmor(float damage){
        damage /= 4.0F;

        if (damage < 1.0F){
            damage = 1.0F;
        }
        boolean hasArmor = false;
        Iterator<Entry<Integer,ItemStack>> ita = npc.inventory.armor.entrySet().iterator();
        while(ita.hasNext()){
        	Entry<Integer,ItemStack> entry = ita.next();
        	ItemStack item = entry.getValue();
        	if(item == null || !(item.getItem() instanceof ItemArmor))
        		continue;
        	hasArmor = true;
        	item.damageItem((int)damage, npc);
        	if(item.stackSize <= 0)
        		ita.remove();
        }
        this.gainExp(hasArmor?4:8);
    }
	
	public int getTotalArmorValue(){
    	int armorValue = 0;
    	for(ItemStack armor : npc.inventory.getArmor().values()){
    		if(armor != null && armor.getItem() instanceof ItemArmor)
    			armorValue += ((ItemArmor)armor.getItem()).damageReduceAmount;
    	}
    	return armorValue;
	}

	public boolean isFollowing(){
		if(jobInterface != null && jobInterface.isSelfSufficient())
			return false;
		return owner != null && !isSitting();
	}
	
	@Override
	public boolean defendOwner() {
		if(!defendOwner || owner == null || stage == EnumCompanionStage.BABY || jobInterface != null && jobInterface.isSelfSufficient())
			return false;
		return true;
	}

	public int followRange() {
		return 9 + 12 * stage.ordinal();
		
	}

	public boolean hasOwner() {
		return !uuid.isEmpty();
	}

	public void addMovementStat(double x, double y, double z) {
        int i = Math.round(MathHelper.sqrt_double(x * x + y * y + z * z) * 100.0F);
        if(npc.isAttacking())
        	foodstats.addExhaustion(0.04F * (float)i * 0.01F);
        else
        	foodstats.addExhaustion(0.02F * (float)i * 0.01F);
	}

    private ItemStack getFood(){
    	List<ItemStack> food = new ArrayList<ItemStack>(inventory.items.values());
    	Iterator<ItemStack> ite = food.iterator();
    	int i = -1;
    	while(ite.hasNext()){
    		ItemStack is = ite.next();
    		if(is == null || !(is.getItem() instanceof ItemFood)){
    			ite.remove();
    			continue;
    		}
    		int amount = ((ItemFood) is.getItem()).func_150905_g(is);
    		if(i == -1 || amount < i)
    			i = amount;
    	}
    	for(ItemStack is : food){
    		if(((ItemFood)is.getItem()).func_150905_g(is) == i)
    			return is;
    	}
		return null;
    }

	public ItemStack getHeldItem() {
		if(eating != null)
			return eating;
		return npc.inventory.getWeapon();
	}

	public boolean isEating() {
		return eating != null;
	}

	public boolean hasInv() {
		if(!hasInv)
			return false;
		return hasTalent(EnumCompanionTalent.INVENTORY) || hasTalent(EnumCompanionTalent.ARMOR) || hasTalent(EnumCompanionTalent.SWORD);
	}

	public void attackedEntity(Entity entity) {
		ItemStack weapon = npc.inventory.getWeapon();
		gainExp(weapon == null?8:4);
		if(weapon == null)
			return;
		weapon.damageItem(1, npc);
		if(weapon.stackSize <= 0)
			npc.inventory.setWeapon(null);
	}

	public void addTalentExp(EnumCompanionTalent talent, int exp) {
		if(talents.containsKey(talent))
			exp += talents.get(talent);
		talents.put(talent, exp);
	}
}
