package noppes.npcs.scripted.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.entity.IDBCPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;

public class ScriptDBCPlayer<T extends EntityPlayerMP> extends ScriptPlayer<T> implements IDBCPlayer {
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

    private double applyOperator(String method, double n1, double n2) {
        if (method.equals("+")) {
            n1 += n2;
        } else if (method.equals("-")) {
            n1 -= n2;
        } else if (method.equals("*")) {
            n1 *= n2;
        } else if (method.equals("/")) {
            n1 /= n2;
        } else if (method.equals("%")) {
            n1 %= n2;
        }

        return n1;
    }

    public void addBonusAttribute(String stat, String bonusID, String operation, double attributeValue){
        addBonusAttribute(stat,bonusID,operation,attributeValue,true);
    }
    public void addBonusAttribute(String stat, String bonusID, String operation, double attributeValue, boolean endOfTheList){
        bonusAttribute("add",stat,bonusID,operation,attributeValue,endOfTheList);
    }
    public void addToBonusAttribute(String stat, String bonusID, String operation, double attributeValue){
        bonusAttribute("addto",stat,bonusID,operation,attributeValue,true);
    }
    public void setBonusAttribute(String stat, String bonusID, String operation, double attributeValue){
        bonusAttribute("set",stat,bonusID,operation,attributeValue,true);
    }
    public void getBonusAttribute(String stat, String bonusID){
        bonusAttribute("get",stat,bonusID,"*",1.0,true);
    }
    public void removeBonusAttribute(String stat, String bonusID){
        bonusAttribute("remove",stat,bonusID,"*",1.0,true);
    }
    public void clearBonusAttribute(String stat){
        bonusAttribute("clear",stat,"","*",1.0,true);
    }

    public String bonusAttribute(String action, String stat, String bonusID) {
        String[] actions = new String[]{"get", "remove", "clear"};
        boolean valid = false;
        for(String s : actions){
            if(s.equals(action.toLowerCase())){
                valid = true;
            }
        }
        if (!valid) {
            throw new CustomNPCsException("Action can be:  get/remove/clear", new Object[0]);
        }

        return bonusAttribute(action,stat,bonusID,"*",1.0,false);
    }

