package noppes.npcs.scripted.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IModelData;
import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.IOverlayHandler;
import noppes.npcs.api.handler.data.*;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.jobs.IJob;
import noppes.npcs.api.roles.IRole;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.*;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.AnimationType;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.roles.*;
import noppes.npcs.util.ValueUtil;

import java.util.ArrayList;
import java.util.UUID;

public class ScriptNpc<T extends EntityNPCInterface> extends ScriptLiving<T> implements ICustomNpc {
	public EntityNPCInterface npc;

	public ScriptNpc(T npc){
		super(npc);
		this.npc = npc;
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
		if(size > ConfigMain.NpcSizeLimit)
			size = ConfigMain.NpcSizeLimit;

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

	public void setRotation(float rotationYaw, float rotationPitch) {
		this.setRotation(rotationYaw);
		npc.rotationPitch = rotationPitch;
	}

	public void setRotation(float rotation){
        super.setRotation(rotation);
        int r = (int) rotation;
        if(npc.ais.orientation != r) {
            npc.updateClient = true;
            npc.rotationYawHead = npc.rotationYaw = npc.renderYawOffset = npc.ais.orientation = r;
            npc.prevRenderYawOffset = r;
            npc.prevRotationYaw = r;
            npc.prevRotationYawHead = r;
        }
	}

	public void setRotationType(int rotationType){
		for (EnumStandingType e : EnumStandingType.values()) {
			if (e.ordinal() == rotationType) {
				npc.ais.standingType = e;
				break;
			}
		}
	}

	public int getRotationType(){
		return npc.ais.standingType.ordinal();
	}

	/**
	 * @param movingType The moving type of the npc. 0 = standing, 1 = wandering, 2 = moving path
	 */
	public void setMovingType(int movingType){
		for(EnumMovingType e : EnumMovingType.values()) {
			if (e.ordinal() == movingType) {
				npc.ais.movingType = e;
				break;
			}
		}
	}

	/**
	 * @return The moving type of the npc. 0 = standing, 1 = wandering, 2 = moving path
	 */
	public int getMovingType(){
		return npc.ais.movingType.ordinal();
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

	public IPos getHome() {
		return NpcAPI.Instance().getIPos(npc.ais.startPos[0],npc.ais.startPos[1],npc.ais.startPos[2]);
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
		npc.ais.startPos[0] = x;
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
		npc.ais.startPos[1] = y;
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
		npc.ais.startPos[2] = z;
	}

	/**
	 * @param x The home x position
	 * @param y The home y position
	 * @param z The home z position
	 */
	public void setHome(int x, int y, int z){
		npc.ais.startPos = new int[]{x, y, z};
	}

	public void setHome(IPos pos){
		npc.ais.startPos = new int[]{pos.getX(), pos.getY(), pos.getZ()};
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
		npc.ais.returnToStart = bo;
	}

	/**
	 * @return Whether or not the npc returns home
	 */
	public boolean getReturnToHome(){
		return npc.ais.returnToStart;
	}

	/**
	 * @return The faction of the npc
	 */
	public IFaction getFaction(){
		return FactionController.getInstance().get(this.npc.faction.id);
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
	public void shootItem(IEntityLivingBase target, IItemStack item, int accuracy){
		if(item == null)
			return;
		if(accuracy < 0)
			accuracy = 0;
		else if(accuracy > 100)
			accuracy = 100;
		npc.shoot(target.getMCEntity(), accuracy, item.getMCItemStack(), false);
	}

	public void setProjectilesKeepTerrain(boolean t) {
		npc.stats.projectilesKeepTerrain = t;
	}

	public boolean getProjectilesKeepTerrain() {
		return npc.stats.projectilesKeepTerrain;
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
	public void say(IPlayer player, String message){
		if(player == null || message == null || message.isEmpty())
			return;
		npc.say((EntityPlayer) player.getMCEntity(), new Line(message));
	}

	public IDialog getDialog(int slot) {
		return NpcAPI.Instance().getDialogs().get(this.getDialogId(slot));
	}

	public int getDialogId(int slot) {
		if (npc.dialogs.containsKey(slot)) {
			DialogOption option = npc.dialogs.get(slot);
			if (option.hasDialog()) {
				return option.dialogId;
			}
		}
		return -1;
	}

	public void setDialog(int slot, IDialog dialog) {
		this.setDialog(slot, dialog.getId());
	}

	public void setDialog(int slot, int dialogId) {
		NoppesUtilServer.setNpcDialog(slot,dialogId, this.npc);
	}

	public ILines getInteractLines() {
		return this.npc.advanced.interactLines;
	}

	public ILines getWorldLines() {
		return this.npc.advanced.worldLines;
	}

	public ILines getAttackLines() {
		return this.npc.advanced.attackLines;
	}

	public ILines getKilledLines() {
		return this.npc.advanced.killedLines;
	}

	public ILines getKillLines() {
		return this.npc.advanced.killLines;
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

	public IAnimationData getAnimationData() {
		return this.npc.display.animationData;
	}

	/**
	 * @return Returns the npcs current role
	 */
	public IRole getRole(){
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
		else if(npc.advanced.role == EnumRoleType.Companion)
			return new ScriptRoleCompanion(npc);
		return new ScriptRoleInterface(npc);
	}

	public void setRole(int role){
		for (EnumRoleType e : EnumRoleType.values()) {
			if (e.ordinal() == role) {
				npc.advanced.role = e;
				npc.advanced.setRole(role);
				break;
			}
		}
	}

	/**
	 * @return Returns the npcs current job
	 */
	public IJob getJob(){
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
		else if(npc.advanced.job == EnumJobType.ItemGiver)
			return new ScriptJobItemGiver(npc);
		else if(npc.advanced.job == EnumJobType.Spawner)
			return new ScriptJobSpawner(npc);
		return new ScriptJobInterface(npc);
	}

	public void setJob(int job){
		for (EnumJobType e : EnumJobType.values()) {
			if (e.ordinal() == job) {
				npc.advanced.job = e;
				npc.advanced.setJob(job);
				break;
			}
		}
	}

	/**
	 * @return The item held in the right hand
	 */
	public IItemStack getRightItem(){
		return NpcAPI.Instance().getIItemStack(npc.inventory.getWeapon());
	}

	/**
	 * @param item Item to be held in the right hand
	 */
	public void setRightItem(IItemStack item){
		if(item == null)
			npc.inventory.setWeapon(null);
		else
			npc.inventory.setWeapon(item.getMCItemStack());
		npc.script.clientNeedsUpdate = true;
	}

	/**
     * Was an old typo
	 * @return The item held in the left hand
	 */
	public IItemStack getLefttItem(){
		return NpcAPI.Instance().getIItemStack(npc.getOffHand());
	}

    /**
     * @return The item held in the left hand
     */
    public IItemStack getLeftItem(){
        return getLefttItem();
    }

	/**
	 * @param item Item to be held in the left hand
	 */
	public void setLeftItem(IItemStack item){
		if(item == null)
			npc.inventory.setOffHand(null);
		else
			npc.inventory.setOffHand(item.getMCItemStack());
		//npc.script.clientNeedsUpdate = true;
	}

	/**
	 * @return Returns the projectile the npc uses
	 */
	public IItemStack getProjectileItem(){
		return NpcAPI.Instance().getIItemStack(npc.inventory.getProjectile());
	}

	/**
	 * @param item Item to be used as projectile
	 */
	public void setProjectileItem(IItemStack item){
		if(item == null)
			npc.inventory.setProjectile(null);
		else
			npc.inventory.setProjectile(item.getMCItemStack());
		npc.script.aiNeedsUpdate = true;
	}

	public boolean canAimWhileShooting() {
		return !npc.stats.aimWhileShooting;
	}

	public void aimWhileShooting(boolean aimWhileShooting) {
		npc.stats.aimWhileShooting = aimWhileShooting;
	}

	public void setMinProjectileDelay(int minDelay){
		npc.stats.minDelay = minDelay;
	}
	public int getMinProjectileDelay(){
		return npc.stats.minDelay;
	}

	public void setMaxProjectileDelay(int maxDelay){
		npc.stats.maxDelay = maxDelay;
	}
	public int getMaxProjectileDelay(){
		return npc.stats.maxDelay;
	}

	public void setRangedRange(int rangedRange) {
		npc.stats.rangedRange = rangedRange;
	}
	public int getRangedRange() {
		return npc.stats.rangedRange;
	}
	public void setRangedRage(int rangedRage) { this.setRangedRange(rangedRage); }
	public int getRangedRage() { return this.getRangedRange(); }

	public void setFireRate(int rate) {
		npc.stats.fireRate = rate;
	}
	public int getFireRate() {
		return npc.stats.fireRate;
	}

	public void setBurstCount(int burstCount) {
		npc.stats.burstCount = burstCount;
	}
	public int getBurstCount() {
		return npc.stats.burstCount;
	}

	public void setShotCount(int shotCount) {
		npc.stats.shotCount = shotCount;
	}
	public int getShotCount() {
		return npc.stats.shotCount;
	}

	public void setAccuracy(int accuracy) {
		npc.stats.accuracy = accuracy;
	}
	public int getAccuracy() {
		return npc.stats.accuracy;
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
	public IItemStack getArmor(int slot){
		return NpcAPI.Instance().getIItemStack(npc.inventory.armor.get(slot));
	}

	/**
	 * @param slot The armor slot to set. 0:head, 1:body, 2:legs, 3:boots
	 * @param item Item to be set as armor
	 */
	@Override
	public void setArmor(int slot, IItemStack item){
		if(item == null)
			npc.inventory.armor.put(slot, null);
		else
			npc.inventory.armor.put(slot, item.getMCItemStack());

		npc.script.clientNeedsUpdate = true;
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to return (0-8)
	 * @return
	 */
	public IItemStack getLootItem(int slot) {
		return NpcAPI.Instance().getIItemStack(npc.inventory.getStackInSlot(slot+7));
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to change
	 * @param item The item the drop list slot will be changed to
	 */
	public void setLootItem(int slot, IItemStack item) {
        if(item == null || item.getMCItemStack() == null){
            npc.inventory.setInventorySlotContents(slot+7, (ItemStack) null);
        } else {
            npc.inventory.setInventorySlotContents(slot+7, item.getMCItemStack());
        }
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to return (0-8)
	 * @return The chance of dropping the item in this slot. Returns 100 if the slot is not found.
	 */
	public double getLootChance(int slot) {
		if(!npc.inventory.dropchance.containsKey(slot))
			return 100;

		return npc.inventory.dropchance.get(slot);
	}

	/**
	 *
	 * @param slot The slot from the NPC's drop list to change
	 * @param chance The new chance of dropping the item in this slot
	 */
	public void setLootChance(int slot, double chance) {
		if(!npc.inventory.dropchance.containsKey(slot))
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
			npc.ais.animationType = EnumAnimation.NONE;
		else if(type == AnimationType.SITTING)
			npc.ais.animationType = EnumAnimation.SITTING;
		else if(type == AnimationType.DANCING)
			npc.ais.animationType = EnumAnimation.DANCING;
		else if(type == AnimationType.SNEAKING)
			npc.ais.animationType = EnumAnimation.SNEAKING;
		else if(type == AnimationType.LYING)
			npc.ais.animationType = EnumAnimation.LYING;
		else if(type == AnimationType.HUGGING)
			npc.ais.animationType = EnumAnimation.HUG;
	}

	public void setTacticalVariant(int variant){
		if(variant > EnumNavType.values().length-1 || variant < 0)
			return;

		npc.ais.tacticalVariant = EnumNavType.values()[variant];
		npc.ais.directLOS = EnumNavType.values()[variant] != EnumNavType.Stalk && npc.ais.directLOS;
	}

	public int getTacticalVariant(){
		return npc.ais.tacticalVariant.ordinal();
	}

	public void setTacticalVariant(String variant){
		boolean found = false;
		for(String s : EnumNavType.names()){
			if(s.equals(variant))
				found = true;
		}

		if(!found)
			return;

		npc.ais.tacticalVariant = EnumNavType.valueOf(variant);
	}

	public String getTacticalVariantName(){
		return npc.ais.tacticalVariant.name();
	}

	public void setCombatPolicy(int variant){
		if(variant > EnumCombatPolicy.values().length-1 || variant < 0)
			return;

		npc.ais.combatPolicy = EnumCombatPolicy.values()[variant];
	}

	public int getCombatPolicy(){
		return npc.ais.combatPolicy.ordinal();
	}

	public void setCombatPolicy(String variant){
		boolean found = false;
		for(String s : EnumCombatPolicy.names()){
			if(s.equals(variant))
				found = true;
		}

		if(!found)
			return;

		npc.ais.combatPolicy = EnumCombatPolicy.valueOf(variant);
	}

	public String getCombatPolicyName(){
		return npc.ais.combatPolicy.name();
	}

	public void setTacticalRadius(int tacticalRadius){
		if(tacticalRadius < 0)
			tacticalRadius = 0;

		npc.ais.tacticalRadius = tacticalRadius;
	}

	public int getTacticalRadius(){
		return npc.ais.tacticalRadius;
	}

	public void setIgnoreCobweb(boolean ignore){
		npc.stats.ignoreCobweb = ignore;
	}

	public boolean getIgnoreCobweb(){
		return npc.stats.ignoreCobweb;
	}

	public void setOnFoundEnemy(int onAttack){
		if(onAttack < 0 || onAttack > 3)
			return;
		npc.ais.onAttack = onAttack;
	}

	public int onFoundEnemy(){
		return npc.ais.onAttack;
	}

	public void setShelterFrom(int shelterFrom){
		if(shelterFrom < 0 || shelterFrom > 2)
			return;
		npc.ais.findShelter = shelterFrom;
	}

	public int getShelterFrom(){
		return npc.ais.findShelter;
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

	public void setVisibleTo(IPlayer player, boolean visible) {
		UUID uuid = player.getMCEntity().getPersistentID();
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

	public boolean isVisibleTo(IPlayer player) {
		return !npc.scriptInvisibleToPlayer((EntityPlayer) player.getMCEntity());
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
	public double getMeleeStrength(){
		return npc.stats.getAttackStrength();
	}

	/**
	 * @param strength The melee strength
	 */
	public void setMeleeStrength(double strength){
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
	 * @return The melee range
	 */
	public int getMeleeRange(){
		return npc.stats.attackRange;
	}

	/**
	 * @param range The melee range
	 */
	public void setMeleeRange(int range){
		npc.stats.attackRange = range;
	}

	/**
	 * @return The swing warmup time in ticks
	 */
	public int getSwingWarmup(){
		return npc.stats.swingWarmUp;
	}

	/**
	 * @param ticks The amount of time before damage to swing arm
	 */
	public void setSwingWarmup(int ticks){
		npc.stats.swingWarmUp = ticks;
	}

	/**
	 * @return The knockback strength
	 */
	public int getKnockback(){
		return npc.stats.knockback;
	}

	/**
	 * @param knockback The melee range
	 */
	public void setKnockback(int knockback){
		npc.stats.knockback = knockback;
	}

	/**
	 * @return The aggro range
	 */
	public int getAggroRange(){
		return npc.stats.aggroRange;
	}

	/**
	 * @param aggroRange The new aggro range
	 */
	public void setAggroRange(int aggroRange){
		npc.stats.aggroRange = aggroRange;
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
		npc.stats.playerSetCanDespawn = canDespawn;
	}

	public boolean spawnedFromSoulStone() {
		return !npc.advanced.soulStonePlayerName.equals("");
	}

	public String getSoulStonePlayerName() {
		return npc.advanced.soulStonePlayerName;
	}

	public boolean getRefuseSoulStone() {
		return npc.advanced.refuseSoulStone;
	}

	public void setRefuseSoulStone(boolean refuse) {
		npc.advanced.refuseSoulStone = refuse;
	}

	public boolean isSoulStoneInit() {
		return npc.advanced.soulStoneInit;
	}

	public int getMinPointsToSoulStone() {
		return npc.advanced.minFactionPointsToSoulStone;
	}

	public void setMinPointsToSoulStone(int points) {
		npc.advanced.minFactionPointsToSoulStone = points;
	}

	public void giveItem(IPlayer player, IItemStack item){
		npc.givePlayerItem((EntityPlayer) player.getMCEntity(), item.getMCItemStack());
	}

	public void executeCommand(String command){
		NoppesUtilServer.runCommand(npc, npc.getCommandSenderName(), command, null);
	}

	public IModelData getModelData() {
		return npc instanceof EntityCustomNpc ? ((EntityCustomNpc) npc).modelData : null;
	}

	public void setHeadScale(float x, float y, float z){
		if(npc instanceof EntityCustomNpc) {
			((EntityCustomNpc) this.npc).modelData.modelScale.head.scaleX = ValueUtil.clamp(x, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.head.scaleY = ValueUtil.clamp(y, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.head.scaleZ = ValueUtil.clamp(z, 0.5f, 1.5f);

			npc.script.clientNeedsUpdate = true;
		}
	}

	public void setBodyScale(float x, float y, float z){
		if(npc instanceof EntityCustomNpc) {
			((EntityCustomNpc) this.npc).modelData.modelScale.body.scaleX = ValueUtil.clamp(x, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.body.scaleY = ValueUtil.clamp(y, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.body.scaleZ = ValueUtil.clamp(z, 0.5f, 1.5f);

			npc.script.clientNeedsUpdate = true;
		}
	}

	public void setArmsScale(float x, float y, float z){
		if(npc instanceof EntityCustomNpc) {
			((EntityCustomNpc) this.npc).modelData.modelScale.arms.scaleX = ValueUtil.clamp(x, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.arms.scaleY = ValueUtil.clamp(y, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.arms.scaleZ = ValueUtil.clamp(z, 0.5f, 1.5f);

			npc.script.clientNeedsUpdate = true;
		}
	}

	public void setLegsScale(float x, float y, float z){
		if(npc instanceof EntityCustomNpc) {
			((EntityCustomNpc) this.npc).modelData.modelScale.legs.scaleX = ValueUtil.clamp(x, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.legs.scaleY = ValueUtil.clamp(y, 0.5f, 1.5f);
			((EntityCustomNpc) this.npc).modelData.modelScale.legs.scaleZ = ValueUtil.clamp(z, 0.5f, 1.5f);

			npc.script.clientNeedsUpdate = true;
		}
	}

    /**
     * @since 1.7.10c
     * @param resistance Explosion resistance (0-2) default is 1
     */
	public void setExplosionResistance(float resistance){
		npc.stats.resistances.explosion = ValueUtil.clamp(resistance, 0, 2);
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
		npc.stats.resistances.playermelee = ValueUtil.clamp(resistance, 0, 2);
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
		npc.stats.resistances.arrow = ValueUtil.clamp(resistance, 0, 2);
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
	public void setKnockbackResistance(double resistance){
		npc.stats.resistances.knockback = ValueUtil.clamp((float) resistance, 0, 2);
	}

    /**
     * @return Returns Knockback Resistance
     */
	public double getKnockbackResistance(){
		return npc.stats.resistances.knockback;
	}

	/**
	 * @param type Retaliation type. 0:normal, 1:panic, 2:retreat, 3:nothing
	 */
	public void setRetaliateType(int type){
		if (type < 0)
			type = 0;
		if (type > 3)
			type = 3;

		npc.ais.onAttack = type;
		npc.updateTasks();
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

	public boolean getCanDrown() {
		return npc.stats.drowningType > 0;
	}
	public void setCanDrown(boolean d) {
		npc.stats.drowningType = d ? 1 : 0;
	}

	public void setDrowningType(int type) {
		if (type < 0)
			type = 0;
		if (type > 2)
			type = 2;

		npc.stats.drowningType = type;
	}

	public boolean canBreathe() {
		return npc.canBreathe();
	}

	@Override
	public long getAge(){
		return npc.totalTicksAlive;
	}

	public ITimers getTimers() {
		return ((EntityNPCInterface)this.npc).timers;
	}

	public void setFly(int fly){
		if(fly > 0)
			fly = 1;
		else fly = 0;

		npc.ais.movementType = fly;
	}

	public boolean canFly(){
		return npc.ais.movementType == 1;
	}

	public void setFlySpeed(double flySpeed){
		if(flySpeed < 0.0D)
			flySpeed = 0.0D;

		npc.ais.flySpeed = flySpeed;
	}

	public double getFlySpeed(double flySpeed){
		return npc.ais.flySpeed;
	}

	public void setFlyGravity(double flyGravity){
		if(flyGravity < 0.0D)
			flyGravity = 0.0D;
		if(flyGravity > 1.0D)
			flyGravity = 1.0D;

		npc.ais.flyGravity = flyGravity;
	}

	public double getFlyGravity(double flySpeed){
		return npc.ais.flyGravity;
	}

	public void setFlyHeightLimit(int flyHeightLimit){
		if(flyHeightLimit < 0)
			flyHeightLimit = 0;

		this.npc.ais.flyHeightLimit = flyHeightLimit;
	}
	public int getFlyHeightLimit(int flyHeightLimit){
		return this.npc.ais.flyHeightLimit;
	}

	public void limitFlyHeight(boolean limit){
		this.npc.ais.hasFlyLimit = limit;
	}
	public boolean isFlyHeightLimited(boolean limit){
		return this.npc.ais.hasFlyLimit;
	}

	public void setSpeed(double speed) {
		npc.ais.setWalkingSpeed(speed);
	}

	public double getSpeed() {
		return npc.ais.getWalkingSpeed();
	}

	public void setSkinType(byte type) {
		npc.display.skinType = type;
		npc.script.clientNeedsUpdate = true;
	}

	public byte getSkinType() {
		return npc.display.skinType;
	}

	public void setSkinUrl(String url){
		if(this.npc.display.url.equals(url))
			return;
		this.npc.display.url = url;
		npc.textureLocation = null;
        if(npc.display.skinType < 2)
            npc.display.skinType = 2;
		npc.script.clientNeedsUpdate = true;
	}

	public String getSkinUrl() {
		return this.npc.display.url;
	}

	public void setCloakTexture(String cloakTexture) {
		npc.display.cloakTexture = cloakTexture;
		npc.script.clientNeedsUpdate = true;
	}

	public String getCloakTexture() {
		return npc.display.cloakTexture;
	}

	public void setOverlayTexture(String overlayTexture) {
		if (this.getOverlays().size() >= ConfigMain.SkinOverlayLimit) {
			return;
		}

		this.getOverlays().add(0, NpcAPI.Instance().createSkinOverlay(overlayTexture));
		npc.script.clientNeedsUpdate = true;
	}

	public String getOverlayTexture() {
		if (!npc.display.skinOverlayData.overlayList.containsKey(0))
			return "";

		return npc.display.skinOverlayData.overlayList.get(0).getTexture();
	}

	public IOverlayHandler getOverlays() {
		return npc.display.skinOverlayData;
	}

	public void setCollisionType(int type){
		npc.stats.collidesWith = type;
	}
	public int getCollisionType(){
		return npc.stats.collidesWith;
	}

	public void updateClient() { this.npc.updateClient(); }

	public void updateAI() { this.npc.updateTasks(); }

    public IActionManager getActionManager() {
        return npc.actionManager;
    }

    public IMagicData getMagicData() {
        return npc.stats.magicData;
    }
}
