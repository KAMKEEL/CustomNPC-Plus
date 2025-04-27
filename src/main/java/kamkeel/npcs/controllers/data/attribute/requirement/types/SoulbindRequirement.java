package kamkeel.npcs.controllers.data.attribute.requirement.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class SoulbindRequirement implements IRequirementChecker {

    public static HashMap<String, String> uuidToUsername = new HashMap<>();

    @Override
    public String getKey() {
        return "cnpc_soulbind";
    }

    @Override
    public String getTranslation() {
        return "requirement.soulbind";
    }

    @Override
    public String getTooltipValue(NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            String uuid = nbt.getString(getKey());
            if (uuid != null && !uuid.isEmpty()) {
                return getUsernameFromUUID(uuid);
            }
        }
        return "null";
    }

    private String getUsernameFromUUID(String uuid) {
        if(uuidToUsername.containsKey(uuid)){
            if(!uuidToUsername.get(uuid).equals("null")){
                return uuidToUsername.get(uuid);
            }
        }
        try {
            String strippedUUID = uuid.replace("-", "");
            String urlStr = "https://sessionserver.mojang.com/session/minecraft/profile/" + strippedUUID;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream stream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");

            JsonElement element = new JsonParser().parse(reader);
            JsonObject jsonObj = element.getAsJsonObject();

            if (jsonObj.has("name")) {
                String name = jsonObj.get("name").getAsString();
                uuidToUsername.put(uuid, name);
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }


    @Override
    public Object getValue(NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            return nbt.getString(getKey());
        }
        return null;
    }

    @Override
    public void apply(NBTTagCompound nbt, Object value) {
        if (value instanceof String) {
            nbt.setString(getKey(), (String) value);
        }
    }

    @Override
    public boolean check(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            String uuidString = nbt.getString(getKey());
            if(uuidString != null && !uuidString.isEmpty()){
                UUID convert = null;
                try {
                    convert = UUID.fromString(uuidString);
                } catch (Exception ignored){}
                if(convert != null)
                    return player.getUniqueID().equals(convert);
            }
            return false;
        }
        return true;
    }
}
