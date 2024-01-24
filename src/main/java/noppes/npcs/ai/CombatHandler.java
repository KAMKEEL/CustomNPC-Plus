package noppes.npcs.ai;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;

public class CombatHandler {
	
	private Map<EntityLivingBase, Float> aggressors = new HashMap<EntityLivingBase, Float>();
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

	public void damage(DamageSource source, float damageAmount){
		combatResetTimer = 0;
		Entity e = NoppesUtilServer.GetDamageSourcee(source);
		System.out.println(e);
		if(e instanceof EntityLivingBase){
			EntityLivingBase el = (EntityLivingBase) e;
			Float f = aggressors.get(el);
			if(f == null)
				f = 0f;
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
		aggressors.clear();
		npc.setBoolFlag(true, 4);
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

}
