package noppes.npcs.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CombatHandler {

	private Map<EntityLivingBase, Float> aggressors = new HashMap<EntityLivingBase, Float>();
	private Map<EntityLivingBase, LinkedList<Float>> recentDamages = new HashMap<>();
	private EntityNPCInterface npc;
	private long startTime = 0;
	private int combatResetTimer = 0;
	public CombatHandler(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public void update(){
		if(npc.isKilled()){
			if(npc.isAttacking()){
				reset();
			}
			return;
		}
		if(npc.getAttackTarget() != null && !npc.isAttacking()){
			start();
		}

		if(!shouldCombatContinue()){
			if(combatResetTimer++ > 40){
				reset();
			}
			return;
		}
		combatResetTimer = 0;
	}


	private boolean shouldCombatContinue() {
    	if(npc.getAttackTarget() == null)
    		return false;
    	return isValidTarget(npc.getAttackTarget());
	}

	public void damage(DamageSource source, float damageAmount) {
		combatResetTimer = 0;
		Entity e = NoppesUtilServer.GetDamageSource(source);

		if (e instanceof EntityLivingBase) {
			EntityLivingBase el = (EntityLivingBase) e;

			// Update recent damages
			LinkedList<Float> recentDamageList = recentDamages.computeIfAbsent(el, k -> new LinkedList<>());
			recentDamageList.addLast(damageAmount);
			if (recentDamageList.size() > 15) {
				recentDamageList.removeFirst();
			}

			// Update total aggressor damage
			Float f = aggressors.get(el);
			if (f == null) {
				f = 0f;
			}
			aggressors.put(el, f + damageAmount);
		}
	}

	public void start(){
		combatResetTimer = 0;
		startTime = npc.worldObj.getWorldInfo().getWorldTotalTime();
		npc.setBoolFlag(true, 4);
	}

	public void reset(){
		combatResetTimer = 0;
		startTime = 0;
		aggressors.clear();
		recentDamages.clear();
		npc.setBoolFlag(false, 4);
	}

	public boolean checkTarget() {
		if(aggressors.isEmpty() || npc.ticksExisted % 10 != 0)
			return false;
		EntityLivingBase target = npc.getAttackTarget();
		Float current = 0f;
		if(isValidTarget(target)){
			current = aggressors.get(target);
			if(current == null)
				current = 0f;
		}
		else
			target = null;
		for (Map.Entry<EntityLivingBase, Float> entry : aggressors.entrySet()){
			if(entry.getValue() > current && isValidTarget(entry.getKey())){
				current = entry.getValue();
				target = entry.getKey();
			}
		}
		return target == null;
	}

	public boolean isValidTarget(EntityLivingBase target){
		if(target == null || !target.isEntityAlive())
			return false;

        if(target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.disableDamage)
        	return false;

		return npc.isInRange(target, npc.stats.aggroRange);
	}

	public float calculateThreatLevel(EntityLivingBase entity) {
		float threatLevel = 0.0f;
		LinkedList<Float> recentDamageList = recentDamages.get(entity);

		if (recentDamageList != null) {
			long currentTime = npc.worldObj.getWorldInfo().getWorldTotalTime();
			for (Float damage : recentDamageList) {
				float decayFactor = Math.max(0, 1 - (float) startTime / currentTime);

				threatLevel += damage * decayFactor;
			}
		}

		return threatLevel;
	}

	public boolean shouldChangeTarget(double chance) {
		// Assuming randomNum is a random number between 0 and 100
		double randomNum = Math.random() * 100; // Generates a random number between 0 and 100
		return randomNum <= chance;
	}

	public boolean shouldSwitchTactically(EntityLivingBase originalTarget, EntityLivingBase newTarget) {
		// Implement your Tactical Switcher conditions here
		// Example: Switch if the new target has a higher threat level
		float currentThreat = calculateThreatLevel(originalTarget);
		float newThreat = calculateThreatLevel(newTarget);

		return newThreat > currentThreat;
	}
}
