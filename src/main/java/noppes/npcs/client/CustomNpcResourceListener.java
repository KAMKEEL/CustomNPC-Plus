package noppes.npcs.client;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class CustomNpcResourceListener implements
		IResourceManagerReloadListener {

	public static int DefaultTextColor = 0x404040;
	@Override
	public void onResourceManagerReload(IResourceManager var1) {
		if(var1 instanceof SimpleReloadableResourceManager){
			createTextureCache(new File(CustomNpcs.Dir,"assets/customnpcs/textures/cache"));
			
	        SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager)var1;
	        
	        FolderResourcePack pack = new FolderResourcePack(CustomNpcs.Dir);
	        simplemanager.reloadResourcePack(pack);
	        
			try{
				DefaultTextColor = Integer.parseInt(StatCollector.translateToLocal("customnpcs.defaultTextColor"),16);
			}
			catch(NumberFormatException e){
				DefaultTextColor = 0x404040;
			}
		}
	}

	
	private void createTextureCache(File dir){
		if(dir == null)
			return;
		enlargeTexture("planks_oak", dir);
		enlargeTexture("planks_big_oak", dir);
		enlargeTexture("planks_birch", dir);
		enlargeTexture("planks_jungle", dir);
		enlargeTexture("planks_spruce", dir);
		enlargeTexture("planks_acacia", dir);
		enlargeTexture("iron_block", dir);
		enlargeTexture("diamond_block", dir);
		enlargeTexture("stone", dir);
		enlargeTexture("gold_block", dir);
		enlargeTexture("wool_colored_white", dir);
	}
	
	private void enlargeTexture(String texture, File dir){
		try{
			IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
			ResourceLocation location = new ResourceLocation("textures/blocks/" + texture +".png");
			BufferedImage bufferedimage = ImageIO.read(manager.getResource(location).getInputStream());
	        int i = bufferedimage.getWidth();
	        int j = bufferedimage.getHeight();
	        
	        BufferedImage image = new BufferedImage(i * 4, j * 2, BufferedImage.TYPE_INT_RGB);
	        Graphics g = image.getGraphics();
	        g.drawImage(bufferedimage, 0, 0, null);
	        g.drawImage(bufferedimage, i, 0, null);
	        g.drawImage(bufferedimage, i * 2, 0, null);
	        g.drawImage(bufferedimage, i * 3, 0, null);
	        g.drawImage(bufferedimage, 0, i, null);
	        g.drawImage(bufferedimage, i, j, null);
	        g.drawImage(bufferedimage, i * 2, j, null);
	        g.drawImage(bufferedimage, i * 3, j, null);
			ImageIO.write(image, "png", new File(dir,texture + ".png"));
		}
		catch(Exception e){
			LogWriter.error("Failed caching texture: " + texture, e);
		}
	}

}
