package kamkeel.developer;

import java.util.ArrayList;
import java.util.UUID;

public class Developer {

    public static ArrayList<UUID> Universal = new ArrayList<>();
    public static ArrayList<UUID> ScriptUser = new ArrayList<>();
    public static ArrayList<UUID> WandUser = new ArrayList<>();
    public static ArrayList<UUID> QuestMaker = new ArrayList<>();
    public static Developer instance;

    public Developer(){
        instance = this;
        // Temp for testing
        Universal.add(UUID.fromString("29cc52dd-2c50-4e8f-a388-be6c497cf0b4"));
    }

    public boolean hasUniversal(UUID uuid){
        return Universal.contains(uuid);
    }

    public boolean hasScriptUser(UUID uuid){
        return ScriptUser.contains(uuid);
    }

    public boolean hasWandUser(UUID uuid){
        return WandUser.contains(uuid);
    }

    public boolean hasQuestMaker(UUID uuid){
        return QuestMaker.contains(uuid);
    }

}
