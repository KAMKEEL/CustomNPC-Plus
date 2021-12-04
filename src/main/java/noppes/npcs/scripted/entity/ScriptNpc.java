package noppes.npcs.scripted.entity;

import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.Line;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.ScriptFaction;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.constants.AnimationType;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.interfaces.ICustomNpc;
import noppes.npcs.scripted.interfaces.ITimers;
import noppes.npcs.scripted.roles.ScriptJobBard;
import noppes.npcs.scripted.roles.ScriptJobConversation;
import noppes.npcs.scripted.roles.ScriptJobFollower;
import noppes.npcs.scripted.roles.ScriptJobGuard;
import noppes.npcs.scripted.roles.ScriptJobHealer;
import noppes.npcs.scripted.roles.ScriptJobInterface;
import noppes.npcs.scripted.roles.ScriptJobItemGiver;
import noppes.npcs.scripted.roles.ScriptJobPuppet;
import noppes.npcs.scripted.roles.ScriptJobSpawner;
import noppes.npcs.scripted.roles.ScriptRoleBank;
import noppes.npcs.scripted.roles.ScriptRoleFollower;
import noppes.npcs.scripted.roles.ScriptRoleInterface;
import noppes.npcs.scripted.roles.ScriptRoleMailman;
import noppes.npcs.scripted.roles.ScriptRoleTrader;
import noppes.npcs.scripted.roles.ScriptRoleTransporter;
import noppes.npcs.util.ValueUtil;

import java.util.ArrayList;
import java.util.UUID;

public class ScriptNpc<T extends EntityNPCInterface> extends ScriptLiving<T> implements ICustomNpc {
	public EntityCustomNpc npc;

	public ScriptNpc(T npc){
		super(npc);
		this.npc = (EntityCustomNpc)npc;
	}
		
	/**
	 * @return Returns the current npcs size
	 */
	public int getSize(){
		return npc.display.modelSize;
	}
	
	/**
	 * @param size The size of the npc. Default is 5
	 */
	public void setSize(int size){
		if(size < 1)
			size = 1;
		npc.display.modelSize = size;
		npc.script.clientNeedsUpdate = true;
	}

	/**
	 * @return Returns the current npcs modelType of the NPC
	 */
	public int getModelType(){
		return npc.display.modelType;
	}

