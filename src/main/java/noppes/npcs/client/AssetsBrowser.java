package noppes.npcs.client;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.NPCResourceHelper;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.SimpleReloadableResourceManager;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class AssetsBrowser {
	public boolean isRoot;
	private int depth;
	private String folder;
	public HashSet<String> folders = new HashSet<String>();
	public HashSet<String> files = new HashSet<String>();
	private String[] extensions;

	public AssetsBrowser(String folder, String[] extensions){
		this.extensions = extensions;
		this.setFolder(folder);
	}
	public void setFolder(String folder){
		if(!folder.endsWith("/"))
			folder += "/";
		isRoot = folder.length() <= 1;
		this.folder = "/assets" + folder;
		depth = StringUtils.countMatches(this.folder, "/");
		getFiles();
	}
	public AssetsBrowser(String[] extensions){
		this.extensions = extensions;
	}

	private void getFiles() {
		folders.clear();
		files.clear();
		
		ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
        SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
				
        Map<String, IResourceManager> map = ObfuscationReflectionHelper.getPrivateValue(SimpleReloadableResourceManager.class, simplemanager, 2);
        HashSet<String> set = new HashSet<String>();
        for(String name: map.keySet()){
        	if(!(map.get(name) instanceof FallbackResourceManager))
        		continue;
        	FallbackResourceManager manager = (FallbackResourceManager) map.get(name);
        	List<IResourcePack> list = ObfuscationReflectionHelper.getPrivateValue(FallbackResourceManager.class, manager, 0);
        	for(IResourcePack pack : list){
        		if(pack instanceof AbstractResourcePack){
        			AbstractResourcePack p = (AbstractResourcePack) pack;
        			File file = NPCResourceHelper.getPackFile(p);
        			if(file != null)
        				set.add(file.getAbsolutePath());
        		}
        	}
        }
        
        for(String file : set){
        	progressFile(new File(file));
        }
        for (ModContainer mod : Loader.instance().getModList()) {
        	if(mod.getSource().exists())
        		progressFile(mod.getSource());
        }
//		List<ResourcePackRepository.Entry> list = repos.getRepositoryEntries();
//		System.out.println(repos.rprDefaultResourcePack);
//		for(ResourcePackRepository.Entry entry : list) {
//			System.out.println(entry.getResourcePack());
//			File file = new File(repos.getDirResourcepacks(),entry.getResourcePackName());
//			if(file.exists()){
//				progressFile(file);
//			}
//			checkFolder(new File(CustomNpcs.Dir,"assets"),CustomNpcs.Dir.getAbsolutePath().length());
//		}
//		File file = new File(repos.getDirResourcepacks(),repos.getResourcePackName());
//		if(file.exists()){
//			progressFile(file);
//		}
//		checkFolder(new File(CustomNpcs.Dir,"assets"),CustomNpcs.Dir.getAbsolutePath().length());
		//TODO fix
	}
	private void progressFile(File file){
		try {
	        if (!file.isDirectory() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")))
	        {
	            ZipFile zip = new ZipFile(file);
	            Enumeration<? extends ZipEntry> entries = zip.entries();
	            while(entries.hasMoreElements()){
	                ZipEntry zipentry = entries.nextElement();
	                String entryName = zipentry.getName();
	        		checkFile(entryName);
	                
	            }
	            zip.close();
	        }
	        else if(file.isDirectory()){
	        	int length = file.getAbsolutePath().length();
	        	checkFolder(file,length);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkFolder(File file, int length){
		File[] files = file.listFiles();
		if(files == null){
			return;
		}
    	for(File f : files){
    		String name = f.getAbsolutePath().substring(length);
    		name = name.replace("\\", "/");
            if(!name.startsWith("/"))
            	name = "/" + name;
    		if(f.isDirectory() && (folder.startsWith(name) || name.startsWith(folder))){
    			checkFile(name + "/");
    			checkFolder(f, length);
    		}
    		else
    			checkFile(name);
    	}
	}
	
	private void checkFile(String name){
        if(!name.startsWith("/"))
        	name = "/" + name;
        if(!name.startsWith(this.folder)){
        	return;
        }
        String[] split = name.split("/");
        int count = split.length;
        if(count == depth + 1){
        	if(validExtension(name))
        		files.add(split[depth]);
        }
        else if(depth + 1 < count){
        	folders.add(split[depth]);
        }
	}

	private boolean validExtension(String entryName) {
		int index = entryName.indexOf(".");
		if(index < 0)
			return false;
    	String extension = entryName.substring(index + 1);
    	for(String ex : extensions){
    		if(ex.equalsIgnoreCase(extension))
    			return true;
    	}
		return false;
	}

	public String getAsset(String asset) {
		String[] split = folder.split("/");
		if(split.length < 3)
			return null;
		String texture = split[2] + ":";
		texture += folder.substring(texture.length() + 8) + asset;
		return texture;
	}

	public static String getRoot(String asset) {
		String mod = "minecraft";
		int index = asset.indexOf(":");
		if(index > 0){
			mod = asset.substring(0,index);
			asset = asset.substring(index + 1);
		}
		if(asset.startsWith("/"))
			asset = asset.substring(1);
		String location = "/" + mod + "/" + asset;
		index = location.lastIndexOf("/");
		if(index > 0)
			location = location.substring(0,index);
		return location;
	}
}
