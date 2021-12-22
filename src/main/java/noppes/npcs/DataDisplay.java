package noppes.npcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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

	private int markovGeneratorId = 8; //roman,japanese,slavic,welsh,sami,oldnorse,ancientgreek,aztec,classic,spanish (0 - 9 inclusively)
	private int markovGender = 0; //0:random, 1:male, 2:female

	public byte skinType = 0;	//0:normal, 1:player, 2:url, 3:url64

	public String url = "";

	public GameProfile playerProfile;
	public String texture = "customnpcs:textures/entity/humanmale/Steve.png";
	public String cloakTexture = "";
	public String glowTexture = "";
	
	public int visible = 0;		//0:visible 1:Invisible 2:semi-invisible
	
	public int modelSize = 5;

	public int showName = 0;

	public int modelType = 0;

	public boolean disableLivingAnimation = false;
	
	public byte showBossBar = 0;

	public ArrayList<UUID> invisibleToList = new ArrayList<>();

	public DataDisplay(EntityNPCInterface npc){
		this.npc = npc;

		/*
		Implementing MarkovNames generator. You can blame this blunder on Nikedemos.

		Please refer to nikedemos.markovnames.generators.MarkovCustomNPCsClassic.java

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
				"Saenorath", "Chris", "Christine", "Marble", "Mable", "Ross", "Rose", "Xalgan ", "Kennet", "Kamkeel"
		};
		name = names[new Random().nextInt(names.length)];
		 */

		markovGeneratorId = new Random().nextInt(CustomNpcs.MARKOV_GENERATOR.length-1);
		name = getRandomName();
	}

	public String getRandomName() {
		return CustomNpcs.MARKOV_GENERATOR[markovGeneratorId].fetch(markovGender);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("Name", name);
		nbttagcompound.setInteger("MarkovGeneratorId", markovGeneratorId);
		nbttagcompound.setInteger("MarkovGender", markovGender);
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
		nbttagcompound.setInteger("modelType", modelType);

		nbttagcompound.setInteger("ShowName", showName);
		nbttagcompound.setInteger("NpcVisible", visible);

		nbttagcompound.setBoolean("NoLivingAnimation", disableLivingAnimation);
		nbttagcompound.setByte("BossBar", showBossBar);

		NBTTagList list = new NBTTagList();
		for(UUID uuid : invisibleToList){
			list.appendTag(new NBTTagString(uuid.toString()));
		}

		nbttagcompound.setTag("InvisibleToList", list);

		return nbttagcompound;
	}
	public void readToNBT(NBTTagCompound nbttagcompound) {
		setName(nbttagcompound.getString("Name"));
		setMarkovGeneratorId(nbttagcompound.getInteger("MarkovGeneratorId"));
		setMarkovGender(nbttagcompound.getInteger("MarkovGender"));

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
		
		modelSize = ValueUtil.CorrectInt(nbttagcompound.getInteger("Size"), 1, Integer.MAX_VALUE);
		modelType = nbttagcompound.getInteger("modelType");
		
		showName = nbttagcompound.getInteger("ShowName");
		visible = nbttagcompound.getInteger("NpcVisible");

		disableLivingAnimation = nbttagcompound.getBoolean("NoLivingAnimation");
		showBossBar = nbttagcompound.getByte("BossBar");

		NBTTagList tagList = (NBTTagList)nbttagcompound.getTag("InvisibleToList");
		if(tagList != null) {
			invisibleToList.clear();
			for (int i = 0; i < tagList.tagCount(); i++) {
				String nbtTagString = tagList.getStringTagAt(i);
				invisibleToList.add(UUID.fromString(nbtTagString));
			}
		}
		else {
			invisibleToList = new ArrayList<>();
		}

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

	public String getSkinTexture(){
		return texture;
	}

	public void setSkinTexture(String texture){
		if(this.texture.equals(texture))
			return;
		this.texture = texture;
		npc.textureLocation = null;
		skinType = 0;
		npc.updateClient = true;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		if(this.name.equals(name))
			return;
		this.name = name;
		npc.updateClient = true;
	}

	public int getMarkovGender(){
		return markovGender;
	}

	public void setMarkovGender(int gender) {
		if(markovGender == gender)
			return;
		this.markovGender = ValueUtil.CorrectInt(gender, 0, 2);
	}

	public int getMarkovGeneratorId(){
		return markovGeneratorId;
	}

	public void setMarkovGeneratorId(int id) {
		if(markovGeneratorId == id)
			return;
		this.markovGeneratorId = ValueUtil.CorrectInt(id, 0, CustomNpcs.MARKOV_GENERATOR.length-1);
	}

}
