package noppes.npcs.util;

import com.google.common.io.Files;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.nbt.*;
import noppes.npcs.LogWriter;
import org.apache.commons.io.Charsets;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NBTJsonUtil {
    public static String Convert(NBTTagCompound compound) {
        List<JsonLine> list = new ArrayList<JsonLine>();
        JsonLine line = ReadTag("", compound, list);
        line.removeComma();
        return ConvertList(list);
    }

    public static NBTTagCompound Convert(String json) throws JsonException {
        json = json.trim();
        JsonFile file = new JsonFile(json);
        if (!json.startsWith("{") || !json.endsWith("}"))
            throw new JsonException("Not properly incapsulated between { }", file);

        NBTTagCompound compound = new NBTTagCompound();
        FillCompound(compound, file);
        return compound;
    }

    public static void FillCompound(NBTTagCompound compound, JsonFile json) throws JsonException {
        if (json.startsWith("{") || json.startsWith(","))
            json.cut(1);
        if (json.startsWith("}"))
            return;
        int index = json.indexOf(":");
        if (index < 1)
            throw new JsonException("Expected key after ,", json);

        String key = json.substring(0, index);
        json.cut(index + 1);

        NBTBase base = ReadValue(json);

        if (base == null)
            base = new NBTTagString();

        if (key.startsWith("\""))
            key = key.substring(1);
        if (key.endsWith("\""))
            key = key.substring(0, key.length() - 1);

        compound.setTag(key, base);
        if (json.startsWith(","))
            FillCompound(compound, json);
    }

    public static NBTBase ReadValue(JsonFile json) throws JsonException {
        if (json.startsWith("{")) {
            NBTTagCompound compound = new NBTTagCompound();
            FillCompound(compound, json);
            if (!json.startsWith("}")) {
                throw new JsonException("Expected }", json);
            }
            json.cut(1);
            return compound;
        }
        if (json.startsWith("[")) {
            json.cut(1);
            NBTTagList list = new NBTTagList();
            NBTBase value = ReadValue(json);
            while (value != null) {
                list.appendTag(value);
                if (!json.startsWith(","))
                    break;
                json.cut(1);
                value = ReadValue(json);
            }
            if (!json.startsWith("]")) {
                throw new JsonException("Expected ]", json);
            }
            json.cut(1);
            if (list.func_150303_d() == 3) {
                int[] arr = new int[list.tagCount()];
                for (int i = 0; list.tagCount() > 0; i++) {
                    arr[i] = ((NBTTagInt) list.removeTag(0)).func_150287_d();
                }
                return new NBTTagIntArray(arr);
            }
            if (list.func_150303_d() == 1) {
                byte[] arr = new byte[list.tagCount()];
                for (int i = 0; list.tagCount() > 0; i++) {
                    arr[i] = ((NBTTagByte) list.removeTag(0)).func_150290_f();
                }
                return new NBTTagByteArray(arr);
            }
            return list;
        }
        if (json.startsWith("\"")) {
            json.cut(1);
            String s = "";
            boolean ignore = false;
            while (!json.startsWith("\"") || ignore) {
                String cut = json.cutDirty(1);
                ignore = cut.equals("\\");
                s += cut;
            }
            json.cut(1);
            return new NBTTagString(s.replace("\\\"", "\""));
        }
        String s = "";
        while (!json.startsWith(",", "]", "}")) {
            s += json.cut(1);
        }
        s = s.trim().toLowerCase();
        if (s.isEmpty() || s.contains("bytes]"))
            return null;
        try {
            if (s.endsWith("d")) {
                return new NBTTagDouble(Double.parseDouble(s.substring(0, s.length() - 1)));
            }
            if (s.endsWith("f")) {
                return new NBTTagFloat(Float.parseFloat(s.substring(0, s.length() - 1)));
            }
            if (s.endsWith("b")) {
                return new NBTTagByte(Byte.parseByte(s.substring(0, s.length() - 1)));
            }
            if (s.endsWith("s")) {
                return new NBTTagShort(Short.parseShort(s.substring(0, s.length() - 1)));
            }
            if (s.endsWith("l")) {
                return new NBTTagLong(Long.parseLong(s.substring(0, s.length() - 1)));
            }
            if (s.contains("."))
                return new NBTTagDouble(Double.parseDouble(s));
            else
                return new NBTTagInt(Integer.parseInt(s));
        } catch (NumberFormatException ex) {
            throw new JsonException("Unable to convert: " + s + " to a number", json);
        }
    }

    private static List<NBTBase> getListData(NBTTagList list) {
        return ObfuscationReflectionHelper.getPrivateValue(NBTTagList.class, list, 0);
    }

    private static JsonLine ReadTag(String name, NBTBase base, List<JsonLine> list) {
        if (!name.isEmpty())
            name = "\"" + name + "\": ";
        if (base.getId() == 8) { // NBTTagString
            String data = ((NBTTagString) base).func_150285_a_();
            data = data.replace("\"", "\\\""); // Escape quotes
            list.add(new JsonLine(name + "\"" + data + "\""));
        } else if (base.getId() == 7) { // NBTTagByteArray
            byte[] arr = ((NBTTagByteArray) base).func_150292_c();
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]).append("b");
                if (i < arr.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            list.add(new JsonLine(name + sb.toString()));
            JsonLine line = list.get(list.size() - 1);
            line.line += ",";
            return line;
        } else if (base.getId() == 9) { // NBTTagList
            list.add(new JsonLine(name + "["));
            NBTTagList tags = (NBTTagList) base;
            JsonLine line = null;
            List<NBTBase> data = getListData(tags);
            for (NBTBase b : data)
                line = ReadTag("", b, list);
            if (line != null)
                line.removeComma();
            list.add(new JsonLine("]"));
        } else if (base.getId() == 10) { // NBTTagCompound
            list.add(new JsonLine(name + "{"));
            NBTTagCompound compound = (NBTTagCompound) base;
            JsonLine line = null;
            for (Object key : compound.func_150296_c())
                line = ReadTag(key.toString(), compound.getTag(key.toString()), list);
            if (line != null)
                line.removeComma();
            list.add(new JsonLine("}"));
        } else if (base.getId() == 11) { // NBTTagIntArray
            list.add(new JsonLine(name + base.toString().replaceFirst(",]", "]")));
        } else {
            list.add(new JsonLine(name + base));
        }
        JsonLine line = list.get(list.size() - 1);
        line.line += ",";
        return line;
    }

    private static String ConvertList(List<JsonLine> list) {
        String json = "";
        int tab = 0;
        for (JsonLine tag : list) {
            if (tag.reduceTab())
                tab--;
            for (int i = 0; i < tab; i++) {
                json += "    ";
            }
            json += tag + "\n";
            if (tag.increaseTab())
                tab++;
        }
        return json;
    }

    public static NBTTagCompound loadNBTData(File file) {
        try {
            return CompressedStreamTools.readCompressed(new FileInputStream(file));
        } catch (Exception e) {
            LogWriter.error("Error loading: " + file.getName(), e);
        }
        return new NBTTagCompound();
    }

    static class JsonLine {
        private String line;

        public JsonLine(String line) {
            this.line = line;
        }

        public void removeComma() {
            if (line.endsWith(","))
                line = line.substring(0, line.length() - 1);
        }

        public boolean reduceTab() {
            int length = line.length();
            return length == 1 && (line.endsWith("}") || line.endsWith("]"))
                || length == 2 && (line.endsWith("},") || line.endsWith("],"));
        }

        public boolean increaseTab() {
            return line.endsWith("{") || line.endsWith("[");
        }

        @Override
        public String toString() {
            return line;
        }
    }

    static class JsonFile {
        private String original;
        private String text;

        public JsonFile(String text) {
            this.text = text;
            this.original = text;
        }

        public String cutDirty(int i) {
            String s = text.substring(0, i);
            text = text.substring(i);
            return s;
        }

        public String cut(int i) {
            String s = text.substring(0, i);
            text = text.substring(i).trim();
            return s;
        }

        public String substring(int beginIndex, int endIndex) {
            return text.substring(beginIndex, endIndex);
        }

        public int indexOf(String s) {
            return text.indexOf(s);
        }

        public String getCurrentPos() {
            int lengthOr = original.length();
            int lengthCur = text.length();
            int currentPos = lengthOr - lengthCur;
            String done = original.substring(0, currentPos);
            String[] lines = done.split("\r\n|\r|\n");
            int pos = 0;
            String line = "";
            if (lines.length > 0) {
                pos = lines[lines.length - 1].length();
                line = original.split("\r\n|\r|\n")[lines.length - 1].trim();
            }
            return "Line: " + lines.length + ", Pos: " + pos + ", Text: " + line;
        }

        public boolean startsWith(String... ss) {
            for (String s : ss)
                if (text.startsWith(s))
                    return true;
            return false;
        }

        public boolean endsWith(String s) {
            return text.endsWith(s);
        }
    }

    public static NBTTagCompound LoadFile(File file) throws IOException, JsonException {
        return Convert(Files.toString(file, Charsets.UTF_8));
    }

    public static void SaveFile(File file, NBTTagCompound compound) throws IOException, JsonException {
        String json = Convert(compound);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
            writer.write(json);
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static void main(String[] args) {
        try {
            // Create a test compound with various NBT types
            NBTTagCompound comp = new NBTTagCompound();
            NBTTagCompound comp2 = new NBTTagCompound();

            // ByteArray and IntArray
            comp2.setByteArray("test", new byte[]{0, 0, 1, 1, 0});
            comp2.setIntArray("intArray", new int[]{0, 0, 1, 1, 0});

            // Primitive types
            comp2.setByte("byte", (byte) 7);
            comp2.setShort("short", (short) 123);
            comp2.setInteger("int", 456);
            comp2.setLong("long", 789L);
            comp2.setFloat("float", 3.14F);
            comp2.setDouble("double", 2.71828);
            comp2.setString("string", "Testing");

            // A list containing strings
            NBTTagList list = new NBTTagList();
            list.appendTag(new NBTTagString("jim"));
            list.appendTag(new NBTTagString("foo"));
            comp2.setTag("list", list);

            // A nested compound
            NBTTagCompound innerComp = new NBTTagCompound();
            innerComp.setString("innerString", "innerValue");
            innerComp.setInteger("innerInt", 10);
            innerComp.setByte("innerByte", (byte) 11);
            comp2.setTag("innerComp", innerComp);

            // Add our compound to the root compound.
            comp.setTag("comp", comp2);

            // Convert NBT to JSON string (writing)
            String json = Convert(comp);
            System.out.println("Generated JSON:");
            System.out.println(json);

            // Read back from JSON string (reading)
            NBTTagCompound compFromJson = Convert(json);
            String jsonAfterRead = Convert(compFromJson);
            System.out.println("\nRe-converted JSON from read NBT:");
            System.out.println(jsonAfterRead);

            // Basic check: compare the JSON outputs to verify consistency.
            if (json.equals(jsonAfterRead)) {
                System.out.println("\nSUCCESS: Read and write operations are consistent for all types.");
            } else {
                System.out.println("\nWARNING: There is a mismatch in read and write results for some types.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