    public String bonusAttribute(String action, String stat, String bonusID, String operation, double attributeValue, boolean endOfTheList) {
        String[] actions = new String[]{"add", "addto", "set", "get", "remove", "clear"};
        String[] operations = new String[]{"+", "-", "*", "/", "%"};

        boolean valid = false;
        for(String s : actions){
            if(s.equals(action.toLowerCase())){
                valid = true;
            }
        }
        if (!valid) {
            throw new CustomNPCsException("Action can be:  add/addTo/set/get/remove/clear", new Object[0]);
        }

        if(!action.equals("remove") && !action.equals("get") && !action.equals("clear")) {
            valid = false;
            for (String s : operations) {
                if (s.equals(operation)) {
                    valid = true;
                }
            }
            if (!valid) {
                throw new CustomNPCsException("Operation can be:  +  -  *  /  %", new Object[0]);
            }
        }

        stat = stat.toLowerCase().trim();
        switch (stat) {
            case "strength":
                stat = "str";
                break;
            case "dexterity":
                stat = "dex";
                break;
            case "constitution":
                stat = "con";
                break;
            case "willpower":
                stat = "wil";
                break;
            case "mind":
                stat = "mnd";
                break;
            case "spirit":
                stat = "spi";
                break;
        }
        if(!(stat.equals("str") || stat.equals("dex") || stat.equals("con") || stat.equals("wil") || stat.equals("mnd") || stat.equals("spi")))
            throw new CustomNPCsException("Invalid stat name: " + stat + "\nValid stat names are:" +
                    "\nstr, dex, con, wil, mnd, spi\nstrength, dexterity, constitution, willpower, mind, spirit",new Object[0]);

        String bonusValueString = operation + attributeValue;
        String bonus = player.getEntityData().getCompoundTag("PlayerPersisted").getString("jrmcAttrBonus"+stat);
        String bonuses[] = bonus.split("\\|");
        String[][] bonusValues = new String[bonuses.length][2];
        if (bonuses.length > 0 && bonuses[0].length() > 0) {
            for(int i = 0; i < bonuses.length; ++i) {
                String[] bonusValue = bonuses[i].split("\\;");
                bonusValues[i][0] = bonusValue[0];
                bonusValues[i][1] = bonusValue[1];
            }
        }

        switch (action) {
            case "get":
                if(player.getEntityData().getCompoundTag("PlayerPersisted").hasKey("jrmcAttrBonus"+stat)) {
                    for (String[] s : bonusValues) {
                        if (s[0].equals(bonusID))
                            return s[1];
                    }
                }
                return "";
            case "clear":
                if(player.getEntityData().getCompoundTag("PlayerPersisted").hasKey("jrmcAttrBonus"+stat))
                    player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcAttrBonus"+stat, "");
                break;
            case "remove":
                String newBonusString = "";
                if(player.getEntityData().getCompoundTag("PlayerPersisted").hasKey("jrmcAttrBonus"+stat)) {
                    int num = -1;
                    boolean number;
                    boolean run = false;
                    String[] bonusValue;

                    try {
                        num = Integer.parseInt(bonusID);
                        number = true;
                    } catch (Exception var33) {
                        number = false;
                    }

                    for(int i = 0; i < bonuses.length; ++i) {
                        bonusValue = bonuses[i].split("\\;");
                        bonusValues[i][0] = bonusValue[0];
                        if (number && i == num || !number && bonusValues[i][0].equals(bonusID)) {
                            bonuses[i] = "";
                            run = true;
                            break;
                        }
                    }

                    if (run) {
                        String startString = "";

                        for (int i = 0; i < bonuses.length; ++i) {
                            if (bonuses[i] != null && bonuses[i].length() > 0) {
                                startString = startString + bonuses[i] + (bonuses.length - 1 == i ? "" : "|");
                            }
                        }

                        player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcAttrBonus"+stat, startString);
                    }
                }
                break;
            case "add":
                boolean nbtFail = false;

                for (int id = 0; id < bonuses.length; ++id) {
                    String[] bonusValue = bonuses[id].split("\\;");
                    bonusValues[id][0] = bonusValue[0];
                    if (bonusValues[id][0].equals(bonusID)) {
                        nbtFail = true;
                        break;
                    }
                }

                if (!nbtFail) {
                    if (endOfTheList) {
                        if (bonus.length() == 0) {
                            bonus = bonusID + ";" + bonusValueString;
                        } else {
                            bonus = bonus + "|" + bonusID + ";" + bonusValueString;
                        }
                    } else {
                        if (bonus.length() == 0) {
                            bonus = bonusID + ";" + bonusValueString;
                        } else {
                            bonus = bonusID + ";" + bonusValueString + "|" + bonus;
                        }
                    }
                    player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcAttrBonus"+stat, bonus);
                }
                break;
            case "set": {
                String noNBTText;
                int startIndex = -1;
                boolean number = false;
                boolean run = false;

                try {
                    startIndex = Integer.parseInt(bonusID);
                    number = true;
                } catch (Exception var34) {
                    number = false;
                }

                for (startIndex = 0; startIndex < bonuses.length; ++startIndex) {
                    String[] bonusValue = bonuses[startIndex].split("\\;");
                    bonusValues[startIndex][0] = bonusValue[0];
                    if (number && startIndex == startIndex || !number && bonusValues[startIndex][0].equals(bonusID)) {
                        noNBTText = bonusValues[startIndex][0] + ";" + bonusValueString;
                        bonuses[startIndex] = "";
                        run = true;
                        bonuses[startIndex] = noNBTText;
                        bonusValue = bonuses[startIndex].split("\\;");
                        bonusValues[startIndex][0] = bonusValue[0];
                        bonusValues[startIndex][1] = bonusValue[1];
                        break;
                    }
                }

                if (run) {
                    String startString = "";

                    for (int i = 0; i < bonuses.length; ++i) {
                        if (bonuses[i] != null && bonuses[i].length() > 0) {
                            startString = startString + bonuses[i] + (bonuses.length - 1 == i ? "" : "|");
                        }
                    }

                    player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcAttrBonus"+stat, startString);
                }
                break;
            }
            case "addto": {
                boolean number;
                boolean run = false;
                int id = -1;
                try {
                    id = Integer.parseInt(bonusID);
                    number = true;
                } catch (Exception var35) {
                    number = false;
                }

                for (int i = 0; i < bonuses.length; ++i) {
                    String[] bonusValue = bonuses[i].split("\\;");
                    bonusValues[i][0] = bonusValue[0];
                    if (number && i == id || !number && bonusValues[i][0].equals(bonusID)) {
                        if (!bonusValues[i][1].contains("nbt_") && !bonusValues[i][1].contains("NBT_") && !bonusValueString.contains("nbt_") && !bonusValueString.contains("NBT_")) {
                            double value = Double.parseDouble(bonusValues[i][1].substring(1));
                            double resultValue = applyOperator(operation, value, attributeValue);
                            String result = bonusValues[i][1].substring(0, 1) + resultValue;
                            String data = bonusValues[i][0] + ";" + result;
                            bonuses[i] = data;
                            bonusValue = bonuses[i].split("\\;");
                            bonusValues[i][0] = bonusValue[0];
                            bonusValues[i][1] = bonusValue[1];
                            run = true;
                        }
                        break;
                    }
                }
                if (run) {
                    String startString = "";

                    for (int i = 0; i < bonuses.length; ++i) {
                        if (bonuses[i] != null && bonuses[i].length() > 0) {
                            startString = startString + bonuses[i] + (bonuses.length - 1 == i ? "" : "|");
                        }
                    }

                    player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcAttrBonus"+stat, startString);
                }
                break;
            }
        }

        return "";
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

    public void setItem(IItemStack itemStack, byte slot, boolean vanity) {
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);

        byte offset = (byte) (vanity ? 10 : 2);

        NBTTagList newList = new NBTTagList();
        NBTTagList list = compound.getCompoundTag("JRMCEP").getTagList("dbcExtraInvTag",10);
        for (int i = 0; i < list.tagCount(); i++) {
            if (offset - list.getCompoundTagAt(i).getByte("Slot") != slot) {
                newList.appendTag(list.getCompoundTagAt(i));
            }
        }

        NBTTagCompound compoundItem = new NBTTagCompound();
        if (itemStack != null) {
            itemStack.getMCItemStack().writeToNBT(compoundItem);
        }
        compoundItem.setByte("Slot", (byte) (offset - slot));
        newList.appendTag(compoundItem);

        compound.getCompoundTag("JRMCEP").setTag("dbcExtraInvTag", newList);
        this.player.readFromNBT(compound);
    }

