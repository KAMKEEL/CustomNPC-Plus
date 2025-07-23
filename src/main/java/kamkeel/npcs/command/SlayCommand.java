package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static kamkeel.npcs.util.ColorUtil.sendResult;

public class SlayCommand extends CommandKamkeelBase {
    public Map<String, Class<?>> SlayMap = new LinkedHashMap<String, Class<?>>();

    public SlayCommand() {
        SlayMap.clear();

        SlayMap.put("all", EntityLivingBase.class);
        SlayMap.put("mobs", EntityMob.class);
        SlayMap.put("animals", EntityAnimal.class);
        SlayMap.put("items", EntityItem.class);
        SlayMap.put("xporbs", EntityXPOrb.class);
        SlayMap.put("npcs", EntityNPCInterface.class);

        HashMap<String, Class<?>> list = new HashMap<String, Class<?>>(EntityList.stringToClassMapping);
        for (String name : list.keySet()) {
            Class<?> cls = list.get(name);
            if (EntityNPCInterface.class.isAssignableFrom(cls))
                continue;
            if (!EntityLivingBase.class.isAssignableFrom(cls))
                continue;
            SlayMap.put(name.toLowerCase(), list.get(name));
        }

        SlayMap.remove("monster");
        SlayMap.remove("mob");
    }

    @Override
    public String getCommandName() {
        return "slay";
    }

    @Override
    public String getDescription() {
        return "Kills given entity within range. Tab to see options.";
    }

    @Override
    public String getUsage() {
        return "<type>.. [range]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        ArrayList<Class<?>> toDelete = new ArrayList<Class<?>>();
        boolean deleteNPCs = false;
        for (String delete : args) {
            delete = delete.toLowerCase();
            Class<?> cls = SlayMap.get(delete);
            if (cls != null)
                toDelete.add(cls);
            if (delete.equals("mobs")) {
                toDelete.add(EntityGhast.class);
                toDelete.add(EntityDragon.class);
            }
            if (delete.equals("npcs"))
                deleteNPCs = true;
        }
        int count = 0;
        int range = 120;
        try {
            range = Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException ex) {

        }
        AxisAlignedBB box = player.boundingBox.expand(range, range, range);
        List<? extends Entity> list = sender.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, box);

        for (Entity entity : list) {
            if (entity instanceof EntityPlayer)
                continue;
            if (entity instanceof EntityTameable && ((EntityTameable) entity).isTamed())
                continue;
            if (entity instanceof EntityNPCInterface && !deleteNPCs)
                continue;
            if (delete(entity, toDelete))
                count++;
        }
        if (toDelete.contains(EntityXPOrb.class)) {
            list = sender.getEntityWorld().getEntitiesWithinAABB(EntityXPOrb.class, box);
            for (Entity entity : list) {
                entity.isDead = true;
                count++;
            }
        }
        if (toDelete.contains(EntityItem.class)) {
            list = sender.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box);
            for (Entity entity : list) {
                entity.isDead = true;
                count++;
            }
        }

        sendResult(sender, count + " entities deleted");
    }

    private boolean delete(Entity entity, ArrayList<Class<?>> toDelete) {
        for (Class<?> delete : toDelete) {
            if (delete == EntityAnimal.class && (entity instanceof EntityHorse)) {
                continue;
            }
            if (delete.isAssignableFrom(entity.getClass())) {
                entity.isDead = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, SlayMap.keySet().toArray(new String[SlayMap.size()]));
    }
}
