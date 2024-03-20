package kamkeel.developer;

import java.util.HashSet;
import java.util.UUID;

public class Developer {

    public static HashSet<UUID> Universal = new HashSet<>();
    public static HashSet<UUID> ScriptUser = new HashSet<>();
    public static HashSet<UUID> WandUser = new HashSet<>();
    public static HashSet<UUID> QuestMaker = new HashSet<>();
    public static Developer Instance;

    public Developer(){
        Instance = this;
        // Temp for testing
        Universal.add(UUID.fromString("29cc52dd-2c50-4e8f-a388-be6c497cf0b4"));
    }

    public boolean hasUniversal(UUID uuid){
        if(Universal == null){
            Universal = new HashSet<>();
        }
        return Universal.contains(uuid);
    }

    public boolean hasScriptUser(UUID uuid){
        if(ScriptUser == null){
            ScriptUser = new HashSet<>();
        }
        return ScriptUser.contains(uuid);
    }

    public boolean hasWandUser(UUID uuid){
        return WandUser.contains(uuid);
    }

    public boolean hasQuestMaker(UUID uuid){
        return QuestMaker.contains(uuid);
    }

}
