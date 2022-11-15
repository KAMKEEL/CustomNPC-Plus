package noppes.npcs.scripted.roles;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.PlayerItemGiverData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.jobs.IJobItemGiver;

import java.util.ArrayList;
import java.util.Arrays;

public class ScriptJobItemGiver extends ScriptJobInterface implements IJobItemGiver {
	private JobItemGiver job;

	public ScriptJobItemGiver(JobItemGiver job) {
		super(job);
		this.job = job;
	}

	public ScriptJobItemGiver(EntityNPCInterface npc){
		super(npc);
		this.job = (JobItemGiver) npc.jobInterface;
	}
		
	@Override
	public int getType(){
		return JobType.ITEMGIVER;
	}

	public void setCooldown(int cooldown) {
		if (cooldown < 0)
			cooldown = 0;

		job.cooldown = cooldown;
	}

	public void setCooldownType(int type) {
		if (type < 0) {
			type = 0;
		} else if (type > 2) {
			type = 2;
		}
		job.cooldownType = type;
	}

	public int getCooldownType() {
		return job.cooldownType;
	}

	public void setGivingMethod(int method) {
		if (method < 0) {
			method = 0;
		} else if (method > 4) {
			method = 4;
		}
		job.givingMethod = method;
	}

	public int getGivingMethod() {
		return job.givingMethod;
	}

	public void setLines(String[] lines) {
		job.lines = new ArrayList<>(Arrays.asList(lines));
	}

	public String[] getLines() {
		return job.lines.toArray(new String[0]);
	}

	public void setAvailability(IAvailability availability) {
		job.availability = (Availability) availability;
	}

	public IAvailability getAvailability() {
		return job.availability;
	}

	public void setItem(int slot, IItemStack item) {
		if (slot < 0) {
			slot = 0;
		} else if (slot > 8) {
			slot = 8;
		}

		job.inventory.items.put(slot, item.getMCItemStack());
	}

	public IItemStack[] getItems() {
		ArrayList<IItemStack> items = new ArrayList<>();

		for (int i = 0; i < job.inventory.getSizeInventory(); i++) {
			items.add(NpcAPI.Instance().getIItemStack(job.inventory.getStackInSlot(i)));
		}

		return items.toArray(new IItemStack[0]);
	}

	public boolean giveItems(IPlayer player) {
		return job.giveItems((EntityPlayer) player.getMCEntity());
	}

	public boolean canPlayerInteract(IPlayer player) {
		return job.canPlayerInteract((PlayerItemGiverData) player.getData().getItemGiverData());
	}
}
