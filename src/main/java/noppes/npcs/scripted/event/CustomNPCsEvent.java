//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.scripted.interfaces.AbstractNpcAPI;

public class CustomNPCsEvent extends Event{
    public final AbstractNpcAPI API = AbstractNpcAPI.Instance();

    public CustomNPCsEvent() {
    }

    public void setCancelled(boolean bo){
        this.setCanceled(bo);
    }

    public boolean isCancelled(){return this.isCanceled();}
}
