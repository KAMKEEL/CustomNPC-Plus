package kamkeel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockVine;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.ChunkController;

import java.util.Arrays;
import java.util.Set;

public class CmdConfig extends CommandKamkeelBase {


	@Override
	public String getCommandName() {
		return "config";
	}

	@Override
	public String getDescription() {
		return "Some config things you can set";
	}
    
    @SubCommand(
    		desc = "Disable/Enable the natural leaves decay",
            usage = "[true/false]",
            permission = 4
    )
    public void leavesdecay(ICommandSender sender, String[] args){
    	if(args.length == 0){
    		sendMessage(sender, "LeavesDecay: " + ConfigMain.LeavesDecayEnabled);
    	}
    	else{
			ConfigMain.LeavesDecayEnabled = Boolean.parseBoolean(args[0]);
			ConfigMain.LeavesDecayEnabledProperty.set(ConfigMain.LeavesDecayEnabled);
			if(ConfigMain.config.hasChanged()){
				ConfigMain.config.save();
			}
            Set<ResourceLocation> names = Block.blockRegistry.getKeys();
            for(ResourceLocation name : names){
            	Block block = (Block) Block.blockRegistry.getObject(name);
            	if(block instanceof BlockLeavesBase){
            		block.setTickRandomly(ConfigMain.LeavesDecayEnabled);
            	}
            }
            sendMessage(sender, "LeavesDecay is now " + ConfigMain.LeavesDecayEnabled);
    	}
    }
    
    @SubCommand(
    		desc = "Disable/Enable the vines growing",
            usage = "[true/false]",
            permission = 4
    )
    public void vinegrowth(ICommandSender sender, String[] args){
    	if(args.length == 0){
    		sendMessage(sender, "VineGrowth: " + ConfigMain.VineGrowthEnabled);
    	}
    	else{
			ConfigMain.VineGrowthEnabled = Boolean.parseBoolean(args[0]);
			ConfigMain.VineGrowthEnabledProperty.set(ConfigMain.VineGrowthEnabled);
			if(ConfigMain.config.hasChanged()){
				ConfigMain.config.save();
			}
            Set<ResourceLocation> names = Block.blockRegistry.getKeys();
            for(ResourceLocation name : names){
            	Block block = (Block) Block.blockRegistry.getObject(name);
            	if(block instanceof BlockVine){
            		block.setTickRandomly(ConfigMain.VineGrowthEnabled);
            	}
            }
            sendMessage(sender, "VineGrowth is now " + ConfigMain.VineGrowthEnabled);
    	}
    }
    
    @SubCommand(
    		desc = "Disable/Enable the ice melting",
            usage = "[true/false]",
            permission = 4
    )
    public void icemelts(ICommandSender sender, String[] args){
    	if(args.length == 0){
    		sendMessage(sender, "IceMelts: " + ConfigMain.IceMeltsEnabled);
    	}
    	else{
			ConfigMain.IceMeltsEnabled = Boolean.parseBoolean(args[0]);
			ConfigMain.IceMeltsEnabledProperty.set(ConfigMain.IceMeltsEnabled);
			if(ConfigMain.config.hasChanged()){
				ConfigMain.config.save();
			}
            Set<ResourceLocation> names = Block.blockRegistry.getKeys();
            for(ResourceLocation name : names){
            	Block block = (Block) Block.blockRegistry.getObject(name);
            	if(block instanceof BlockIce){
            		block.setTickRandomly(ConfigMain.IceMeltsEnabled);
            	}
            }
            sendMessage(sender, "IceMelts is now " + ConfigMain.IceMeltsEnabled);
    	}
    }

    @SubCommand(
    		desc = "Disable/Enable guns shooting",
            usage = "[true/false]",
            permission = 4
    )
    public void guns(ICommandSender sender, String[] args){
    	if(args.length == 0){
    		sendMessage(sender, "GunsEnabled: " + ConfigMain.GunsEnabled);
    	}
    	else{
			ConfigMain.GunsEnabled = Boolean.parseBoolean(args[0]);
			ConfigMain.GunsEnabledProperty.set(ConfigMain.GunsEnabled);
			if(ConfigMain.config.hasChanged()){
				ConfigMain.config.save();
			}
            sendMessage(sender, "GunsEnabled is now " + ConfigMain.GunsEnabled);
    	}
    }
    
    @SubCommand(
    		desc = "Freezes/Unfreezes npcs",
            usage = "[true/false]",
            permission = 4
    )
    public void freezenpcs(ICommandSender sender, String[] args){
    	if(args.length == 0){
    		sendMessage(sender, "Frozen NPCs: " + CustomNpcs.FreezeNPCs);
    	}
    	else{
			CustomNpcs.FreezeNPCs = Boolean.parseBoolean(args[0]);
            sendMessage(sender, "FrozenNPCs is now " + CustomNpcs.FreezeNPCs);
    	}
    }
    
    @SubCommand(
    		desc = "Set how many active chunkloaders you can have",
            usage = "<number>",
            permission = 4
    )
    public void chunkloaders(ICommandSender sender, String[] args) throws CommandException{
    	if(args.length == 0){
    		sendMessage(sender, "ChunkLoaders: " + ChunkController.instance.size() + "/" + ConfigMain.ChunkLoaders);
    	}
    	else{
    		try{
				ConfigMain.ChunkLoaders = Integer.parseInt(args[0]);
    		}
    		catch(NumberFormatException ex){
    			throw new CommandException("Did not get a number: " + args[0]);
    		}
			ConfigMain.ChunkLoadersProperty.set(ConfigMain.ChunkLoaders);
			if(ConfigMain.config.hasChanged()){
				ConfigMain.config.save();
			}

			int size = ChunkController.instance.size();
			if(size > ConfigMain.ChunkLoaders){
				ChunkController.instance.unload(size - ConfigMain.ChunkLoaders);
				sendMessage(sender, size - ConfigMain.ChunkLoaders + " chunksloaders unloaded");
			}
    		sendMessage(sender, "ChunkLoaders: " + ChunkController.instance.size() + "/" + ConfigMain.ChunkLoaders);
    	}
    }
    
    @SubCommand(
    		desc = "Get/Set font",
            usage = "[type] [size]",
            permission = 4
    )
    public void font(ICommandSender sender, String[] args){
    	if(!(sender instanceof EntityPlayerMP))
    		return;
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
    	Server.sendData((EntityPlayerMP)sender, EnumPacketClient.CONFIG, 0, font.trim(), size);
    }
    
}

