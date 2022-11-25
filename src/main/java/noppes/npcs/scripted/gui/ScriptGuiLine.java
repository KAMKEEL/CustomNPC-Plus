package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ILine;

public class ScriptGuiLine extends ScriptGuiComponent implements ILine {
    int x1;
    int y1;
    int x2;
    int y2;
    int thickness = 2;

    public ScriptGuiLine(){
    }

    public ScriptGuiLine(int id, int x1, int y1, int x2, int y2){
        this.setPos(x1, y1);
        this.setID(id);
        this.hoverText = new String[]{""};

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public ScriptGuiLine(int id, int x1, int y1, int x2, int y2, int color, int thickness){
        this.setPos(x1, y1);
        this.setID(id);
        this.hoverText = new String[]{""};

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public int getType() {
        return 6;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setInteger("X1",x1);
        nbt.setInteger("Y1",y1);
        nbt.setInteger("X2",x2);
        nbt.setInteger("Y2",y2);
        nbt.setInteger("Thickness",thickness);

        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        x1 = nbt.getInteger("X1");
        y1 = nbt.getInteger("Y1");
        x2 = nbt.getInteger("X2");
        y2 = nbt.getInteger("Y2");
        thickness = nbt.getInteger("Thickness");

        return this;
    }

    public int getX1(){
        return this.x1;
    }
    public int getY1(){
        return this.y1;
    }
    public int getX2(){
        return this.x2;
    }
    public int getY2(){
        return this.y2;
    }
    public int getThickness(){
        return this.thickness;
    }

    public void setX1(int x1){
        this.x1 = x1;
    }
    public void setY1(int y1){
        this.y1 = y1;
    }
    public void setX2(int x2){
        this.x2 = x2;
    }
    public void setY2(int y2){
        this.y2 = y2;
    }
    public void setThickness(int thickness){
        this.thickness = thickness;
    }
}
