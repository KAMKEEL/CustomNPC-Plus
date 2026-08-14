package kamkeel.npcs.fixtures;

/**
 * One piece of content, the category it belongs in, and why it exists.
 *
 * <p>The note is the part that does not survive serialization. A directory of numbered files says
 * nothing about which one is the dialog whose availability is gated four ways, and the next person
 * to need that case will otherwise author a second one.
 */
public final class Case<T> {

    public final String category;
    public final T value;
    public final String note;

    public Case(String category, T value, String note) {
        this.category = category;
        this.value = value;
        this.note = note;
    }
}
