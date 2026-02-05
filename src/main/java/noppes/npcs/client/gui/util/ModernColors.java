package noppes.npcs.client.gui.util;

/**
 * Color palette for the Modern GUI system.
 * All colors include alpha channel (0xAARRGGBB format).
 */
public class ModernColors {

    // === Panel Backgrounds ===
    public static final int PANEL_BG = 0xC0101010;           // Semi-transparent dark
    public static final int PANEL_BG_SOLID = 0xFF101010;     // Solid dark
    public static final int PANEL_BORDER = 0xFF333333;       // Dark gray border
    public static final int TOP_BAR_BG = 0xC0181818;         // Top bar background

    // === Button Colors ===
    public static final int BUTTON_BG = 0xFF2A2A2A;          // Normal background
    public static final int BUTTON_BG_HOVER = 0xFF3A3A3A;    // Hover background
    public static final int BUTTON_BG_PRESSED = 0xFF4A4A4A;  // Pressed background
    public static final int BUTTON_BG_DISABLED = 0xFF1A1A1A; // Disabled background
    public static final int BUTTON_BORDER = 0xFF444444;      // Button border
    public static final int BUTTON_BORDER_HOVER = 0xFF555555;// Hover border
    public static final int BUTTON_BORDER_FOCUSED = 0xFF6688CC; // Focused/active border

    // === Text Colors ===
    public static final int TEXT_WHITE = 0xFFFFFFFF;         // Primary text
    public static final int TEXT_LIGHT = 0xFFCCCCCC;         // Secondary text
    public static final int TEXT_GRAY = 0xFF888888;          // Muted text
    public static final int TEXT_DARK = 0xFF666666;          // Disabled text
    public static final int TEXT_PLACEHOLDER = 0xFF555555;   // Placeholder text

    // === Accent Colors ===
    public static final int ACCENT_BLUE = 0xFF4488CC;        // Primary accent (dialogs)
    public static final int ACCENT_GREEN = 0xFF55FF55;       // Success/linked
    public static final int ACCENT_GOLD = 0xFFCC8800;        // Quest-related
    public static final int ACCENT_RED = 0xFFFF5555;         // Quit/delete
    public static final int ACCENT_ORANGE = 0xFFFFAA00;      // Command/warning
    public static final int ACCENT_PURPLE = 0xFF7744AA;      // Cross-category

    // === Toggle Colors ===
    public static final int TOGGLE_ON = 0xFF55AA55;          // Toggle enabled
    public static final int TOGGLE_OFF = 0xFF555555;         // Toggle disabled
    public static final int TOGGLE_KNOB = 0xFFDDDDDD;        // Toggle knob

    // === Input Field Colors ===
    public static final int INPUT_BG = 0xFF1A1A1A;           // Input background
    public static final int INPUT_BG_FOCUSED = 0xFF222222;   // Focused input background
    public static final int INPUT_BG_DISABLED = 0xFF141414;  // Disabled input background
    public static final int INPUT_BORDER = 0xFF444444;       // Input border
    public static final int INPUT_BORDER_FOCUSED = 0xFF6688CC; // Focused input border
    public static final int INPUT_TEXT = 0xFFFFFFFF;         // Input text
    public static final int SELECTION = 0x804488CC;          // Text selection highlight

    // === Scrollbar Colors ===
    public static final int SCROLLBAR_BG = 0xFF1A1A1A;       // Scrollbar track
    public static final int SCROLLBAR_THUMB = 0xFF444444;    // Scrollbar thumb
    public static final int SCROLLBAR_THUMB_HOVER = 0xFF555555; // Thumb hover

    // === Collapsible Section ===
    public static final int SECTION_HEADER_BG = 0xFF252525;  // Section header
    public static final int SECTION_HEADER_HOVER = 0xFF303030; // Header hover
    public static final int SECTION_ARROW = 0xFFAAAAAA;      // Expand/collapse arrow

    // === Selection & Highlight ===
    public static final int SELECTION_BG = 0xFF3A4A5A;       // Selected item background
    public static final int HOVER_HIGHLIGHT = 0x40FFFFFF;    // Hover overlay
    public static final int DIRTY_INDICATOR = 0xFFFFAA00;    // Unsaved changes indicator

    // === Legend Colors (Dialog Types) ===
    public static final int LEGEND_DIALOG = 0xFF4488CC;      // Dialog type
    public static final int LEGEND_QUEST = 0xFFCC8800;       // Quest type
    public static final int LEGEND_QUIT = 0xFFFF5555;        // Quit type
    public static final int LEGEND_COMMAND = 0xFFFFAA00;     // Command type
    public static final int LEGEND_TERMINAL = 0xFF666666;    // Terminal type

    // === Utility Methods ===

    /**
     * Extract alpha component (0-255) from packed color.
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Extract red component (0-255) from packed color.
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extract green component (0-255) from packed color.
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extract blue component (0-255) from packed color.
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }

    /**
     * Create color from ARGB components (0-255 each).
     */
    public static int pack(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Blend two colors by factor (0.0 = colorA, 1.0 = colorB).
     */
    public static int blend(int colorA, int colorB, float factor) {
        if (factor <= 0) return colorA;
        if (factor >= 1) return colorB;

        int aA = getAlpha(colorA), aB = getAlpha(colorB);
        int rA = getRed(colorA), rB = getRed(colorB);
        int gA = getGreen(colorA), gB = getGreen(colorB);
        int bA = getBlue(colorA), bB = getBlue(colorB);

        int a = (int) (aA + (aB - aA) * factor);
        int r = (int) (rA + (rB - rA) * factor);
        int g = (int) (gA + (gB - gA) * factor);
        int b = (int) (bA + (bB - bA) * factor);

        return pack(a, r, g, b);
    }

    /**
     * Apply alpha to a color (replaces existing alpha).
     */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /**
     * Lighten a color by factor (0.0 = no change, 1.0 = white).
     */
    public static int lighten(int color, float factor) {
        return blend(color, 0xFFFFFFFF, factor);
    }

    /**
     * Darken a color by factor (0.0 = no change, 1.0 = black).
     */
    public static int darken(int color, float factor) {
        int a = getAlpha(color);
        int darkened = blend(color, 0xFF000000, factor);
        return withAlpha(darkened, a);
    }
}
