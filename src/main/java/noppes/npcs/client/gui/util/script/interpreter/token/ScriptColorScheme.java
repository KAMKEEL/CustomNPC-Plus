package noppes.npcs.client.gui.util.script.interpreter.token;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Manages reloadable style overrides (color, priority, bold, italic) for {@link TokenType},
 * plus editor background/gutter/scrollbar colors via {@link BackgroundStyleEntry}.
 * <p>
 * Loaded from {@code assets/customnpcs/colorscheme/script_editor_scheme.json} on resource reload.
 * Styles are cached in a flat array indexed by {@link TokenType#ordinal()} for zero-cost lookups.
 */
public final class ScriptColorScheme {

    // Volatile array reference swapped atomically — all tokens update at once
    static volatile StyleEntry[] styles = buildDefaults();

    // Background/gutter/scrollbar colors — volatile for same atomic-swap pattern
    private static volatile BackgroundStyleEntry background = BackgroundStyleEntry.DEFAULT;

    private ScriptColorScheme() {}

    /** Returns the current background style (never null). */
    public static BackgroundStyleEntry getBackgroundStyle() {
        return background;
    }

    /**
     * Reload the color scheme from the resource pack's JSON file.
     * Uses {@code getAllResources()} to find the highest-priority version
     * (the last entry in the list, which is the latest resource pack override).
     * Falls back to defaults if the file is missing or malformed.
     */
    public static void reloadColorScheme(IResourceManager resourceManager) {
        ResourceLocation loc = new ResourceLocation("customnpcs", "colorscheme/script_editor_scheme.json");
        try {
            IResource resource = resourceManager.getResource(loc);

            try (InputStream is = resource.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                loadFromJson(reader);
                LogWriter.info("[ColorScheme] Loaded script_editor_scheme.json successfully");
                logLoadedSample();
            }
        } catch (FileNotFoundException e) {
            LogWriter.info("[ColorScheme] No script_editor_scheme.json found, using defaults");
            resetToDefaults();
        } catch (Exception e) {
            LogWriter.error("[ColorScheme] Failed to load script_editor_scheme.json: " + e.getMessage());
            resetToDefaults();
        }
    }

    private static String peekResourceContent(IResource res) {
        try (InputStream peekIs = res.getInputStream()) {
            byte[] buf = new byte[120];
            int read = peekIs.read(buf);
            if (read > 0) {
                return new String(buf, 0, read, "UTF-8").replaceAll("\\s+", " ").trim();
            }
        } catch (Exception ignored) {}
        return "<could not peek>";
    }

    private static void logLoadedSample() {
        StyleEntry[] current = styles;
        TokenType[] samples = { TokenType.COMMENT, TokenType.KEYWORD, TokenType.STRING, TokenType.METHOD_CALL, TokenType.DEFAULT };
        for (TokenType tt : samples) {
            StyleEntry e = current[tt.ordinal()];
            LogWriter.info("[ColorScheme]   " + tt.name() + " -> color=#" + String.format("%08X", e.hexColor)
                    + ", priority=" + e.priority + ", bold=" + e.bold + ", italic=" + e.italic);
        }

        BackgroundStyleEntry bg = background;
        LogWriter.info("[ColorScheme]   background -> bg=#" + String.format("%08X", bg.getBackgroundColor())
                + ", gutter=#" + String.format("%08X", bg.getGutterColor()));
    }

    /**
     * Parse a color scheme JSON and install it as the active scheme.
     * Expected format: {@code { "TOKEN_NAME": { "color": "#RRGGBB", "priority": int, "bold": bool, "italic": bool }, ... }}
     * Missing properties per token fall back to the enum default. Parse errors are logged per-token.
     */
    public static void loadFromJson(Reader reader) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(reader);
            if (!root.isJsonObject()) {
                LogWriter.error("[ColorScheme] script_editor_scheme.json root is not a JSON object, using defaults");
                return;
            }

            JsonObject obj = root.getAsJsonObject();

            background = parseBackground(obj);

            TokenType[] all = TokenType.values();
            StyleEntry[] next = new StyleEntry[all.length];
            for (TokenType tt : all) {
                next[tt.ordinal()] = parseEntry(obj, tt);
            }
            styles = next;
        } catch (Exception e) {
            LogWriter.error("[ColorScheme] Failed to parse script_editor_scheme.json, using defaults: " + e.getMessage());
        }
    }

    public static void resetToDefaults() {
        styles = buildDefaults();
        background = BackgroundStyleEntry.DEFAULT;
    }

    private static StyleEntry parseEntry(JsonObject root, TokenType tt) {
        if (!root.has(tt.name())) {
            return defaultEntry(tt);
        }
        try {
            JsonElement elem = root.get(tt.name());
            if (!elem.isJsonObject()) {
                LogWriter.error("[ColorScheme] Entry for " + tt.name() + " is not an object, skipping");
                return defaultEntry(tt);
            }
            JsonObject entry = elem.getAsJsonObject();

            int defaultColor = tt.getDefaultHexColor();
            if ((defaultColor & 0xFF000000) == 0) {
                defaultColor |= 0xFF000000;
            }

            int color = entry.has("color")
                    ? parseColor(entry.get("color"), defaultColor, tt.name())
                    : defaultColor;
            int priority = entry.has("priority") ? entry.get("priority").getAsInt() : tt.getDefaultPriority();
            boolean bold = entry.has("bold") ? entry.get("bold").getAsBoolean() : tt.getDefaultBold();
            boolean italic = entry.has("italic") ? entry.get("italic").getAsBoolean() : tt.getDefaultItalic();

            return new StyleEntry(color, priority, bold, italic);
        } catch (Exception e) {
            LogWriter.error("[ColorScheme] Error parsing token " + tt.name() + ", using defaults: " + e.getMessage());
            return defaultEntry(tt);
        }
    }

    private static StyleEntry defaultEntry(TokenType tt) {
        int color = tt.getDefaultHexColor();
        if ((color & 0xFF000000) == 0) {
            color |= 0xFF000000;
        }
        return new StyleEntry(color, tt.getDefaultPriority(), tt.getDefaultBold(), tt.getDefaultItalic());
    }

    private static BackgroundStyleEntry parseBackground(JsonObject root) {
        if (!root.has("background")) {
            return BackgroundStyleEntry.DEFAULT;
        }
        try {
            JsonElement elem = root.get("background");
            if (!elem.isJsonObject()) {
                LogWriter.error("[ColorScheme] 'background' is not an object, using defaults");
                return BackgroundStyleEntry.DEFAULT;
            }
            JsonObject bg = elem.getAsJsonObject();
            BackgroundStyleEntry d = BackgroundStyleEntry.DEFAULT;

            int backgroundColor      = bg.has("backgroundColor")      ? parseColor(bg.get("backgroundColor"),      d.getBackgroundColor(),       "background.backgroundColor")      : d.getBackgroundColor();
            int gutterColor           = bg.has("gutterColor")           ? parseColor(bg.get("gutterColor"),           d.getGutterColor(),           "background.gutterColor")           : d.getGutterColor();
            int gutterSeparatorColor  = bg.has("gutterSeparatorColor")  ? parseColor(bg.get("gutterSeparatorColor"),  d.getGutterSeparatorColor(),  "background.gutterSeparatorColor")  : d.getGutterSeparatorColor();
            int lineNumberColor       = bg.has("lineNumberColor")       ? parseColor(bg.get("lineNumberColor"),       d.getLineNumberColor(),       "background.lineNumberColor")       : d.getLineNumberColor();
            int lineNumberActiveColor = bg.has("lineNumberActiveColor") ? parseColor(bg.get("lineNumberActiveColor"), d.getLineNumberActiveColor(), "background.lineNumberActiveColor") : d.getLineNumberActiveColor();
            int scrollbarColor        = bg.has("scrollbarColor")        ? parseColor(bg.get("scrollbarColor"),        d.getScrollbarColor(),        "background.scrollbarColor")        : d.getScrollbarColor();
            int borderColor           = bg.has("borderColor")           ? parseColor(bg.get("borderColor"),           d.getBorderColor(),           "background.borderColor")           : d.getBorderColor();

            return new BackgroundStyleEntry(backgroundColor, gutterColor, gutterSeparatorColor,
                    lineNumberColor, lineNumberActiveColor, scrollbarColor, borderColor);
        } catch (Exception e) {
            LogWriter.error("[ColorScheme] Error parsing 'background', using defaults: " + e.getMessage());
            return BackgroundStyleEntry.DEFAULT;
        }
    }

    private static StyleEntry[] buildDefaults() {
        TokenType[] all = TokenType.values();
        StyleEntry[] defaults = new StyleEntry[all.length];
        for (TokenType tt : all) {
            defaults[tt.ordinal()] = defaultEntry(tt);
        }
        return defaults;
    }

    private static int parseColor(JsonElement element, int fallback, String tokenName) {
        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isString()) {
                    String s = element.getAsString().trim();
                    if (s.startsWith("#")) {
                        s = s.substring(1);
                    }
                    int color = (int) Long.parseLong(s, 16);
                    if (s.length() <= 6) {
                        color |= 0xFF000000;
                    }
                    return color;
                } else if (element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsInt();
                }
            }
        } catch (NumberFormatException e) {
            LogWriter.error("[ColorScheme] Invalid color for " + tokenName + ": " + element + ", using default");
        }
        return fallback;
    }

    static final class StyleEntry {
        final int hexColor;
        final int priority;
        final boolean bold;
        final boolean italic;

        StyleEntry(int hexColor, int priority, boolean bold, boolean italic) {
            this.hexColor = hexColor;
            this.priority = priority;
            this.bold = bold;
            this.italic = italic;
        }
    }

    public static final class BackgroundStyleEntry {
        static final BackgroundStyleEntry DEFAULT = new BackgroundStyleEntry(
                0xff000000, // backgroundColor  (text viewport)
                0xff000000, // gutterColor       (line-number gutter)
                0xff3c3f41, // gutterSeparatorColor
                0xFF606366, // lineNumberColor   (inactive)
                0xFFb9c7d6, // lineNumberActiveColor (current line)
                0xFFe0e0e0, // scrollbarColor
                0xffa0a0a0  // borderColor       (outer border)
        );

        private final int backgroundColor;
        private final int gutterColor;
        private final int gutterSeparatorColor;
        private final int lineNumberColor;
        private final int lineNumberActiveColor;
        private final int scrollbarColor;
        private final int borderColor;

        BackgroundStyleEntry(int backgroundColor, int gutterColor, int gutterSeparatorColor,
                             int lineNumberColor, int lineNumberActiveColor,
                             int scrollbarColor, int borderColor) {
            this.backgroundColor = backgroundColor;
            this.gutterColor = gutterColor;
            this.gutterSeparatorColor = gutterSeparatorColor;
            this.lineNumberColor = lineNumberColor;
            this.lineNumberActiveColor = lineNumberActiveColor;
            this.scrollbarColor = scrollbarColor;
            this.borderColor = borderColor;
        }

        public int getBackgroundColor()       { return backgroundColor; }
        public int getGutterColor()           { return gutterColor; }
        public int getGutterSeparatorColor()  { return gutterSeparatorColor; }
        public int getLineNumberColor()       { return lineNumberColor; }
        public int getLineNumberActiveColor() { return lineNumberActiveColor; }
        public int getScrollbarColor()        { return scrollbarColor; }
        public int getBorderColor()           { return borderColor; }
    }
}