	/**
	 * @param modelType The modelType of the NPC. 0: Steve, 1: Steve64, 2: Alex
	 */
	public void setModelType(int modelType){
		if(modelType > 2)
			modelType = 2;
		else if(modelType < 0)
			modelType = 0;
		npc.display.modelType = modelType;
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return The npcs name
	 */
	public String getName(){
		return npc.display.name;
	}

	public void setRotation(float rotation){
		npc.ai.orientation = (int)rotation;
		super.setRotation(rotation);
	}
	/**
	 * @param name The name of the npc
	 */
	public void setName(String name){
		npc.display.name = name;
		npc.script.clientNeedsUpdate = true;
	}	
	
	/**
	 * @since 1.7.10
	 * @return The npcs title
	 */
	public String getTitle(){
		return npc.display.title;
	}
	
	/**
	 * @since 1.7.10
	 * @param title The title of the npc
	 */
	public void setTitle(String title){
		npc.display.title = title;
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return The npcs texture
	 */
	public String getTexture(){
		return npc.display.texture;
	}
	
	/**
	 * @param texture The texture of the npc
	 */
	public void setTexture(String texture){
		npc.display.texture = texture;
		npc.script.clientNeedsUpdate = true;
	}

	/**
	 * @return Home position x
	 */
	public int getHomeX(){
		return npc.getStartPos()[0];
	}
	
	/**
	 * @param x The home x position
	 */
	public void setHomeX(int x){
		npc.ai.startPos[0] = x;
	}
	
	/**
	 * @return Home position x
	 */
	public int getHomeY(){
		return npc.getStartPos()[1];
	}
	
	/**
	 * @param y The home y position
	 */
	public void setHomeY(int y){
		npc.ai.startPos[1] = y;
	}
	
	/**
	 * @return Home position x
	 */
	public int getHomeZ(){
		return npc.getStartPos()[2];
	}
	
	/**
	 * @param z The home x position
	 */
	public void setHomeZ(int z){
		npc.ai.startPos[2] = z;
	}
	
	/**
	 * @param x The home x position
	 * @param y The home y position
	 * @param z The home z position
	 */
	public void setHome(int x, int y, int z){
		npc.ai.startPos = new int[]{x, y, z};
	}
	/**
	 * @param health New max health
	 */
	public void setMaxHealth(double health){
		npc.stats.setMaxHealth(health);
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @param bo Whether or not the npc will try to return to his home position
	 */
	public void setReturnToHome(boolean bo){
		npc.ai.returnToStart = bo;
	}
	
	/**
	 * @return Whether or not the npc returns home
	 */
	public boolean getReturnToHome(){
		return npc.ai.returnToStart;
	}
	
	/**
	 * @return The faction of the npc
	 */
	public ScriptFaction getFaction(){
		return new ScriptFaction(npc.getFaction());
	}
	
	/**
	 * @param id The id of the new faction
	 */
	public void setFaction(int id){
		npc.setFaction(id);
	}

	public void setAttackFactions(boolean attackOtherFactions){
		npc.advanced.attackOtherFactions = attackOtherFactions;
	}

	public boolean getAttackFactions(){
		return npc.advanced.attackOtherFactions;
	}

	public void setDefendFaction(boolean defendFaction){
		npc.advanced.defendFaction = defendFaction;
	}

	public boolean getDefendFaction(){
		return npc.advanced.defendFaction;
	}

	@Override
	public int getType(){
		return EntityType.NPC;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntityType.NPC?true:super.typeOf(type);
	}
	
	/**
	 * @param target The targeted npc 
	 * @param item The item you want to shoot
	 * @param accuracy Accuracy of the shot (0-100)
	 */
	public void shootItem(ScriptLivingBase target, ScriptItemStack item, int accuracy){
		if(item == null)
			return;
		if(accuracy < 0)
			accuracy = 0;
		else if(accuracy > 100)
			accuracy = 100;
		npc.shoot(target.entity, accuracy, item.item, false);
	}
	
	/**
	 * @param message The message the npc will say
	 */
	public void say(String message){
		npc.saySurrounding(new Line(message));
	}
	
	/**
	 * @param message The message the npc will say
	 */
	public void say(ScriptPlayer player, String message){
		if(player == null || message == null || message.isEmpty())
			return;
		npc.say(player.player, new Line(message));
	}
	
	/**
	 * Kill the npc, doesnt't despawn it
	 */
	public void kill(){
		npc.setDead();
	}
	
	/**
	 * Basically completely resets the npc. This will also call the Init script
	 */
	public void reset(){
		npc.reset();
	}

	/**
	 * @return Returns the npcs current role
	 */
	public ScriptRoleInterface getRole(){
		if(npc.advanced.role == EnumRoleType.Bank)
			return new ScriptRoleBank(npc);
		else if(npc.advanced.role == EnumRoleType.Follower)
			return new ScriptRoleFollower(npc);
		else if(npc.advanced.role == EnumRoleType.Postman)
			return new ScriptRoleMailman(npc);
		else if(npc.advanced.role == EnumRoleType.Trader)
			return new ScriptRoleTrader(npc);
		else if(npc.advanced.role == EnumRoleType.Transporter)
			return new ScriptRoleTransporter(npc);
		return new ScriptRoleInterface(npc);
	}

	/**
	 * @return Returns the npcs current job
	 */
	public ScriptJobInterface getJob(){
		if(npc.advanced.job == EnumJobType.Bard)
			return new ScriptJobBard(npc);
		else if(npc.advanced.job == EnumJobType.Conversation)
			return new ScriptJobConversation(npc);
		else if(npc.advanced.job == EnumJobType.Follower)
			return new ScriptJobFollower(npc);
		else if(npc.advanced.job == EnumJobType.Guard)
			return new ScriptJobGuard(npc);
		else if(npc.advanced.job == EnumJobType.Healer)
			return new ScriptJobHealer(npc);
		else if(npc.advanced.job == EnumJobType.Puppet)
			return new ScriptJobPuppet(npc);
		else if(npc.advanced.job == EnumJobType.ItemGiver)
			return new ScriptJobItemGiver(npc);
		else if(npc.advanced.job == EnumJobType.Spawner)
			return new ScriptJobSpawner(npc);
		return new ScriptJobInterface(npc);
	}
	
	/**
	 * @return The item held in the right hand
	 */
	public ScriptItemStack getRightItem(){
		ItemStack item = npc.inventory.getWeapon();
		if(item == null || item.getItem() == null)
			return null;
		return new ScriptItemStack(item);
	}
	
	/**
	 * @param item Item to be held in the right hand
	 */
	public void setRightItem(ScriptItemStack item){
		if(item == null)
			npc.inventory.setWeapon(null);
		else
			npc.inventory.setWeapon(item.item);
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return The item held in the left hand
	 */
	public ScriptItemStack getLefttItem(){
		ItemStack item = npc.getOffHand();
		if(item == null || item.getItem() == null)
			return null;
		return new ScriptItemStack(item);
	}
	
	/**
	 * @param item Item to be held in the left hand
	 */
	public void setLeftItem(ScriptItemStack item){
		if(item == null)
			npc.inventory.setOffHand(null);
		else
			npc.inventory.setOffHand(item.item);
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return Returns the projectile the npc uses
	 */
	public ScriptItemStack getProjectileItem(){
		ItemStack item = npc.inventory.getProjectile();
		if(item == null || item.getItem() == null)
			return null;
		return new ScriptItemStack(item);
	}
	
	/**
	 * @param item Item to be used as projectile
	 */
	public void setProjectileItem(ScriptItemStack item){
		if(item == null)
			npc.inventory.setProjectile(null);
		else
			npc.inventory.setProjectile(item.item);
		npc.script.aiNeedsUpdate = true;
	}

	public boolean canAimWhileShooting() {
		return !npc.stats.aimWhileShooting;
	}

	public void aimWhileShooting(boolean aimWhileShooting) {
		npc.stats.aimWhileShooting = aimWhileShooting;
	}

	public String getFireSound() {
		return npc.stats.fireSound;
	}

	public void setFireSound(String fireSound) {
		npc.stats.fireSound = fireSound;
	}
	
	/**
	 * @param slot The armor slot to return. 0:head, 1:body, 2:legs, 3:boots
	 * @return Returns the worn armor in slot
	 */
	@Override
	public ScriptItemStack getArmor(int slot){
		ItemStack item = npc.inventory.armor.get(slot);
		if(item == null)
			return null;
		return new ScriptItemStack(item);
	}
	
	/**
	 * @param slot The armor slot to set. 0:head, 1:body, 2:legs, 3:boots
	 * @param item Item to be set as armor
	 */
	@Override
	public void setArmor(int slot, ScriptItemStack item){
		if(item == null)
			npc.inventory.armor.put(slot, null);
		else
			npc.inventory.armor.put(slot, item.item);
		
		npc.script.clientNeedsUpdate = true;
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to return (0-8)
	 * @return
	 */
	public ScriptItemStack getLootItem(int slot) {
		return new ScriptItemStack(npc.inventory.getStackInSlot(slot+7));
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to change
	 * @param item The item the drop list slot will be changed to
	 */
	public void setLootItem(int slot, ScriptItemStack item) {
		npc.inventory.setInventorySlotContents(slot+7, item.item);
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to return (0-8)
	 * @return The chance of dropping the item in this slot. Returns 100 if the slot is not found.
	 */
	public int getLootChance(int slot) {
		if(!npc.inventory.dropchance.containsKey(slot))
			return 100;

		return npc.inventory.dropchance.get(slot);
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to change
	 * @param chance The new chance of dropping the item in this slot
	 */
	public void setLootChance(int slot, int chance) {
		if(slot < 0 || slot > 8)
			return;

		if(chance < 0)
			chance = 0;
		if(chance > 100)
			chance = 100;

		npc.inventory.dropchance.put(slot,chance);
	}

	public int getLootMode(){
		return npc.inventory.lootMode;
	}

	public void setLootMode(int lootMode){
		if(lootMode < 0 || lootMode > 1)
			return;
		npc.inventory.lootMode = lootMode;
	}

	public void setMinLootXP(int lootXP) {
		if(lootXP > npc.inventory.maxExp)
			lootXP = npc.inventory.maxExp;
		if(lootXP < 0)
			lootXP = 0;
		if(lootXP > Short.MAX_VALUE)
			lootXP = Short.MAX_VALUE;

		npc.inventory.minExp = lootXP;
	}
	public void setMaxLootXP(int lootXP) {
		if(lootXP < npc.inventory.minExp)
			lootXP = npc.inventory.minExp;
		if(lootXP < 0)
			lootXP = 0;
		if(lootXP > Short.MAX_VALUE)
			lootXP = Short.MAX_VALUE;

		npc.inventory.maxExp = lootXP;
	}

	public int getMinLootXP(){
		return npc.inventory.minExp;
	}
	public int getMaxLootXP(){
		return npc.inventory.maxExp;
	}
	
	/**
	 * @param type The AnimationType
	 */
	public void setAnimation(int type){
		if(type == AnimationType.NORMAL)
			npc.ai.animationType = EnumAnimation.NONE;
		else if(type == AnimationType.SITTING)
			npc.ai.animationType = EnumAnimation.SITTING;
		else if(type == AnimationType.DANCING)
			npc.ai.animationType = EnumAnimation.DANCING;
		else if(type == AnimationType.SNEAKING)
			npc.ai.animationType = EnumAnimation.SNEAKING;
		else if(type == AnimationType.LYING)
			npc.ai.animationType = EnumAnimation.LYING;
		else if(type == AnimationType.HUGGING)
			npc.ai.animationType = EnumAnimation.HUG;
	}

	public void setTacticalVariant(int variant){
		if(variant > EnumNavType.values().length-1)
			return;

		npc.ai.tacticalVariant = EnumNavType.values()[variant];
	}

	public int getTacticalVariant(){
		return npc.ai.tacticalVariant.ordinal();
	}

	public void setTacticalVariant(String variant){
		boolean found = false;
		for(String s : EnumNavType.names()){
			if(s.equals(variant))
				found = true;
		}

		if(!found)
			return;

		npc.ai.tacticalVariant = EnumNavType.valueOf(variant);
	}

	public String getTacticalVariantName(){
		return npc.ai.tacticalVariant.name();
	}

	public void setTacticalRadius(int tacticalRadius){
		if(tacticalRadius < 0)
			tacticalRadius = 0;

		npc.ai.tacticalRadius = tacticalRadius;
	}

	public int getTacticalRadius(){
		return npc.ai.tacticalRadius;
	}

	public void setIgnoreCobweb(boolean ignore){
		npc.ai.ignoreCobweb = ignore;
	}

	public boolean getIgnoreCobweb(){
		return npc.ai.ignoreCobweb;
	}

	public void setOnFoundEnemy(int onAttack){
		if(onAttack < 0 || onAttack > 3)
			return;
		npc.ai.onAttack = onAttack;
	}

	public int onFoundEnemy(){
		return npc.ai.onAttack;
	}

	public void setShelterFrom(int shelterFrom){
		if(shelterFrom < 0 || shelterFrom > 2)
			return;
		npc.ai.findShelter = shelterFrom;
	}

	public int getShelterFrom(){
		return npc.ai.findShelter;
	}

	public boolean hasLivingAnimation() {
		return !npc.display.disableLivingAnimation;
	}

	public void setLivingAnimation(boolean livingAnimation) {
		npc.display.disableLivingAnimation = !livingAnimation;
	}
	
	/**
	 * @param type The visibility type of the npc, 0:visible, 1:invisible, 2:semi-visible
	 */
	public void setVisibleType(int type){
		npc.display.visible = type;
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return The visibility type of the npc, 0:visible, 1:invisible, 2:semi-visible
	 */
	public int getVisibleType(){
		return npc.display.visible;
	}

	public void setVisibleTo(ScriptPlayer player, boolean visible) {
		UUID uuid = player.player.getPersistentID();
		ArrayList<UUID> uuidList = npc.display.invisibleToList;
		if(uuidList != null) {
			if (!uuidList.contains(uuid)) {
				if (!visible)
					npc.display.invisibleToList.add(uuid);
			} else if (visible) {
				npc.display.invisibleToList.remove(uuid);
			}
		}
		npc.script.clientNeedsUpdate = true;
	}

	public boolean isVisibleTo(ScriptPlayer player) {
		return !npc.scriptInvisibleToPlayer(player.player);
	}
	
	/**
	 * @param type The visibility type of the name, 0:visible, 1:invisible, 2:when-attacking
	 */
	public void setShowName(int type){
		npc.display.showName = type;
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return Returns the visibility type of the name, 0:visible, 1:invisible, 2:when-attacking
	 */
	public int getShowName(){
		return npc.display.showName;
	}
	
	/**
	 * @return Returns the visiblity of the boss bar, 0:invisible, 1:visible, 2:when-attacking
	 */
	public int getShowBossBar(){
		return npc.display.showBossBar;
	}
	
	/**
	 * @param type The visibility type of the boss bar, 0:invisible, 1:visible, 2:when-attacking
	 */
	public void setShowBossBar(int type){
		npc.display.showBossBar = (byte) type;
		npc.script.clientNeedsUpdate = true;
	}
	
	/**
	 * @return The melee strength
	 */
	public float getMeleeStrength(){
		return npc.stats.getAttackStrength();
	}
	
	/**
	 * @param strength The melee strength
	 */
	public void setMeleeStrength(float strength){
		npc.stats.setAttackStrength(strength);
	}
	
	/**
	 * @return The melee speed
	 */
	public int getMeleeSpeed(){
		return npc.stats.attackSpeed;
	}
	
	/**
	 * @param speed The melee speed
	 */
	public void setMeleeSpeed(int speed){
		npc.stats.attackSpeed = speed;
	}

	/**
	 * @return The ranged strength
	 */
	public float getRangedStrength(){
		return npc.stats.pDamage;
	}
	
	/**
	 * @param strength The ranged strength
	 */
	public void setRangedStrength(float strength){
		npc.stats.pDamage = (float)(Math.floor(strength));
	}
	
	/**
	 * @return The ranged speed
	 */
	public int getRangedSpeed(){
		return npc.stats.pSpeed;
	}
	
	/**
	 * @param speed The ranged speed
	 */
	public void setRangedSpeed(int speed){
		npc.stats.pSpeed = speed;
	}
	
	/**
	 * @return The ranged burst count
	 */
	public int getRangedBurst(){
		return npc.stats.burstCount;
	}
	
	/**
	 * @param count The ranged burst count
	 */
	public void setRangedBurst(int count){
		npc.stats.burstCount = count;
	}

	public int getRespawnTime() {
		return npc.stats.respawnTime;
	}

	public void setRespawnTime(int time) {
		npc.stats.respawnTime = time;
	}

	public int getRespawnCycle() {
		return npc.stats.spawnCycle;
	}

	public void setRespawnCycle(int cycle) {
		if(cycle < 0)
			cycle = 0;
		if(cycle > 3)
			cycle = 3;

		npc.stats.spawnCycle = cycle;
	}

	public boolean getHideKilledBody() {
		return npc.stats.hideKilledBody;
	}

	public void hideKilledBody(boolean hide) {
		npc.stats.hideKilledBody = hide;
	}

	public boolean naturallyDespawns() {
		return npc.stats.canDespawn;
	}

	public void setNaturallyDespawns(boolean canDespawn) {
		npc.stats.canDespawn = canDespawn;
	}

	public void giveItem(ScriptPlayer player, ScriptItemStack item){
		npc.givePlayerItem(player.player, item.item);
	}

	public void executeCommand(String command){
		NoppesUtilServer.runCommand(npc, npc.getCommandSenderName(), command, null);
	}
	
	public void setHeadScale(float x, float y, float z){
		npc.modelData.head.scaleX = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		npc.modelData.head.scaleY = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		npc.modelData.head.scaleZ = ValueUtil.correctFloat(z, 0.5f, 1.5f);
		
		npc.script.clientNeedsUpdate = true;
	}

	public void setBodyScale(float x, float y, float z){
		npc.modelData.body.scaleX = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		npc.modelData.body.scaleY = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		npc.modelData.body.scaleZ = ValueUtil.correctFloat(z, 0.5f, 1.5f);
		
		npc.script.clientNeedsUpdate = true;
	}
	
	public void setArmsScale(float x, float y, float z){
		npc.modelData.arms.scaleX = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		npc.modelData.arms.scaleY = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		npc.modelData.arms.scaleZ = ValueUtil.correctFloat(z, 0.5f, 1.5f);
		
		npc.script.clientNeedsUpdate = true;
	}
	
	public void setLegsScale(float x, float y, float z){
		npc.modelData.legs.scaleX = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		npc.modelData.legs.scaleY = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		npc.modelData.legs.scaleZ = ValueUtil.correctFloat(z, 0.5f, 1.5f);
		
		npc.script.clientNeedsUpdate = true;
	}

    /**
     * @since 1.7.10c
     * @param resistance Explosion resistance (0-2) default is 1
     */
	public void setExplosionResistance(float resistance){
		npc.stats.resistances.explosion = ValueUtil.correctFloat(resistance, 0, 2);
	}

    /**
     * @since 1.7.10c
     * @return Returns Explosion Resistance
     */
	public float getExplosionResistance(){
		return npc.stats.resistances.explosion;
	}

    /**
     * @param resistance Melee resistance (0-2) default is 1
     */
	public void setMeleeResistance(float resistance){
		npc.stats.resistances.playermelee = ValueUtil.correctFloat(resistance, 0, 2);
	}

    /**
     * @return Returns Melee Resistance
     */
	public float getMeleeResistance(){
		return npc.stats.resistances.playermelee;
	}

    /**
     * @param resistance Arrow resistance (0-2) default is 1
     */
	public void setArrowResistance(float resistance){
		npc.stats.resistances.arrow = ValueUtil.correctFloat(resistance, 0, 2);
	}

    /**
     * @return Returns Arrow Resistance
     */
	public float getArrowResistance(){
		return npc.stats.resistances.arrow;
	}

    /**
     * @param resistance Knockback resistance (0-2) default is 1
     */
	public void setKnockbackResistance(float resistance){
		npc.stats.resistances.knockback = ValueUtil.correctFloat(resistance, 0, 2);
	}

    /**
     * @return Returns Knockback Resistance
     */
	public float getKnockbackResistance(){
		return npc.stats.resistances.knockback;
	}
	
	/**
	 * @param type Retaliation type. 0:normal, 1:panic, 2:retreat, 3:nothing
	 */
	public void setRetaliateType(int type){
		npc.ai.onAttack = type;
		npc.setResponse();
	}
	
	/**
	 * @return Returns the combat health regen per second
	 */
	public float getCombatRegen(){
		return npc.stats.combatRegen;
	}
	
	/**
	 * @param regen The combat health regen per second
	 */
	public void setCombatRegen(float regen){
		npc.stats.combatRegen = (float)(Math.floor(regen));
	}
	
	/**
	 * @return Returns the health regen per second when not in combat
	 */
	public float getHealthRegen(){
		return npc.stats.healthRegen;
	}
	
	/**
	 * @param regen The health regen per second when not in combat
	 */
	public void setHealthRegen(float regen){
		npc.stats.healthRegen = (float)(Math.floor(regen));
	}
	
	@Override
	public long getAge(){
		return npc.totalTicksAlive;
	}

	public ITimers getTimers() {
		return ((EntityNPCInterface)this.npc).timers;
	}

	public boolean canFly(){
		return npc.ai.movementType == 1;
	}

	public void setFly(int fly){
		if(fly > 0)
			fly = 1;
		else fly = 0;

		npc.ai.movementType = fly;
	}

	public void setSkinType(byte type) {
		npc.display.skinType = type;
		npc.updateClient = true;
	}

	public byte getSkinType() {
		return npc.display.skinType;
	}

	public void setSkinUrl(String url){
		if(this.npc.display.url.equals(url))
			return;
		this.npc.display.url = url;
		npc.textureLocation = null;
		npc.display.skinType = 2;
		npc.updateClient = true;
	}

	public String getSkinUrl() {
		return this.npc.display.url;
	}

	public void setCloakTexture(String cloakTexture) {
		npc.display.cloakTexture = cloakTexture;
		npc.updateClient = true;
	}

	public String getCloakTexture() {
		return npc.display.cloakTexture;
	}

	public void setOverlayTexture(String overlayTexture) {
		npc.display.glowTexture = overlayTexture;
		npc.updateClient = true;
	}

	public String getOverlayTexture() {
		return npc.display.glowTexture;
	}
}
