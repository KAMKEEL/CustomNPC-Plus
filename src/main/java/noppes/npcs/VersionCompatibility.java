package noppes.npcs;

import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.Lines;
import noppes.npcs.entity.EntityNPCInterface;

public class VersionCompatibility {
	public static int ModRev = 16;

	public static void CheckNpcCompatibility(EntityNPCInterface npc, NBTTagCompound compound){
		if(npc.npcVersion == ModRev)
			return;
		if(npc.npcVersion < 12){
			CompatabilityFix(compound, npc.advanced.writeToNBT(new NBTTagCompound()));
			CompatabilityFix(compound, npc.ai.writeToNBT(new NBTTagCompound()));
			CompatabilityFix(compound, npc.stats.writeToNBT(new NBTTagCompound()));
			CompatabilityFix(compound, npc.display.writeToNBT(new NBTTagCompound()));	
			CompatabilityFix(compound, npc.inventory.writeEntityToNBT(new NBTTagCompound()));	
		}
		if(npc.npcVersion < 5){
			String texture = compound.getString("Texture");
			texture = texture.replace("/mob/customnpcs/", "customnpcs:textures/entity/");
			texture = texture.replace("/mob/", "customnpcs:textures/entity/");
			compound.setString("Texture", texture);
		}
		if(npc.npcVersion < 6 && compound.getTag("NpcInteractLines") instanceof NBTTagList){
			List<String> interactLines = NBTTags.getStringList(compound.getTagList("NpcInteractLines", 10));
			Lines lines = new Lines();
			for(int i = 0; i < interactLines.size(); i++){
				Line line = new Line();
				line.text = (String) interactLines.toArray()[i];
				lines.lines.put(i, line);
			}
			compound.setTag("NpcInteractLines", lines.writeToNBT());

			List<String> worldLines = NBTTags.getStringList(compound.getTagList("NpcLines", 10));
			lines = new Lines();
			for(int i = 0; i < worldLines.size(); i++){
				Line line = new Line();
				line.text = (String) worldLines.toArray()[i];
				lines.lines.put(i, line);
			}
			compound.setTag("NpcLines", lines.writeToNBT());

			List<String> attackLines = NBTTags.getStringList(compound.getTagList("NpcAttackLines", 10));
			lines = new Lines();
			for(int i = 0; i < attackLines.size(); i++){
				Line line = new Line();
				line.text = (String) attackLines.toArray()[i];
				lines.lines.put(i, line);
			}
			compound.setTag("NpcAttackLines", lines.writeToNBT());

			List<String> killedLines = NBTTags.getStringList(compound.getTagList("NpcKilledLines", 10));
			lines = new Lines();
			for(int i = 0; i < killedLines.size(); i++){
				Line line = new Line();
				line.text = (String) killedLines.toArray()[i];
				lines.lines.put(i, line);
			}
			compound.setTag("NpcKilledLines", lines.writeToNBT());

		}
		if(npc.npcVersion == 12){
			NBTTagList list = compound.getTagList("StartPos", 3);
			if(list.tagCount() == 3){
				int z = ((NBTTagInt) list.removeTag(2)).func_150287_d();
				int y = ((NBTTagInt) list.removeTag(1)).func_150287_d();
				int x = ((NBTTagInt) list.removeTag(0)).func_150287_d();
				
				compound.setIntArray("StartPosNew", new int[]{x,y,z});
			}
		}
		if(npc.npcVersion == 13){
			boolean bo = compound.getBoolean("HealthRegen");
			compound.setInteger("HealthRegen", bo?1:0);
			NBTTagCompound comp = compound.getCompoundTag("TransformStats");
			bo = comp.getBoolean("HealthRegen");
			comp.setInteger("HealthRegen", bo?1:0);
    		compound.setTag("TransformStats", comp);
			
		}
		npc.npcVersion = ModRev;
	}
	public static void CheckAvailabilityCompatibility(ICompatibilty compatibilty, NBTTagCompound compound){
		if(compatibilty.getVersion() == ModRev)
			return;
		CompatabilityFix(compound, compatibilty.writeToNBT(new NBTTagCompound()));
		
		compatibilty.setVersion(ModRev);
	}
	private static void CompatabilityFix(NBTTagCompound compound,
			NBTTagCompound check) {
		Collection<String> tags = check.func_150296_c();
		for(String name : tags){
			NBTBase nbt = check.getTag(name);
			if(!compound.hasKey(name)){
				compound.setTag(name, nbt);
			}
			else if(nbt instanceof NBTTagCompound && compound.getTag(name) instanceof NBTTagCompound){
				CompatabilityFix(compound.getCompoundTag(name), (NBTTagCompound)nbt);
			}
		}
	}
}
