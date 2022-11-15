package noppes.npcs.scripted.overlay;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.overlay.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScriptOverlay implements ICustomOverlay {
    int id;
    int defaultAlignment = 0;
    List<ICustomOverlayComponent> components = new ArrayList();

    public ScriptOverlay(){
    }

    public ScriptOverlay(int id){
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public List<ICustomOverlayComponent> getComponents() {
        return this.components;
    }

    public int getDefaultAlignment(){
        return this.defaultAlignment;
    }

    public void setDefaultAlignment(int defaultAlignment) {
        this.defaultAlignment = defaultAlignment;
    }

    public IOverlayTexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height) {
        ScriptOverlayTexturedRect component = new ScriptOverlayTexturedRect(id, texture, x, y, width, height);
        this.components.add(component);
        return (IOverlayTexturedRect)this.components.get(this.components.size() - 1);
    }

    public IOverlayTexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        ScriptOverlayTexturedRect component = new ScriptOverlayTexturedRect(id, texture, x, y, width, height, textureX, textureY);
        this.components.add(component);
        return (IOverlayTexturedRect)this.components.get(this.components.size() - 1);
    }

    public IOverlayLabel addLabel(int id, String label, int x, int y, int width, int height) {
        ScriptOverlayLabel component = new ScriptOverlayLabel(id, label, x, y, width, height);
        this.components.add(component);
        return (IOverlayLabel)this.components.get(this.components.size() - 1);
    }

    public IOverlayLabel addLabel(int id, String label, int x, int y, int width, int height, int color) {
        ScriptOverlayLabel component = new ScriptOverlayLabel(id, label, x, y, width, height, color);
        this.components.add(component);
        return (IOverlayLabel)this.components.get(this.components.size() - 1);
    }

    public IOverlayLine addLine(int id, int x1, int y1, int x2, int y2, int color, int thickness) {
        ScriptOverlayLine line = new ScriptOverlayLine(id, x1, y1, x2, y2, color, thickness);
        this.components.add(line);
        return (IOverlayLine) this.components.get(this.components.size() - 1);
    }

    public IOverlayLine addLine(int id, int x1, int y1, int x2, int y2) {
        ScriptOverlayLine line = new ScriptOverlayLine(id, x1, y1, x2, y2);
        this.components.add(line);
        return (IOverlayLine) this.components.get(this.components.size() - 1);
    }

    public ICustomOverlayComponent getComponent(int componentID) {
        Iterator var2 = this.components.iterator();

        ICustomOverlayComponent component;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            component = (ICustomOverlayComponent)var2.next();
        } while(component.getID() != componentID);

        return component;
    }

    public void removeComponent(int componentID) {
        for(int i = 0; i < this.components.size(); ++i) {
            if (((ICustomOverlayComponent)this.components.get(i)).getID() == componentID) {
                this.components.remove(i);
                return;
            }
        }

    }

    public void updateComponent(ICustomOverlayComponent component) {
        for(int i = 0; i < this.components.size(); ++i) {
            ICustomOverlayComponent c = (ICustomOverlayComponent)this.components.get(i);
            if (c.getID() == component.getID()) {
                this.components.set(i, component);
                return;
            }
        }

    }

    public void update(IPlayer player) {
        CustomGuiController.updateOverlay(player, this);
    }

    public ICustomOverlay fromNBT(NBTTagCompound tag) {
        this.id = tag.getInteger("id");
        List<ICustomOverlayComponent> components = new ArrayList();

        NBTTagList list = tag.getTagList("components", 10);
        for(int i = 0; i < list.tagCount(); i++){
            NBTBase b = list.getCompoundTagAt(i);
            ScriptOverlayComponent component = ScriptOverlayComponent.createFromNBT((NBTTagCompound)b);
            components.add(component);
        }

        this.components = components;

        return this;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("id", this.id);

        NBTTagList list = new NBTTagList();
        Iterator var3 = this.components.iterator();

        ICustomOverlayComponent c;
        while(var3.hasNext()) {
            c = (ICustomOverlayComponent)var3.next();
            list.appendTag(((ScriptOverlayComponent)c).toNBT(new NBTTagCompound()));
        }

        tag.setTag("components", list);

        return tag;
    }
}
