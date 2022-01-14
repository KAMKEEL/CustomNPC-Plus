package noppes.npcs.scripted.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.scripted.interfaces.ICustomGuiComponent;

public abstract class ScriptGuiComponent implements ICustomGuiComponent {
    int id;
    int posX;
    int posY;
    String[] hoverText;

    public ScriptGuiComponent() {
    }

    public int getID() {
        return this.id;
    }

    public ICustomGuiComponent setID(int id) {
        this.id = id;
        return this;
    }

    public int getPosX() {
        return this.posX;
    }

    public int getPosY() {
        return this.posY;
    }

    public ICustomGuiComponent setPos(int x, int y) {
        this.posX = x;
        this.posY = y;
        return this;
    }

    public boolean hasHoverText() {
        return this.hoverText != null && this.hoverText.length > 0;
    }

    public String[] getHoverText() {
        return this.hoverText;
    }

    public ICustomGuiComponent setHoverText(String text) {
        this.hoverText = new String[]{text};
        return this;
    }

    public ICustomGuiComponent setHoverText(String[] text) {
        this.hoverText = text;
        return this;
    }

    public abstract int getType();

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        nbt.setInteger("id", this.id);
        nbt.setIntArray("pos", new int[]{this.posX, this.posY});
        if (this.hoverText != null) {
            NBTTagList list = new NBTTagList();
            String[] var3 = this.hoverText;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String s = var3[var5];
                if (s != null && !s.isEmpty()) {
                    list.appendTag(new NBTTagString(s));
                }
            }

            if (list.tagCount() > 0) {
                nbt.setTag("hover", list);
            }
        }

        nbt.setInteger("type", this.getType());
        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        this.setID(nbt.getInteger("id"));
        this.setPos(nbt.getIntArray("pos")[0], nbt.getIntArray("pos")[1]);
        if (nbt.hasKey("hover")) {
            NBTTagList list = nbt.getTagList("hover", 8);
            String[] hoverText = new String[list.tagCount()];

            for(int i = 0; i < list.tagCount(); ++i) {
                hoverText[i] = list.getStringTagAt(i);
            }

            this.setHoverText(hoverText);
        }

        return this;
    }

    public static ScriptGuiComponent createFromNBT(NBTTagCompound nbt) {
        switch(nbt.getInteger("type")) {
            case 0:
                return (new ScriptGuiButton()).fromNBT(nbt);
            case 1:
                return (new ScriptGuiLabel()).fromNBT(nbt);
            case 2:
                return (new ScriptGuiTexturedRect()).fromNBT(nbt);
            case 3:
                return (new ScriptGuiTextField()).fromNBT(nbt);
            case 4:
                return (new ScriptGuiScroll()).fromNBT(nbt);
            case 5:
                return (new ScriptGuiItemSlot()).fromNBT(nbt);
            default:
                return null;
        }
    }
}
