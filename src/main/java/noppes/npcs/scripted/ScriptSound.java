package noppes.npcs.scripted;

import net.minecraft.nbt.*;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.data.ISound;

import java.lang.reflect.Field;

public class ScriptSound implements ISound {
    public IEntity sourceEntity;
    public String directory;
    public float volume = 1.0F;
    public float pitch = 1.0F;
    public float xPosF;
    public float yPosF;
    public float zPosF;
    public boolean repeat = false;
    public int repeatDelay = 0;

    public ScriptSound(String location) {
        this.directory = location;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        ScriptSound newSound = new ScriptSound("");

        //Iterate through fields, check if they're not equal to a new object, write to NBT
        for (Field field : ScriptSound.class.getDeclaredFields()) {
            try {
                if (field.getName().equals("sourceEntity") && this.sourceEntity != null) {
                    compound.setInteger("EntityID",this.sourceEntity.getEntityId());
                } else if (!field.get(this).equals(field.get(newSound))) {
                    compound = writeNBTTag(compound, field.getType(), field.getName(), field.get(this));
                }
            } catch (Exception ignored) {}
        }

        return compound;
    }

    public NBTTagCompound writeNBTTag(NBTTagCompound compound, Class<?> c, String key, Object value) {
        if (c == boolean.class) {
            compound.setBoolean(key, (Boolean) value);
        }
        if (c == int.class) {
            compound.setInteger(key, (Integer) value);
        }
        if (c == double.class) {
            compound.setDouble(key, (Double) value);
        }
        if (c == float.class) {
            compound.setFloat(key, (Float) value);
        }
        if (c == String.class) {
            compound.setString(key, (String) value);
        }

        return compound;
    }

    private static Object readNBTTag(NBTTagCompound compound, String key) {
        if (compound.getTag(key) instanceof NBTTagByte) {
            return compound.getBoolean(key);
        }
        if (compound.getTag(key) instanceof NBTTagInt) {
            return compound.getInteger(key);
        }
        if (compound.getTag(key) instanceof NBTTagDouble) {
            return compound.getDouble(key);
        }
        if (compound.getTag(key) instanceof NBTTagFloat) {
            return compound.getFloat(key);
        }
        if (compound.getTag(key) instanceof NBTTagString) {
            return compound.getString(key);
        }

        return null;
    }

    public static ScriptSound fromNBT(NBTTagCompound compound) {
        ScriptSound particle = new ScriptSound(compound.getString("directory"));

        //Iterate through fields, check if compound has a key equal to the field's name, and if so set the field equal to that value from NBT.
        for (Field field : ScriptSound.class.getDeclaredFields()) {
            try {
                if (compound.hasKey(field.getName())) {
                    if (!field.getName().equals("sourceEntity")) {
                        Object val = ScriptSound.readNBTTag(compound, field.getName());
                        if (val != null) {
                            field.set(particle, val);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return particle;
    }

    public void setEntity(IEntity entity) {
        this.sourceEntity = entity;
    }

    public IEntity getEntity() {
        return sourceEntity;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean repeats() {
        return this.repeat;
    }

    public void setRepeatDelay(int delay) {
        this.repeatDelay = delay;
    }

    public int getRepeatDelay() {
        return this.repeatDelay;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getVolume()
    {
        return this.volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getPitch()
    {
        return this.pitch;
    }

    public void setPosition(IPos pos) {
        this.xPosF = pos.getX();
        this.yPosF = pos.getY();
        this.zPosF = pos.getZ();
    }

    public void setPosition(float x, float y, float z) {
        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;
    }

    public IPos getPos() {
        return NpcAPI.Instance().getIPos(xPosF, yPosF, zPosF);
    }

    public float getX() {
        return this.xPosF;
    }

    public float getY() {
        return this.yPosF;
    }

    public float getZ() {
        return this.zPosF;
    }
}
