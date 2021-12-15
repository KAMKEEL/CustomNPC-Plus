package noppes.npcs.scripted.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptItemStack;

import java.util.ArrayList;

public class ScriptDBCPlayer<T extends EntityPlayerMP> extends ScriptPlayer<T>{
    public T player;

    public ScriptDBCPlayer(T player){
        super(player);
        this.player = player;
    }

    public void setStat(String stat, int value) {
        stat = stat.toLowerCase().trim();
        switch (stat) {
            case "str":
            case "strength":
                player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcStrI", value);
                break;
            case "dex":
            case "dexterity":
                player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcDexI", value);
                break;
            case "con":
            case "constitution":
                player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcCnsI", value);
                break;
            case "wil":
            case "willpower":
                player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcWilI", value);
                break;
            case "mnd":
            case "mind":
                player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcIntI", value);
                break;
            case "spi":
            case "spirit":
                player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcCncI", value);
                break;
        }
    }

    public int getStat(String stat) {
        stat = stat.toLowerCase().trim();
        switch (stat) {
            case "str":
            case "strength":
                return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcStrI");
            case "dex":
            case "dexterity":
                return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcDexI");
            case "con":
            case "constitution":
                return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcCnsI");
            case "wil":
            case "willpower":
                return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcWilI");
            case "mnd":
            case "mind":
                return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcIntI");
            case "spi":
            case "spirit":
                return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcCncI");
        }

        throw new CustomNPCsException("Invalid stat name: " + stat + "\nValid stat names are:" +
                "\nstr, dex, con, wil, mnd, spi\nstrength, dexterity, constitution, willpower, mind, spirit",new Object[0]);
    }

    public void setRelease(byte release){
        player.getEntityData().getCompoundTag("PlayerPersisted").setByte("jrmcRelease",release);
    }
    public byte getRelease(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcRelease");
    }

    public void setBody(int body){
        player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcBdy",body);
    }
    public int getBody(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcBdy");
    }

    public void setStamina(int stamina){
        player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcStamina",stamina);
    }
    public int getStamina(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcStamina");
    }

    public void setKi(int ki){
        player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcEnrgy",ki);
    }
    public int getKi(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcEnrgy");
    }

    public void setTP(int tp){
        player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcTpint",tp);
    }
    public int getTP(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcTpint");
    }

    public void setGravity(float gravity){
        player.getEntityData().getCompoundTag("PlayerPersisted").setFloat("jrmcGravForce",gravity);
    }
    public float getGravity(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getFloat("jrmcGravForce");
    }

    public boolean isBlocking(){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);
        return compound.getCompoundTag("JRMCEP").getInteger("blocking") == 1;
    }

    public void setHairCode(String hairCode){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);
        compound.getCompoundTag("JRMCEP").setString("haircode",hairCode);
    }
    public String getHairCode(){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);
        return compound.getCompoundTag("JRMCEP").getString("haircode");
    }

    public void setExtraCode(String extraCode){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);
        compound.getCompoundTag("JRMCEP").setString("extracode",extraCode);
    }
    public String getExtraCode(){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);
        return compound.getCompoundTag("JRMCEP").getString("extracode");
    }

    public ScriptItemStack[] getInventory(){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);

        NBTTagList list = compound.getCompoundTag("JRMCEP").getTagList("dbcExtraInvTag",10);
        ScriptItemStack[] itemList = new ScriptItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
            itemList[i] = new ScriptItemStack(itemStack);
        }

        return itemList;
    }

    public void setForm(byte form){
        player.getEntityData().getCompoundTag("PlayerPersisted").setByte("jrmcState",form);
    }
    public byte getForm(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcState");
    }
    public void setForm2(byte form2){
        player.getEntityData().getCompoundTag("PlayerPersisted").setByte("jrmcState2",form2);
    }
    public byte getForm2(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcState2");
    }

    public void setPowerPoints(int points){
        player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcArcRsrv",points);
    }
    public int getPowerPoints(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcArcRsrv");
    }

    public void setAuraColor(int color){
        player.getEntityData().getCompoundTag("PlayerPersisted").setInteger("jrmcAuraColor",color);
    }
    public int getAuraColor(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcAuraColor");
    }

    public void setFormLevel(int level){
        player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcSSltX","TR"+level);
    }
    public int getFormLevel(){
        return Integer.parseInt(player.getEntityData().getCompoundTag("PlayerPersisted").getString("jrmcSSltX").replace("TR",""));
    }

    public byte getDBCClass(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcClass");
    }
    public int getRace(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcRace");
    }
    public int getPowerType(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcPwrtyp");
    }
    public int getKillCount(String type){
        type = type.toLowerCase().trim();

        int evilKills = player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcKillCountEvil");
        int goodKills = player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcKillCountGood");
        int neutKills = player.getEntityData().getCompoundTag("PlayerPersisted").getInteger("jrmcKillCountNeut");

        switch (type) {
            case "evil":
                return evilKills;
            case "good":
                return goodKills;
            case "neutral":
                return neutKills;
            case "all":
                return evilKills + goodKills + neutKills;
        }

        throw new CustomNPCsException("Invalid kill type: " + type + "\nValid kill types are: evil, good, neutral, all", new Object[0]);
    }
}
