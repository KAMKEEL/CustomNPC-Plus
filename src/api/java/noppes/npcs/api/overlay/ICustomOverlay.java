package noppes.npcs.api.overlay;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.IPlayer;

import java.util.List;

public interface ICustomOverlay {
    int getID();

    List<ICustomOverlayComponent> getComponents();

    int getDefaultAlignment();

    void setDefaultAlignment(int defaultAlignment);

    IOverlayTexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height);

    IOverlayTexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY);

    IOverlayLabel addLabel(int id, String label, int x, int y, int width, int height);

    IOverlayLabel addLabel(int id, String label, int x, int y, int width, int height, int color);

    IOverlayLine addLine(int id, int x1, int y1, int x2, int y2, int color, int thickness);

    IOverlayLine addLine(int id, int x1, int y1, int x2, int y2);

    ICustomOverlayComponent getComponent(int componentID);

    void removeComponent(int componentID);

    void updateComponent(ICustomOverlayComponent component);

    void update(IPlayer player);

    ICustomOverlay fromNBT(NBTTagCompound tag);

    NBTTagCompound toNBT();
}
