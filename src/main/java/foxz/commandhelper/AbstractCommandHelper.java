// (c)SJFN@ CommandHelper v14.03.21 by FoxZ@free.fr
// licence : cc-by-nc+no gov,mil usage
package foxz.commandhelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.AbstractPermission;

public abstract class AbstractCommandHelper extends CommandHelper {

    public Object ctorParm;
    public ICommandSender pcParam;
    public Object xParam;
    public AbstractCommandHelper parentCmdHelper;
    public AbstractCommandHelper rootCmdHelper;

    public AbstractCommandHelper(Object sender) {
        this.ctorParm = sender;
        ctor();
    }

    public void ctor() {
        this.commandHelper.name = ((Command) this.getClass().getAnnotation(Command.class)).name();
        this.commandHelper.usage = ((Command) this.getClass().getAnnotation(Command.class)).usage();
        this.commandHelper.desc = ((Command) this.getClass().getAnnotation(Command.class)).desc();
        // sub
        for (Class c : this.getClass().getAnnotation(Command.class).sub()) {
            try {
                String name = ((Command) c.getAnnotation(Command.class)).name().toUpperCase();
                Constructor<AbstractCommandHelper> ctor = c.getConstructor(Object.class);
                ctor.setAccessible(true);
                AbstractCommandHelper sc = (AbstractCommandHelper) ctor.newInstance(ctorParm);
                commands.put(name, sc);
            } catch (Exception ex) {
                Logger.getLogger(AbstractCommandHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // intern
        for (Method m : this.getClass().getDeclaredMethods()) {
            SubCommand sc = m.getAnnotation(SubCommand.class);
            if (sc != null) {
                String name = sc.name();
                if (name.equals("")) {
                    name = m.getName();
                }
                commands.put(name.toUpperCase(), new MethodSubCmd(this, m));
            }
        }
    }

    public Map<String, CommandHelper> commands = new HashMap<String, CommandHelper>();

    public abstract void help(String cmd, String desc, String usa);

    public abstract void cmdError(String cmd);

    public abstract void error(String err);

    protected class MethodSubCmd extends CommandHelper {

        public List<AbstractPermission> permissions = new ArrayList<AbstractPermission>();

        public MethodSubCmd(AbstractCommandHelper ch, Method m) {
            SubCommand s = m.getAnnotation(SubCommand.class);
            commandHelper.name = s.name();
            if (commandHelper.name.equals("")) {
                commandHelper.name = m.getName();
            }
            commandHelper.usage = s.usage();
            commandHelper.desc = s.desc();
            commandHelper.hasEmptyCall = s.hasEmptyCall();

            method = m;
            for (Class c : s.permissions()) {
                try {
                    Constructor<AbstractPermission> ctor = c.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    AbstractPermission i = ctor.newInstance();
                    permissions.add(i);
                } catch (Exception ex) {
                    Logger.getLogger(AbstractCommandHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        @Override
    	public List addTabCompletion(ICommandSender par1, String[] args) {
            String[] np = currentHelper.usage.split(" ");
            if(np.length < args.length)
            	return null;
            String parameter = np[args.length - 1];
            if(parameter.equals("<player>"))
                return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
 
        	return null;
        }

        public Method method;

    }

    public void allHelp() {
        for (CommandHelper cur : commands.values()) {
            help(cur.commandHelper.name, cur.commandHelper.desc, "");
        }
    }

    public Helper currentHelper;

    public Boolean processCommand(ICommandSender param, String[] args) {
        pcParam = param;
        if (parentCmdHelper == null) {
            rootCmdHelper = this;
        }
        if (args.length == 0) {
            allHelp();
            return true;
        }

        String cmd = args[0].toUpperCase();
        args = Arrays.copyOfRange(args, 1, args.length);

        if ((cmd.equals("HELP") || args.length == 0) && doHelp(param, args, cmd)) {
        	return true;
        }

        CommandHelper ch = commands.get(cmd);
        if (ch == null) {
            cmdError(cmd);
            return false;
        }
        if (ch instanceof AbstractCommandHelper) {
            AbstractCommandHelper f = (AbstractCommandHelper) ch;
            f.parentCmdHelper = this;
            f.rootCmdHelper = this.rootCmdHelper;
            return f.processCommand(param, args);
        }
        else if (ch instanceof MethodSubCmd) {
            MethodSubCmd m = (MethodSubCmd) ch;
            m.method.setAccessible(true);
            currentHelper = ch.commandHelper;
            try {
                for (AbstractPermission p : m.permissions) {
                    if (!p.delegate(this, args)) {
                        error(p.errorMsg());
                        return false;
                    }
                }
                return (Boolean) m.method.invoke(this, (Object) args);
            } catch (Exception ex) {
                Logger.getLogger(AbstractCommandHelper.class.getName()).log(Level.SEVERE, m.commandHelper.name, ex);
            }
            return true;
        }
        else
            cmdError(cmd);
        return false;
    }
    
    private boolean doHelp(ICommandSender param, String[] args, String cmd){
    	boolean isHelp = cmd.equals("HELP");
    	if(args.length > 0){
    		cmd = args[0];
    	}
		CommandHelper ch = commands.get(cmd.toUpperCase());
		if(ch != null){
			if(ch.commandHelper.hasEmptyCall && !isHelp)
				return false;
			if(ch instanceof AbstractCommandHelper){
				((AbstractCommandHelper)ch).pcParam = param;
				((AbstractCommandHelper)ch).allHelp();
			}
			else if(ch instanceof MethodSubCmd && ((MethodSubCmd)ch).commandHelper.usage.isEmpty())
				return false;
			else
				help(ch.commandHelper.name, ch.commandHelper.desc, ch.commandHelper.usage);
		}
		else
			allHelp();
        return true;
    }
    
    @Override
	public List addTabCompletion(ICommandSender par1, String[] args) {
		if(args.length  <= 1){
			List<String> list = new ArrayList<String>();
			for(String command : commands.keySet())
				list.add(command.toLowerCase());
			list.add("help");
			return CommandBase.getListOfStringsMatchingLastWord(args, list.toArray(new String[list.size()]));
		}
        CommandHelper ch = commands.get(args[0].toUpperCase());
		if(ch == null)
			return null;
        args = Arrays.copyOfRange(args, 1, args.length);
        currentHelper = ch.commandHelper;
    	return ch.addTabCompletion(par1, args);
	}
    
    public List<PlayerData> getPlayersData(String username){
    	ArrayList<PlayerData> list = new ArrayList<PlayerData>();
    	EntityPlayerMP[] players = PlayerSelector.matchPlayers(pcParam, username);
    	if(players == null || players.length == 0){
    		PlayerData data = PlayerDataController.instance.getDataFromUsername(username);
    		if(data != null)
    			list.add(data);
    	}
    	else{
            for(EntityPlayer player : players){
    	        list.add(PlayerDataController.instance.getPlayerData(player));   
            }
    	}
    	
    	return list;
    }

    public <T> List<T> getNearbeEntityFromPlayer(Class<? extends T> cls, World world, int x, int y, int z, int range) {
    	AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(range, range, range);
    	List<T> list = world.getEntitiesWithinAABB(cls, bb);
		return list;
	}
}
