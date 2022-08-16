package noppes.npcs.scripted.interfaces.handler.data;

import noppes.npcs.scripted.interfaces.IPos;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public interface ISound {

    void setEntity(IEntity entity);

    IEntity getEntity();

    void setRepeat(boolean repeat);

    boolean repeats();

    void setRepeatDelay(int delay);

    int getRepeatDelay();

    void setVolume(float volume);

    float getVolume();

    void setPitch(float pitch);

    float getPitch();

    void setPosition(IPos pos);

    void setPosition(float x, float y, float z);

    float getX();

    float getY();

    float getZ();
}
