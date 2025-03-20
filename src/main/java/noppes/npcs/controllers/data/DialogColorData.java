package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class DialogColorData {
    private boolean enableColorSettings = false;
    private int lineColor1 = 0xff8d3800;
    private int lineColor2 = 0xfffea53b;
    private int lineColor3 = 0xff8d3800;
    private int slotColor = 0xfff96605;
    private int buttonAcceptColor = 0xfff96605;
    private int buttonRejectColor = 0xfff96605;

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("ColorSettings", enableColorSettings);
        if(enableColorSettings){
            compound.setInteger("LineColor1", lineColor1);
            compound.setInteger("LineColor2", lineColor2);
            compound.setInteger("LineColor3", lineColor3);
            compound.setInteger("SlotColor", slotColor);
            compound.setInteger("ButtonAcceptColor", buttonAcceptColor);
            compound.setInteger("ButtonRejectColor", buttonRejectColor);
        }
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.enableColorSettings = compound.getBoolean("ColorSettings");
        if(enableColorSettings){
            lineColor1 =compound.getInteger("LineColor1");
            lineColor2 =compound.getInteger("LineColor2");
            lineColor3 =compound.getInteger("LineColor3");
            slotColor =compound.getInteger("SlotColor");
            buttonAcceptColor =compound.getInteger("ButtonAcceptColor");
            buttonRejectColor =compound.getInteger("ButtonRejectColor");
        }
    }

    public boolean getEnableColorSettings() {
        return enableColorSettings;
    }

    public void setEnableColorSettings(boolean enableColorSettings) {
        this.enableColorSettings = enableColorSettings;
    }

    public int getLineColor1() {
        return lineColor1;
    }

    public void setLineColor1(int lineColour1) {
        this.lineColor1 = lineColour1;
    }

    public int getLineColor2() {
        return lineColor2;
    }

    public void setLineColor2(int lineColour2) {
        this.lineColor2 = lineColour2;
    }

    public int getLineColor3() {
        return lineColor3;
    }

    public void setLineColor3(int lineColour3) {
        this.lineColor3 = lineColour3;
    }

    public int getSlotColor() {
        return slotColor;
    }

    public void setSlotColor(int slotColour) {
        this.slotColor = slotColour;
    }

    public int getButtonAcceptColor() {
        return buttonAcceptColor;
    }

    public void setButtonAcceptColor(int buttonAcceptColour) {
        this.buttonAcceptColor = buttonAcceptColour;
    }

    public int getButtonRejectColor() {
        return buttonRejectColor;
    }

    public void setButtonRejectColor(int buttonRejectColour) {
        this.buttonRejectColor = buttonRejectColour;
    }
}
