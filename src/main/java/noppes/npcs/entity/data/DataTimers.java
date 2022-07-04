//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.interfaces.ITimers;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.CustomNPCsException;

public class DataTimers implements ITimers {
    private final Object parent;
    private Map<Integer, DataTimers.Timer> timers = new HashMap();

    public DataTimers(Object parent) {
        this.parent = parent;
    }

    public void start(int id, int ticks, boolean repeat) {
        if(this.timers.containsKey(Integer.valueOf(id))) {
            throw new CustomNPCsException("There is already a timer with id: " + id, new Object[0]);
        } else {
            this.timers.put(Integer.valueOf(id), new DataTimers.Timer(id, ticks, repeat));
        }
    }

    public void forceStart(int id, int ticks, boolean repeat) {
        this.timers.put(Integer.valueOf(id), new DataTimers.Timer(id, ticks, repeat));
    }

    public boolean has(int id) {
        return this.timers.containsKey(Integer.valueOf(id));
    }

    public boolean stop(int id) {
        return this.timers.remove(Integer.valueOf(id)) != null;
    }

    public void reset(int id) {
        timerException(id);

        DataTimers.Timer timer = (DataTimers.Timer)this.timers.get(Integer.valueOf(id));
        if(timer == null) {
            throw new CustomNPCsException("There is no timer with id: " + id, new Object[0]);
        } else {
            timer.ticks = 0;
        }
    }

    public int ticks(int id){
        timerException(id);
        return this.timers.get(Integer.valueOf(id)).ticks;
    }

    public void setTicks(int id, int ticks){
        timerException(id);

        DataTimers.Timer timer = (DataTimers.Timer)this.timers.get(Integer.valueOf(id));
        if(ticks < 0)
            ticks = 0;
        if(ticks > timer.timerTicks)
            ticks = timer.timerTicks;

        timer.ticks = ticks;
    }

    public int maxTicks(int id){
        timerException(id);
        return this.timers.get(Integer.valueOf(id)).timerTicks;
    }

    public void setMaxTicks(int id, int maxTicks){
        timerException(id);

        DataTimers.Timer timer = (DataTimers.Timer)this.timers.get(Integer.valueOf(id));
        if(maxTicks < 0)
            maxTicks = 0;
        if(timer.ticks > maxTicks)
            timer.ticks = maxTicks;

        timer.timerTicks = maxTicks;
    }

    public boolean repeats(int id){
        timerException(id);
        return this.timers.get(Integer.valueOf(id)).repeat;
    }

    public void setRepeats(int id, boolean repeat){
        timerException(id);
        this.timers.get(Integer.valueOf(id)).repeat = repeat;
    }

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        Iterator var3 = this.timers.values().iterator();

        while(var3.hasNext()) {
            DataTimers.Timer timer = (DataTimers.Timer)var3.next();
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("ID", timer.id);
            c.setInteger("TimerTicks", timer.timerTicks);
            c.setBoolean("Repeat", timer.repeat);
            c.setInteger("Ticks", timer.ticks);
            list.appendTag(c);
        }

        compound.setTag("NpcsTimers", list);
    }

    private void timerException(int id) {
        if (!this.timers.containsKey(id)) {
            throw new CustomNPCsException("Timer with id "+id+" does not exist.");
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        Map<Integer, DataTimers.Timer> timers = new HashMap();
        NBTTagList list = compound.getTagList("NpcsTimers", 10);

        if(list != null) {
            for (int i = 0; i < list.tagCount(); ++i) {
                NBTTagCompound c = list.getCompoundTagAt(i);
                DataTimers.Timer t = new DataTimers.Timer(c.getInteger("ID"), c.getInteger("TimerTicks"), c.getBoolean("Repeat"));
                t.ticks = c.getInteger("Ticks");
                timers.put(t.id, t);
            }
        }

        this.timers = timers;
    }

    public void update() {
        Iterator var1 = (new ArrayList(this.timers.values())).iterator();

        while(var1.hasNext()) {
            DataTimers.Timer timer = (DataTimers.Timer)var1.next();
            timer.update();
        }
    }

    public void clear() {
        this.timers = new HashMap();
    }

    public int size() { return this.timers.size(); }

    class Timer {
        public int id;
        private boolean repeat;
        private int timerTicks;
        private int ticks = 0;

        public Timer(int id, int ticks, boolean repeat) {
            this.id = id;
            this.repeat = repeat;
            this.timerTicks = ticks;
            this.ticks = ticks;
        }

        public void update() {
            if(this.ticks-- <= 0) {
                if(this.repeat) {
                    this.ticks = this.timerTicks;
                } else {
                    DataTimers.this.stop(this.id);
                }

                Object ob = DataTimers.this.parent;
                if (ob instanceof EntityNPCInterface) {
                    EventHooks.onNPCTimer((EntityNPCInterface)ob, this.id);
                }
                else if(ob instanceof PlayerData) {
                    EventHooks.onPlayerTimer((PlayerData)ob, this.id);
                }
            }
        }
    }
}
