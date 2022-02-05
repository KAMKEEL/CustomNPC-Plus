package noppes.npcs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAI {
	private EntityNPCInterface npc;
	
	public int onAttack = 0; //0:fight 1:panic 2:retreat 3:nothing
	public int doorInteract = 2;
	public int findShelter = 2;
	public int distanceToMelee = 4;
	public int canFireIndirect = 0;
	public boolean canSwim = true;
	public boolean reactsToFire = false;
	public boolean avoidsWater = false;
	public boolean avoidsSun = false;
	public boolean returnToStart = true;
	public boolean directLOS = true;
	public boolean canLeap = false;
	public boolean canSprint = false;
	public boolean stopAndInteract = true;
	public EnumNavType tacticalVariant = EnumNavType.Default;
	public int useRangeMelee = 0;
	public int tacticalRadius = 8;
	
	public EnumAnimation animationType = EnumAnimation.NONE;
	public EnumStandingType standingType = EnumStandingType.RotateBody;
	public EnumMovingType movingType = EnumMovingType.Standing;
	public boolean npcInteracting = true;

	public int orientation = 0;
	public float bodyOffsetX = 5, bodyOffsetY = 5, bodyOffsetZ = 5;
	public int walkingRange = 10;
	private int moveSpeed = 5;

	private List<int[]> movingPath = new ArrayList<int[]>();
	public int[] startPos;
	public int movingPos = 0;
	public int movingPattern = 0; // 0:Looping 1:Backtracking

	public boolean movingPause = true;

	public boolean ignoreCobweb = false;
	
	public DataAI(EntityNPCInterface npc){
		this.npc = npc;
	}

	public void readToNBT(NBTTagCompound compound) {		
		canSwim = compound.getBoolean("CanSwim");
		reactsToFire = compound.getBoolean("ReactsToFire");
		avoidsWater = compound.getBoolean("AvoidsWater");
		avoidsSun = compound.getBoolean("AvoidsSun");
		returnToStart = compound.getBoolean("ReturnToStart");
		onAttack = compound.getInteger("OnAttack");
		doorInteract = compound.getInteger("DoorInteract");
		findShelter = compound.getInteger("FindShelter");
		directLOS = compound.getBoolean("DirectLOS");
		canLeap = compound.getBoolean("CanLeap");
		canSprint = compound.getBoolean("CanSprint");
		canFireIndirect = compound.getInteger("FireIndirect");
		useRangeMelee = compound.getInteger("RangeAndMelee");
		distanceToMelee = compound.getInteger("DistanceToMelee");
		tacticalRadius = compound.getInteger("TacticalRadius");	
		movingPause = compound.getBoolean("MovingPause");
		ignoreCobweb = compound.getBoolean("IgnoreCobweb");
		npcInteracting = compound.getBoolean("npcInteracting");
		stopAndInteract = compound.getBoolean("stopAndInteract");
		

		animationType = EnumAnimation.values()[compound.getInteger("MoveState") % EnumAnimation.values().length];
		standingType = EnumStandingType.values()[compound.getInteger("StandingState") % EnumStandingType.values().length];
		movingType = EnumMovingType.values()[compound.getInteger("MovingState") % EnumMovingType.values().length];
		tacticalVariant = EnumNavType.values()[compound.getInteger("TacticalVariant") % EnumNavType.values().length];
	
		orientation = compound.getInteger("Orientation");
		bodyOffsetY = compound.getFloat("PositionOffsetY");
		bodyOffsetZ = compound.getFloat("PositionOffsetZ");
		bodyOffsetX = compound.getFloat("PositionOffsetX");
		walkingRange = compound.getInteger("WalkingRange");
		setWalkingSpeed(compound.getInteger("MoveSpeed"));
		
		setMovingPath(NBTTags.getIntegerArraySet(compound.getTagList("MovingPathNew",10)));
		movingPos = compound.getInteger("MovingPos");
		movingPattern = compound.getInteger("MovingPatern");
		
		startPos = compound.getIntArray("StartPosNew");
		if (startPos == null || startPos.length != 3){
			startPos = new int[] { 
				MathHelper.floor_double(npc.posX),
				MathHelper.floor_double(npc.posY),
				MathHelper.floor_double(npc.posZ) };
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("CanSwim", canSwim);
		compound.setBoolean("ReactsToFire", reactsToFire);
		compound.setBoolean("AvoidsWater", avoidsWater );
		compound.setBoolean("AvoidsSun", avoidsSun);
		compound.setBoolean("ReturnToStart", returnToStart);
		compound.setInteger("OnAttack", onAttack);
		compound.setInteger("DoorInteract", doorInteract);
		compound.setInteger("FindShelter", findShelter);
		compound.setBoolean("DirectLOS", directLOS);
		compound.setBoolean("CanLeap", canLeap);
		compound.setBoolean("CanSprint", canSprint);
		compound.setInteger("FireIndirect", canFireIndirect);
		compound.setInteger("RangeAndMelee", useRangeMelee);
		compound.setInteger("DistanceToMelee", distanceToMelee);
		compound.setInteger("TacticalRadius", tacticalRadius);
		compound.setBoolean("MovingPause", movingPause);
		compound.setBoolean("IgnoreCobweb", ignoreCobweb);
		compound.setBoolean("npcInteracting", npcInteracting);
		compound.setBoolean("stopAndInteract", stopAndInteract);

		compound.setInteger("MoveState", animationType.ordinal());
		compound.setInteger("StandingState", standingType.ordinal());
		compound.setInteger("MovingState", movingType.ordinal());
		compound.setInteger("TacticalVariant", tacticalVariant.ordinal());
		
		compound.setInteger("Orientation", orientation);
		compound.setFloat("PositionOffsetX", bodyOffsetX);
		compound.setFloat("PositionOffsetY", bodyOffsetY);
		compound.setFloat("PositionOffsetZ", bodyOffsetZ);
		compound.setInteger("WalkingRange", walkingRange);
		compound.setInteger("MoveSpeed", moveSpeed);
		
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(movingPath));
		compound.setInteger("MovingPos", movingPos);
		compound.setInteger("MovingPatern", movingPattern);
		
		npc.setAvoidWater(avoidsWater);
		
		compound.setIntArray("StartPosNew", npc.getStartPos());
		
		return compound;
	}

	public List<int[]> getMovingPath() {
		if(movingPath.isEmpty() && startPos != null)
			movingPath.add(startPos);
		return movingPath;
	}

	public void setMovingPath(List<int[]> list) {
		movingPath = list;
		if(!movingPath.isEmpty())
			startPos = movingPath.get(0);
	}
	
	public int[] getCurrentMovingPath(){
		List<int[]> list = getMovingPath();
		if(list.size() == 1){
			movingPos = 0;
		}
		else if(movingPos >= list.size()){
			if(movingPattern == 0)
				movingPos = 0;
			else{
				int size = list.size() * 2 - 2;
				if(movingPos >= size){
					movingPos = 0;
				}
				else if(movingPos >= list.size()){
					return list.get(list.size() - (movingPos % list.size()) - 2);
				}				
			}
		}
		
		
		return list.get(movingPos);
	}
	
	public void incrementMovingPath(){
		List<int[]> list = getMovingPath();
		if(list.size() == 1){
			movingPos = 0;
		}
		else if(movingPattern == 0){
			movingPos++;
			movingPos = movingPos % list.size();
		}
		else if(movingPattern == 1){
			movingPos++;
			int size = list.size() * 2 - 2;
			movingPos = movingPos % size;
		}
	}
	

	public void decreaseMovingPath() {
		List<int[]> list = getMovingPath();
		if(list.size() == 1){
			movingPos = 0;
		}
		else if(movingPattern == 0){
			movingPos--;
			if(movingPos < 0)
				movingPos += list.size();
		}
		else if(movingPattern == 1){
			movingPos--;
			if(movingPos < 0){
				int size = list.size() * 2 - 2;
				movingPos += size;
			}
		}
	}

	public double getDistanceSqToPathPoint() {
		int[] pos = getCurrentMovingPath();
		return npc.getDistanceSq(pos[0] + 0.5, pos[1], pos[2] + 0.5);
	}
	
	public void setWalkingSpeed(int speed){
		this.moveSpeed = speed;
		npc.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(npc.getSpeed());
	}
	
	public int getWalkingSpeed(){
		return this.moveSpeed;
	}
}
