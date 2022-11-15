package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.gui.IScroll;

public class ScriptGuiScroll extends ScriptGuiComponent implements IScroll {
    int width;
    int height;
    int defaultSelection = -1;
    String[] list;
    boolean multiSelect = false;

    public ScriptGuiScroll() {
    }

    public ScriptGuiScroll(int id, int x, int y, int width, int height, String[] list) {
        this.setID(id);
        this.setPos(x, y);
        this.setSize(width, height);
        this.setList(list);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public IScroll setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public String[] getList() {
        return this.list;
    }

    public IScroll setList(String[] list) {
        this.list = list;
        return this;
    }

    public int getDefaultSelection() {
        return this.defaultSelection;
    }

    public IScroll setDefaultSelection(int defaultSelection) {
        this.defaultSelection = defaultSelection;
        return this;
    }

    public boolean isMultiSelect() {
        return this.multiSelect;
    }

    public IScroll setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        return this;
    }

    public int getType() {
        return 4;
    }

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        nbt.setIntArray("size", new int[]{this.width, this.height});
        if (this.defaultSelection >= 0) {
            nbt.setInteger("default", this.defaultSelection);
        }

        NBTTagList list = new NBTTagList();
        String[] var3 = this.list;
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String s = var3[var5];
            list.appendTag(new NBTTagString(s));
        }

        nbt.setTag("list", list);
        nbt.setBoolean("multiSelect", this.multiSelect);
        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
        if (nbt.hasKey("default")) {
            this.setDefaultSelection(nbt.getInteger("default"));
        }

        NBTTagList tagList = nbt.getTagList("list", 8);
        String[] list = new String[tagList.tagCount()];

        for(int i = 0; i < tagList.tagCount(); ++i) {
            list[i] = tagList.getStringTagAt(i);
        }

        this.setList(list);
        this.setMultiSelect(nbt.getBoolean("multiSelect"));
        return this;
    }
}
