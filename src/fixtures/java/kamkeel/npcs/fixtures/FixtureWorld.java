package kamkeel.npcs.fixtures;

/** One generated world. Each answers a different question, so tests can load only what they need. */
public interface FixtureWorld {

    /** Directory name, and the name a test refers to. */
    String name();

    /** What this world is for. Goes into the manifest, because a folder of ids explains nothing. */
    String purpose();

    void build(WorldWriter out);
}
