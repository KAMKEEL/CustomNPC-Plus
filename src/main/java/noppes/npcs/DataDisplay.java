package noppes.npcs;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class DataDisplay {
	EntityNPCInterface npc;

	public String name;
	public String title = "";

	public byte skinType = 0; //0:normal, 1:player, 2:url
	public String url = "";
	public GameProfile playerProfile;
	public String texture = "customnpcs:textures/entity/humanmale/Steve.png";;
	public String cloakTexture = "";
	public String glowTexture = "";
	
	public int visible = 0;//0:visible 1:Invisible 2:semi-invisible
	
	public int modelSize = 5;

	public int showName = 0;
	
	public boolean disableLivingAnimation = false;
	
	public byte showBossBar = 0;
	
	public DataDisplay(EntityNPCInterface npc){
		this.npc = npc;
		String[] names = { "Noppes", "Noppes", "Noppes", "Noppes", "Atesson",
				"Rothcersul", "Achdranys", "Pegato", "Chald", "Gareld",
				"Nalworche", "Ineald", "Tia'kim", "Torerod", "Turturdar",
				"Ranler", "Dyntan", "Oldrake", "Gharis", "Elmn", "Tanal",
				"Waran-ess", "Ach-aldhat", "Athi", "Itageray", "Tasr",
				"Ightech", "Gakih", "Adkal", "Qua'an", "Sieq", "Urnp", "Rods",
				"Vorbani", "Smaik", "Fian", "Hir", "Ristai", "Kineth", "Naif",
				"Issraya", "Arisotura", "Honf", "Rilfom", "Estz", "Ghatroth",
				"Yosil", "Darage", "Aldny", "Tyltran", "Armos", "Loxiku",
				"Burhat", "Tinlt", "Ightyd", "Mia",
				"Ken", "Karla", "Lily", "Carina", "Daniel", "Slater", "Zidane", "Valentine", "Eirina", 
				"Carnow", "Grave", "Shadow", "Drakken", "Kaoz", "Silk", "Drake", "Oldam", "Lynxx", "Lenyx", 
				"Winter", "Seth", "Apolitho", "Amethyst", "Ankin", "Seinkan", "Ayumu", "Sakamoto", "Divina", 
				"Div", "Magia", "Magnus", "Tiakono", "Ruin", "Hailinx", "Ethan", "Wate", "Carter", "William", 
				"Brion", "Sparrow", "Basrrelen", "Gyaku", "Claire", "Crowfeather", "Blackwell", "Raven", "Farcri",
				"Lucas", "Bangheart", "Kamoku", "Kyoukan", "Blaze", "Benjamin", "Larianne", "Kakaragon", 
				"Melancholy", "Epodyno", "Thanato", "Mika", "Dacks", "Ylander", "Neve", "Meadow", "Cuero", 
				"Embrera", "Eldamore", "Faolan", "Chim", "Nasu", "Kathrine", "Ariel", "Arei", "Demytrix", 
				"Kora", "Ava", "Larson", "Leonardo", "Wyrl", "Sakiama", "Lambton", "Kederath", "Malus", "Riplette", 
				"Andern", "Ezall", "Lucien", "Droco", "Cray", "Tymen", "Zenix", "Entranger", 
				"Saenorath", "Chris", "Christine", "Marble", "Mable", "Ross", "Rose", "Xalgan ", "Kennet"
		};
		name = names[new Random().nextInt(names.length)];
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("Name", name);
		nbttagcompound.setString("Title", title);
		nbttagcompound.setString("SkinUrl", url);
		nbttagcompound.setString("Texture", texture);
		nbttagcompound.setString("CloakTexture", cloakTexture);
		nbttagcompound.setString("GlowTexture", glowTexture);
		nbttagcompound.setByte("UsingSkinUrl", skinType);
		
        if (this.playerProfile != null)
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTUtil.func_152460_a(nbttagcompound1, this.playerProfile);
            nbttagcompound.setTag("SkinUsername", nbttagcompound1);
        }
		
		nbttagcompound.setInteger("Size", modelSize);

		nbttagcompound.setInteger("ShowName", showName);
		nbttagcompound.setInteger("NpcVisible", visible);

		nbttagcompound.setBoolean("NoLivingAnimation", disableLivingAnimation);
		nbttagcompound.setByte("BossBar", showBossBar);

		return nbttagcompound;
	}
	public void readToNBT(NBTTagCompound nbttagcompound) {
		name = nbttagcompound.getString("Name");
		title = nbttagcompound.getString("Title");
		
		url = nbttagcompound.getString("SkinUrl");
        int prevSkinType = skinType;
		skinType = nbttagcompound.getByte("UsingSkinUrl");
		
		this.playerProfile = null;
		if(skinType == 1){
	        if (nbttagcompound.hasKey("SkinUsername", 10)){
	            this.playerProfile = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("SkinUsername"));
	        }
	        else if (nbttagcompound.hasKey("SkinUsername", 8) && !StringUtils.isNullOrEmpty(nbttagcompound.getString("SkinUsername"))){
	            this.playerProfile = new GameProfile(null, nbttagcompound.getString("SkinUsername"));
	        }
	        this.loadProfile();
		}

        String prevTexture = texture;
        
		texture = nbttagcompound.getString("Texture");
		cloakTexture = nbttagcompound.getString("CloakTexture");
		glowTexture = nbttagcompound.getString("GlowTexture");
		
		modelSize = ValueUtil.CorrectInt(nbttagcompound.getInteger("Size"), 1, 30);

		showName = nbttagcompound.getInteger("ShowName");
		visible = nbttagcompound.getInteger("NpcVisible");

		disableLivingAnimation = nbttagcompound.getBoolean("NoLivingAnimation");
		showBossBar = nbttagcompound.getByte("BossBar");

		if(prevSkinType != skinType || !texture.equals(prevTexture))
			npc.textureLocation = null;
		npc.textureGlowLocation = null;
		npc.textureCloakLocation = null;
		npc.updateHitbox();
	}
	
    public void loadProfile(){
        if (this.playerProfile != null && !StringUtils.isNullOrEmpty(this.playerProfile.getName()) && MinecraftServer.getServer() != null){
            if (!this.playerProfile.isComplete() || !this.playerProfile.getProperties().containsKey("textures")){
                GameProfile gameprofile = MinecraftServer.getServer().func_152358_ax().func_152655_a(this.playerProfile.getName());

                if (gameprofile != null){
                    Property property = (Property)Iterables.getFirst(gameprofile.getProperties().get("textures"), (Object)null);

                    if (property == null){
                        gameprofile = MinecraftServer.getServer().func_147130_as().fillProfileProperties(gameprofile, false);
                    }

                    this.playerProfile = gameprofile;
                }
            }
        }
    }
	
	public boolean showName() {
		if(npc.isKilled())
			return false;
		return showName == 0 || (showName == 2 && npc.isAttacking());
	}
}
