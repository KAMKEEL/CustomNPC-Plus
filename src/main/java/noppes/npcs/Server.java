package noppes.npcs;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Server {
    public static boolean fillBuffer(ByteBuf buffer, Enum enu, Object... obs) throws IOException {
        buffer.writeInt(enu.ordinal());
        for (Object ob : obs) {
            if (ob == null)
                continue;
            if (ob instanceof Map) {
                Map<String, Integer> map = (Map<String, Integer>) ob;

                buffer.writeInt(map.size());
                for (String key : map.keySet()) {
                    int value = map.get(key);
                    buffer.writeInt(value);
                    ByteBufUtils.writeString(buffer, key);
                }
            } else if (ob instanceof MerchantRecipeList)
                ((MerchantRecipeList) ob).func_151391_a(new PacketBuffer(buffer));
            else if (ob instanceof List) {
                List<String> list = (List<String>) ob;
                buffer.writeInt(list.size());
                for (String s : list)
                    ByteBufUtils.writeString(buffer, s);
            } else if (ob instanceof Enum)
                buffer.writeInt(((Enum<?>) ob).ordinal());
            else if (ob instanceof Integer)
                buffer.writeInt((Integer) ob);
            else if (ob instanceof Boolean)
                buffer.writeBoolean((Boolean) ob);
            else if (ob instanceof String)
                ByteBufUtils.writeString(buffer, (String) ob);
            else if (ob instanceof Float)
                buffer.writeFloat((Float) ob);
            else if (ob instanceof Long)
                buffer.writeLong((Long) ob);
            else if (ob instanceof Double)
                buffer.writeDouble((Double) ob);
            else if (ob instanceof byte[]) {
                // Write byte array length followed by bytes
                byte[] byteArray = (byte[]) ob;
                buffer.writeShort((short) byteArray.length);
                buffer.writeBytes(byteArray);
            } else if (ob instanceof NBTTagCompound)
                ByteBufUtils.writeNBT(buffer, (NBTTagCompound) ob);
        }
        if (buffer.array().length >= 32767) {
            LogWriter.error("Packet " + enu + " was too big to be send");
            LogWriter.script("Issue occurred with the following Packet - " + enu + ":\n" + Arrays.toString(obs));
            return false;
        }
        return true;
    }
}
