package kamkeel.npcs.fixtures;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An index of what was generated, why, and where it landed.
 *
 * <p>Deliberately not a second copy of the data. The files are the fixture; this says which file
 * holds which case, so a test can pick the one it needs without opening twenty of them, and so a
 * reviewer can see the intent that a compound of integers does not carry.
 */
public final class Manifest {

    /** One generated file. */
    public static final class Entry {
        final String world;
        final String system;
        final String id;
        final String path;
        final String note;

        Entry(String world, String system, String id, String path, String note) {
            this.world = world;
            this.system = system;
            this.id = id;
            this.path = path;
            this.note = note;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();
    private final Set<String> warnings = new LinkedHashSet<String>();
    private final List<String[]> worlds = new ArrayList<String[]>();

    public void world(String name, String purpose) {
        worlds.add(new String[]{name, purpose});
    }

    public void record(String world, String system, String id, String path, String note) {
        entries.add(new Entry(world, system, id, path, note));
    }

    /**
     * Records something a consumer of these fixtures has to know and could not discover from the
     * bytes -- above all, the places CustomNPC+ writes a value that changes between runs.
     */
    public void warn(String warning) {
        warnings.add(warning);
    }

    public Set<String> warnings() {
        return warnings;
    }

    public int count() {
        return entries.size();
    }

    public int countOf(String system) {
        int n = 0;
        for (Entry e : entries) {
            if (e.system.equals(system)) {
                n++;
            }
        }
        return n;
    }

    public void write(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"generatedBy\": \"CustomNPC+ scribeFixtures\",\n");
        sb.append("  \"note\": \"The files are the fixture. This index records intent and location, ");
        sb.append("not a second copy of the data. Readable renderings of every .dat sit under ");
        sb.append("<world>/_readable/, written by CustomNPC+'s own NBTJsonUtil.\",\n");

        sb.append("  \"warnings\": [\n");
        int wi = 0;
        for (String w : warnings) {
            sb.append("    ").append(quote(w));
            sb.append(++wi < warnings.size() ? ",\n" : "\n");
        }
        sb.append("  ],\n");

        sb.append("  \"worlds\": {\n");
        for (int i = 0; i < worlds.size(); i++) {
            String[] w = worlds.get(i);
            sb.append("    ").append(quote(w[0])).append(": ").append(quote(w[1]));
            sb.append(i + 1 < worlds.size() ? ",\n" : "\n");
        }
        sb.append("  },\n");

        sb.append("  \"entries\": [\n");
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            sb.append("    {");
            sb.append("\"world\": ").append(quote(e.world));
            sb.append(", \"system\": ").append(quote(e.system));
            sb.append(", \"id\": ").append(quote(e.id));
            sb.append(", \"path\": ").append(quote(e.path));
            sb.append(", \"note\": ").append(quote(e.note));
            sb.append("}");
            sb.append(i + 1 < entries.size() ? ",\n" : "\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");

        Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        try {
            out.write(sb.toString());
        } finally {
            out.close();
        }
    }

    /**
     * JSON string escaping. Hand-rolled rather than pulled from a library because the manifest is
     * the one file here that is not written by CustomNPC+, so it should have no dependency that
     * could change what it produces between environments.
     */
    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
