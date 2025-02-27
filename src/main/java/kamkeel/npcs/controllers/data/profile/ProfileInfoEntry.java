package kamkeel.npcs.controllers.data.profile;

import net.minecraft.nbt.NBTTagCompound;

public class ProfileInfoEntry {
    private String label;
    private String result;
    private int labelColor;
    private int resultColor;

    public ProfileInfoEntry() {}

    public ProfileInfoEntry(String label, int labelColor, String result, int resultColor) {
        this.label = label;
        this.result = result;
        this.labelColor = labelColor;
        this.resultColor = resultColor;
    }

    public ProfileInfoEntry(String label, int labelColor, int result, int resultColor) {
        this(label, labelColor, String.valueOf(result), resultColor);
    }

    public ProfileInfoEntry(String label, String result) {
        this(label, 0xFFFFFF, result, 0xFFFFFF);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Label", label);
        tag.setInteger("LabelColor", labelColor);
        tag.setString("Result", result);
        tag.setInteger("ResultColor", resultColor);
        return tag;
    }

    public static ProfileInfoEntry readFromNBT(NBTTagCompound tag) {
        return new ProfileInfoEntry(
            tag.getString("Label"),
            tag.getInteger("LabelColor"),
            tag.getString("Result"),
            tag.getInteger("ResultColor")
        );
    }

    public String getLabel() { return label; }
    public String getResult() { return result; }
    public int getLabelColor() { return labelColor; }
    public int getResultColor() { return resultColor; }
}
