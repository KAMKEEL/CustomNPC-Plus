package kamkeel.util;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.LogWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ByteBufUtils extends cpw.mods.fml.common.network.ByteBufUtils {

    public static void writeIntArray(ByteBuf buffer, int[] arr) {
        buffer.writeInt(arr.length);
        for (int i : arr) {
            buffer.writeInt(i);
        }
    }

    public static int[] readIntArray(ByteBuf buffer) {
        int length = buffer.readInt();
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) {
            arr[i] = buffer.readInt();
        }
        return arr;
    }

    public static void writeNBT(ByteBuf buffer, NBTTagCompound compound) throws IOException {
        byte[] bytes = CompressedStreamTools.compress(compound);
        buffer.writeShort((short)bytes.length);
        buffer.writeBytes(bytes);
    }

    public static NBTTagCompound readNBT(ByteBuf buffer) throws IOException {
        byte[] bytes = new byte[buffer.readShort()];
        buffer.readBytes(bytes);
        return CompressedStreamTools.func_152457_a(bytes, new NBTSizeTracker(2097152L));
    }

    public static void writeString(ByteBuf buffer, String s){
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        buffer.writeShort((short)bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readString(ByteBuf buffer){
        try{
            byte[] bytes = new byte[buffer.readShort()];
            buffer.readBytes(bytes);
            return new String(bytes, Charsets.UTF_8);
        }
        catch(IndexOutOfBoundsException ex){
            return null;
        }
    }
}
