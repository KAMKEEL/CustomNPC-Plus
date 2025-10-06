package noppes.npcs.client.gui.script;

import com.google.common.reflect.ClassPath;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import kamkeel.npcs.network.packets.request.script.ForgeScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.ForgeDataScript;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GuiScriptForge extends GuiScriptInterface {
    private final ForgeDataScript script = new ForgeDataScript();

    public GuiScriptForge() {
        hookList.add("init");

        ArrayList<ClassPath.ClassInfo> list = new ArrayList();
        try {
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.gameevent"));
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
            list.removeAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event.terraingen"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ClassPath.ClassInfo e1 : list) {
            ClassPath.ClassInfo classLoader = e1;
            Class infoClass = classLoader.load();
            ArrayList c = new ArrayList(Arrays.asList(infoClass.getDeclaredClasses()));
            if (c.isEmpty()) {
                c.add(infoClass);
            }
            Iterator var10 = c.iterator();
            while (var10.hasNext()) {
                Class c1 = (Class) var10.next();
                if (!EntityEvent.EntityConstructing.class.isAssignableFrom(c1) && !WorldEvent.PotentialSpawns.class.isAssignableFrom(c1) && !TickEvent.RenderTickEvent.class.isAssignableFrom(c1) && !TickEvent.ClientTickEvent.class.isAssignableFrom(c1) && !FMLNetworkEvent.ClientCustomPacketEvent.class.isAssignableFrom(c1) && !ItemTooltipEvent.class.isAssignableFrom(c1) && Event.class.isAssignableFrom(c1) && !Modifier.isAbstract(c1.getModifiers()) && Modifier.isPublic(c1.getModifiers()) && !ChunkEvent.class.isAssignableFrom(c1) && !ChunkWatchEvent.class.isAssignableFrom(c1) && !ChunkDataEvent.class.isAssignableFrom(c1)) {
                    String eventName = c1.getName();
                    int i = eventName.lastIndexOf(".");
                    eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));

                    hookList.add(eventName);
                }
            }
        }
        hookList.add("onCNPCNaturalSpawn");

        this.handler = this.script;
        ForgeScriptPacket.Get();
    }

    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            loaded = true;
            return;
        }

        if (!compound.hasKey("Tab")) {
            script.setLanguage(compound.getString("ScriptLanguage"));
            script.setEnabled(compound.getBoolean("ScriptEnabled"));
            super.setGuiData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            ScriptContainer container = new ScriptContainer(script);
            container.readFromNBT(compound.getCompoundTag("Script"));
            if (script.getScripts().isEmpty()) {
                for (int i = 0; i < compound.getInteger("TotalScripts"); i++) {
                    script.getScripts().add(new ScriptContainer(script));
                }
            }
            script.getScripts().set(tab, container);
            initGui();
        }
    }

    public void save() {
        if (loaded) {
            super.save();
            List<ScriptContainer> containers = this.script.getScripts();
            for (int i = 0; i < containers.size(); i++) {
                ScriptContainer container = containers.get(i);
                ForgeScriptPacket.Save(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
            }
            NBTTagCompound scriptData = new NBTTagCompound();
            scriptData.setString("ScriptLanguage", this.script.getLanguage());
            scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
            scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
            ForgeScriptPacket.Save(-1, containers.size(), scriptData);
        }
    }
}
