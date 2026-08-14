package kamkeel.npcs.fixtures.content;

/**
 * Non-ASCII strings the fixtures use, built from code points.
 *
 * <p>Every source file here is pure ASCII, and this class is why. A generator whose own source has
 * been mojibaked by a CP-1252 toolchain emits fixtures that contain the mojibake, and then every
 * reader tested against those fixtures agrees with it -- the corruption becomes the specification.
 * Writing the code points out removes the whole class of accident, at the cost of one indirection.
 *
 * <p>The strings themselves matter: CustomNPC+ content is authored by players, in every language,
 * with the section sign used for colour codes throughout.
 */
public final class Text {

    private Text() {
    }

    /** The colour-code prefix, U+00A7. Appears in CustomNPC+'s own default effect name. */
    public static final String SECTION = cp(0x00A7);

    /** U+2014. Punctuation above Latin-1 that a byte-oriented reader truncates. */
    public static final String EM_DASH = cp(0x2014);

    /** "Eleves" with acute and grave accents -- two-byte UTF-8, still one char in Java. */
    public static final String ELEVES = cp(0x00C9, 0x6C, 0x00E8, 0x76, 0x65, 0x73);

    /** Katakana for "faction". Three-byte UTF-8, and outside anything CP-1252 can represent. */
    public static final String KATAKANA_FACTION = cp(0x30D5, 0x30A1, 0x30AF, 0x30B7, 0x30E7, 0x30F3);

    /** Cyrillic "Zadanie" (quest). */
    public static final String CYRILLIC_QUEST = cp(0x0417, 0x0430, 0x0434, 0x0430, 0x043D, 0x0438, 0x0435);

    /**
     * A character outside the Basic Multilingual Plane -- U+1F5FF, the moai. One code point, two
     * Java chars, four UTF-8 bytes. NBT strings are modified UTF-8, which encodes this as a
     * surrogate pair rather than as one four-byte sequence, so it is the case where a reader that
     * treats NBT strings as standard UTF-8 diverges from one that does not.
     */
    public static final String ASTRAL = cp(0x1F5FF);

    /** A right-to-left string: Arabic for "quest". Exercises no encoding rule, only rendering. */
    public static final String RTL = cp(0x0645, 0x0647, 0x0645, 0x0629);

    private static String cp(int... codePoints) {
        StringBuilder sb = new StringBuilder(codePoints.length);
        for (int codePoint : codePoints) {
            sb.appendCodePoint(codePoint);
        }
        return sb.toString();
    }
}
