package kamkeel.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class InfoEntry {
    private String label;
    private String result;
    private int labelColor;
    private int resultColor;

    public InfoEntry() {}

    public InfoEntry(String label, int labelColor, String result, int resultColor) {
        this.label = label;
        this.result = result;
        this.labelColor = labelColor;
        this.resultColor = resultColor;
    }

    public InfoEntry(String label, int labelColor, int result, int resultColor) {
        this(label, labelColor, String.valueOf(result), resultColor);
    }

    public InfoEntry(String label, String result) {
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

    public static InfoEntry readFromNBT(NBTTagCompound tag) {
        return new InfoEntry(
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
