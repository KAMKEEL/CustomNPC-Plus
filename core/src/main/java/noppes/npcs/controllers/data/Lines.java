package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.ILine;
import noppes.npcs.api.handler.data.ILines;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.core.NBT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Lines implements ILines {
    private static final Random random = new Random();
    private int lastLine = -1;

    public HashMap<Integer, Line> lines = new HashMap<Integer, Line>();

    public INbt writeToNBT() {
        INbt compound = NBT.compound();

        INbtList nbttaglist = NBT.list();
        for (int slot : lines.keySet()) {
            Line line = lines.get(slot);
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setString("Line", line.text);
            nbttagcompound.setString("Song", line.sound);

            nbttaglist.addCompound(nbttagcompound);
        }

        compound.setTagList("Lines", nbttaglist);
        return compound;
    }

    public void readNBT(INbt compound) {
        INbtList nbttaglist = compound.getTagList("Lines", 10);

        HashMap<Integer, Line> map = new HashMap<Integer, Line>();
        for (int i = 0; i < nbttaglist.size(); i++) {
            INbt nbttagcompound = nbttaglist.getCompound(i);
            Line line = new Line();
            line.text = nbttagcompound.getString("Line");
            line.sound = nbttagcompound.getString("Song");

            map.put(nbttagcompound.getInteger("Slot"), line);
        }
        lines = map;
    }

    public ILine createLine(String text) {
        return new Line(text);
    }

    public ILine getLine(boolean isRandom) {
        if (lines.isEmpty())
            return null;
        if (isRandom) {
            List<Line> lines = new ArrayList<Line>(this.lines.values());
            return lines.get(random.nextInt(lines.size()));
        }
        lastLine++;
        while (true) {
            lastLine %= 8;
            Line line = lines.get(lastLine);
            if (line != null)
                return line.copy();
            lastLine++;
        }
    }

    public ILine getLine(int lineIndex) {
        return lines.get(lineIndex);
    }

    public void setLine(int lineIndex, ILine line) {
        lines.put(lineIndex, (Line) line);
    }

    public void removeLine(int lineIndex) {
        lines.remove(lineIndex);
    }

    public void clear() {
        lines.clear();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public Integer[] getKeys() {
        return lines.keySet().toArray(new Integer[0]);
    }
}
