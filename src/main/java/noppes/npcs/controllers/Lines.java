package noppes.npcs.controllers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Lines {
	private static final Random random = new Random();
	private int lastLine = -1;

	public HashMap<Integer,Line> lines = new HashMap<Integer,Line>();

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();

        NBTTagList nbttaglist = new NBTTagList();
        for(int slot : lines.keySet())
        {
        	Line line = lines.get(slot);
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setString("Line", line.text);
            nbttagcompound.setString("Song", line.sound);
            
            nbttaglist.appendTag(nbttagcompound);
        }
        
		compound.setTag("Lines", nbttaglist);
		return compound;
	}

	public void readNBT(NBTTagCompound compound) {
		NBTTagList nbttaglist = compound.getTagList("Lines", 10);

		HashMap<Integer, Line> map = new HashMap<Integer, Line>();
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            Line line = new Line();
            line.text = nbttagcompound.getString("Line");
            line.sound = nbttagcompound.getString("Song");
            
            map.put(nbttagcompound.getInteger("Slot"), line);
        }
        lines = map;
	}

	public Line getLine(boolean isRandom) {
		if(lines.isEmpty())
			return null;
		if(isRandom){
			List<Line> lines = new ArrayList<Line>(this.lines.values());
			return lines.get(random.nextInt(lines.size()));
		}
		lastLine++;
		while(true){
			lastLine %= 8;
			Line line = lines.get(lastLine);
			if(line != null)
				return line.copy();
			lastLine++;
		}
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}
}
