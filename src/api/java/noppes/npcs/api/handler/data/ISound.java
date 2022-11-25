package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;

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
