package noppes.npcs.scripted.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.scripted.CustomNPCsException;

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
                player.getEntityData().setInteger("jrmcStrI", value);
                break;
            case "dex":
            case "dexterity":
                player.getEntityData().setInteger("jrmcDexI", value);
                break;
            case "con":
            case "constitution":
                player.getEntityData().setInteger("jrmcCnsI", value);
                break;
            case "wil":
            case "willpower":
                player.getEntityData().setInteger("jrmcWilI", value);
                break;
            case "mnd":
            case "mind":
                player.getEntityData().setInteger("jrmcIntI", value);
                break;
            case "spi":
            case "spirit":
                player.getEntityData().setInteger("jrmcCncI", value);
                break;
        }
    }

    public int getStat(String stat) {
        stat = stat.toLowerCase().trim();
        switch (stat) {
            case "str":
            case "strength":
                return player.getEntityData().getInteger("jrmcStrI");
            case "dex":
            case "dexterity":
                return player.getEntityData().getInteger("jrmcDexI");
            case "con":
            case "constitution":
                return player.getEntityData().getInteger("jrmcCnsI");
            case "wil":
            case "willpower":
                return player.getEntityData().getInteger("jrmcWilI");
            case "mnd":
            case "mind":
                return player.getEntityData().getInteger("jrmcIntI");
            case "spi":
            case "spirit":
                return player.getEntityData().getInteger("jrmcCncI");
        }

        throw new CustomNPCsException("Invalid stat name: " + stat + "\nValid stat names are:" +
                "\nstr, dex, con, wil, mnd, spi\nstrength, dexterity, constitution, willpower, mind, spirit",new Object[0]);
    }

    public void setRelease(byte release){
        player.getEntityData().setByte("jrmcRelease",release);
    }
    public byte getRelease(){
        return player.getEntityData().getByte("jrmcRelease");
    }

    public void setBody(int body){
        player.getEntityData().setInteger("jrmcBdy",body);
    }
    public int getBody(){
        return player.getEntityData().getInteger("jrmcBdy");
    }

    public void setStamina(int stamina){
        player.getEntityData().setInteger("jrmcStamina",stamina);
    }
    public int getStamina(){
        return player.getEntityData().getInteger("jrmcStamina");
    }

    public void setKi(int ki){
        player.getEntityData().setInteger("jrmcEnrgy",ki);
    }
    public int getKi(){
        return player.getEntityData().getInteger("jrmcEnrgy");
    }

    public void setTP(int tp){
        player.getEntityData().setInteger("jrmcTpint",tp);
    }
    public int getTP(){
        return player.getEntityData().getInteger("jrmcTpint");
    }

    public void setGravity(float gravity){
        player.getEntityData().setFloat("jrmcGravForce",gravity);
    }
    public float getGravity(){
        return player.getEntityData().getFloat("jrmcGravForce");
    }

    public void setForm(byte form){
        player.getEntityData().setByte("jrmcState",form);
    }
    public byte getForm(){
        return player.getEntityData().getByte("jrmcState");
    }
    public void setForm2(byte form2){
        player.getEntityData().setByte("jrmcState2",form2);
    }
    public byte getForm2(){
        return player.getEntityData().getByte("jrmcState2");
    }

    public void setPowerPoints(int points){
        player.getEntityData().setInteger("jrmcArcRsrv",points);
    }
    public int getPowerPoints(){
        return player.getEntityData().getInteger("jrmcArcRsrv");
    }

    public void setAuraColor(int color){
        player.getEntityData().setInteger("jrmcAuraColor",color);
    }
    public int getAuraColor(){
        return player.getEntityData().getInteger("jrmcAuraColor");
    }

    public byte getDBCClass(){
        return player.getEntityData().getByte("jrmcClass");
    }
    public int getRace(){
        return player.getEntityData().getByte("jrmcRace");
    }
    public int getPowerType(){
        return player.getEntityData().getByte("jrmcPwrtyp");
    }
    public int getKillCount(String type){
        type = type.toLowerCase().trim();

        int evilKills = player.getEntityData().getInteger("jrmcKillCountEvil");
        int goodKills = player.getEntityData().getInteger("jrmcKillCountGood");
        int neutKills = player.getEntityData().getInteger("jrmcKillCountNeut");

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
