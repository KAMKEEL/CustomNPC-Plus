package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class DialogColourData {
    private boolean enableColourSettings = false;
    private int lineColour1 = 0xff8d3800;
    private int lineColour2 = 0xfffea53b;
    private int lineColour3 = 0xff8d3800;
    private int slotColour = 0xfff96605;
    private int buttonAcceptColour = 0xfff96605;
    private int buttonRejectColour = 0xfff96605;
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("EnableColourSettings", enableColourSettings);
        if(enableColourSettings){
            compound.setInteger("LineColour1",lineColour1);
            compound.setInteger("LineColour2",lineColour2);
            compound.setInteger("LineColour3",lineColour3);
            compound.setInteger("SlotColour",slotColour);
            compound.setInteger("ButtonAcceptColour",buttonAcceptColour);
            compound.setInteger("ButtonRejectColour",buttonRejectColour);
        }
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.enableColourSettings = compound.getBoolean("EnableColourSettings");
        if(enableColourSettings){
            lineColour1=compound.getInteger("LineColour1");
            lineColour2=compound.getInteger("LineColour2");
            lineColour3=compound.getInteger("LineColour3");
            slotColour=compound.getInteger("SlotColour");
            buttonAcceptColour=compound.getInteger("ButtonAcceptColour");
            buttonRejectColour=compound.getInteger("ButtonRejectColour");
        }
    }

    public boolean getEnableColourSettings() {
        return enableColourSettings;
    }

    public void setEnableColourSettings(boolean enableColourSettings) {
        this.enableColourSettings = enableColourSettings;
    }

    public int getLineColour1() {
        return lineColour1;
    }

    public void setLineColour1(int lineColour1) {
        this.lineColour1 = lineColour1;
    }

    public int getLineColour2() {
        return lineColour2;
    }

    public void setLineColour2(int lineColour2) {
        this.lineColour2 = lineColour2;
    }

    public int getLineColour3() {
        return lineColour3;
    }

    public void setLineColour3(int lineColour3) {
        this.lineColour3 = lineColour3;
    }

    public int getSlotColour() {
        return slotColour;
    }

    public void setSlotColour(int slotColour) {
        this.slotColour = slotColour;
    }

    public int getButtonAcceptColour() {
        return buttonAcceptColour;
    }

    public void setButtonAcceptColour(int buttonAcceptColour) {
        this.buttonAcceptColour = buttonAcceptColour;
    }

    public int getButtonRejectColour() {
        return buttonRejectColour;
    }

    public void setButtonRejectColour(int buttonRejectColour) {
        this.buttonRejectColour = buttonRejectColour;
    }
}