    public IItemStack getItem(byte slot, boolean vanity) {
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);

        byte offset = (byte) (vanity ? 10 : 2);

        NBTTagList list = compound.getCompoundTag("JRMCEP").getTagList("dbcExtraInvTag",10);
        for (int i = 0; i < list.tagCount(); i++) {
            if (offset - list.getCompoundTagAt(i).getByte("Slot") == slot) {
                return NpcAPI.Instance().getIItemStack(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
            }
        }

        return null;
    }

    public IItemStack[] getInventory(){
        NBTTagCompound compound = new NBTTagCompound();
        this.player.writeToNBT(compound);

        NBTTagList list = compound.getCompoundTag("JRMCEP").getTagList("dbcExtraInvTag",10);
        IItemStack[] itemList = new IItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
            itemList[i] = NpcAPI.Instance().getIItemStack(itemStack);
        }

        return itemList;
    }

    public void setForm(byte form){
        if(form < 0)
            return;

        switch(getRace()){
            case 0:
            case 3:
                if(form > 3)
                    return;
                break;
            case 1:
            case 2:
                if(form > 15)
                    return;
                break;
            case 4:
                if(form > 7)
                    return;
                break;
        }

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
        String formString = player.getEntityData().getCompoundTag("PlayerPersisted").getString("jrmcSSltX");
        if(formString.length() < 1)
            return 0;

        StringBuilder digitString = new StringBuilder();
        for(char c : formString.toCharArray()){
            if(Character.isDigit(c))
                digitString.append(c);
        }

        if(digitString.length() < 1)
            return 0;

        return Integer.parseInt(digitString.toString());
    }

    public void setSkills(String skills){
        player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcSSlts",skills);
    }
    public String getSkills(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getString("jrmcSSlts");
    }

    public void setJRMCSE(String statusEffects){
        player.getEntityData().getCompoundTag("PlayerPersisted").setString("jrmcStatusEff",statusEffects);
    }
    public String getJRMCSE(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getString("jrmcStatusEff");
    }

    public byte getDBCClass(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcClass");
    }
    public void setDBCClass(byte dbcClass) {
        if (dbcClass < 0 || dbcClass > 2)
            return;

        player.getEntityData().getCompoundTag("PlayerPersisted").setByte("jrmcClass", dbcClass);
    }

    public int getRace(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcRace");
    }
    public void setRace(byte race) {
        if (race < 0 || race > 1)
            return;

        player.getEntityData().getCompoundTag("PlayerPersisted").setByte("jrmcRace", race);
    }

    public int getPowerType(){
        return player.getEntityData().getCompoundTag("PlayerPersisted").getByte("jrmcPwrtyp");
    }
    public void setPowerType(byte powerType) {
        if (powerType < 0 || powerType > 1)
            return;

        player.getEntityData().getCompoundTag("PlayerPersisted").setByte("jrmcPwrtyp", powerType);
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