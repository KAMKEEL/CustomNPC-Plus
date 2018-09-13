package foxz.command;

import java.util.Arrays;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.ChunkController;
import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.PlayerOnly;

@Command(
        name="config",
        desc="Some config things you can set ",
        usage=""
)
public class CmdConfig extends ChMcLogger {

    public CmdConfig(Object sender) {
        super (sender);
    }  
    
    @SubCommand(
    		desc="Disable/Enable the natural leaves decay",
            usage="<true/false>",
            permissions={OpOnly.class},
            hasEmptyCall = true
    )
    public boolean leavesdecay(String[] args){
    	if(args.length == 0){
    		sendmessage("LeavesDecay: " + CustomNpcs.LeavesDecayEnabled);
    	}
    	else{
			CustomNpcs.LeavesDecayEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.updateConfig();
            Set<String> names = Block.blockRegistry.getKeys();
            for(String name : names){
            	Block block = (Block) Block.blockRegistry.getObject(name);
            	if(block instanceof BlockLeavesBase){
            		block.setTickRandomly(CustomNpcs.LeavesDecayEnabled);
            	}
            }
            sendmessage("LeavesDecay is now " + CustomNpcs.LeavesDecayEnabled);
    	}
        return true;
    }
    
    @SubCommand(
    		desc="Disable/Enable the vines growing",
            usage="<true/false>",
            permissions={OpOnly.class},
            hasEmptyCall = true
    )
    public boolean vinegrowth(String[] args){
    	if(args.length == 0){
    		sendmessage("VineGrowth: " + CustomNpcs.VineGrowthEnabled);
    	}
    	else{
			CustomNpcs.VineGrowthEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.updateConfig();
            Set<String> names = Block.blockRegistry.getKeys();
            for(String name : names){
            	Block block = (Block) Block.blockRegistry.getObject(name);
            	if(block instanceof BlockVine){
            		block.setTickRandomly(CustomNpcs.VineGrowthEnabled);
            	}
            }
            sendmessage("VineGrowth is now " + CustomNpcs.VineGrowthEnabled);
    	}
        return true;
    }
    
    @SubCommand(
    		desc="Disable/Enable the ice melting",
            usage="<true/false>",
            permissions={OpOnly.class},
            hasEmptyCall = true
    )
    public boolean icemelts(String[] args){
    	if(args.length == 0){
    		sendmessage("IceMelts: " + CustomNpcs.IceMeltsEnabled);
    	}
    	else{
			CustomNpcs.IceMeltsEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.updateConfig();
            Set<String> names = Block.blockRegistry.getKeys();
            for(String name : names){
            	Block block = (Block) Block.blockRegistry.getObject(name);
            	if(block instanceof BlockIce){
            		block.setTickRandomly(CustomNpcs.IceMeltsEnabled);
            	}
            }
            sendmessage("IceMelts is now " + CustomNpcs.IceMeltsEnabled);
    	}
        return true;
    }

    @SubCommand(
    		desc="Disable/Enable guns shooting",
            usage="<true/false>",
            permissions={OpOnly.class},
            hasEmptyCall = true
    )
    public boolean guns(String[] args){
    	if(args.length == 0){
    		sendmessage("GunsEnabled: " + CustomNpcs.GunsEnabled);
    	}
    	else{
			CustomNpcs.GunsEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.updateConfig();
            sendmessage("GunsEnabled is now " + CustomNpcs.GunsEnabled);
    	}
        return true;
    }
    
    @SubCommand(
    		desc="Freezes/Unfreezes npcs",
            usage="<true/false>",
            permissions={OpOnly.class},
            hasEmptyCall = true
    )
    public boolean freezenpcs(String[] args){
    	if(args.length == 0){
    		sendmessage("Frozen NPCs: " + CustomNpcs.FreezeNPCs);
    	}
    	else{
			CustomNpcs.FreezeNPCs = Boolean.parseBoolean(args[0]);
            sendmessage("FrozenNPCs is now " + CustomNpcs.FreezeNPCs);
    	}
        return true;
    }
    
    @SubCommand(
    		desc="Set how many active chunkloaders you can have",
            usage="<number>",
            permissions={OpOnly.class},
            hasEmptyCall = true
    )
    public boolean chunkloaders(String[] args){
    	if(args.length == 0){
    		sendmessage("ChunkLoaders: " + ChunkController.instance.size() + "/" + CustomNpcs.ChuckLoaders);
    	}
    	else{
    		try{
    			CustomNpcs.ChuckLoaders = Integer.parseInt(args[0]);
    		}
    		catch(NumberFormatException ex){
    			sendmessage("Didnt get a number");
    			return false;
    		}
			CustomNpcs.Config.updateConfig();
			
    		int size = ChunkController.instance.size();
			if(size > CustomNpcs.ChuckLoaders){
				ChunkController.instance.unload(size - CustomNpcs.ChuckLoaders);
				sendmessage(size - CustomNpcs.ChuckLoaders + " chunksloaders unloaded");
			}
    		sendmessage("ChunkLoaders: " + ChunkController.instance.size() + "/" + CustomNpcs.ChuckLoaders);
    	}
        return true;
    }

    
    @SubCommand(
    		desc="Get/Set font",
            usage="[type] [size]",
            permissions={PlayerOnly.class},
            hasEmptyCall = true
    )
    public void font(String[] args){
    	int size = 18;
    	if(args.length > 1){
    		try{
    			size = Integer.parseInt(args[args.length - 1]);
    			args = Arrays.copyOfRange(args, 0, args.length - 1);
    		}
    		catch(Exception e){
    			
    		}
    	}
		String font = "";
		for(int i = 0; i < args.length; i++){
			font += " " + args[i];
		}
    	Server.sendData((EntityPlayerMP)this.pcParam, EnumPacketClient.CONFIG, 0, font.trim(), size);
    }
}
