package noppes.npcs.controllers.data;

public class EffectKey {
    private final int id;
    private final int index;

    public EffectKey(int key1, int key2) {
        this.id = key1;
        this.index = key2;
    }

    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EffectKey)) return false;
        EffectKey that = (EffectKey) o;
        return id == that.id && index == that.index;
    }

    @Override
    public int hashCode() {
        return 31 * id + index;
    }

    @Override
    public String toString() {
        return "EffectKey{" + "key1=" + id + ", key2=" + index + '}';
    }
}
